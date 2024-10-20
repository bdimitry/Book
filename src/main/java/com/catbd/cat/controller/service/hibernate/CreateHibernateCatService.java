package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.CatI;
import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.entity.ImageCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import com.catbd.cat.repositories.ImageCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class CreateHibernateCatService implements CatService {

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    @Autowired
    private ImageCatRepository imageCatRepository;

    public CatI createHibernateCat(CatI cat) {
        return hibernateCatRepository.save(cat);
    }

    public Optional<HibernateCat> deleteCat(Long id) {
        Optional<HibernateCat> catToDelete = hibernateCatRepository.findById(id);
        if (catToDelete.isPresent()) {
            hibernateCatRepository.deleteById(id);
        }
        return catToDelete;
    }

    public ResponseEntity<Object> createImageCat(Long id, MultipartFile imageFile) {
        try {
            if (imageFile.isEmpty()) {
                throw new IOException("Image can't be empty");
            }

            ImageCat image = ImageCat.builder()
                    .id(id)
                    .imageData(imageFile.getBytes())
                    .build();

            imageCatRepository.save(image);

            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (IOException e) {
            return new ResponseEntity<>("Image upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
