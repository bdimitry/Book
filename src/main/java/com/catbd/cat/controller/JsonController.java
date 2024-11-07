package com.catbd.cat.controller;

import com.catbd.cat.controller.service.hibernate.JsonCatServiceInterface;
import com.catbd.cat.entity.CatDTO;
import com.catbd.cat.entity.JsonCat;
import com.catbd.cat.repositories.ImageCatRepository;
import com.catbd.cat.repositories.JsonCatRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v4/api/cats")
public class JsonController {

    private static final Logger logger = LoggerFactory.getLogger(JsonController.class);

    @Value("${aws.s3.region}")
    private String awsRegion;

    @Value("${aws.s3.bucket-name}")
    private String awsBucketName;

    @Autowired
    private JsonCatServiceInterface jsonCatServiceInterface;

    @Autowired
    private JsonCatRepository jsonCatRepository;

    @Autowired
    private ImageCatRepository imageCatRepository;

    @Autowired
    private S3Client s3Client;

    // Получить всех котов
    @GetMapping
    public List<JsonCat> getAllHibernateCats() {
        return jsonCatServiceInterface.getAllJsonCats();
    }

    // Получить кота по ID
    @GetMapping("/{id}")
    public ResponseEntity<JsonCat> getHibernateCatById(@PathVariable Long id) {
        return jsonCatServiceInterface.getJsonCatById(id);
    }

    // Создать нового кота
    @PostMapping
    public ResponseEntity<Object> createHibernateCat(@Valid @org.springframework.web.bind.annotation.RequestBody JsonCat jsonCat, BindingResult bindingResult) {
        return jsonCatServiceInterface.createJsonCat(jsonCat, bindingResult);
    }

    // Обновить данные о коте
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateHibernateCat(@PathVariable Long id, @Valid @org.springframework.web.bind.annotation.RequestBody CatDTO catDTO, BindingResult bindingResult) {
        return jsonCatServiceInterface.updateJsonCat(id, catDTO, bindingResult);
    }

    // Удалить кота по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHibernateCat(@PathVariable Long id) {
        return jsonCatServiceInterface.deleteJsonCat(id);
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<String> uploadS3Image(@PathVariable Long id, @RequestParam("image") MultipartFile imageFile) {
        return jsonCatServiceInterface.uploadS3Image(id, imageFile);
    }

    // Получить изображение для кота из S3
    @GetMapping("/{id}/image")
    public ResponseEntity<Object> getImageCat(@PathVariable Long id) {
        return jsonCatServiceInterface.getImageCat(id);
    }

    // Получить котов по возрасту
    @GetMapping("/by-age")
    public List<JsonCat> getCatsByAge(@RequestParam int age) {
        logger.info("Fetching cats with age: {}", age);
        return jsonCatRepository.findByAge(age);
    }

    // Получить котов по весу
    @GetMapping("/by-weight")
    public List<JsonCat> getCatsByWeight(@RequestParam BigDecimal weight) {
        logger.info("Fetching cats with weight: {}", weight);
        return jsonCatRepository.findByWeight(weight);
    }
}