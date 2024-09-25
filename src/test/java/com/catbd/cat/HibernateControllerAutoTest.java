package com.catbd.cat;


import com.catbd.cat.Repositories.HibernateCatRepository;
import com.catbd.cat.controller.HibernateController;
import com.catbd.cat.entity.HibernateCat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

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


    @Test
    public void testGetCats() {
//        restTemplate.getCats();
        HibernateCat[] mockCats = restTemplate.getForObject("/v3/api/cats",
                HibernateCat[].class);

        ResponseEntity<List<HibernateCat>> response = restTemplate.exchange(
                "/v3/api/cats",
                HttpMethod.GET,
                null,  // Request entity (e.g., headers), or null if none
                new ParameterizedTypeReference<List<HibernateCat>>() {
                } // Generic type token
        );
        List<HibernateCat> cats = response.getBody();
//        assertEquals(2, cats.size());
        assertNotNull(cats.get(0).getName());
        assertNotNull(cats.get(1).getName());
        assertEquals(4, cats.get(0).getAge());
        assertEquals(2, cats.get(1).getAge());
        assertEquals(8, cats.get(0).getWeight());
        assertEquals(5, cats.get(1).getWeight());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testGetCat() {
        ResponseEntity<List<HibernateCat>> response = restTemplate.exchange(
                "/v3/api/cats/1",
                HttpMethod.GET,
                null,  // Request entity (e.g., headers), or null if none
                new ParameterizedTypeReference<List<HibernateCat>>() {
                } // Generic type token
        );
        List<HibernateCat> cats = response.getBody();
        assertEquals(1, cats.size());
        assertNotNull(cats.getFirst().getName());
        assertEquals(4, cats.getFirst().getAge());
        assertEquals(8, cats.getFirst().getWeight());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testCreateHibernateCat() {
        HibernateCat cat = new HibernateCat();
        cat.setName("Farcuad The Second");
        cat.setAge(3);
        cat.setWeight(3);
        HttpEntity<HibernateCat> catEntity = new HttpEntity<>(cat);
        ResponseEntity<HibernateCat> response = restTemplate.exchange(
                "/v3/api/cats",
                HttpMethod.POST,
                catEntity,
                new ParameterizedTypeReference<HibernateCat>() {
                }
        );
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
    public void testUpdateHibernateCat() {
        // Создание тестового объекта HibernateCat
        HibernateCat cat = new HibernateCat();
        cat.setId(1L);
        cat.setName("Farcuad");
        cat.setWeight(5);
        cat.setAge(3);

        // Мокирование существующего объекта в репозитории
        doReturn(Optional.of(cat)).when(hibernateCatRepository).findById(1L);

        // Мокирование обновления объекта в репозитории
        doReturn(cat).when(hibernateCatRepository).save(any(HibernateCat.class));

        // Вызов метода контроллера для обновления
        ResponseEntity<Object> response = hibernateController.updateHibernateCat(1L, cat, null);

        // Проверка что статус ответа соответствует OK (200)
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверка что в теле ответа возвращается обновленный объект HibernateCat с корректными данными
        HibernateCat updatedCat = (HibernateCat) response.getBody();
        assertNotNull(updatedCat);
        assertEquals(1L, updatedCat.getId());
        assertEquals("Farcuad", updatedCat.getName());
        assertEquals(5, updatedCat.getWeight());
        assertEquals(3, updatedCat.getAge());
    }

    @Test
    public void testDeleteHibernateCat() {
        // Мокируем существование объекта
        when(hibernateCatRepository.existsById(1L)).thenReturn(true);

        // Вызываем метод контроллера для удаления
        ResponseEntity<Void> response = hibernateController.deleteHibernateCat(1L);

        // Проверяем что статус NO_CONTENT (204)
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Проверяем что метод удаления был вызван в репозитории
        verify(hibernateCatRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testUploadImageCat() throws Exception {
        // Мокируем загрузку изображения
        restTemplate.getForObject("/v3/api/cats",
                HibernateCat.class);

    }
}
