package com.catbd.cat.controller;

import com.catbd.cat.entity.Cat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class InMemoryController {
    private final List<Cat> catList = new ArrayList<>();
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/v1/api/cats")
    public List<Cat> getCats() {
        return catList;
    }

    @GetMapping("/v1/api/cats/{id}")
    public Cat getCat(@PathVariable int id) {
        return catList.get(id - 1);
    }

    @PostMapping("/v1/api/cats")
    public Cat createCat(@RequestBody Cat cat) {
        cat.setId(catList.size() + 1);
        catList.add(cat);
        return cat;
    }

    @PutMapping("/v1/api/cats/{id}")
    public Cat updateCat(@RequestBody Cat cat, @PathVariable int id) {
        catList.set(id - 1, cat);
        return cat;
    }

    @DeleteMapping("/v1/api/cats/{id}")
    public void deleteCat(@PathVariable int id) {

        catList.remove(id - 1);
    }
}
