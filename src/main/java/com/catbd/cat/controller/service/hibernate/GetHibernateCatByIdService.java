package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetHibernateCatByIdService {

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    public HibernateCat getHibernateCatById(Long id) {
        return hibernateCatRepository.findById(id).orElse(null);
    }

}
