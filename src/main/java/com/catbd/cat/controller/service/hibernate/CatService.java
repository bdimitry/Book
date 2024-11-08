package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.HibernateCat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CatService {

    ResponseEntity<HibernateCat> getHibernateCatById(Long id);

    ResponseEntity<String> deleteCat(Long id);

    ResponseEntity<Object> createHibernateCat(HibernateCat hibernateCat, BindingResult bindingResult);

    ResponseEntity<Object> createImageCat(Long id, MultipartFile imageFile);

    List<HibernateCat> getAllHibernateCats(Double weight, Integer age);

    ResponseEntity<Object> updateHibernateCat(Long id, HibernateCat hibernateCat, BindingResult bindingResult);

    ResponseEntity<byte[]> getImageCat(Long id);
}
