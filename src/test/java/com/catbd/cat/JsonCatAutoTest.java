package com.catbd.cat;

import com.catbd.cat.controller.JsonController;
import com.catbd.cat.entity.CatEntity;
import com.catbd.cat.entity.JsonCat;
import com.catbd.cat.repositories.JsonCatRepository;
import model.TestCat;
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

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "config.dir=src//test//resources//",
        "spring.config.location=classpath:test-application.properties"
})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {BaseAutoTestConfiguration.class})
@DirtiesContext
class JsonCatAutoTest {

    @InjectMocks
    private JsonController jsonController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private JsonCatRepository jsonCatRepository;

    @Mock
    private S3Client s3Client;

    @BeforeAll
    public static void setup() {
        Region region = Region.EU_NORTH_1;
        String bucketName = "cats-storage";

        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create("http://127.0.0.1:4566"))
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();

        createBucketIfNotExists(s3, bucketName, region);
    }

    @Test
    public void testGetCats() {
        ResponseEntity<List<TestCat>> response = restTemplate.exchange(
                "/v4/api/cats/allCats",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TestCat>>() {
                }
        );
        List<TestCat> cats = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(cats);
    }

    @Test
    public void testGetCatById() {
        ResponseEntity<TestCat> response = restTemplate.exchange(
                "/v4/api/cats/1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<TestCat>() {
                }
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        TestCat cat = response.getBody();
        assertEquals("Farcuad", cat.getName());
        assertEquals(4, cat.getAge().intValue());
        assertEquals(4, cat.getWeight().intValue());
    }

    @Test
    public void testGetCatByIdError() {
        ResponseEntity<JsonCat> response = restTemplate.exchange(
                "/v4/api/cats/0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<JsonCat>() {
                }
        );
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void testCreateCat() {
        TestCat cat = TestCat.builder().name("Felix").age(2L).weight(BigDecimal.valueOf(4)).build();

        HttpEntity<TestCat> catEntity = new HttpEntity<>(cat);
        ResponseEntity<TestCat> response = restTemplate.exchange(
                "/v4/api/cats",
                HttpMethod.POST,
                catEntity,
                new ParameterizedTypeReference<TestCat>() {
                }
        );

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        TestCat createdCat = response.getBody();
        assertEquals("Felix", createdCat.getName());
        assertEquals(2, createdCat.getAge().intValue());
        assertEquals(4, createdCat.getWeight().intValue());
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
        TestCat cat = TestCat.builder().name("Farcuad The Second").age(3L).weight(BigDecimal.valueOf(3)).build();
        ResponseEntity<TestCat> responsePost = createCatRequest(cat);

        TestCat postCat = responsePost.getBody();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/v4/api/cats/" + postCat.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    public void testUploadImageCat() throws Exception {
        TestCat cat = TestCat.builder().name("Farcuad The Second").age(3L).weight(BigDecimal.valueOf(3)).build();
        ResponseEntity<TestCat> response = createCatRequest(cat);

        TestCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge().intValue());
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight().intValue());

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
        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v4/api/cats/" + postCat.getId() + "/image", requestEntity, String.class, 1L);

        assertEquals(201, responseImage.getStatusCode().value());
        // Проверка результата
    }

    @Test
    void testUploadInvalidImageCat() throws Exception {
        TestCat cat = TestCat.builder().name("Farcuad The Second").age(3L).weight(BigDecimal.valueOf(3)).build();
        ResponseEntity<TestCat> response = createCatRequest(cat);

        TestCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge().intValue());
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight().intValue());

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
        TestCat cat = TestCat.builder().name("Farcuad The Second").age(3L).weight(BigDecimal.valueOf(3)).build();
        ResponseEntity<TestCat> response = createCatRequest(cat);

        // Проверка успешного создания кота
        TestCat postCat = response.getBody();
        assertEquals(201, response.getStatusCode().value());
        assertEquals("Farcuad The Second", postCat.getName());
        assertEquals(3, postCat.getAge().intValue());
        assertNotNull(postCat.getId());
        assertEquals(3, postCat.getWeight().intValue());

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
        ResponseEntity<String> responseImage = restTemplate.postForEntity("/v4/api/cats/" + postCat.getId() + "/image", requestEntity, String.class);
        assertEquals(201, responseImage.getStatusCode().value());

        // Проверка получения созданного кота
        ResponseEntity<TestCat> responseGet = restTemplate.exchange(
                "/v4/api/cats/" + postCat.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<TestCat>() {
                }
        );
        assertEquals(200, responseGet.getStatusCode().value());

        // Теперь добавляем тест на получение изображения

        // Отправка GET-запроса на получение изображения для созданного кота
        ResponseEntity<byte[]> responseGetImage = restTemplate.exchange(
                "/v4/api/cats/" + postCat.getId() + "/image",
                HttpMethod.GET,
                null,
                byte[].class
        );

        // Проверка успешного получения изображения
        assertEquals(200, responseGetImage.getStatusCode().value());
        assertNotNull(responseGetImage.getBody());
        assertArrayEquals(imageBytes, responseGetImage.getBody());


        ResponseEntity<byte[]> responseGetImageNotFound = restTemplate.exchange(
                "/v4/api/cats/99999/image",
                HttpMethod.GET,
                null,
                byte[].class
        );
        assertEquals(404, responseGetImageNotFound.getStatusCode().value());
    }

    @Test
    public void testGetCatsFilteredByWeight() {
        ResponseEntity<List<TestCat>> response = restTemplate.exchange(
                "/v4/api/cats/by-weight?weight=9",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TestCat>>() {
                }
        );
        List<TestCat> cats = response.getBody();
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testGetCatsFilteredByAge() {
        ResponseEntity<List<TestCat>> response = restTemplate.exchange(
                "/v4/api/cats/by-age?age=2",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TestCat>>() {
                }
        );
        List<TestCat> cats = response.getBody();
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testGetCatsFilteredByRsql() {
        ResponseEntity<List<TestCat>> response = restTemplate.exchange(
                "/v4/api/cats/filter?filter=cat.weight=gt=1;age=gt=1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TestCat>>() {
                }
        );
        List<TestCat> cats = response.getBody();
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

    private JsonCat createJsonCat(String name, int age, int weight) {
        CatEntity catEntity = new CatEntity();
        catEntity.setName(name);
        catEntity.setAge(age);
        catEntity.setWeight(weight);

        JsonCat jsonCat = new JsonCat();
//        jsonCat.setCat(catEntity);
        return jsonCat;
    }

    private ResponseEntity<TestCat> createCatRequest(TestCat cat) {
        HttpEntity<TestCat> catEntity = new HttpEntity<>(cat);
        return restTemplate.exchange(
                "/v4/api/cats",
                HttpMethod.POST,
                catEntity,
                new ParameterizedTypeReference<TestCat>() {
                }
        );
    }

    private void assertCatFields(JsonCat cat) {
//        assertNotNull(cat.getCat().getName(), "Name should not be null");
//        assertNotNull(cat.getCat().getAge(), "Age should not be null");
//        assertNotNull(cat.getCat().getWeight(), "Weight should not be null");
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
