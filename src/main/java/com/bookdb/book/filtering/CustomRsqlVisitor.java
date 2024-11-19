package com.bookdb.book.filtering;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public class CustomRsqlVisitor<T> implements RSQLVisitor<Specification<T>, Void> {

    private final Map<String, String> propertyPathMapper;

    public CustomRsqlVisitor(Map<String, String> propertyPathMapper) {
        this.propertyPathMapper = propertyPathMapper;
    }

    @Override
    public Specification<T> visit(AndNode node, Void param) {
        return node.getChildren().stream()
                .map(n -> n.accept(this, param))
                .reduce(Specification::and)
                .orElse(null);
    }

    @Override
    public Specification<T> visit(OrNode node, Void param) {
        return node.getChildren().stream()
                .map(n -> n.accept(this, param))
                .reduce(Specification::or)
                .orElse(null);
    }

    @Override
    public Specification<T> visit(ComparisonNode node, Void param) {
        String selector = node.getSelector();
        String operator = node.getOperator().getSymbol();
        List<String> arguments = node.getArguments();

        if (isJsonBField(selector)) {
            return handleJsonBField(selector, operator, arguments);
        }

        String mappedSelector = propertyPathMapper.getOrDefault(selector, selector);

        return (root, _, builder) -> {
            Path<Object> path = getPath(root, mappedSelector);
            return buildPredicate(path, operator, arguments, builder);
        };
    }

    private Specification<T> handleJsonBField(String selector, String operator, List<String> arguments) {
        return (root, _, builder) -> {
            String[] jsonPath = selector.split("\\.");
            Expression<String> jsonExpression = root.get(jsonPath[0]);

            for (int i = 1; i < jsonPath.length; i++) {
                jsonExpression = builder.function("jsonb_extract_path_text", String.class, jsonExpression, builder.literal(jsonPath[i]));
            }

            return buildPredicate(jsonExpression, operator, arguments, builder);
        };
    }

    private boolean isJsonBField(String selector) {
        return selector.contains(".");
    }

    private Predicate buildPredicate(Expression<?> path, String operator, List<String> arguments, CriteriaBuilder builder) {
        String argument = arguments.getFirst();

        return switch (operator) {
            case "==" -> builder.equal(path, argument);
            case "!=" -> builder.notEqual(path, argument);
            case "=in=" -> path.in(arguments);
            case "=out=" -> builder.not(path.in(arguments));
            case "=like=" -> builder.like(path.as(String.class), "%" + argument + "%");
            case "=gt=" -> builder.greaterThan(path.as(String.class), argument);
            case "=lt=" -> builder.lessThan(path.as(String.class), argument);
            case "=isnull=" -> builder.isNull(path);
            case "=notnull=" -> builder.isNotNull(path);
            default -> throw new UnsupportedOperationException("Unsupported operator: " + operator);
        };
    }

    private Path<Object> getPath(Root<T> root, String selector) {
        if (selector.contains(".")) {
            String[] parts = selector.split("\\.");
            Path<Object> path = root.get(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                path = path.get(parts[i]);
            }
            return path;
        } else {
            return root.get(selector);
        }
    }
}
