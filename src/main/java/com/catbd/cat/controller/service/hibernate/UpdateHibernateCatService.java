package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UpdateHibernateCatService {

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    public Map<String, String> validateBindingResult(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }

    public Optional<HibernateCat> findById(Long id) {
        return hibernateCatRepository.findById(id);
    }

    public HibernateCat updateExistingCat(HibernateCat existingCat, HibernateCat updatedCat) {
        existingCat.setName(updatedCat.getName());
        existingCat.setAge(updatedCat.getAge());
        existingCat.setWeight(updatedCat.getWeight());
        return hibernateCatRepository.save(existingCat);
    }
}