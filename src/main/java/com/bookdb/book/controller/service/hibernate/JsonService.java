package com.bookdb.book.controller.service.hibernate;

import com.bookdb.book.controller.JsonController;
import com.bookdb.book.controller.pagination.PageResponse;
import com.bookdb.book.entity.BookDTO;
import com.bookdb.book.entity.JsonBook;
import com.bookdb.book.repositories.JsonRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("LoggingSimilarMessage")
@Service
public class JsonService implements JsonServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(JsonController.class);

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Value("${aws.s3.bucket-name}")
    private String awsBucketName;

    @Autowired
    private JsonRepository jsonRepository;

    @Autowired
    private S3Client s3Client;

    public Page<JsonBook> getAllJsonBooks(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return jsonRepository.findAll(pageable);
    }



    public ResponseEntity<JsonBook> getJsonBookById(@PathVariable Long id) {
        logger.info("Fetching HibernateBook with ID: {}", id);
        Optional<JsonBook> book = jsonRepository.findById(id);
        if (book.isPresent()) {
            logger.info("Found HibernateBook with ID: {}", id);
            return ResponseEntity.ok(book.get());
        } else {
            logger.warn("HibernateBook with ID: {} not found.", id);
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Object> createJsonBook(@Valid @org.springframework.web.bind.annotation.RequestBody JsonBook jsonBook, BindingResult bindingResult) {
        logger.info("Creating new HibernateBook with data: {}", jsonBook);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while creating HibernateBook.");
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.warn("Validation error in field '{}': {}", error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        JsonBook savedBook = jsonRepository.save(jsonBook);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> updateJsonBook(@PathVariable Long id, @Valid @org.springframework.web.bind.annotation.RequestBody BookDTO bookDTO, BindingResult bindingResult) {
        logger.info("Updating JsonBook with ID: {}", id);

        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while updating JsonBook with ID: {}", id);
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.warn("Validation error in field '{}': {}", error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Optional<JsonBook> existingBookOptional = jsonRepository.findById(id);
        if (existingBookOptional.isEmpty()) {
            logger.warn("JsonBook with ID: {} not found.", id);
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }
        JsonBook existingBook = existingBookOptional.get();

        JsonBook updatedBook = jsonRepository.save(existingBook);
        logger.info("JsonBook with ID: {} updated successfully.", id);

        return new ResponseEntity<>(updatedBook, HttpStatus.OK);
    }

    public ResponseEntity<Void> deleteJsonBook(@PathVariable Long id) {
        logger.info("Deleting HibernateBook with ID: {}", id);
        if (jsonRepository.existsById(id)) {
            jsonRepository.deleteById(id);
            logger.info("HibernateBook with ID: {} deleted successfully.", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            logger.warn("HibernateBook with ID: {} not found for deletion.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> uploadS3Image(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile) {
        logger.info("Uploading image for JsonBook with ID: {} to S3", id);
        return jsonRepository.findById(id).map(book -> {

            if (imageFile.isEmpty()) {
                logger.warn("Uploaded image is empty for JsonBook ID: {}", id);
                return new ResponseEntity<>("Image can't be empty", HttpStatus.BAD_REQUEST);
            }

            if (!Objects.requireNonNull(imageFile.getContentType()).startsWith("image/")) {
                logger.warn("Invalid file type uploaded for JsonBook with ID: {}", id);
                return new ResponseEntity<>("Invalid file type. Only images are allowed", HttpStatus.BAD_REQUEST);
            }

            try {

                Region region = Region.of(awsRegion);
                String fileName = "book-images/" + id;

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(awsBucketName)
                        .key(fileName)
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageFile.getBytes()));

                String imageUrl = "https://" + awsBucketName + ".s3." + region.id() + ".amazonaws.com/" + fileName;

//                book.setImageUrl(imageUrl);
                jsonRepository.save(book);

                logger.info("Image for JsonBook with ID: {} uploaded successfully to S3 at URL: {}", id, imageUrl);
                return new ResponseEntity<>("Image uploaded successfully", HttpStatus.CREATED);

            } catch (S3Exception | IOException e) {
                logger.error("Failed to upload image for JsonBook with ID: {}", id, e);
                return new ResponseEntity<>("Image upload to S3 failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        }).orElseGet(() -> {
            logger.warn("JsonBook with ID: {} not found.", id);
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        });
    }

    public ResponseEntity<Object> getImageBook(@PathVariable Long id) {

        logger.info("Fetching image for JsonBook with ID: {} from S3", id);

        Optional<JsonBook> bookOptional = jsonRepository.findById(id);
        if (bookOptional.isEmpty()) {
            logger.warn("JsonBook with ID: {} not found.", id);
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }

        try {
            String fileName = "book-images/" + id;

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
                logger.info("Image for JsonBook with ID: {} fetched successfully", id);
                return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), HttpStatus.OK);
            } catch (NoSuchKeyException e) {
                logger.warn("Image for JsonBook with ID: {} not found on S3", id);
                return new ResponseEntity<>("Image not found on S3", HttpStatus.NOT_FOUND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (S3Exception e) {
            logger.error("Failed to fetch image for JsonBook with ID: {}", id, e);
            return new ResponseEntity<>("Failed to retrieve image from S3", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<JsonBook> getBooksByAge(@RequestParam String author) {
        logger.info("Fetching books with age: {}", author);
        return jsonRepository.findByAuthor(author);
    }

    public List<JsonBook> getBooksBylastReaded(@RequestParam BigDecimal lastReaded) {
        logger.info("Fetching books with lastReaded: {}", lastReaded);
        return jsonRepository.findBylastReaded(lastReaded);
    }
}
