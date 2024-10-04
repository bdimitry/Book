package com.catbd.cat.entity;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatEntity {
    private long id;
    private String name;
    private int age;
    private double weight;
}

