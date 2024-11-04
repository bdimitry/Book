package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.HibernateCat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface CatService {
    HibernateCat createHibernateCat(HibernateCat cat);

    Optional<HibernateCat> deleteCat(Long id);

    ResponseEntity<Object> createImageCat(Long id, MultipartFile imageFile);

    List<HibernateCat> getAllHibernateCats(Double weight, Integer age);

    HibernateCat updateExistingCat(HibernateCat existingCat, HibernateCat updatedCat);

    ResponseEntity<byte[]> getImageCat(@PathVariable Long id);
}