package com.catbd.cat.controller;

import com.catbd.cat.controller.service.hibernate.CatService;
import com.catbd.cat.entity.CatDTO;
import com.catbd.cat.entity.JsonCat;
import com.catbd.cat.repositories.ImageCatRepository;
import com.catbd.cat.repositories.JsonCatRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static io.github.perplexhub.rsql.RSQLJPASupport.toSpecification;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v4/api/cats")
public class JsonController {

    private static final Logger logger = LoggerFactory.getLogger(JsonController.class);

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Value("${aws.s3.bucket-name}")
    private String awsBucketName;

    @Autowired
    private JsonCatRepository jsonCatRepository;

    @Autowired @Qualifier("jsonCatService")
    private CatService createHibernateCatService;

    @Autowired
    private ImageCatRepository imageCatRepository;

    @Autowired
    private S3Client s3Client;

    // Получить всех котов
    @GetMapping
    public List<JsonCat> getAllHibernateCats() {
        logger.info("Fetching all HibernateCat records.");
        String filter = "weight=gt=1;age=gt=1";
        Map<String, String> propertyPathMapper = new HashMap<>();
        jsonCatRepository.findAll(toSpecification(filter, propertyPathMapper));
        return jsonCatRepository.findAll();
    }

    // Получить кота по ID
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

    // Создать нового кота
    @PostMapping
    public ResponseEntity<Object> createHibernateCat(@Valid @org.springframework.web.bind.annotation.RequestBody JsonCat jsonCat, BindingResult bindingResult) {
        logger.info("Creating new HibernateCat with data: {}", jsonCat);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while creating HibernateCat.");
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.warn("Validation error in field '{}': {}", error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        JsonCat savedCat = jsonCatRepository.save(jsonCat);
        logger.info("HibernateCat created successfully with ID: {}", savedCat.getId());
        return new ResponseEntity<>(savedCat, HttpStatus.CREATED);
    }

    // Обновить данные о коте
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateHibernateCat(@PathVariable Long id, @Valid @org.springframework.web.bind.annotation.RequestBody CatDTO catDTO, BindingResult bindingResult) {
        logger.info("Updating JsonCat with ID: {}", id);

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while updating JsonCat with ID: {}", id);
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.warn("Validation error in field '{}': {}", error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Optional<JsonCat> existingCatOptional = jsonCatRepository.findById(id);
        if (!existingCatOptional.isPresent()) {
            logger.warn("JsonCat with ID: {} not found.", id);
            return new ResponseEntity<>("Cat not found", HttpStatus.NOT_FOUND);
        }

        JsonCat existingCat = existingCatOptional.get();
        existingCat.setImageUrl(catDTO.getImageUrl());

        JsonCat updatedCat = jsonCatRepository.save(existingCat);
        logger.info("JsonCat with ID: {} updated successfully.", id);

        return new ResponseEntity<>(updatedCat, HttpStatus.OK);
    }

    // Удалить кота по ID
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

    @PostMapping("/{id}/image")
    public ResponseEntity<String> uploadS3Image(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile) {
        logger.info("Uploading image for JsonCat with ID: {} to S3", id);
        return jsonCatRepository.findById(id).map(cat -> {

            if (imageFile.isEmpty()) {
                logger.warn("Uploaded image is empty for JsonCat ID: {}", id);
                return new ResponseEntity<>("Image can't be empty", HttpStatus.BAD_REQUEST);
            }

            if (!imageFile.getContentType().startsWith("image/")) {
                logger.warn("Invalid file type uploaded for JsonCat with ID: {}", id);
                return new ResponseEntity<>("Invalid file type. Only images are allowed", HttpStatus.BAD_REQUEST);
            }

            try {

                Region region = Region.of(awsRegion);
                String fileName = "cat-images/" + id;

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(awsBucketName)
                        .key(fileName)
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageFile.getBytes()));

                String imageUrl = "https://" + awsBucketName + ".s3." + region.id() + ".amazonaws.com/" + fileName;

//                cat.setImageUrl(imageUrl);
                jsonCatRepository.save(cat);

                logger.info("Image for JsonCat with ID: {} uploaded successfully to S3 at URL: {}", id, imageUrl);
                return new ResponseEntity<>("Image uploaded successfully", HttpStatus.CREATED);

            } catch (S3Exception | IOException e) {
                logger.error("Failed to upload image for JsonCat with ID: {}", id, e);
                return new ResponseEntity<>("Image upload to S3 failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        }).orElseGet(() -> {
            logger.warn("JsonCat with ID: {} not found.", id);
            return new ResponseEntity<>("Cat not found", HttpStatus.NOT_FOUND);
        });
    }

    // Получить изображение для кота из S3
    @GetMapping("/{id}/image")
    public ResponseEntity<Object> getImageCat(@PathVariable Long id) {

        logger.info("Fetching image for JsonCat with ID: {} from S3", id);

        Optional<JsonCat> catOptional = jsonCatRepository.findById(id);
        if (!catOptional.isPresent()) {
            logger.warn("JsonCat with ID: {} not found.", id);
            return new ResponseEntity<>("Cat not found", HttpStatus.NOT_FOUND);
        }

        try {
            String fileName = "cat-images/" + id;

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(fileName)
                    .build();
            try {
                ResponseInputStream<GetObjectResponse> s3Image = s3Client.getObject(objectRequest);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = s3Image.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                logger.info("Image for JsonCat with ID: {} fetched successfully", id);
                return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), HttpStatus.OK);
            } catch (NoSuchKeyException e) {
                logger.warn("Image for JsonCat with ID: {} not found on S3", id);
                return new ResponseEntity<>("Image not found on S3", HttpStatus.NOT_FOUND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (S3Exception e) {
            logger.error("Failed to fetch image for JsonCat with ID: {}", id, e);
            return new ResponseEntity<>("Failed to retrieve image from S3", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Получить котов по возрасту
    @GetMapping("/by-age")
    public List<JsonCat> getCatsByAge(@RequestParam int age) {
        logger.info("Fetching cats with age: {}", age);
        return jsonCatRepository.findByAge(age);
    }

    // Получить котов по весу
    @GetMapping("/by-weight")
    public List<JsonCat> getCatsByWeight(@RequestParam BigDecimal weight) {
        logger.info("Fetching cats with weight: {}", weight);
        return jsonCatRepository.findByWeight(weight);
    }



}
