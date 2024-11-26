package com.bookdb.book.entity;

import lombok.Data;

@Data
public class BookDTO {
    private long id;
    private String name;
    private String author;
    private double lastReaded;
    private String imageUrl;

    public BookDTO(long id, String name, String author, double lastReaded, String imageUrl) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.lastReaded = lastReaded;
        this.imageUrl = imageUrl;
    }

}

