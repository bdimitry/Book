package com.catbd.cat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "jsonCat", schema = "cats")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class JsonCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private int weight;

    private int age;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private CatEntity cat;
}
