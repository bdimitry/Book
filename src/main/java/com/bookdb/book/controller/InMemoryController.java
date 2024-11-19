package com.bookdb.book.controller;

import com.bookdb.book.entity.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class InMemoryController {
    private final List<Book> bookList = new ArrayList<>();
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/v1/api/books")
    public List<Book> getBooks() {
        return bookList;
    }

    @GetMapping("/v1/api/books/{id}")
    public Book getBook(@PathVariable int id) {
        return bookList.get(id - 1);
    }

    @PostMapping("/v1/api/books")
    public Book createBook(@RequestBody Book book) {
        book.setId(bookList.size() + 1);
        bookList.add(book);
        return book;
    }

    @PutMapping("/v1/api/books/{id}")
    public Book updateBook(@RequestBody Book book, @PathVariable int id) {
        bookList.set(id - 1, book);
        return book;
    }

    @DeleteMapping("/v1/api/books/{id}")
    public void deleteBook(@PathVariable int id) {

        bookList.remove(id - 1);
    }
}
