package com.catbd.cat.filtering;

import com.catbd.cat.entity.JsonCat;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class RsqlFilter {

    public Specification<JsonCat> parseRSQL(String rsqlQuery) {
        Node rootNode = new RSQLParser().parse(rsqlQuery);
        return rootNode.accept(new CustomRsqlVisitor<>());
    }
}

