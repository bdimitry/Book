package com.bookdb.book.controller;

import com.bookdb.book.db.BookDAO;
import com.bookdb.book.entity.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DBController {
    private final List<Book> bookList = new ArrayList<>();
    @Autowired
    private ObjectMapper objectMapper;
    private final BookDAO bookDAO = new BookDAO();

    @GetMapping("/v2/api/books")
    public List<Book> getBook() {
        return bookDAO.getAllBooks();
    }

    @GetMapping("/v2/api/books/{id}")
    public Book getBook(@PathVariable int id) {
        return bookDAO.getBookById(id);
    }

    @PostMapping("/v2/api/books")
    public Book createBook(@RequestBody Book book) {
        bookDAO.createBooks(book);
        return book;
    }

    @PutMapping("/v2/api/books/{id}")
    public Book updateBook(@RequestBody Book book, @PathVariable int id) {
        bookDAO.updateBook(book);
        return book;
    }

    @DeleteMapping("/v2/api/books/{id}")
    public void deleteBook(@PathVariable int id) {
        bookDAO.deleteBook(id);
    }
}
