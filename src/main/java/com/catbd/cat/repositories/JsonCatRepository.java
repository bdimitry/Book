package com.catbd.cat.repositories;

import com.catbd.cat.entity.JsonCat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JsonCatRepository extends JpaRepository<JsonCat, Long>, JpaSpecificationExecutor<JsonCat> {
    @Query(value = "SELECT p.id, " +
            "jsonb_extract_path_text(p.cat, 'name') AS name, " +
            "CAST(jsonb_extract_path_text(p.cat, 'age') AS INTEGER) AS age, " +
            "CAST(jsonb_extract_path_text(p.cat, 'weight') AS DOUBLE PRECISION) AS weight " +
            "FROM jsonCat p WHERE p.id = :id", nativeQuery = true)
    List<Object[]> findParentWithChildDetails(@Param("id") Long id);

    @Query(value = "SELECT * FROM cats.json_cat jc WHERE CAST(jc.cat->>'age' AS INTEGER) = :age", nativeQuery = true)
    List<JsonCat> findByAge(@Param("age") int age);

    @Query(value = "SELECT * FROM cats.json_cat jc WHERE CAST(jc.cat->>'weight' AS DOUBLE PRECISION) = :weight", nativeQuery = true)
    List<JsonCat> findByWeight(@Param("weight") BigDecimal weight);
}
