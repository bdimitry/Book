package com.bookdb.book.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image", schema = "books")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ImageBook {
    @Id
    private Long id;

    @Column(name = "book_photo", columnDefinition = "BYTEA")
    private byte[] imageData;

}
