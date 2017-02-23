package com.linux.batch;

/**
 * Holds the sql query and the output sheet name for the same.
 * @author comdotlinux
 */
public class QueryData {

    private String outputsheetname;
    private String sqlquery;

    public String getSqlquery() {
        return sqlquery;
    }

    public void setSqlquery(String sqlquery) {
        this.sqlquery = sqlquery;
    }

    public String getOutputsheetname() {
        return outputsheetname;
    }

    public void setOutputsheetname(String outputsheetname) {
        this.outputsheetname = outputsheetname;
    }

}
