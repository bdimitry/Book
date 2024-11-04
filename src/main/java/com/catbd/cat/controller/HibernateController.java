package com.catbd.cat.controller;

import com.catbd.cat.controller.service.hibernate.*;
import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import com.catbd.cat.repositories.ImageCatRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/v3/api/cats")
public class HibernateController {

    private static final Logger logger = LoggerFactory.getLogger(HibernateController.class);

    @Autowired
    private HibernateCatService hibernateCatService;

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    @Autowired
    private ImageCatRepository imageCatRepository;

    @Autowired
    private S3Client s3Client;

    @GetMapping
    public List<HibernateCat> getAllHibernateCats(@RequestParam(value = "weight", required = false) Double weight, @RequestParam(value = "age", required = false) Integer age) {
        return hibernateCatService.getAllHibernateCats(weight, age);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HibernateCat> getHibernateCatById(@PathVariable Long id) {
        Optional<HibernateCat> cat = hibernateCatRepository.findById(id);
        if (cat.isPresent()) {
            return ResponseEntity.ok(cat.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Object> createHibernateCat(@Valid @RequestBody HibernateCat cat, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        HibernateCat savedCat = hibernateCatService.createHibernateCat(cat);
        return new ResponseEntity<>(savedCat, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateHibernateCat(@PathVariable Long id, @Valid @RequestBody HibernateCat updatedCat, BindingResult bindingResult) {
        logger.info("Updating HibernateCat with ID: {}", id);

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while updating HibernateCat with ID: {}", id);
            Map<String, String> errors = hibernateCatService.validateBindingResult(bindingResult);
            errors.forEach((field, message) -> logger.warn("Validation error in field '{}': {}", field, message));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Optional<HibernateCat> existingCatOpt = hibernateCatService.findById(id);
        if (existingCatOpt.isPresent()) {
            HibernateCat existingCat = existingCatOpt.get();
            HibernateCat updatedCatEntity = hibernateCatService.updateExistingCat(existingCat, updatedCat);
            logger.info("HibernateCat with ID: {} updated successfully.", id);
            return new ResponseEntity<>(updatedCatEntity, HttpStatus.OK);
        } else {
            logger.warn("HibernateCat with ID: {} not found for update.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHibernateCat(@PathVariable Long id) {
        Optional<HibernateCat> deletedCat = hibernateCatService.deleteCat(id);
        if (deletedCat.isPresent()) {
            logger.info("HibernateCat with ID: {} deleted successfully.", id);
            return new ResponseEntity<>("Cat with ID " + id + " deleted successfully.", HttpStatus.OK);
        } else {
            logger.warn("HibernateCat with ID: {} not found for deletion.", id);
            return new ResponseEntity<>("Cat with ID " + id + " not found.", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Object> createImageCat(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile) {
        return hibernateCatService.createImageCat(id, imageFile);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageCat(@PathVariable Long id) {
        return hibernateCatService.getImageCat(id);
    }
}
