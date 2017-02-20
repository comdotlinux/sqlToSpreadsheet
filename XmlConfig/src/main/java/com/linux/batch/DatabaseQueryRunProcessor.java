package com.linux.batch;

import java.util.List;
import java.util.Map;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author comdotlinux
 */
public class DatabaseQueryRunProcessor implements ItemProcessor<QueryData, QueryResult>{

    private JdbcTemplate jdbcTemplate;

    public QueryResult process(QueryData qd) throws Exception {
        
        List<Map<String, Object>> tableData = jdbcTemplate.query(qd.getSqlquery(), new ColumnMapRowMapper());
        return new QueryResult(tableData, qd.getOutputfilename());
    }
    
}
