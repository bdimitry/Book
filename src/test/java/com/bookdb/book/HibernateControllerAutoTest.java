package com.bookdb.book;

import com.bookdb.book.entity.HibernateBook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class HibernateControllerAutoTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BUCKET_NAME = "bookstorage";
    private static final String S3_ENDPOINT = "http://127.0.0.1:4566";
    private static final Region REGION = Region.EU_NORTH_1;

    @BeforeAll
    public static void setup() {
        try {
            S3Client s3 = S3Client.builder()
                    .endpointOverride(URI.create(S3_ENDPOINT))
                    .region(REGION)
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                    .build();

            createBucketIfNotExists(s3, BUCKET_NAME, REGION);
        } catch (Exception e) {
            System.err.println("Skipping S3 setup due to error: " + e.getMessage());
        }
    }

    @Test
    public void testGetBooks() {
        ResponseEntity<List<HibernateBook>> response = restTemplate.exchange(
                "/v3/api/books",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetBooksRsqlSearch() {
        ResponseEntity<List<HibernateBook>> response = restTemplate.exchange(
                "/v3/api/books?lastReaded=3&author=3",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testCreateHibernateBook() {
        HibernateBook book = createBook("Farcuad The Second", "Author", 3);
        ResponseEntity<HibernateBook> response = createBookRequest(book);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Farcuad The Second", response.getBody().getName());
    }

    @Test
    public void testCreateHibernateBookValidationErrors() {
        HibernateBook invalidBook = createBook("Inv", "", 0);
        ResponseEntity<Map<String, String>> response = createBookInvalidRequest(invalidBook);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("name"));
        assertTrue(response.getBody().containsKey("lastReaded"));
    }

    @Test
    public void testDeleteHibernateBook() {
        HibernateBook book = createBook("ToDelete", "Author", 3);
        ResponseEntity<HibernateBook> createResponse = createBookRequest(book);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        Long bookId = createResponse.getBody().getId();
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
                "/v3/api/books/" + bookId,
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertEquals("Book deleted successfully", deleteResponse.getBody());
    }

    @Test
    public void testUploadImageBook() throws IOException {
        HibernateBook book = createBook("ImageTest", "Author", 3);
        ResponseEntity<HibernateBook> createResponse = createBookRequest(book);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Long bookId = createResponse.getBody().getId();

        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MultiValueMap<String, Object> body = getMultiValueMap(imageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v3/api/books/" + bookId + "/image",
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testPagination() {
        for (int i = 1; i <= 15; i++) {
            HibernateBook book = createBook("Entity " + i, "Author " + i, i);
            ResponseEntity<HibernateBook> response = createBookRequest(book);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        int page = 0;
        int size = 5;

        ResponseEntity<RestPageImpl<HibernateBook>> response = restTemplate.exchange(
                "/v3/api/books?page=" + page + "&size=" + size,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        RestPageImpl<HibernateBook> resultPage = response.getBody();
        assertNotNull(resultPage);
        assertEquals(5, resultPage.getSize());
        assertEquals(0, resultPage.getNumber());
        assertTrue(resultPage.hasNext());
        assertFalse(resultPage.isLast());

        List<HibernateBook> content = resultPage.getContent();
        assertNotNull(content);
        assertEquals(5, content.size());
        assertEquals("Farcuad", content.get(0).getName());
    }

    private static void createBucketIfNotExists(S3Client s3, String bucketName, Region region) {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            System.out.println("Bucket already exists: " + bucketName);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                s3.createBucket(CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .createBucketConfiguration(builder -> builder.locationConstraint(region.id()))
                        .build());
                System.out.println("Bucket created: " + bucketName);
            } else {
                throw e;
            }
        }
    }

    private HibernateBook createBook(String name, String author, int lastReaded) {
        HibernateBook book = new HibernateBook();
        book.setName(name);
        book.setAuthor(author);
        book.setLastReaded(lastReaded);
        return book;
    }

    private ResponseEntity<HibernateBook> createBookRequest(HibernateBook book) {
        HttpEntity<HibernateBook> request = new HttpEntity<>(book);
        return restTemplate.exchange(
                "/v3/api/books",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private ResponseEntity<Map<String, String>> createBookInvalidRequest(HibernateBook book) {
        HttpEntity<HibernateBook> request = new HttpEntity<>(book);
        return restTemplate.exchange(
                "/v3/api/books",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private MultiValueMap<String, Object> getMultiValueMap(byte[] imageBytes) throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "multipart/form-data",
                imageBytes
        );

        Resource resource = new ByteArrayResource(mockMultipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return mockMultipartFile.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);
        return body;
    }
}
