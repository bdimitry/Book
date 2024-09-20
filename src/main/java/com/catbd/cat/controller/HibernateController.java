package com.catbd.cat.controller;

import com.catbd.cat.Repositories.HibernateCatRepository;
import com.catbd.cat.Repositories.ImageCatRepository;
import com.catbd.cat.entity.HibernateCat;
import com.catbd.cat.entity.ImageCat;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v3/api/cats")
//@Validated
public class HibernateController {

    @Autowired
    private HibernateCatRepository hibernateCatRepository;
    @Autowired
    private ImageCatRepository imageCatRepository;

    @GetMapping
    public List<HibernateCat> getAllHibernateCats() {
        return hibernateCatRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HibernateCat> getHibernateCatById(@PathVariable Long id) {
        Optional<HibernateCat> Cat = hibernateCatRepository.findById(id);
        return Cat.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Object> createHibernateCat(@Valid @RequestBody HibernateCat cat, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(hibernateCatRepository.save(cat), HttpStatus.CREATED);
    }

    // Update existing Cat
    @PutMapping("/{id}")
    public ResponseEntity<HibernateCat> updateHibernateCat(@PathVariable Long id, @RequestBody HibernateCat CatDetails) {
        Optional<HibernateCat> CatOptional = hibernateCatRepository.findById(id);
        if (CatOptional.isPresent()) {
            HibernateCat Cat = CatOptional.get();
            Cat.setName(CatDetails.getName());
            Cat.setAge(CatDetails.getAge());
            Cat.setWeight(CatDetails.getWeight());
            HibernateCat updatedHibernateCat = hibernateCatRepository.save(Cat);
            return ResponseEntity.ok(updatedHibernateCat);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    // Image post
    @PostMapping("/{id}/image")
    public ResponseEntity<Object> createImageCat(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile) {
        try {
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

    // Image get
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageCat(@PathVariable Long id) {
        Optional<ImageCat> imageCatOpt = imageCatRepository.findById(id);
        if (imageCatOpt.isPresent()) {
            return new ResponseEntity<>(imageCatOpt.get().getImageData(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete Cat
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHibernateCat(@PathVariable Long id) {
        Optional<HibernateCat> CatOptional = hibernateCatRepository.findById(id);
        if (CatOptional.isPresent()) {
            hibernateCatRepository.delete(CatOptional.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
