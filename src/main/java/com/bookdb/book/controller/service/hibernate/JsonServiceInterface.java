package com.bookdb.book.controller.service.hibernate;

import com.bookdb.book.entity.BookDTO;
import com.bookdb.book.entity.JsonBook;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface JsonServiceInterface {

    ResponseEntity<JsonBook> getJsonBookById(@PathVariable Long id);

    List<JsonBook> getAllJsonBooks();

    ResponseEntity<Object> createJsonBook(@Valid @org.springframework.web.bind.annotation.RequestBody JsonBook jsonBook, BindingResult bindingResult);

    ResponseEntity<Object> updateJsonBook(@PathVariable Long id, @Valid @org.springframework.web.bind.annotation.RequestBody BookDTO bookDTO, BindingResult bindingResult);

    ResponseEntity<Void> deleteJsonBook(@PathVariable Long id);

    ResponseEntity<String> uploadS3Image(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile);

    ResponseEntity<Object> getImageBook(@PathVariable Long id);

    List<JsonBook> getBooksByAge(@RequestParam String author);

    List<JsonBook> getBooksByWeight(@RequestParam BigDecimal weight);
}