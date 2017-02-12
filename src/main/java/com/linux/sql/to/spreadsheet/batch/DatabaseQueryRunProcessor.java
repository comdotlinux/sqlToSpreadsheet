package com.linux.sql.to.spreadsheet.batch;

import java.util.List;
import java.util.Map;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author comdotlinux
 */
public class DatabaseQueryRunProcessor implements ItemProcessor<String, List<Map<String, Object>>>{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> process(String query) throws Exception {
        return jdbcTemplate.query(query, new ColumnMapRowMapper());
    }
    
}
