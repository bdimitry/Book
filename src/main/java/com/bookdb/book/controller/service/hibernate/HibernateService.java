package com.bookdb.book.controller.service.hibernate;

import com.bookdb.book.controller.HibernateController;
import com.bookdb.book.entity.HibernateBook;
import com.bookdb.book.entity.ImageBook;
import com.bookdb.book.repositories.HibernateRepository;
import com.bookdb.book.repositories.ImageRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HibernateService implements HibernateInterfaceService {

    private static final Logger logger = LoggerFactory.getLogger(HibernateController.class);

    @Autowired
    private HibernateRepository hibernateRepository;

    @Autowired
    private ImageRepository imageRepository;

    public Map<String, String> validateBindingResult(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }

    public ResponseEntity<HibernateBook> getHibernateBookById(@PathVariable Long id) {
        Optional<HibernateBook> book = hibernateRepository.findById(id);
        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Object> createHibernateBook(@Valid @RequestBody HibernateBook hibernateBook, BindingResult bindingResult) {
        logger.info("Creating new HibernateBook with data: {}", hibernateBook);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors occurred while creating HibernateBook.");
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
                logger.warn("Validation error in field '{}': {}", error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        HibernateBook savedBook = hibernateRepository.save(hibernateBook);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> updateHibernateBook(Long id, HibernateBook hibernateBook, BindingResult bindingResult) {
        logger.info("Updating HibernateBook with ID: {}", id);

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = validateBindingResult(bindingResult);
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Optional<HibernateBook> existingBookOptional = hibernateRepository.findById(id);
        if (existingBookOptional.isEmpty()) {
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }

        HibernateBook existingBook = existingBookOptional.get();
        existingBook.setName(hibernateBook.getName());
        existingBook.setAuthor(hibernateBook.getAuthor());
        existingBook.setWeight(hibernateBook.getWeight());
        // Copy any other fields that need to be updated

        HibernateBook savedBook = hibernateRepository.save(existingBook);
        return new ResponseEntity<>(savedBook, HttpStatus.OK);
    }

    public ResponseEntity<String> deleteBook(Long id) {
        if (id == null) {
            logger.warn("Attempt to delete book with null ID");
            return new ResponseEntity<>("Book ID cannot be null", HttpStatus.BAD_REQUEST);
        }

        Optional<HibernateBook> bookToDelete = hibernateRepository.findById(id);
        if (bookToDelete.isPresent()) {
            hibernateRepository.deleteById(id);
            return new ResponseEntity<>("Book deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Object> createImageBook(Long id, MultipartFile imageFile) {
        try {
            if (imageFile.isEmpty()) {
                throw new IOException("Image can't be empty");
            }

            ImageBook image = ImageBook.builder()
                    .id(id)
                    .imageData(imageFile.getBytes())
                    .build();

            imageRepository.save(image);

            return new ResponseEntity<>(HttpStatus.CREATED);

        } catch (IOException e) {
            return new ResponseEntity<>("Image upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Page<HibernateBook> getAllHibernateBooks(Double weight, String author, int page, int size) {
        Specification<HibernateBook> specification = toSpecification(weight, author);

        PageRequest pageable = PageRequest.of(page, size);

        return hibernateRepository.findAll(specification, pageable);
    }


    public ResponseEntity<byte[]> getImageBook(@PathVariable Long id) {
        Optional<ImageBook> imageBookOpt = imageRepository.findById(id);
        return imageBookOpt.map(imageBook -> new ResponseEntity<>(imageBook.getImageData(),
                HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private Specification<HibernateBook> toSpecification(Double weight, String author) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction(); // Инициализируем предикат
            if (weight != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("weight"), weight));
            }
            if (author != null && !author.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("author"), "%" + author + "%"));
            }
            return predicate;
        };
    }
}
