package com.catbd.cat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "cat", schema = "cats")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
//@NoArgsConstructor
public class HibernateCat {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull(message = "Name cannot be null")
    @Size(min = 4, message = "Name should have at least 4 characters")
    private String name;
    private int age;
    private int weight;
}

