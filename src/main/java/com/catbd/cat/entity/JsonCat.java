package com.catbd.cat.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "jsonCat", schema = "cats")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class JsonCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private long id;

    @Column(columnDefinition = "jsonb")
    @JsonProperty("cat")
    private CatEntity cat;

    @Column(name = "image_url")
    @JsonProperty("imageUrl")
    private String imageUrl;

    // Геттеры и сеттеры
}

