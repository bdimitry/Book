package com.catbd.cat.filtering;

import cz.jirutka.rsql.parser.ast.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class CustomRsqlVisitor<T> implements RSQLVisitor<Specification<T>, Void> {

    @Override
    public Specification<T> visit(AndNode andNode, Void unused) {
        return null;
    }

    @Override
    public Specification<T> visit(OrNode orNode, Void unused) {
        return null;
    }

    @Override
    public Specification<T> visit(ComparisonNode node, Void param) {
        ComparisonOperator operator = node.getOperator();
        String selector = node.getSelector();
        List<String> arguments = node.getArguments();

        // Проверяем, если фильтр для JSONB поля
        if (selector.startsWith("cat.")) {
            String jsonbKey = selector.substring(4); // получаем ключ JSONB

            return (root, query, criteriaBuilder) -> {
                String value = arguments.get(0);
                if (operator.equals(RSQLOperators.EQUAL)) {
                    return criteriaBuilder.equal(
                            criteriaBuilder.function("jsonb_extract_path_text", String.class, root.get("cat"), criteriaBuilder.literal(jsonbKey)),
                            value
                    );
                } else if (operator.equals(RSQLOperators.GREATER_THAN)) {
                    return criteriaBuilder.greaterThan(
                            criteriaBuilder.function("CAST", Double.class,
                                    criteriaBuilder.function("jsonb_extract_path_text", String.class, root.get("cat"), criteriaBuilder.literal(jsonbKey))),
                            Double.valueOf(value)
                    );
                }
                // Добавить обработку других операторов, если нужно
                return null;
            };
        }

        // Стандартная логика RSQL для других полей
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(selector), arguments.get(0));
    }
}

