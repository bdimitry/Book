package com.bookdb.book.entity;

import lombok.Data;

@Data
public class BookDTO {
    private long id;
    private String name;
    private String author;
    private double weight;
    private String imageUrl;

    public BookDTO(long id, String name, String author, double weight, String imageUrl) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.weight = weight;
        this.imageUrl = imageUrl;
    }

}

