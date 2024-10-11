package com.catbd.cat.entity;

import com.catbd.cat.entity.conventer.JsonbConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "json_cat", schema = "cats")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class JsonCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @JsonIgnore
    private Map<String, Object> cat = new HashMap();

    @Column(name = "image_url")
    @JsonProperty("imageUrl")
    private String imageUrl;

    // Геттеры и сеттеры
    @JsonProperty("name")
    public String getName() {
        if (cat.get("name") == null) return null;
        return cat.get("name").toString();
    }

    @JsonProperty("age")
    public Long getAge() {
        if (cat.get("age") == null) return null;
        return ((Number) cat.get("age")).longValue();
    }

    @JsonProperty("weight")
    public BigDecimal getWeight() {
        if (cat.get("weight") == null) return null;
        return BigDecimal.valueOf(((Number) cat.get("weight")).doubleValue());
    }

    public void setName(String name) {
        if (name == null) return;
        cat.put("name", name);
    }

    public void setAge(Long age) {
        if (age == null) return;
        cat.put("age", BigDecimal.valueOf(age));
    }

    public void setWeight(BigDecimal weight) {
        if (weight == null) return;
        cat.put("weight", weight);
    }
}

