package ru.hnm1nd.base.utils;

import java.util.Map;

@FunctionalInterface
public interface EntityMapper<T> {
    T map(Map<String, Object> entity, int rowNum);
}
