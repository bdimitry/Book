package com.catbd.cat.entity.conventer;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

//@Converter(autoApply = true)
public class JsonbConverter implements AttributeConverter<Object, String> {

    private static final Jsonb jsonb = JsonbBuilder.create();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return jsonb.toJson(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error serializing JSON object", e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return jsonb.fromJson(dbData, Object.class); // Adjust the class type to your specific use case
        } catch (Exception e) {
            throw new IllegalArgumentException("Error deserializing JSON object", e);
        }
    }
}
