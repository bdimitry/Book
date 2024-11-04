package com.catbd.cat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "cat", schema = "cats")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HibernateCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "Name cannot be null")
    @Size(min = 4, max = 100, message = "Name should have between 4 and 100 characters")
    private String name;

    @Min(value = 0, message = "Age must be a non-negative number")
    private int age;

    @Min(value = 1, message = "Weight must be at least 1 kilo")
    private int weight;

//    @Column(name = "image_url")
//    private String imageUrl;
}
