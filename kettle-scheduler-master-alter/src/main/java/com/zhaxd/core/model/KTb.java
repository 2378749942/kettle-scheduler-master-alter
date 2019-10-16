package com.zhaxd.core.model;

import java.util.Date;

public class KTb {

    private String tbId;
    private String tbCnname;
    private String tbEnname;
    private String tbSourceId;
    private String tbDataColumn;
    private Date tbMaxTime;
    private String tbCount;
    private Date lastUpdateTime;
    private Integer tbStatus;

    public String getTbId() {
        return tbId;
    }

    public void setTbId(String tbId) {
        this.tbId = tbId;
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

    public String getTbSourceId() {
        return tbSourceId;
    }

    public void setTbSourceId(String tbSourceId) {
        this.tbSourceId = tbSourceId;
    }

    public String getTbDataColumn() {
        return tbDataColumn;
    }

    public void setTbDataColumn(String tbDataColumn) {
        this.tbDataColumn = tbDataColumn;
    }

    public Date getTbMaxTime() {
        return tbMaxTime;
    }

    public void setTbMaxTime(Date tbMaxTime) {
        this.tbMaxTime = tbMaxTime;
    }

    public String getTbCount() {
        return tbCount;
    }

    public void setTbCount(String tbCount) {
        this.tbCount = tbCount;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getTbStatus() {
        return tbStatus;
    }

    public void setTbStatus(Integer tbStatus) {
        this.tbStatus = tbStatus;
    }

    @Override
    public String toString() {
        return "KTb{" +
                "tbId='" + tbId + '\'' +
                ", tbCnname='" + tbCnname + '\'' +
                ", tbenname='" + tbEnname + '\'' +
                ", tbSourceId='" + tbSourceId + '\'' +
                ", tbDataColumn='" + tbDataColumn + '\'' +
                ", tbMaxTime='" + tbMaxTime + '\'' +
                ", tbCount='" + tbCount + '\'' +
                ", lastUpdateTime=" + lastUpdateTime +
                ", tbStatus=" + tbStatus +
                '}';
    }
}
