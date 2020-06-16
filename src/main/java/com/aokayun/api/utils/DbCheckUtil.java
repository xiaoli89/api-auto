package com.aokayun.api.utils;

import com.aokayun.api.beans.ApiDataBean;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
对于一些对数据库有增删改操作的接口，需要做数据库校验
 */
public class DbCheckUtil {
    private static final String USERNAME = "";
    private static final String PASSWORD = "";
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "";
    public static HashMap<String, Object> getCheckString() {
        List<ApiDataBean> apiDataBeans = new ArrayList<>();
        ApiDataBean dataBean = apiDataBeans.get(12);
        String check_sql = dataBean.getCheck_sql();
        HashMap<String,Object> map = new HashMap<>();
        if (check_sql!=null && check_sql.length()>0){
            Connection connection = null;
            Statement statement = null;

            try{
                Class.forName(JDBC_DRIVER);
                connection = DriverManager.getConnection(USERNAME,PASSWORD,DB_URL);
                statement = connection.createStatement();
                ResultSet set = statement.executeQuery(check_sql);
                ResultSetMetaData metaData = set.getMetaData();
                while (set.next()){
                    for (int i = 0; i <metaData.getColumnCount() ; i++) {
                        String columnLabel = metaData.getColumnLabel(i+1);
                        Object columnValue = set.getObject(columnLabel);
                        map.put(columnLabel, columnValue);
                    }
                }

                statement.close();
                set.close();
                connection.close();

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return map;

    }
}
