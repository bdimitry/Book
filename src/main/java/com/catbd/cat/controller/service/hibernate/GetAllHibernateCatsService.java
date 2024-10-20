package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllHibernateCatsService {

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    public List<HibernateCat> getAllHibernateCats(Double weight, Integer age) {
        Specification<HibernateCat> specification = toSpecification(weight, age);
        return hibernateCatRepository.findAll(specification);
    }

    private Specification<HibernateCat> toSpecification(Double weight, Integer age) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.and(
                    weight != null ? criteriaBuilder.greaterThan(root.get("weight"), weight) : criteriaBuilder.conjunction(),
                    age != null ? criteriaBuilder.greaterThan(root.get("age"), age) : criteriaBuilder.conjunction()
            );
        };
    }
}