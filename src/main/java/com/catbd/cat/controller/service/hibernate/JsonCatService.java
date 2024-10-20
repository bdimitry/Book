package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.CatI;
import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.repositories.JsonCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service()
public class JsonCatService implements CatService{
    @Autowired
    private JsonCatRepository jsonCatRepository;

    @Override
    public CatI createHibernateCat(CatI cat) {
        return jsonCatRepository.save(cat);
    }

    @Override
    public Optional<HibernateCat> deleteCat(Long id) {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<Object> createImageCat(Long id, MultipartFile imageFile) {
        return null;
    }
}
