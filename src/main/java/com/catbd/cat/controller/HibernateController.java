package com.catbd.cat.controller;

import com.catbd.cat.controller.service.hibernate.CatService;
import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import com.catbd.cat.repositories.ImageCatRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v3/api/cats")
public class HibernateController {

    @Autowired
    private CatService catService;

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    @Autowired
    private ImageCatRepository imageCatRepository;

    @Autowired
    private S3Client s3Client;

    @GetMapping
    public List<HibernateCat> getAllHibernateCats(@RequestParam(value = "weight", required = false) Double weight, @RequestParam(value = "age", required = false) Integer age) {
        return catService.getAllHibernateCats(weight, age);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HibernateCat> getHibernateCatById(@PathVariable Long id) {
        return catService.getHibernateCatById(id);
    }

    @PostMapping
    public ResponseEntity<Object> createHibernateCat(@Valid @org.springframework.web.bind.annotation.RequestBody HibernateCat cat, BindingResult bindingResult) {
        return catService.createHibernateCat(cat, bindingResult);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateHibernateCat(
            @PathVariable Long id,
            @Valid @RequestBody HibernateCat updatedCat,
            BindingResult bindingResult) {
        return catService.updateHibernateCat(id, updatedCat, bindingResult);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCat(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Id cannot be null");
        }

        Optional<HibernateCat> catToDelete = hibernateCatRepository.findById(id);
        if (catToDelete.isPresent()) {
            hibernateCatRepository.deleteById(id);
            return ResponseEntity.ok("Cat deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cat not found");
        }
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Object> createImageCat(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile) {
        return catService.createImageCat(id, imageFile);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageCat(@PathVariable Long id) {
        return catService.getImageCat(id);
    }
}
