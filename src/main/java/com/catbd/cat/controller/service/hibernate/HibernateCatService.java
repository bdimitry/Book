package com.catbd.cat.controller.service.hibernate;

import com.catbd.cat.controller.HibernateController;
import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.entity.ImageCat;
import com.catbd.cat.repositories.HibernateCatRepository;
import com.catbd.cat.repositories.ImageCatRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HibernateCatService implements CatService {

    private static final Logger logger = LoggerFactory.getLogger(HibernateController.class);

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

    public ResponseEntity<HibernateCat> getHibernateCatById(@PathVariable Long id) {
        Optional<HibernateCat> cat = hibernateCatRepository.findById(id);
        return cat.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Object> createHibernateCat(@Valid @RequestBody HibernateCat hibernateCat, BindingResult bindingResult) {
        logger.info("Creating new HibernateCat with data: {}", hibernateCat);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while creating HibernateCat.");
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.warn("Validation error in field '{}': {}", error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        HibernateCat savedCat = hibernateCatRepository.save(hibernateCat);
        return new ResponseEntity<>(savedCat, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> updateHibernateCat(Long id, HibernateCat updatedCat, BindingResult bindingResult) {
        logger.info("Updating HibernateCat with ID: {}", id);

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = validateBindingResult(bindingResult);
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Optional<HibernateCat> existingCatOptional = hibernateCatRepository.findById(id);
        if (existingCatOptional.isEmpty()) {
            return new ResponseEntity<>("Cat not found", HttpStatus.NOT_FOUND);
        }

        HibernateCat existingCat = existingCatOptional.get();
        existingCat.setName(updatedCat.getName());
        existingCat.setAge(updatedCat.getAge());
        existingCat.setWeight(updatedCat.getWeight());
        // Copy any other fields that need to be updated

        HibernateCat savedCat = hibernateCatRepository.save(existingCat);
        return new ResponseEntity<>(savedCat, HttpStatus.OK);
    }

    public ResponseEntity<String> deleteCat(Long id) {
        if (id == null) {
            logger.warn("Attempt to delete cat with null ID");
            return new ResponseEntity<>("Cat ID cannot be null", HttpStatus.BAD_REQUEST);
        }

        Optional<HibernateCat> catToDelete = hibernateCatRepository.findById(id);
        if (catToDelete.isPresent()) {
            hibernateCatRepository.deleteById(id);
            return new ResponseEntity<>("Cat deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Cat not found", HttpStatus.NOT_FOUND);
        }
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

    public ResponseEntity<byte[]> getImageCat(@PathVariable Long id) {
        Optional<ImageCat> imageCatOpt = imageCatRepository.findById(id);
        return imageCatOpt.map(imageCat -> new ResponseEntity<>(imageCat.getImageData(),
                HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private Specification<HibernateCat> toSpecification(Double weight, Integer age) {
        return (root, _, criteriaBuilder) -> criteriaBuilder.and(
                weight != null ? criteriaBuilder.greaterThan(root.get("weight"), weight) : criteriaBuilder.conjunction(),
                age != null ? criteriaBuilder.greaterThan(root.get("age"), age) : criteriaBuilder.conjunction()
        );
    }
}
