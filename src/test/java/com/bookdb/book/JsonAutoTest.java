package com.bookdb.book;

import com.bookdb.book.controller.JsonController;
import com.bookdb.book.controller.pagination.PageResponse;
import com.bookdb.book.entity.BookEntity;
import com.bookdb.book.entity.JsonBook;
import com.bookdb.book.repositories.JsonRepository;
import lombok.Data;
import model.TestBook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Data
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "config.dir=src//test//resources//",
        "spring.config.location=classpath:test-application.properties"
})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {BaseAutoTestConfiguration.class})
@DirtiesContext
class JsonAutoTest {

    @InjectMocks
    private JsonController jsonController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private JsonRepository jsonRepository;

    @Mock
    private S3Client s3Client;

    @BeforeAll
    public static void setup() {
        Region region = Region.EU_NORTH_1;
        String bucketName = "books-storage";

        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://127.0.0.1:4566"))
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();

        createBucketIfNotExists(s3, bucketName, region);
    }

    @Test
    public void testGetBooks() {
        ResponseEntity<List<TestBook>> response = restTemplate.exchange(
                "/v4/api/books",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TestBook>>() {}
        );

        List<TestBook> books = response.getBody();
        assertNotNull(books);
        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    public void testGetBookById() {
        ResponseEntity<TestBook> response = restTemplate.exchange(
                "/v4/api/books/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        TestBook book = response.getBody();
        assertEquals("Farcuad", book.getName());
        assertEquals(4, book.getLastReaded().intValue());
    }

    @Test
    public void testGetBookByIdError() {
        ResponseEntity<JsonBook> response = restTemplate.exchange(
                "/v4/api/books/0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void testCreateBook() {
        TestBook book = TestBook.builder()
                .name("Felix")
                .author("holl")
                .lastReaded(BigDecimal.valueOf(4))
                .build();

        // Проверяем корректность заполнения объекта
        assertNotNull(book);
        assertEquals("holl", book.getAuthor());

        // Отправляем запрос
        HttpEntity<TestBook> bookEntity = new HttpEntity<>(book);
        ResponseEntity<JsonBook> response = restTemplate.exchange(
                "/v4/api/books",
                HttpMethod.POST,
                bookEntity,
                new ParameterizedTypeReference<>() {}
        );

        // Проверяем ответ
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        JsonBook createdBook = response.getBody();
        assertEquals("Felix", createdBook.getName());
        assertEquals("holl", createdBook.getAuthor());
        assertEquals(4, createdBook.getlastReaded().intValue());
    }

    @Test
    public void testUpdateBook() {
        JsonBook existingBook = createJsonBook("Tom", "Toma", 4);
        JsonBook updatedBook = createJsonBook("Tommy", "Kola", 5);

        when(jsonRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(jsonRepository.save(any(JsonBook.class))).thenReturn(updatedBook);

        HttpEntity<JsonBook> bookEntity = new HttpEntity<>(updatedBook);
        ResponseEntity<JsonBook> response = restTemplate.exchange(
                "/v4/api/books/1",
                HttpMethod.PUT,
                bookEntity,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    public void testDeleteBook() {
        TestBook book = TestBook.builder().name("Farcuad The Second").author("Json").lastReaded(BigDecimal.valueOf(3)).build();
        ResponseEntity<TestBook> responsePost = createBookRequest(book);

        TestBook postBook = responsePost.getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/v4/api/books/" + postBook.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    public void testUploadImageBook() throws Exception {
        TestBook book = TestBook.builder().name("Farcuad The Second").author("noll").lastReaded(BigDecimal.valueOf(3)).build();
        ResponseEntity<TestBook> response = createBookRequest(book);

        TestBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals("noll", postBook.getAuthor());
        assertNotNull(postBook.getId());
        assertEquals(3, postBook.getLastReaded().intValue());

        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MultiValueMap<String, Object> body = getStringObjectMultiValueMap(imageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v4/api/books/" + postBook.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(201, responseImage.getStatusCode().value());
    }

    @Test
    void testUploadInvalidImageBook() throws Exception {
        TestBook book = TestBook.builder().name("Farcuad The Second").author("holl").lastReaded(BigDecimal.valueOf(3)).build();
        ResponseEntity<TestBook> response = createBookRequest(book);

        TestBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals("holl", postBook.getAuthor());
        assertNotNull(postBook.getId());
        assertEquals(3, postBook.getLastReaded().intValue());


        MultiValueMap<String, Object> body = getStringObjectMultiValueMap(new byte[0]);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/books/" + postBook.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(500, responseImage.getStatusCode().value());
    }

    @Test
    public void testGetImageBook() throws IOException {
        TestBook book = TestBook.builder().name("Farcuad The Second").author("jool").lastReaded(BigDecimal.valueOf(3)).build();
        ResponseEntity<TestBook> response = createBookRequest(book);

        TestBook postBook = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postBook.getName());
        assertEquals("jool", postBook.getAuthor());
        assertNotNull(postBook.getId());
        assertEquals(3, postBook.getLastReaded().intValue());

        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MultiValueMap<String, Object> body = getStringObjectMultiValueMap(imageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v4/api/books/" + postBook.getId() + "/image", requestEntity, String.class);
        assertEquals(201, responseImage.getStatusCode().value());

        ResponseEntity<TestBook> responseGet = restTemplate.exchange(
                "/v4/api/books/" + postBook.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(200, responseGet.getStatusCode().value());

        ResponseEntity<byte[]> responseGetImage = restTemplate.exchange(
                "/v4/api/books/" + postBook.getId() + "/image",
                HttpMethod.GET,
                null,
                byte[].class
        );

        assertEquals(200, responseGetImage.getStatusCode().value());
        assertNotNull(responseGetImage.getBody());
        assertArrayEquals(imageBytes, responseGetImage.getBody());


        ResponseEntity<byte[]> responseGetImageNotFound = restTemplate.exchange(
                "/v4/api/books/99999/image",
                HttpMethod.GET,
                null,
                byte[].class
        );
        assertEquals(404, responseGetImageNotFound.getStatusCode().value());
    }

    @Test
    public void testGetBooksFilteredBylastReaded() {
        ResponseEntity<List<TestBook>> response = restTemplate.exchange(
                "/v4/api/books/by-lastReaded?lastReaded=9",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(200, response.getStatusCode().value());
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

    private JsonBook createJsonBook(String name, String author, int lastReaded) {
        BookEntity bookEntity = new BookEntity();
        bookEntity.setName(name);
        bookEntity.setAuthor(author);
        bookEntity.setLastReaded(lastReaded);

        return new JsonBook();
    }

    private static MultiValueMap<String, Object> getStringObjectMultiValueMap(byte[] content) throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "multipart/form-data",
                content
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

    private ResponseEntity<TestBook> createBookRequest(TestBook book) {
        HttpEntity<TestBook> bookEntity = new HttpEntity<>(book);
        return restTemplate.exchange(
                "/v4/api/books",
                HttpMethod.POST,
                bookEntity,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
