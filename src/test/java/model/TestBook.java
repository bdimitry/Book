package model;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class TestBook {

    private Long id;

    private String name;

    private String author;

    private BigDecimal lastReaded;

    private String imageUrl;
}
