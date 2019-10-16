package com.zhaxd.core.model;

import java.util.Date;

public class DataListWD {
    private String id;
    private String tbCnname;
    private String tbEnname;
    private Date tbMaxTime;
    private Date lastUpdateTime;
    private String tbCount;
    private Integer status;
    private String updateTime;

    public DataListWD(){}

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public DataListWD(String id, String tbCnname, String tbEnname, Date tbMaxTime, Date lastUpdateTime, String tbCount, Integer status, String updateTime) {
        this.id = id;
        this.tbCnname = tbCnname;
        this.tbEnname = tbEnname;
        this.tbMaxTime = tbMaxTime;
        this.lastUpdateTime = lastUpdateTime;
        this.tbCount = tbCount;
        this.status = status;
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "DataListWD{" +
                "id='" + id + '\'' +
                ", tbCnname='" + tbCnname + '\'' +
                ", tbEnname='" + tbEnname + '\'' +
                ", tbMaxTime='" + tbMaxTime + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", tbCount='" + tbCount + '\'' +
                ", tbStatus='" + status + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTbCnname() {
        return tbCnname;
    }

    public void setTbCnname(String tbCnname) {
        this.tbCnname = tbCnname;
    }

    public String getTbEnname() {
        return tbEnname;
    }

    public void setTbEnname(String tbEnname) {
        this.tbEnname = tbEnname;
    }

    public Date getTbMaxTime() {
        return tbMaxTime;
    }

    public void setTbMaxTime(Date tbMaxTime) {
        this.tbMaxTime = tbMaxTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getTbCount() {
        return tbCount;
    }

    public void setTbCount(String tbCount) {
        this.tbCount = tbCount;
    }
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }



}
