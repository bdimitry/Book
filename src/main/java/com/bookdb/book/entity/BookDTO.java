package com.bookdb.book.entity;

import lombok.Data;

@Data
public class BookDTO {
    private long id;
    private String name;
    private int age;
    private double weight;
    private String imageUrl;

    public BookDTO(long id, String name, int age, double weight, String imageUrl) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.imageUrl = imageUrl;
    }

}

