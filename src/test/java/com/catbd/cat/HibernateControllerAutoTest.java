package com.catbd.cat;

import com.catbd.cat.Repositories.HibernateCatRepository;
import com.catbd.cat.controller.HibernateController;
import com.catbd.cat.entity.HibernateCat;
import org.junit.BeforeClass;
import org.junit.Test;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {
        "config.dir=src//test//resources//",
        "spring.config.location=classpath:test-application.properties"
})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {BaseAutoTestConfiguration.class})
@DirtiesContext
public class HibernateControllerAutoTest {

    @InjectMocks
    private HibernateController hibernateController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private HibernateCatRepository hibernateCatRepository;

    @BeforeClass
    public static void setup() {
        Region region = Region.EU_NORTH_1;
        String bucketName = "catsStorage";

        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://127.0.0.1:4566"))
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();

        createBucketIfNotExists(s3, bucketName, region);
    }

    @Test
    public void testGetCats() {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        HibernateCat cat2 = createCat("Farcuad The Third", 4, 4);
        ResponseEntity<HibernateCat> responseFirstCat = createCatRequest(cat);
        ResponseEntity<HibernateCat> responseSecondCat = createCatRequest(cat2);
        ResponseEntity<List<HibernateCat>> response = restTemplate.exchange(
                "/v3/api/cats",
                HttpMethod.GET,
                null,  // Request entity (e.g., headers), or null if none
                new ParameterizedTypeReference<List<HibernateCat>>() {
                } // Generic type token
        );
        List<HibernateCat> cats = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(cats.get(0).getName());
        assertNotNull(cats.get(1).getName());
        assertNotNull(cats.get(0).getAge());
        assertNotNull(cats.get(1).getAge());
        assertNotNull(cats.get(0).getWeight());
        assertNotNull(cats.get(1).getWeight());
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
                new ParameterizedTypeReference<HibernateCat>() {
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
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight());
        ResponseEntity<HibernateCat> responseGet = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<HibernateCat>() {
                }
        );
        HibernateCat getCat = responseGet.getBody();
        assertEquals(200, responseGet.getStatusCode().value());
        assertEquals("Farcuad The Second", getCat.getName());
        assertEquals(3, getCat.getAge());
        assertNotNull(getCat.getId());
        assertEquals(3, getCat.getWeight());
    }

    @Test
    public void testCreateHibernateCatValidationErrors() {
        HibernateCat invalidCat = createCat("Inv", -1, 0);
        ResponseEntity<Object> response = createCatInvalidRequest(invalidCat);

        assertEquals(400, response.getStatusCode().value());

        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(3, responseBody.size());
        assertEquals("Name should have between 4 and 100 characters", responseBody.get("name"));
        assertEquals("Age must be a non-negative number", responseBody.get("age"));
        assertEquals("Weight must be at least 1 kilo", responseBody.get("weight"));
    }

    @Test
    public void testUpdateHibernateCat() {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();

        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight());

        HibernateCat catUpdate = new HibernateCat();
        cat.setName("Farcuad The Third");
        cat.setAge(3);
        cat.setWeight(3);
        HttpEntity<HibernateCat> catEntityUpdated = new HttpEntity<>(cat);

        ResponseEntity<HibernateCat> responseUpdate = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.PUT,
                catEntityUpdated,
                new ParameterizedTypeReference<HibernateCat>() {
                }
        );

        HibernateCat getCat = responseUpdate.getBody();

        assertEquals(200, responseUpdate.getStatusCode().value());
        assertEquals("Farcuad The Third", getCat.getName());
        assertEquals(3, getCat.getAge());
        assertNotNull(getCat.getId());
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
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight());

        ResponseEntity<HibernateCat> responseDelete = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.DELETE,
                null,
                new ParameterizedTypeReference<HibernateCat>() {
                }
        );

        assertEquals(204, responseDelete.getStatusCode().value());

        ResponseEntity<HibernateCat> responseGet = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<HibernateCat>() {
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
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight());

        // Имитация файла изображения
        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "multipart/form-data",
                imageBytes
        );

        // Создание сущности ByteArrayResource для RestTemplate
        Resource resource = new ByteArrayResource(mockMultipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return mockMultipartFile.getOriginalFilename();
            }
        };

        // Установка заголовков и тела запроса
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Отправка POST-запроса с изображением
        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/cats/" + postCat.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(201, responseImage.getStatusCode().value());
        // Проверка результата
    }

    @Test
    public void testUploadInvalidImageCat() throws Exception {
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        HibernateCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight());

        // Имитация файла изображения
        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "multipart/form-data",
                new byte[0]
        );

        // Создание сущности ByteArrayResource для RestTemplate
        Resource resource = new ByteArrayResource(mockMultipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return mockMultipartFile.getOriginalFilename();
            }
        };

        // Установка заголовков и тела запроса
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Отправка POST-запроса с изображением
        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/cats/" + postCat.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(500, responseImage.getStatusCode().value());
        // Проверка результата
    }

    @Test
    public void testGetImageCat() throws IOException {
        // Создание HibernateCat и отправка запроса на его создание
        HibernateCat cat = createCat("Farcuad The Second", 3, 3);
        ResponseEntity<HibernateCat> response = createCatRequest(cat);

        // Проверка успешного создания кота
        HibernateCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge());
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight());

        // Имитация файла изображения
        byte[] imageBytes = "dummy image content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "multipart/form-data",
                imageBytes
        );

        // Создание сущности ByteArrayResource для RestTemplate
        Resource resource = new ByteArrayResource(mockMultipartFile.getBytes()) {
            @Override
            public String getFilename() {
                return mockMultipartFile.getOriginalFilename();
            }
        };

        // Установка заголовков и тела запроса для отправки изображения
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Отправка POST-запроса с изображением
        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v3/api/cats/" + postCat.getId() + "/image", requestEntity, String.class, 1L);
        assertEquals(201, responseImage.getStatusCode().value());

        // Проверка получения созданного кота
        ResponseEntity<HibernateCat> responseGet = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<HibernateCat>() {
                }
        );
        assertEquals(200, responseGet.getStatusCode().value());

        // Теперь добавляем тест на получение изображения

        // Отправка GET-запроса на получение изображения для созданного кота
        ResponseEntity<byte[]> responseGetImage = restTemplate.exchange(
                "/v3/api/cats/" + postCat.getId() + "/image",
                HttpMethod.GET,
                null,
                byte[].class
        );

        // Проверка успешного получения изображения
        assertEquals(200, responseGetImage.getStatusCode().value());
        assertNotNull(responseGetImage.getBody());
        assertArrayEquals(imageBytes, responseGetImage.getBody());

        // Проверка статуса 404 для некорректного ID
        ResponseEntity<byte[]> responseGetImageNotFound = restTemplate.exchange(
                "/v3/api/cats/99999/image",
                HttpMethod.GET,
                null,
                byte[].class
        );
        assertEquals(404, responseGetImageNotFound.getStatusCode().value());
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
                new ParameterizedTypeReference<HibernateCat>() {
                }
        );
    }

    private ResponseEntity<Object> createCatInvalidRequest(HibernateCat cat) {
        HttpEntity<Object> catEntity = new HttpEntity<>(cat);
        return restTemplate.exchange(
                "/v3/api/cats",
                HttpMethod.POST,
                catEntity,
                new ParameterizedTypeReference<Object>() {
                }
        );
    }
}