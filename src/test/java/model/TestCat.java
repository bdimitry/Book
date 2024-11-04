package model;

import jakarta.persistence.Entity;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TestCat {

    private Long id;

    private String name;

    private Long age;

    private BigDecimal weight;

    private String imageUrl;
}
