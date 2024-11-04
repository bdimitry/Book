package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.CatDTO;
import com.catbd.cat.entity.JsonCat;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service()
public interface JsonCatServiceInterface {

    ResponseEntity<JsonCat> getHibernateCatById(@PathVariable Long id);
    List<JsonCat> getAllHibernateCats();
    ResponseEntity<Object> createHibernateCat(@Valid @org.springframework.web.bind.annotation.RequestBody JsonCat jsonCat, BindingResult bindingResult);
    ResponseEntity<Object> updateHibernateCat(@PathVariable Long id, @Valid @org.springframework.web.bind.annotation.RequestBody CatDTO catDTO, BindingResult bindingResult);
    ResponseEntity<Void> deleteHibernateCat(@PathVariable Long id);
    ResponseEntity<String> uploadS3Image(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile);
    ResponseEntity<Object> getImageCat(@PathVariable Long id);
    List<JsonCat> getCatsByAge(@RequestParam int age);
    List<JsonCat> getCatsByWeight(@RequestParam BigDecimal weight);
}