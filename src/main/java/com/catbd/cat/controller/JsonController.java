package com.catbd.cat.controller;

import com.catbd.cat.Repositories.HibernateCatRepository;
import com.catbd.cat.Repositories.ImageCatRepository;
import com.catbd.cat.Repositories.JsonCatRepository;
import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.entity.JsonCat;
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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v4/api/cats")
public class JsonController {

    private static final Logger logger = LoggerFactory.getLogger(HibernateController.class);

    @Autowired
    private JsonCatRepository jsonCatRepository;

    @Autowired
    private ImageCatRepository imageCatRepository;

    @Autowired
    private S3Client s3Client;

    @GetMapping
    public List<JsonCat> getAllHibernateCats() {
        logger.info("Fetching all HibernateCat records.");
        return jsonCatRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonCat> getHibernateCatById(@PathVariable Long id) {
        logger.info("Fetching HibernateCat with ID: {}", id);
        Optional<JsonCat> cat = jsonCatRepository.findById(id);
        if (cat.isPresent()) {
            logger.info("Found HibernateCat with ID: {}", id);
            return ResponseEntity.ok(cat.get());
        } else {
            logger.warn("HibernateCat with ID: {} not found.", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Object> createHibernateCat(@Valid @RequestBody JsonCat cat, BindingResult bindingResult) {
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

        JsonCat savedCat = jsonCatRepository.save(cat);
        logger.info("HibernateCat created successfully with ID: {}", savedCat.getId());
        return new ResponseEntity<>(savedCat, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateHibernateCat(@PathVariable Long id, @Valid @RequestBody JsonCat updatedCat, BindingResult bindingResult) {
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

        Optional<JsonCat> existingCatOpt = jsonCatRepository.findById(id);
        if (existingCatOpt.isPresent()) {
            JsonCat existingCat = existingCatOpt.get();
            existingCat.setName(updatedCat.getName());
            existingCat.setAge(updatedCat.getAge());
            existingCat.setWeight(updatedCat.getWeight());
            jsonCatRepository.save(existingCat);
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
        if (jsonCatRepository.existsById(id)) {
            jsonCatRepository.deleteById(id);
            logger.info("HibernateCat with ID: {} deleted successfully.", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            logger.warn("HibernateCat with ID: {} not found for deletion.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/{id}/s3image")
    public ResponseEntity<Object> createImageCat(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile) {

        logger.info("Uploading image for HibernateCat with ID: {} to S3", id);

        Optional<JsonCat> catOptional = jsonCatRepository.findById(id);
        if (!catOptional.isPresent()) {
            logger.warn("JsonCat with ID: {} not found.", id);
            return new ResponseEntity<>("Cat not found", HttpStatus.NOT_FOUND);
        }

        try {
            if (imageFile.isEmpty()) {
                logger.warn("Uploaded image is empty");
                return new ResponseEntity<>("Image can't be empty", HttpStatus.BAD_REQUEST);
            }

            Region region = Region.EU_NORTH_1;
            String bucketName = "your-s3-bucket-name";
            String fileName = "cat-images/" + id;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.putObject(putObjectRequest, Paths.get(imageFile.getOriginalFilename()));

            String imageUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + fileName;

            JsonCat cat = catOptional.get();
            jsonCatRepository.save(cat);

            logger.info("Image for HibernateCat with ID: {} uploaded successfully to S3 at URL: {}", id, imageUrl);
            return new ResponseEntity<>("Image uploaded successfully", HttpStatus.CREATED);

        } catch (S3Exception e) {
            logger.error("Failed to upload image for HibernateCat with ID: {}", id, e);
            return new ResponseEntity<>("Image upload to S3 failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}/s3image")
    public ResponseEntity<Object> getImageCat(@PathVariable Long id) {

        logger.info("Fetching image for JsonCat with ID: {} from S3", id);

        Optional<JsonCat> catOptional = jsonCatRepository.findById(id);
        if (!catOptional.isPresent()) {
            logger.warn("JsonCat with ID: {} not found.", id);
            return new ResponseEntity<>("Cat not found", HttpStatus.NOT_FOUND);
        }

        try {
            String bucketName = "your-s3-bucket-name";
            String fileName = "cat-images/" + id;
            Region region = Region.EU_NORTH_1;

            String imageUrl = "https://" + bucketName + ".s3." + region.id() + ".amazonaws.com/" + fileName;

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            try {
                s3Client.headObject(headObjectRequest);
            } catch (NoSuchKeyException e) {
                logger.warn("Image for JsonCat with ID: {} not found on S3", id);
                return new ResponseEntity<>("Image not found on S3", HttpStatus.NOT_FOUND);
            }

            // Возвращаем URL изображения
            logger.info("Image for JsonCat with ID: {} fetched successfully from S3 at URL: {}", id, imageUrl);
            return ResponseEntity.ok(imageUrl);

        } catch (S3Exception e) {
            logger.error("Failed to fetch image for JsonCat with ID: {}", id, e);
            return new ResponseEntity<>("Failed to retrieve image from S3", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
