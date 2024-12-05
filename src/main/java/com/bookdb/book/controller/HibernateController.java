package com.bookdb.book.controller;

import com.bookdb.book.controller.service.hibernate.HibernateInterfaceService;
import com.bookdb.book.entity.HibernateBook;
import com.bookdb.book.repositories.HibernateRepository;
import com.bookdb.book.repositories.ImageRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

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
    public Page<HibernateBook> getAllHibernateBooks(
            @RequestParam(value = "lastReaded", required = false) Double lastReaded,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        return hibernateInterfaceService.getAllHibernateBooks(lastReaded, author, page, size);
    }


    @GetMapping("/{id}")
    public ResponseEntity<HibernateBook> getHibernateBookById(@PathVariable Long id) {
        return hibernateInterfaceService.getHibernateBookById(id);
    }

    @PostMapping
    public ResponseEntity<Object> createHibernateBook(
            @Valid @RequestBody HibernateBook book,
            BindingResult bindingResult) {
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
        return hibernateInterfaceService.deleteBook(id);
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<Object> createImageBook(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile) {
        return hibernateInterfaceService.createImageBook(id, imageFile);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageBook(@PathVariable Long id) {
        return hibernateInterfaceService.getImageBook(id);
    }
}

