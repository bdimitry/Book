package com.bookdb.book.repositories;

import com.bookdb.book.entity.ImageBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ImageRepository extends JpaRepository<ImageBook, Long> {
}