package ru.hnm1nd.base.utils;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataAccessUtils {

    public static <T> T getSingleField(List<Map<String, T>> dataList, int index, String fieldName) {
        if(dataList != null && !dataList.isEmpty() && dataList.get(index).containsKey(fieldName))
            return dataList.get(index).get(fieldName);
        else return null;
    }

    public static JdbcTemplate initialize(JdbcTemplate template) {
        try {
            if(!checkIfDataExist(template)) {
                initSchema(template);
            }
        } catch (SQLException | IOException t) {
            t.printStackTrace();
        }
        return template;
    }

    public static boolean checkIfDataExist(JdbcTemplate template) throws SQLException {
        return template.queryForList("SELECT NAME FROM sqlite_master WHERE TYPE='table' AND NAME='SETTINGS'").size() > 0;
    }

    private static void initSchema(JdbcTemplate template) throws IOException, SQLException {
        String sqlQuery = read(DataAccessUtils.class.getResourceAsStream("/database/init/dbinit.sql")).trim();
        String[] queries = sqlQuery.split(";");
        for (String query : queries) {
            template.batchUpdate(query);
        }
    }

    private static String read(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().parallel().collect(Collectors.joining("\n"));
    }

}
