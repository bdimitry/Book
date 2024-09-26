package com.catbd.cat.controller;

import com.catbd.cat.Repositories.HibernateCatRepository;
import com.catbd.cat.Repositories.ImageCatRepository;
import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.entity.ImageCat;
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


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;



@RestController
@RequestMapping("/v3/api/cats")
public class HibernateController {

    private static final Logger logger = LoggerFactory.getLogger(HibernateController.class);

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    @Autowired
    private ImageCatRepository imageCatRepository;

    @GetMapping
    public List<HibernateCat> getAllHibernateCats() {
        logger.info("Fetching all HibernateCat records.");
        return hibernateCatRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HibernateCat> getHibernateCatById(@PathVariable Long id) {
        logger.info("Fetching HibernateCat with ID: {}", id);
        Optional<HibernateCat> cat = hibernateCatRepository.findById(id);
        if (cat.isPresent()) {
            logger.info("Found HibernateCat with ID: {}", id);
            return ResponseEntity.ok(cat.get());
        } else {
            logger.warn("HibernateCat with ID: {} not found.", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Object> createHibernateCat(@Valid @RequestBody HibernateCat cat, BindingResult bindingResult) {
        logger.info("Creating new HibernateCat with data: {}", cat);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while creating HibernateCat.");
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.warn("Validation error in field '{}': {}", error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        HibernateCat savedCat = hibernateCatRepository.save(cat);
        logger.info("HibernateCat created successfully with ID: {}", savedCat.getId());
        return new ResponseEntity<>(savedCat, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateHibernateCat(@PathVariable Long id, @Valid @RequestBody HibernateCat updatedCat, BindingResult bindingResult) {
        logger.info("Updating HibernateCat with ID: {}", id);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while updating HibernateCat with ID: {}", id);
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.warn("Validation error in field '{}': {}", error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Optional<HibernateCat> existingCatOpt = hibernateCatRepository.findById(id);
        if (existingCatOpt.isPresent()) {
            HibernateCat existingCat = existingCatOpt.get();
            existingCat.setName(updatedCat.getName());
            existingCat.setAge(updatedCat.getAge());
            existingCat.setWeight(updatedCat.getWeight());
            hibernateCatRepository.save(existingCat);
            logger.info("HibernateCat with ID: {} updated successfully.", id);
            return new ResponseEntity<>(existingCat, HttpStatus.OK);
        } else {
            logger.warn("HibernateCat with ID: {} not found for update.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHibernateCat(@PathVariable Long id) {
        logger.info("Deleting HibernateCat with ID: {}", id);
        if (hibernateCatRepository.existsById(id)) {
            hibernateCatRepository.deleteById(id);
            logger.info("HibernateCat with ID: {} deleted successfully.", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            logger.warn("HibernateCat with ID: {} not found for deletion.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Object> createImageCat(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile) {
        logger.info("Uploading image for HibernateCat with ID: {}", id);
        try {
            ImageCat image = ImageCat.builder()
                    .id(id)
                    .imageData(imageFile.getBytes())
                    .build();
            imageCatRepository.save(image);
            logger.info("Image for HibernateCat with ID: {} uploaded successfully.", id);
            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (IOException e) {
            logger.error("Failed to upload image for HibernateCat with ID: {}", id, e);
            return new ResponseEntity<>("Image upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageCat(@PathVariable Long id) {
        logger.info("Fetching image for HibernateCat with ID: {}", id);
        Optional<ImageCat> imageCatOpt = imageCatRepository.findById(id);
        if (imageCatOpt.isPresent()) {
            logger.info("Image for HibernateCat with ID: {} retrieved successfully.", id);
            return new ResponseEntity<>(imageCatOpt.get().getImageData(), HttpStatus.OK);
        } else {
            logger.warn("Image for HibernateCat with ID: {} not found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
