package com.catbd.cat;

import com.catbd.cat.Repositories.HibernateCatRepository;
import com.catbd.cat.Repositories.ImageCatRepository;
import com.catbd.cat.controller.HibernateController;
import com.catbd.cat.entity.HibernateCat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class HibernateControllerTest {

    @InjectMocks
    private HibernateController hibernateController;

    @Mock
    private HibernateCatRepository hibernateCatRepository;

    @Mock
    private ImageCatRepository imageCatRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllHibernateCats() {

        HibernateCat cat1 = new HibernateCat();
        cat1.setId(1L);
        cat1.setName("Cat1");

        HibernateCat cat2 = new HibernateCat();
        cat2.setId(2L);
        cat2.setName("Cat2");

        List<HibernateCat> mockCats = Arrays.asList(cat1, cat2);


        doReturn(mockCats).when(hibernateCatRepository).findAll();


        List<HibernateCat> cats = hibernateController.getAllHibernateCats();

        // Проверяем результат
        assertEquals(2, cats.size());
        assertEquals("Cat1", cats.get(0).getName());
        assertEquals("Cat2", cats.get(1).getName());
    }

    @Test
    public void testGetHibernateCatById_CatExists() {

        HibernateCat cat = new HibernateCat();
        cat.setId(1L);
        cat.setName("TestCat");

        when(hibernateCatRepository.findById(1L)).thenReturn(Optional.of(cat));


        ResponseEntity<HibernateCat> response = hibernateController.getHibernateCatById(1L);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("TestCat", response.getBody().getName());
    }

    @Test
    public void testGetHibernateCatById_CatDoesNotExist() {

        when(hibernateCatRepository.findById(1L)).thenReturn(Optional.empty());


        ResponseEntity<HibernateCat> response = hibernateController.getHibernateCatById(1L);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteHibernateCat_CatExists() {

        when(hibernateCatRepository.existsById(1L)).thenReturn(true);


        ResponseEntity<Void> response = hibernateController.deleteHibernateCat(1L);


        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testDeleteHibernateCat_CatDoesNotExist() {

        when(hibernateCatRepository.existsById(1L)).thenReturn(false);


        ResponseEntity<Void> response = hibernateController.deleteHibernateCat(1L);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
