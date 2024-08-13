package com.task.cat.controller;

import com.task.cat.db.CatsDAO;
import com.task.cat.entity.Cat;
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
    private CatsDAO catsDAO = new CatsDAO();

    @GetMapping("/api/cats/db")
    public List<Cat> getCats() {
        return catsDAO.getAllCats();
    }

    @GetMapping("/api/cats/db/{id}")
    public Cat getCat(@PathVariable int id) {
        return catsDAO.getCatById(id);
    }

    @PostMapping("/api/cats/db")
    public Cat createCat(@RequestBody Cat cat) {
        catsDAO.createCat(cat);
        return cat;
    }

    @PutMapping("/api/cats/db/{id}")
    public Cat updateCat(@RequestBody Cat cat, @PathVariable int id) {
        catsDAO.updateCat(cat);
        return cat;
    }

    @DeleteMapping("/api/cats/db/{id}")
    public void deleteCat(@PathVariable int id){
        catsDAO.deleteCat(id);
    }
}
