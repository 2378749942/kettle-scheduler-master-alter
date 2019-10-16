package com.zhaxd.web.service;

import com.zhaxd.core.dto.BootTablePage;
import com.zhaxd.core.mapper.DataListDao;
import com.zhaxd.core.model.DataListWD;
import com.zhaxd.core.model.KTb;
import com.zhaxd.web.utils.DBConn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.util.*;

@Service
public class DataListservice {

    @Autowired
    private DataListDao dataListDao;

    public BootTablePage getList(Integer start, Integer size) {
        //获取配置文件中的时间
        Properties props = null;
        String timing = "resource/timing.properties";
        do {
            try {
                props = new Properties();
                props.load(new FileInputStream(DataListservice.class.getResource("/")
                        .getPath().replace("%20", " ")
                        + timing));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (props == null);
        String exceptionTime = props.getProperty("exception.time");
        int exTime = Integer.parseInt(exceptionTime) * (1000 * 60 * 60);
        KTb template = new KTb();
        List<KTb> kDataList = dataListDao.template(template, start, size);
        List<DataListWD> kDataListWD = new ArrayList<DataListWD>();

        for (KTb DL : kDataList) {
            if ("".equals(DL.getTbCount()) || "null".equals(DL.getTbCount()) || DL.getTbCount() == null) {
                DL.setTbCount("0");
            }
            DataListWD templateWD = null;
            Date now = new Date();
            if ("null".equals(DL.getTbMaxTime()) || "".equals(DL.getTbMaxTime()) || DL.getTbMaxTime() == null || (now.getTime() - DL.getTbMaxTime().getTime()) >= exTime) {

                templateWD = new DataListWD(DL.getTbId(), DL.getTbCnname(), DL.getTbEnname(),
                        DL.getTbMaxTime(), DL.getLastUpdateTime(), DL.getTbCount(), DL.getTbStatus(),"超过"+exceptionTime+"小时");
            } else {
                Long hh = 0L;
                if ((now.getTime()-DL.getTbMaxTime().getTime())/(1000*60*60) > 0){
                      hh = (now.getTime()-DL.getTbMaxTime().getTime())/(1000*60*60);
                }
                Long mi = 0L;
                if ((now.getTime()-DL.getTbMaxTime().getTime()-hh*1000*60*60)/(1000*60)>0){
                    mi = (now.getTime()-DL.getTbMaxTime().getTime()-hh*1000*60*60)/(1000*60);
                };
                Long ss = 0L;
                if (((now.getTime()-DL.getTbMaxTime().getTime()-hh*1000*60*60)-mi*1000*60)/(1000)>0){
                    ss=((now.getTime()-DL.getTbMaxTime().getTime()-hh*1000*60*60)-mi*1000*60)/(1000);
                }
                String updateTime = hh+"小时"+mi+"分"+ss+"秒";
                templateWD = new DataListWD(DL.getTbId(), DL.getTbCnname(), DL.getTbEnname(),
                        DL.getTbMaxTime(), DL.getLastUpdateTime(), DL.getTbCount(), DL.getTbStatus(),updateTime);
            }
            kDataListWD.add(templateWD);
        }
        long allCount = dataListDao.templateCount(template);
        BootTablePage bootTablePage = new BootTablePage();
        bootTablePage.setRows(kDataListWD);
        bootTablePage.setTotal(allCount);
        return bootTablePage;
    }

    public Integer getAllTb() {
        List<KTb> kDataList = dataListDao.template(new KTb());
        return kDataList.size();
    }

    public Integer getAllEx() {
        KTb template = new KTb();
        template.setTbStatus(2);
        List<KTb> kDataList = dataListDao.template(template);
        return kDataList.size();
    }

    public Object getAllSuccess() {
        KTb template = new KTb();
        template.setTbStatus(1);
        List<KTb> kDataList = dataListDao.template(template);
        return kDataList.size();
    }

    @PostConstruct
    public void autoRun() {

        boolean flag = false;
        try {
            //获取配置文件中的时间
            Properties props = null;
            String timing = "resource/timing.properties";
            do {
                try {
                    props = new Properties();
                    props.load(new FileInputStream(DataListservice.class.getResource("/")
                            .getPath().replace("%20", " ")
                            + timing));
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                System.out.println(props);
            } while (props == null);

            String intervalTime = props.getProperty("interval.time");
            String concurrentNumber = props.getProperty("concurrent.number");
            int ccNumber = Integer.parseInt(concurrentNumber);
            String[] strs = intervalTime.split("\\*");
            int timed = 1;
            for (String str : strs
            ) {
                int i = Integer.parseInt(str);
                timed *= i;
            }
//            System.out.println(timed);
//        int timed = Integer.parseInt(intervalTime);

//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, );
//        calendar.set(Calendar.MINUTE, 26);
//        calendar.set(Calendar.SECOND, 0);
//
//        Date time = calendar.getTime();
            //select max(RKSJ) from T_ZA_CZRK

            //创建对象
            Timer timer = new Timer();

            //启动
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    KTb template = new KTb();
                    Long size = dataListDao.templateCount(template);
                    int start = 0;
                    while ((start + ccNumber) <= size) {
                        List<KTb> kDataList = dataListDao.template(template, start, ccNumber);
                        for (KTb DL : kDataList) {
//                            System.out.println(DL);
                            DBConn dbconn = new DBConn();
                            String tbName = DL.getTbEnname();
                            String column = DL.getTbDataColumn();
                            String timeS = "select max(" + column + ") from " + tbName;
                            String countS = "select count(*) from " + tbName;
                            try {
                                Date maxTime = dbconn.getMaxTime(timeS);
                                if (maxTime != null && maxTime.getTime() > 1) {
                                    DL.setTbMaxTime(maxTime);
                                }
                                DL.setTbCount("" + dbconn.getCount(countS));
                                System.out.println("timeS------dbconn.getMaxTime(timeS)--------" + dbconn.getMaxTime(timeS));
                                System.out.println("countS-----dbconn.getCount(countS)--------" + dbconn.getCount(countS));
                                DL.setTbStatus(1);
                            }catch (Exception e){
                                System.out.println("查询出错");
                                System.out.println("查询出错");
                                System.out.println("查询出错");
                                DL.setTbStatus(2);
                                e.printStackTrace();
                            }
                            System.out.println("-------------****"+new Date()+"***--------------");
                            //将更新时间写入表中
                            DL.setLastUpdateTime(new Date());
                            //更新表数据
                            dataListDao.updateById(DL);
                        }
                        start += ccNumber;
                    }
                }
            }, new Date(), timed);// 这里设定将延时每天固定执行
            flag = true;
        } finally {
            if (flag) {
                System.out.println("-------------*******--------------");
                System.out.println(new Date());
                System.out.println("-------------启动成功-------------");
                System.out.println("-------------*******--------------");
            } else {
                System.out.println("-------------*******--------------");
                System.out.println(new Date());
                System.out.println("--------启动失败，请稍后重试--------");
                System.out.println("-------------*******--------------");
            }
        }

    }
}
