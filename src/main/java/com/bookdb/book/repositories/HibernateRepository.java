package com.bookdb.book.repositories;

import com.bookdb.book.entity.HibernateBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface HibernateRepository extends JpaRepository<HibernateBook, Long>, JpaSpecificationExecutor<HibernateBook> {
}
