package com.catbd.cat.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public interface CatI {

    String getName();

    Long getAge();
    BigDecimal getWeight();


}