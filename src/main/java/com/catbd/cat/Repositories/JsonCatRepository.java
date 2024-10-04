package com.catbd.cat.Repositories;

import com.catbd.cat.entity.JsonCat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JsonCatRepository extends JpaRepository<JsonCat, Long> {
    @Query(value = "SELECT p.id, " +
            "jsonb_extract_path_text(p.cat, 'name') AS name, " +
            "CAST(jsonb_extract_path_text(p.cat, 'age') AS INTEGER) AS age, " +
            "CAST(jsonb_extract_path_text(p.cat, 'weight') AS DOUBLE PRECISION) AS weight " +
            "FROM jsonCat p WHERE p.id = :id", nativeQuery = true)
    List<Object[]> findParentWithChildDetails(@Param("id") Long id);
}
