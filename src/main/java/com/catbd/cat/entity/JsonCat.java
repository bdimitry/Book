package com.catbd.cat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "json_cat", schema = "cats")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class JsonCat {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @JsonIgnore
    private String cat;

    @Column(name = "image_url")
    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("name")
    public String getName() {
        return getJsonFieldAsString("name");
    }

    @JsonProperty("age")
    public Long getAge() {
        return getJsonFieldAsLong("age");
    }

    @JsonProperty("weight")
    public BigDecimal getWeight() {
        return getJsonFieldAsBigDecimal("weight");
    }

    public void setName(String name) {
        setJsonField("name", name);
    }

    public void setAge(Long age) {
        setJsonField("age", age);
    }

    public void setWeight(BigDecimal weight) {
        setJsonField("weight", weight);
    }

    private String getJsonFieldAsString(String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(cat);
            return node.has(fieldName) ? node.get(fieldName).asText() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Long getJsonFieldAsLong(String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(cat);
            return node.has(fieldName) ? node.get(fieldName).asLong() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private BigDecimal getJsonFieldAsBigDecimal(String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(cat);
            return node.has(fieldName) ? node.get(fieldName).decimalValue() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setJsonField(String fieldName, Object value) {
        try {
            ObjectNode node = cat == null ? objectMapper.createObjectNode() : (ObjectNode) objectMapper.readTree(cat);
            if (value == null) {
                node.remove(fieldName);
            } else {
                node.putPOJO(fieldName, value);
            }
            cat = node.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
