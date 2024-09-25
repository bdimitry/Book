package com.catbd.cat;


import com.catbd.cat.entity.HibernateCat;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HibernateCatServiceClientTest {

    private final RestTemplate restTemplate;

    public HibernateCatServiceClientTest(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public HibernateCat getCats() {
        return restTemplate.getForObject("/v3/api/cats",
                HibernateCat.class);
    }
}

