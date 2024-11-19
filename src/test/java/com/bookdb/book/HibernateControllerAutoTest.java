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
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "location=classpath:test-application.properties"
})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {BaseAutoTestConfiguration.class})
@DirtiesContext
public class HibernateControllerAutoTest {

    @Autowired
    private final TestRestTemplate restTemplate;

    public HibernateControllerAutoTest(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @BeforeAll
    public static void setup() {
        Region region = Region.EU_NORTH_1;
        String bucketName = "bookstorage";

        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://127.0.0.1:4566"))
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();

        createBucketIfNotExists(s3, bucketName, region);
    }

    @Test
    public void testGetBooks() {
        ResponseEntity<List<HibernateBook>> response = restTemplate.exchange(
                "/v3/api/books",
                HttpMethod.GET,
                null,  // Request entity (e.g., headers), or null if none
                new ParameterizedTypeReference<>() {
                } // Generic type token
        );
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testGetBooksRsqlSearch() {
        ResponseEntity<List<HibernateBook>> response = restTemplate.exchange(
                "/v3/api/books?weight=3&age=3",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        List<HibernateBook> books = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        for (HibernateBook book : books) {
            assertTrue(book.getWeight() >= 3);
            assertTrue(book.getAge() >= 3);
        }
    }

    @Test
    public void testGetBook() {
        HibernateBook book = createBook("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateBook> response = createBookRequest(book);

        HibernateBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());

        ResponseEntity<HibernateBook> responseGet = restTemplate.exchange(
                "/v3/api/books/" + postBook.getId(),
                HttpMethod.GET,
                null,  // Request entity (e.g., headers), or null if none
                new ParameterizedTypeReference<>() {
                } // Generic type token
        );
        HibernateBook books = responseGet.getBody();
        assertEquals(200, responseGet.getStatusCode().value());
        assertNotNull(books.getName());
        assertEquals(3, books.getAge());
        assertEquals(3, books.getWeight());
    }

    @Test
    public void testCreateHibernateBook() {
        HibernateBook book = createBook("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateBook> response = createBookRequest(book);

        HibernateBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals(3, postBook.getAge());
        assertEquals(3, postBook.getWeight());
        ResponseEntity<HibernateBook> responseGet = restTemplate.exchange(
                "/v3/api/books/" + postBook.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        HibernateBook getBook = responseGet.getBody();
        assertEquals(200, responseGet.getStatusCode().value());
        assertEquals("Farcuad The Second", getBook.getName());
        assertEquals(3, getBook.getAge());
        assertEquals(3, getBook.getWeight());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateHibernateBookValidationErrors() {
        HibernateBook invalidBook = createBook("Inv", -1, 0);
        ResponseEntity<Object> response = createBookInvalidRequest(invalidBook);

        assertEquals(400, response.getStatusCode().value());

        Map<String, String> responseBody;
        responseBody = Collections.unmodifiableMap((Map<String, String>) Objects.requireNonNull(response.getBody()));
        assertNotNull(responseBody);
        assertEquals(3, responseBody.size());
        assertEquals("Name should have between 4 and 100 characters", responseBody.get("name"));
        assertEquals("Age must be a non-negative number", responseBody.get("age"));
        assertEquals("Weight must be at least 1 kilo", responseBody.get("weight"));
    }

    @Test
    public void testUpdateHibernateBook() {
        // Initial create
        HibernateBook book = createBook("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateBook> response = createBookRequest(book);

        HibernateBook postBook = response.getBody();

        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals(3, postBook.getAge());
        assertEquals(3, postBook.getWeight());

        // Prepare the updated book entity
        HibernateBook bookUpdate = new HibernateBook();
        bookUpdate.setId(postBook.getId()); // Set correct ID for update
        bookUpdate.setName("Farcuad The Third");
        bookUpdate.setAge(3);
        bookUpdate.setWeight(3);

        HttpEntity<HibernateBook> bookEntityUpdated = new HttpEntity<>(bookUpdate);

        // Update request
        ResponseEntity<HibernateBook> responseUpdate = restTemplate.exchange(
                "/v3/api/books/" + postBook.getId(),
                HttpMethod.PUT,
                bookEntityUpdated,
                new ParameterizedTypeReference<>() {
                }
        );

        HibernateBook getBook = responseUpdate.getBody();

        // Final assertions
        assertEquals(200, responseUpdate.getStatusCode().value());
        assertEquals("Farcuad The Third", getBook.getName());
        assertEquals(3, getBook.getAge());
        assertEquals(3, getBook.getWeight());
    }

    @Test
    public void testDeleteHibernateBook() {
        HibernateBook book = createBook("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateBook> response = createBookRequest(book);

        HibernateBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals(3, postBook.getAge());
        assertEquals(3, postBook.getWeight());

        ResponseEntity<String> responseDelete = restTemplate.exchange(
                "/v3/api/books/" + postBook.getId(),
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(200, responseDelete.getStatusCode().value());
        assertEquals("Book deleted successfully", responseDelete.getBody());

        ResponseEntity<HibernateBook> responseGet = restTemplate.exchange(
                "/v3/api/books/" + postBook.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(404, responseGet.getStatusCode().value());
    }

    @Test
    public void testUploadImageBook() throws Exception {
        HibernateBook book = createBook("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateBook> response = createBookRequest(book);

        HibernateBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals(3, postBook.getAge());
        assertEquals(3, postBook.getWeight());

        // Picture file imitation
        MultiValueMap<String, Object> body = getObjectMultiValueMap();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/books/" + postBook.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(201, responseImage.getStatusCode().value());
    }

    private static MultiValueMap<String, Object> getObjectMultiValueMap() throws IOException {
        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        return getStringObjectMultiValueMap(imageBytes);
    }

    @Test
    public void testUploadInvalidImageBook() throws Exception {
        HibernateBook book = createBook("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateBook> response = createBookRequest(book);

        HibernateBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals(3, postBook.getAge());
        assertEquals(3, postBook.getWeight());

        MultiValueMap<String, Object> body = getStringObjectMultiValueMap();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/books/" + postBook.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(500, responseImage.getStatusCode().value());
    }

    @Test
    public void testGetImageBook() throws IOException {
        HibernateBook book = createBook("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateBook> response = createBookRequest(book);

        HibernateBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals(3, postBook.getAge());
        assertEquals(3, postBook.getWeight());

        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MultiValueMap<String, Object> body = getStringObjectMultiValueMap(imageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/books/" + postBook.getId() + "/image", requestEntity, String.class, 1L);
        assertEquals(201, responseImage.getStatusCode().value());

        ResponseEntity<HibernateBook> responseGet = restTemplate.exchange(
                "/v3/api/books/" + postBook.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(200, responseGet.getStatusCode().value());

        ResponseEntity<byte[]> responseGetImage = restTemplate.exchange(
                "/v3/api/books/" + postBook.getId() + "/image",
                HttpMethod.GET,
                null,
                byte[].class
        );

        assertEquals(200, responseGetImage.getStatusCode().value());
        assertNotNull(responseGetImage.getBody());
        assertArrayEquals(imageBytes, responseGetImage.getBody());

        ResponseEntity<byte[]> responseGetImageNotFound = restTemplate.exchange(
                "/v3/api/books/99999/image",
                HttpMethod.GET,
                null,
                byte[].class
        );
        assertEquals(404, responseGetImageNotFound.getStatusCode().value());
    }

    private static MultiValueMap<String, Object> getStringObjectMultiValueMap() throws IOException {
        return getStringObjectMultiValueMap(new byte[0]);
    }

    private static MultiValueMap<String, Object> getStringObjectMultiValueMap(byte[] imageBytes) throws IOException {
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

    private static void createBucketIfNotExists(S3Client s3, String bucketName, Region region) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
            s3.headBucket(headBucketRequest);
            System.out.println("Bucket already exists: " + bucketName);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .createBucketConfiguration(builder -> builder.locationConstraint(region.id()))
                        .build();
                s3.createBucket(createBucketRequest);
                System.out.println("Bucket created: " + bucketName);
            } else {
                throw e;
            }
        }
    }

    private HibernateBook createBook(String name, int age, int weight) {
        HibernateBook book = new HibernateBook();
        book.setName(name);
        book.setAge(age);
        book.setWeight(weight);
        return book;
    }

    private ResponseEntity<HibernateBook> createBookRequest(HibernateBook book) {
        HttpEntity<HibernateBook> bookEntity = new HttpEntity<>(book);
        return restTemplate.exchange(
                "/v3/api/books",
                HttpMethod.POST,
                bookEntity,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private ResponseEntity<Object> createBookInvalidRequest(HibernateBook book) {
        HttpEntity<Object> bookEntity = new HttpEntity<>(book);
        return restTemplate.exchange(
                "/v3/api/books",
                HttpMethod.POST,
                bookEntity,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}