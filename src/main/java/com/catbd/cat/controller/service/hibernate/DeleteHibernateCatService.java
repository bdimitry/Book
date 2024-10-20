package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeleteHibernateCatService {

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    public Optional<HibernateCat> deleteCat(Long id) {
        Optional<HibernateCat> catToDelete = hibernateCatRepository.findById(id);
        if (catToDelete.isPresent()) {
            hibernateCatRepository.deleteById(id);
        }
        return catToDelete;
    }
}
