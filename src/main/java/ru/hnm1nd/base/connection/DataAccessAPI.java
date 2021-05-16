package ru.hnm1nd.base.connection;

import ru.hnm1nd.base.utils.EntityMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public interface DataAccessAPI {

    List<Map<String, Object>> get(String objectType, Map<String, Object> conditions, String... selectableFields);

    <T> List<T> get(String objectType, Map<String, Object> conditions, EntityMapper<T> mapper, String... selectableFields);

    int insert(String objectType, Map<String, Object> data);

    int update(String objectType, Map<String, Object> data, Map<String, Object> conditions);

    int remove(String objectType, Map<String, Object> conditions);

    long count(String objectType, Map<String, Object> conditions);

    Object getDriver();

    default String makeParamList(Map<String, Object> map) {
        return map.entrySet().stream().filter(o -> Objects.nonNull(o.getValue()))
                .map(o -> {
                    String result = "'" + o.getValue().toString() + "'";
                    if (o.getValue() instanceof String && o.getValue().toString().startsWith("{") && o.getValue().toString().endsWith("}")) {
                        result = o.getValue().toString().replace("{", "").replace("}", "");
                    }
                    return o.getKey() + " = " + result;
                }).collect(Collectors.joining("and "));
    }

    default String makeKeysList(Map<String, Object> keys) {
        return keys.entrySet().stream().filter(o -> Objects.nonNull(o.getValue()))
                .map(src -> {
                    String o = src.getKey();
                    String result = o;
                    if (o.startsWith("{") && o.endsWith("}")) {
                        result = o.replace("{", "").replace("}", "");
                    }
                    return result;
                }).collect(Collectors.joining(", "));
    }

    default String makeValuesList(Collection<Object> values) {
        return values.stream().filter(Objects::nonNull)
                .map(o -> {
                    String result = "'" + o.toString() + "'";
                    if (o instanceof String && o.toString().startsWith("{") && o.toString().endsWith("}")) {
                        result = o.toString().replace("{", "").replace("}", "");
                    }
                    return result;
                }).collect(Collectors.joining(", "));
    }

    default Map<String, Object> mapDefault(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            result.put(columnName, rs.getObject(columnName));
        }
        return result;
    }

    default <T> T mapDefault(ResultSet rs, int rowNum, EntityMapper<T> mapper) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            result.put(columnName, rs.getObject(columnName));
        }
        return mapper.map(result, rowNum);
    }

    default long mapCount(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong(1);
    }
}
