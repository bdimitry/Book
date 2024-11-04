package com.catbd.cat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image", schema = "cats")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ImageCat  {
    @Id
    private Long id;

    @Column(name = "cat_photo", columnDefinition = "BYTEA")
    private byte[] imageData;

//    @OneToOne TODO: Try to use this instead of Id field above
//    @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false)
//    private HibernateCat hibernateCat;
}
