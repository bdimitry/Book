package com.bookdb.book.controller;

import com.bookdb.book.controller.service.hibernate.JsonServiceInterface;
import com.bookdb.book.entity.BookDTO;
import com.bookdb.book.entity.JsonBook;
import com.bookdb.book.repositories.JsonRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v4/api/books")
public class JsonController {

    private static final Logger logger = LoggerFactory.getLogger(JsonController.class);

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Value("${aws.s3.bucket-name}")
    private String awsBucketName;

    @Autowired
    private JsonServiceInterface jsonServiceInterface;

    @Autowired
    private JsonRepository jsonRepository;

    @GetMapping
    public List<JsonBook> getAllHibernateBooks() {
        return jsonServiceInterface.getAllJsonBooks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonBook> getHibernateBookById(@PathVariable Long id) {
        return jsonServiceInterface.getJsonBookById(id);
    }

    @PostMapping
    public ResponseEntity<Object> createHibernateBook(@Valid @org.springframework.web.bind.annotation.RequestBody JsonBook jsonBook, BindingResult bindingResult) {
        return jsonServiceInterface.createJsonBook(jsonBook, bindingResult);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateHibernateBook(@PathVariable Long id, @Valid @org.springframework.web.bind.annotation.RequestBody BookDTO bookDTO, BindingResult bindingResult) {
        return jsonServiceInterface.updateJsonBook(id, bookDTO, bindingResult);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHibernateBook(@PathVariable Long id) {
        return jsonServiceInterface.deleteJsonBook(id);
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<String> uploadS3Image(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile) {
        return jsonServiceInterface.uploadS3Image(id, imageFile);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Object> getImageBook(@PathVariable Long id) {
        return jsonServiceInterface.getImageBook(id);
    }

    @GetMapping("/by-age")
    public List<JsonBook> getBooksByAge(@RequestParam int age) {
        logger.info("Fetching books with age: {}", age);
        return jsonRepository.findByAge(age);
    }

    @GetMapping("/by-weight")
    public List<JsonBook> getBooksByWeight(@RequestParam BigDecimal weight) {
        logger.info("Fetching books with weight: {}", weight);
        return jsonRepository.findByWeight(weight);
    }
}