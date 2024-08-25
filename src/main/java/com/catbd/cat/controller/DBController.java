package com.catbd.cat.controller;

import com.catbd.cat.db.CatsDAO;
import com.catbd.cat.entity.Cat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DBController {
    private final List<Cat> catList = new ArrayList<>();
    @Autowired
    private ObjectMapper objectMapper;
    private final CatsDAO catsDAO = new CatsDAO();

    @GetMapping("/v2/api/cats")
    public List<Cat> getCats() {
        return catsDAO.getAllCats();
    }

    @GetMapping("/v2/api/cats/{id}")
    public Cat getCat(@PathVariable int id) {
        return catsDAO.getCatById(id);
    }

    @PostMapping("/v2/api/cats")
    public Cat createCat(@RequestBody Cat cat) {
        catsDAO.createCat(cat);
        return cat;
    }

    @PutMapping("/v2/api/cats/{id}")
    public Cat updateCat(@RequestBody Cat cat, @PathVariable int id) {
        catsDAO.updateCat(cat);
        return cat;
    }

    @DeleteMapping("/v2/api/cats/{id}")
    public void deleteCat(@PathVariable int id) {
        catsDAO.deleteCat(id);
    }
}
