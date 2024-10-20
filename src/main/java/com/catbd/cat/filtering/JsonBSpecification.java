//package com.catbd.cat.filtering;
//
//import cz.jirutka.rsql.parser.ast.ComparisonNode;
//import jakarta.persistence.criteria.*;
//import org.springframework.data.jpa.domain.Specification;
//
//
//
//public class JsonBSpecification<T> implements Specification<T> {
//
//    private final ComparisonNode node;
//
//    public JsonBSpecification(ComparisonNode node) {
//        this.node = node;
//    }
//
//    @Override
//    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
//        String selector = node.getSelector();
//        String operator = node.getOperator().getSymbol();
//        String argument = node.getArguments().get(0);
//
//        Expression<String> jsonPath = builder.function("jsonb_extract_path_text", String.class,
//                root.get("jsonData"), builder.literal(selector));
//
//        if (operator.equals("==")) {
//            return builder.equal(jsonPath, argument);
//        } else if (operator.equals("!=")) {
//            return builder.notEqual(jsonPath, argument);
//        } else {
//            throw new UnsupportedOperationException("Unsupported operator: " + operator);
//        }
//    }
//}
//
