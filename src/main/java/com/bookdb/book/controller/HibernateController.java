package com.bookdb.book.controller;

import com.bookdb.book.controller.service.hibernate.HibernateInterfaceService;
import com.bookdb.book.entity.HibernateBook;
import com.bookdb.book.repositories.HibernateRepository;
import com.bookdb.book.repositories.ImageRepository;
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
@RequestMapping("/v3/api/books")
public class HibernateController {

    @Autowired
    private HibernateInterfaceService hibernateInterfaceService;

    @Autowired
    private HibernateRepository hibernateRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private S3Client s3Client;

    @GetMapping
    public List<HibernateBook> getAllHibernateBooks(@RequestParam(value = "weight", required = false) Double weight, @RequestParam(value = "age", required = false) Integer age) {
        return hibernateInterfaceService.getAllHibernateBooks(weight, age);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HibernateBook> getHibernateBookById(@PathVariable Long id) {
        return hibernateInterfaceService.getHibernateBookById(id);
    }

    @PostMapping
    public ResponseEntity<Object> createHibernateBook(@Valid @org.springframework.web.bind.annotation.RequestBody HibernateBook book, BindingResult bindingResult) {
        return hibernateInterfaceService.createHibernateBook(book, bindingResult);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateHibernateBook(
            @PathVariable Long id,
            @Valid @RequestBody HibernateBook updatedBook,
            BindingResult bindingResult) {
        return hibernateInterfaceService.updateHibernateBook(id, updatedBook, bindingResult);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Id cannot be null");
        }

        Optional<HibernateBook> bookToDelete = hibernateRepository.findById(id);
        if (bookToDelete.isPresent()) {
            hibernateRepository.deleteById(id);
            return ResponseEntity.ok("Book deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found");
        }
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Object> createImageBook(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile) {
        return hibernateInterfaceService.createImageBook(id, imageFile);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageBook(@PathVariable Long id) {
        return hibernateInterfaceService.getImageBook(id);
    }
}
