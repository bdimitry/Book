package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.ImageCat;
import com.catbd.cat.repositories.ImageCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Service
public class GetImageCatService {

    @Autowired
    private ImageCatRepository imageCatRepository;

    public ResponseEntity<byte[]> getImageCat(@PathVariable Long id) {
        Optional<ImageCat> imageCatOpt = imageCatRepository.findById(id);
        if (imageCatOpt.isPresent()) {
            return new ResponseEntity<>(imageCatOpt.get().getImageData(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
