package com.catbd.cat.Repositories;

import com.catbd.cat.entity.HibernateCat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


    @Repository
    public interface HibernateCatRepository extends JpaRepository<HibernateCat, Long> {
    }
