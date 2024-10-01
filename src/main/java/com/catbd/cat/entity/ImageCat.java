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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "cat_photo", columnDefinition = "BYTEA")
    private byte[] imageData;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false)
    private HibernateCat hibernateCat;
}
