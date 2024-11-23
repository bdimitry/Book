package com.bookdb.book.repositories;

import com.bookdb.book.entity.JsonBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JsonRepository extends JpaRepository<JsonBook, Long>, JpaSpecificationExecutor<JsonBook> {
    @Query(value = "SELECT p.id, " +
            "jsonb_extract_path_text(p.book, 'name') AS name, " +
            "CAST(jsonb_extract_path_text(p.book, 'author') AS INTEGER) AS author, " +
            "CAST(jsonb_extract_path_text(p.book, 'weight') AS DOUBLE PRECISION) AS weight " +
            "FROM jsonBook p WHERE p.id = :id", nativeQuery = true)
    List<Object[]> findParentWithChildDetails(@Param("id") Long id);

    @Query(value = "SELECT * FROM books.json_book jc WHERE CAST(jc.book->>'author' AS INTEGER) = :author", nativeQuery = true)
    List<JsonBook> findByAuthor(@Param("author") String author);

    @Query(value = "SELECT * FROM books.json_book jc WHERE CAST(jc.book->>'weight' AS DOUBLE PRECISION) = :weight", nativeQuery = true)
    List<JsonBook> findByWeight(@Param("weight") BigDecimal weight);
}
