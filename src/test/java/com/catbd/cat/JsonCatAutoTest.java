package com.catbd.cat;

import com.catbd.cat.Repositories.JsonCatRepository;
import com.catbd.cat.controller.JsonController;
import com.catbd.cat.entity.CatEntity;
import com.catbd.cat.entity.JsonCat;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "config.dir=src//test//resources//",
        "spring.config.location=classpath:test-application.properties"
})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {BaseAutoTestConfiguration.class})
@DirtiesContext
public class JsonCatAutoTest {

    @InjectMocks
    private JsonController jsonController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private JsonCatRepository jsonCatRepository;

    @Mock
    private S3Client s3Client;

    @BeforeClass
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
        JsonCat cat1 = createJsonCat("Farcuad The Second", 3, 3);
        JsonCat cat2 = createJsonCat("Farcuad The Third", 4, 4);

        when(jsonCatRepository.findAll()).thenReturn(List.of(cat1, cat2));

        ResponseEntity<List<JsonCat>> response = restTemplate.exchange(
                "/v4/api/cats",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<JsonCat>>() {
                }
        );

        List<JsonCat> cats = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(cats);
        assertEquals(2, cats.size());
        assertCatFields(cats.get(0));
        assertCatFields(cats.get(1));
    }

    @Test
    public void testGetCatById() {
        JsonCat cat = createJsonCat("Whiskers", 5, 6);

        when(jsonCatRepository.findById(1L)).thenReturn(Optional.of(cat));

        ResponseEntity<JsonCat> response = restTemplate.exchange(
                "/v4/api/cats/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<JsonCat>() {
                }
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertCatFields(response.getBody());
    }

    @Test
    public void testGetCatByIdError() {
        when(jsonCatRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<JsonCat> response = restTemplate.exchange(
                "/v4/api/cats/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<JsonCat>() {
                }
        );

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void testCreateCat() {
        JsonCat cat = createJsonCat("Felix", 2, 4);

        when(jsonCatRepository.save(any(JsonCat.class))).thenReturn(cat);

        ResponseEntity<JsonCat> response = createCatRequest(cat);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertCatFields(response.getBody());
    }

    @Test
    public void testUpdateCat() {
        JsonCat existingCat = createJsonCat("Tom", 3, 4);
        JsonCat updatedCat = createJsonCat("Tommy", 4, 5);

        when(jsonCatRepository.findById(1L)).thenReturn(Optional.of(existingCat));
        when(jsonCatRepository.save(any(JsonCat.class))).thenReturn(updatedCat);

        HttpEntity<JsonCat> catEntity = new HttpEntity<>(updatedCat);
        ResponseEntity<JsonCat> response = restTemplate.exchange(
                "/v4/api/cats/1",
                HttpMethod.PUT,
                catEntity,
                new ParameterizedTypeReference<JsonCat>() {
                }
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertCatFields(response.getBody());
    }

    @Test
    public void testDeleteCat() {
        when(jsonCatRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/v4/api/cats/1",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    public void testUploadImageCat() throws Exception {
        JsonCat cat = createJsonCat("Felix", 2, 4);
        when(jsonCatRepository.findById(1L)).thenReturn(Optional.of(cat));

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image", "test-image.jpg", "multipart/form-data", "dummy image content".getBytes(StandardCharsets.UTF_8)
        );

        Resource resource = new ByteArrayResource(mockMultipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return mockMultipartFile.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v4/api/cats/1/s3image", requestEntity, String.class);

        assertEquals(201, responseImage.getStatusCode().value());
    }

    @Test
    public void testGetImageCat() throws IOException {
        JsonCat cat = createJsonCat("Felix", 2, 4);
        when(jsonCatRepository.findById(1L)).thenReturn(Optional.of(cat));

        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image", "test-image.jpg", "multipart/form-data", imageBytes
        );

        Resource resource = new ByteArrayResource(mockMultipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return mockMultipartFile.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        restTemplate.postForEntity("/v4/api/cats/1/s3image", requestEntity, String.class);

        ResponseEntity<byte[]> responseGetImage = restTemplate.exchange(
                "/v4/api/cats/1/s3image",
                HttpMethod.GET,
                null,
                byte[].class
        );

        assertEquals(200, responseGetImage.getStatusCode().value());
        assertNotNull(responseGetImage.getBody());
        assertArrayEquals(imageBytes, responseGetImage.getBody());
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

    private JsonCat createJsonCat(String name, int age, int weight) {
        CatEntity catEntity = new CatEntity();
        catEntity.setName(name);
        catEntity.setAge(age);
        catEntity.setWeight(weight);

        JsonCat jsonCat = new JsonCat();
        jsonCat.setCat(catEntity);
        return jsonCat;
    }

    private ResponseEntity<JsonCat> createCatRequest(JsonCat cat) {
        HttpEntity<JsonCat> catEntity = new HttpEntity<>(cat);
        return restTemplate.exchange(
                "/v4/api/cats",
                HttpMethod.POST,
                catEntity,
                new ParameterizedTypeReference<JsonCat>() {
                }
        );
    }

    private void assertCatFields(JsonCat cat) {
        assertNotNull(cat.getCat().getName(), "Name should not be null");
        assertNotNull(cat.getCat().getAge(), "Age should not be null");
        assertNotNull(cat.getCat().getWeight(), "Weight should not be null");
    }

    private ResponseEntity<Object> createInvalidCatRequest(JsonCat cat) {
        HttpEntity<Object> catEntity = new HttpEntity<>(cat);
        return restTemplate.exchange(
                "/v4/api/cats",
                HttpMethod.POST,
                catEntity,
                new ParameterizedTypeReference<Object>() {
                }
        );
    }
}
