package com.bookdb.book.entity;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookEntity {
    private Long id;
    private String name;
    private int age;
    private double weight;
}

