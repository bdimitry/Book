package com.catbd.cat.Repositories;

import com.catbd.cat.entity.JsonCat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JsonCatRepository extends JpaRepository<JsonCat, Long> {
}
