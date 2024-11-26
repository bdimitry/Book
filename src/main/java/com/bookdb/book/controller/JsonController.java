package com.bookdb.book.controller;

import com.bookdb.book.controller.pagination.PageResponse;
import com.bookdb.book.controller.service.hibernate.JsonServiceInterface;
import com.bookdb.book.entity.BookDTO;
import com.bookdb.book.entity.JsonBook;
import com.bookdb.book.repositories.JsonRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<List<JsonBook>> getAllJsonBooks(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        Page<JsonBook> result = jsonServiceInterface.getAllJsonBooks(page, size);
        return ResponseEntity.ok(result.getContent()); // Возвращаем только content
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
    public List<JsonBook> getBooksByAge(@RequestParam String author) {
        logger.info("Fetching books with age: {}", author);
        return jsonRepository.findByAuthor(author);
    }

    @GetMapping("/by-lastReaded")
    public List<JsonBook> getBooksBylastReaded(@RequestParam BigDecimal lastReaded) {
        logger.info("Fetching books with lastReaded: {}", lastReaded);
        return jsonRepository.findBylastReaded(lastReaded);
    }
}