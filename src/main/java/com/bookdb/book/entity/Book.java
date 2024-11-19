package com.bookdb.book.entity;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    private int id;
    private String name;
    private int age;
    private int weight;
}
