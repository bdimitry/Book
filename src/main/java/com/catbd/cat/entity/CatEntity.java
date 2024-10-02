package com.catbd.cat.entity;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CatEntity {
    private int id;
    private String name;
    private int age;
    private int weight;
}
