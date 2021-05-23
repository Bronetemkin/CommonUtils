package ru.hnm1nd.base.connection.db;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.hnm1nd.base.connection.DataAccessAPI;
import ru.hnm1nd.base.utils.EntityMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseDBAccessAPI implements DataAccessAPI {

    private JdbcTemplate jdbcTemplate;

    public BaseDBAccessAPI(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> get(String objectType, Map<String, Object> conditions, String... selectableFields) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ")
                .append(selectableFields != null && selectableFields.length > 0 ? String.join(", ", selectableFields) : "*")
                .append(" FROM ")
                .append(objectType);
        if(conditions.size() > 0) {
                builder.append(" WHERE ")
                    .append(makeParamList(conditions));
        }
        return jdbcTemplate.query(builder.toString(), this::mapDefault);
    }

    @Override
    public <T> List<T> get(String objectType, Map<String, Object> conditions, EntityMapper<T> mapper, String... selectableFields) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ")
                .append(selectableFields != null && selectableFields.length > 0 ? String.join(", ", selectableFields) : "*")
                .append(" FROM ")
                .append(objectType);
        if(conditions.size() > 0) {
            builder.append(" WHERE ")
                    .append(makeParamList(conditions));
        }
        return jdbcTemplate.query(builder.toString(), (o, o1)-> mapDefault(o, o1, mapper));
    }

    @Override
    public int insert(String objectType, Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ")
                .append(objectType)
                .append("(")
                .append(makeKeysList(data))
                .append(")")
                .append(" VALUES ")
                .append("(")
                .append(makeValuesList(data.values()))
                .append(")");
        jdbcTemplate.execute(builder.toString());
        return 0;
    }

    @Override
    public int update(String objectType, Map<String, Object> data, Map<String, Object> conditions) {
        StringBuilder builder = new StringBuilder();
        Map<String, Object> nonCollisionalData = removeCollisionsFromData(data, conditions);
        builder.append("UPDATE ")
                .append(objectType)
                .append(" SET ")
                .append(makeParamList0(nonCollisionalData))
                .append(" WHERE ")
                .append(makeParamList(conditions));
        jdbcTemplate.execute(builder.toString());
        return 0;
    }

    @Override
    public int remove(String objectType, Map<String, Object> conditions) {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ")
                .append(objectType)
                .append(" WHERE ")
                .append(makeParamList(conditions));
        jdbcTemplate.execute(builder.toString());
        return 0;
    }

    @Override
    public long count(String objectType, Map<String, Object> conditions) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT COUNT(*) FROM ")
                .append(objectType);
        if(conditions.size() > 0) {
            builder.append(" WHERE ")
                    .append(makeParamList(conditions));
        }
        return jdbcTemplate.query(builder.toString(), this::mapCount).get(0);
    }

    @Override
    public Object getDriver() {
        return jdbcTemplate;
    }

    private Map<String, Object> removeCollisionsFromData(Map<String, Object> data, Map<String, Object> conditions) {
        return data.entrySet().stream().filter(e->!(conditions.containsKey(e.getKey()) && conditions.get(e.getKey()).equals(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
