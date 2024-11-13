package com.catbd.cat;

import com.catbd.cat.entity.HibernateCat;
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
        String bucketName = "catstorage";

        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://127.0.0.1:4566"))
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();

        createBucketIfNotExists(s3, bucketName, region);
    }

    @Test
    public void testGetCats() {
        ResponseEntity<List<HibernateCat>> response = restTemplate.exchange(
                "/v3/api/cats",
                HttpMethod.GET,
                null,  // Request entity (e.g., headers), or null if none
                new ParameterizedTypeReference<>() {
                } // Generic type token
        );
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testGetCatsRsqlSearch() {
        ResponseEntity<List<HibernateCat>> response = restTemplate.exchange(
                "/v3/api/cats?weight=3&age=3",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        List<HibernateCat> cats = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        for (HibernateCat cat : cats) {
            assertTrue(cat.getWeight() >= 3);
            assertTrue(cat.getAge() >= 3);
        }
    }

    @Test
    public void testGetCat() {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());

        ResponseEntity<HibernateCat> responseGet = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.GET,
                null,  // Request entity (e.g., headers), or null if none
                new ParameterizedTypeReference<>() {
                } // Generic type token
        );
        HibernateCat cats = responseGet.getBody();
        assertEquals(200, responseGet.getStatusCode().value());
        assertNotNull(cats.getName());
        assertEquals(3, cats.getAge());
        assertEquals(3, cats.getWeight());
    }

    @Test
    public void testCreateHibernateCat() {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertEquals(3, postCat.getWeight());
        ResponseEntity<HibernateCat> responseGet = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        HibernateCat getCat = responseGet.getBody();
        assertEquals(200, responseGet.getStatusCode().value());
        assertEquals("Farcuad The Second", getCat.getName());
        assertEquals(3, getCat.getAge());
        assertEquals(3, getCat.getWeight());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateHibernateCatValidationErrors() {
        HibernateCat invalidCat = createCat("Inv", -1, 0);
        ResponseEntity<Object> response = createCatInvalidRequest(invalidCat);

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
    public void testUpdateHibernateCat() {
        // Initial create
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();

        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertEquals(3, postCat.getWeight());

        // Prepare the updated cat entity
        HibernateCat catUpdate = new HibernateCat();
        catUpdate.setId(postCat.getId()); // Set correct ID for update
        catUpdate.setName("Farcuad The Third");
        catUpdate.setAge(3);
        catUpdate.setWeight(3);

        HttpEntity<HibernateCat> catEntityUpdated = new HttpEntity<>(catUpdate);

        // Update request
        ResponseEntity<HibernateCat> responseUpdate = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.PUT,
                catEntityUpdated,
                new ParameterizedTypeReference<>() {
                }
        );

        HibernateCat getCat = responseUpdate.getBody();

        // Final assertions
        assertEquals(200, responseUpdate.getStatusCode().value());
        assertEquals("Farcuad The Third", getCat.getName());
        assertEquals(3, getCat.getAge());
        assertEquals(3, getCat.getWeight());
    }

    @Test
    public void testDeleteHibernateCat() {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertEquals(3, postCat.getWeight());

        ResponseEntity<String> responseDelete = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(200, responseDelete.getStatusCode().value());
        assertEquals("Cat deleted successfully", responseDelete.getBody());

        ResponseEntity<HibernateCat> responseGet = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(404, responseGet.getStatusCode().value());
    }

    @Test
    public void testUploadImageCat() throws Exception {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertEquals(3, postCat.getWeight());

        // Picture file imitation
        MultiValueMap<String, Object> body = getObjectMultiValueMap();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/cats/" + postCat.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(201, responseImage.getStatusCode().value());
    }

    private static MultiValueMap<String, Object> getObjectMultiValueMap() throws IOException {
        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        return getStringObjectMultiValueMap(imageBytes);
    }

    @Test
    public void testUploadInvalidImageCat() throws Exception {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertEquals(3, postCat.getWeight());

        MultiValueMap<String, Object> body = getStringObjectMultiValueMap();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/cats/" + postCat.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(500, responseImage.getStatusCode().value());
    }

    @Test
    public void testGetImageCat() throws IOException {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertEquals(3, postCat.getWeight());

        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MultiValueMap<String, Object> body = getStringObjectMultiValueMap(imageBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/cats/" + postCat.getId() + "/image", requestEntity, String.class, 1L);
        assertEquals(201, responseImage.getStatusCode().value());

        ResponseEntity<HibernateCat> responseGet = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(200, responseGet.getStatusCode().value());

        ResponseEntity<byte[]> responseGetImage = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId() + "/image",
                HttpMethod.GET,
                null,
                byte[].class
        );

        assertEquals(200, responseGetImage.getStatusCode().value());
        assertNotNull(responseGetImage.getBody());
        assertArrayEquals(imageBytes, responseGetImage.getBody());

        ResponseEntity<byte[]> responseGetImageNotFound = restTemplate.exchange(
                "/v3/api/cats/99999/image",
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

    private HibernateCat createCat(String name, int age, int weight) {
        HibernateCat cat = new HibernateCat();
        cat.setName(name);
        cat.setAge(age);
        cat.setWeight(weight);
        return cat;
    }

    private ResponseEntity<HibernateCat> createCatRequest(HibernateCat cat) {
        HttpEntity<HibernateCat> catEntity = new HttpEntity<>(cat);
        return restTemplate.exchange(
                "/v3/api/cats",
                HttpMethod.POST,
                catEntity,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private ResponseEntity<Object> createCatInvalidRequest(HibernateCat cat) {
        HttpEntity<Object> catEntity = new HttpEntity<>(cat);
        return restTemplate.exchange(
                "/v3/api/cats",
                HttpMethod.POST,
                catEntity,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}