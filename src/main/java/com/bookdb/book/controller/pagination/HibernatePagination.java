package com.bookdb.book.controller.pagination;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;


public class HibernatePagination {

    private EntityManager entityManager;

    public HibernatePagination(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> PageImpl<T> getPaginationResult(String jpql, int page, int size, Class<T> resultType) {
        int offset = page * size;

        TypedQuery<T> query = entityManager.createQuery(jpql, resultType);
        query.setFirstResult(offset);
        query.setMaxResults(size);

        List<T> results = query.getResultList();

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(*) FROM " + resultType.getSimpleName(), Long.class);
        Long totalElements = countQuery.getSingleResult();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(results, pageable, totalElements);
    }
}