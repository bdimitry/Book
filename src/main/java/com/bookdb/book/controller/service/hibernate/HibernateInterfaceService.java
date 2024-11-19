package com.bookdb.book.controller.service.hibernate;

import com.bookdb.book.entity.HibernateBook;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HibernateInterfaceService {

    ResponseEntity<HibernateBook> getHibernateBookById(Long id);

    ResponseEntity<String> deleteBook(Long id);

    ResponseEntity<Object> createHibernateBook(HibernateBook hibernateBook, BindingResult bindingResult);

    ResponseEntity<Object> createImageBook(Long id, MultipartFile imageFile);

    List<HibernateBook> getAllHibernateBooks(Double weight, Integer age);

    ResponseEntity<Object> updateHibernateBook(Long id, HibernateBook hibernateBook, BindingResult bindingResult);

    ResponseEntity<byte[]> getImageBook(Long id);
}
