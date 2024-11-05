package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.entity.ImageCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import com.catbd.cat.repositories.ImageCatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HibernateCatService implements CatService {

    @Autowired
    private HibernateCatRepository hibernateCatRepository;

    @Autowired
    private ImageCatRepository imageCatRepository;

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

    public HibernateCat createHibernateCat(HibernateCat cat) {
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

    public ResponseEntity<byte[]> getImageCat(@PathVariable Long id) {
        Optional<ImageCat> imageCatOpt = imageCatRepository.findById(id);
        if (imageCatOpt.isPresent()) {
            return new ResponseEntity<>(imageCatOpt.get().getImageData(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
