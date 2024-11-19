package com.bookdb.book.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "book", schema = "books")
@Data
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HibernateBook {

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

}
