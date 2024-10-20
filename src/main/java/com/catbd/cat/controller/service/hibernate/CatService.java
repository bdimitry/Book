package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.CatI;
import com.catbd.cat.entity.HibernateCat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface CatService {
    CatI createHibernateCat(CatI cat);
    Optional<HibernateCat> deleteCat(Long id);
    ResponseEntity<Object> createImageCat(Long id, MultipartFile imageFile);
}
