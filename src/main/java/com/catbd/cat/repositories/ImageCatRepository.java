package com.catbd.cat.repositories;

import com.catbd.cat.entity.ImageCat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ImageCatRepository extends JpaRepository<ImageCat, Long> {
}