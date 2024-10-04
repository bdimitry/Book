package com.catbd.cat.controller;

public class CatDTO {
    private long id;
    private String name;
    private int age;
    private double weight;
    private String imageUrl;

    // Конструктор
    public CatDTO(long id, String name, int age, double weight, String imageUrl) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.imageUrl = imageUrl;
    }

    // Геттеры
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Сеттеры (если они нужны)
    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

