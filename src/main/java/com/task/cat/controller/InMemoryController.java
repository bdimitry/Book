package com.task.cat.controller;

import com.task.cat.entity.Cat;
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

    @GetMapping("/api/cats")
    public List<Cat> getCats() {
        return catList;
    }

    @GetMapping("/api/cats/{id}")
    public Cat getCat(@PathVariable int id) {
        return catList.get(id - 1);
    }
//        @PostMapping("/api/special")
//        public String giveSpecialCat(@RequestParam String name){
//         Cat cat = new Cat(name, 5, 10);
//         String jsonData = null;
//         try {
//             jsonData = objectMapper.writeValueAsString(cat);
//         } catch (JsonProcessingException e) {
//             System.out.println("Error with Cat");
//         }
//         return jsonData;
//    }
    @PostMapping("/api/cats")
    public Cat createCat(@RequestBody Cat cat) {
        cat.setId(catList.size() + 1);
        catList.add(cat);
        return cat;
    }

    @PutMapping("/api/cats/{id}")
    public Cat updateCat(@RequestBody Cat cat, @PathVariable int id) {
        catList.set(id - 1, cat);
        return cat;
    }
//    @DeleteMapping("/api/cat/{id}")
//    public String deleteCat(@PathVariable Long id) {
//        Cat cat = new Cat(name, 5, 10);
//        objectMapper.delete(cat);
//        return "Cat deleted with id: " + id;
//    }
//    @PutMapping("/api/cat/{id}")
//    public Cat updateCat(@PathVariable Long id, @RequestBody Cat catDetails) {
//
//        Cat cat = new Cat(name, 5, 10);
//        cat.setName(catDetails.getName());
//        cat.setAge(catDetails.getAge());
//        cat.setWeight(catDetails.getWeight());
//        return objectMapper.wait(cat);
//    }
    @DeleteMapping("/api/cats/{id}")
    public void deleteCat(@PathVariable int id){

        catList.remove(id - 1);    }
}
