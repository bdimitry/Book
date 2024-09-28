package com.catbd.cat.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "image", schema = "cats")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ImageCat {
    @Id
    private Long id;

    @Column(name = "cat_photo", columnDefinition = "BYTEA")
    private byte[] imageData;
}
