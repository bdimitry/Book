//package com.catbd.cat.filtering;
//
//import cz.jirutka.rsql.parser.RSQLParser;
//import cz.jirutka.rsql.parser.ast.Node;
//import org.springframework.data.jpa.domain.Specification;
//
//public class RsqlFilterService<T> {
//
//    public Specification<T> createSpecification(String rsqlQuery) {
//        Node rootNode = new RSQLParser().parse(rsqlQuery);
//        return rootNode.accept(new CustomRsqlVisitor<>());
//    }
//}
