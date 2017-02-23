/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.linux.batch;

import java.util.List;
import java.util.Map;

/**
 *
 * @author comdotlinux
 */
public class QueryResult {
    
    private List<Map<String, Object>> tableData;
    private String outputsheetname;

    public QueryResult(List<Map<String, Object>> tableData, String outputsheetname) {
        this.tableData = tableData;
        this.outputsheetname = outputsheetname;
    }

    public QueryResult() {
    }

    public List<Map<String, Object>> getTableData() {
        return tableData;
    }

    public void setTableData(List<Map<String, Object>> tableData) {
        this.tableData = tableData;
    }

    public String getOutputsheetname() {
        return outputsheetname;
    }

    public void setOutputsheetname(String outputsheetname) {
        this.outputsheetname = outputsheetname;
    }

}
