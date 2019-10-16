package com.zhaxd.web.utils;

import com.zhaxd.web.service.DataListservice;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class DBConn {

    // 数据库类型
    private String DBType;
    // 数据库服务器IP
    private String host;
    // 数据库服务器端口
    private String port;
    // 数据库名称
    private String DBName;

    private String className; //驱动名
    private String url; //连接数据库的URL地址
    private String username; //数据库的用户名
    private String password; //数据库的密码
    private Connection con; //数据库连接对象
    private PreparedStatement ps; //数据库预编译处理对象
    private ResultSet rs =  null;

    public DBConn() {
        Properties props = null;
        String timing = "resource/DBConn.properties";
        do {
            try {
                props = new Properties();
                props.load(new FileInputStream(DataListservice.class.getResource("/")
                        .getPath().replace("%20", " ")
                        + timing));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println(props);
        } while (props == null);
        DBType = props.getProperty("repository.type");
        host = props.getProperty("host");
        port = props.getProperty("port");
        username = props.getProperty("database.username");
        password = props.getProperty("database.password");
        DBName = props.getProperty("database.name");
        className = createConnection(DBType);
        url = getJdbcUrl(DBType, host, port, DBName);
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            System.out.println("加载数据库驱动程序失败！");
            e.printStackTrace();
        }
    }


    public void getCon() {
        try {
            con = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.println("获取数据库连接失败！");
            e.printStackTrace();
        }
    }

    public String getJdbcUrl(String DBType, String IP, String Port, String DBName) {

        String sUrl = "";
        if (DBType.trim().toUpperCase().equals("MYSQL")) {
            sUrl = "jdbc:mysql://" + IP + ":" + Port + "/" + DBName;
        } else if (DBType.trim().toUpperCase().equals("DB2")) {
            sUrl = "jdbc:db2://" + IP + ":" + Port + "/" + DBName;
        } else if (DBType.trim().toUpperCase().equals("ORACLE")) {
            sUrl = "jdbc:oracle:thin:@" + IP + ":" + Port + ":" + DBName;
        } else if (DBType.trim().toUpperCase().equals("SQLSERVER")) {
            sUrl = "jdbc:microsoft:sqlserver://" + IP + ":" + Port + ";databaseName=" + DBName + ";selectMethod=cursor";
        } else if (DBType.trim().toUpperCase().equals("WEBLOGICPOOL")) {
            sUrl = "jdbc:weblogic:pool:" + DBName;
        } else {
            System.out.println("暂无对应数据库驱动");
        }
        return sUrl;
    }

    // 创建连接
    public String createConnection(String DBType) {

        String className = "";
        if (DBType.trim().toUpperCase().equals("MYSQL")) {
            className = "com.mysql.jdbc.Driver";
        }else if (DBType.trim().toUpperCase().equals("SQLSERVER")) {
            className = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
        }else if (DBType.trim().toUpperCase().equals("DB2")) {
            className = "com.ibm.db2.jcc.DB2Driver";
        }else if (DBType.trim().toUpperCase().equals("ORACLE")) {
            className = "oracle.jdbc.driver.OracleDriver";
        } else {
            System.out.println("未匹配到数据库类型！");
        }
        return className;
    }

    public Date getMaxTime(String sql) throws Exception{
        synchronized (sql) {
            System.out.println("sql------查最大时间------"+sql);
            Date time = null;
            String times = "";
            getCon();
            try {
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    times = rs.getString(1);
//                    System.out.println("time=" + times);
                    SimpleDateFormat sdf = null;
                    if (times.contains("/")) {
                        sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    } else if (times.contains("\\")) {
                        sdf = new SimpleDateFormat("yyyy\\MM\\dd HH:mm:ss");
                    } else if (times.contains("-")) {
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    }else{
                        System.out.println("未能识别的时间----------------"+times);
                    }
                    //System.out.println("sdf===" + sdf);
                    try {
                        time = sdf.parse(times);
                        //System.out.println("time=====" + time);
                    } catch (ParseException e) {
                        throw e;
                    }
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                closed();
            }
            return time;
        }
    }

    public Integer getCount(String sql) throws Exception{
        synchronized (sql) {
            System.out.println("sql------查表数量------"+sql);
            Integer count = null;
            getCon();
            try {
                ps = con.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    count = rs.getInt(1);
                    if (count==null)
                        count=0;
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                closed();
            }
            return count;
        }
    }

    public void closed() {
        try {
            if (ps != null)
                ps.close();
        } catch (Exception e) {
            System.out.println("关闭pstm对象失败！");
        }
        try {
            if (con != null)
                con.close();
        } catch (Exception e) {
            System.out.println("关闭con对象失败！");
        }
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }finally{
                rs = null;
            }
        }
    }


}
