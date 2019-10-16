package com.zhaxd.web.service;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServlet;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

//@Component("processor")
public class TransAndJobInitService extends HttpServlet {

    private static final long serialVersionUID = -9045451275234606838L;

    @Override
    public void init() {
        start();
        System.out.println("启动结束"+new Date());
//        FutureTask<String> task = new FutureTask<String>(new Callable<String>(){
//
//            @Override
//            public String call() throws Exception {
//               // start(); // 使用另一个线程来执行该方法，会避免占用Tomcat的启动时间
//                return "Collection ---------------------------------" +
//                        "" +
//                        "" +
//                        "" +
//                        "========================================Completed";
//            }
//        });
//
//        new Thread(task).start();
    }


    private void start(){
        //initStr();
        new TransService().startT();
        new JobService().startJ();
    }

/*

    public void startJ() {
        Thread.yield();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            int i = 1;
            int maxI = 9;
            boolean flag = true;
            while (i <= maxI) {
                flag = true;
                synchronized (this.getClass()) {
                    try {
                        System.out.println("+++++++++++++jjjjjjjj++++start+++++++++++++++++");
                        //创建转换M对象 并 指定
                        KJobMonitor template = new KJobMonitor();
                        template.setMonitorStatus(1);
                        Thread.sleep(60);
                        //根据指定查询数据
                        List<KJobMonitor> kJobMonitorList = kJobMonitorDao.template(template);
                        //遍历数据，然后判断下次执行时间是否在当前时间之前，在当前时间之前启动转换
                        for (KJobMonitor kt : kJobMonitorList) {
                            //判断当前对象的时间是否为空
                            Date when = kt.getNextExecuteTime();
                            if (when != null) {
                                //创建当前时间对象
                                Date now = new Date();
                                //判断下次执行时间是否在当前时间之前，在当前时间之前启动转换
                                if (when.before(now)) {
                                    //启动转换
                                    Integer JobId = kt.getMonitorJob();
                                    Thread.sleep(60);
                                    // 获取到转换对象
                                    KJob kJob = kJobDao.unique(JobId);
                                    Thread.sleep(60);
                                    // 获取到定时策略对象
                                    KQuartz kQuartz = kQuartzDao.unique(kJob.getJobQuartz());
                                    // 定时策略
                                    String quartzCron = kQuartz.getQuartzCron();
                                    // 用户ID
                                    Integer userId = kJob.getAddUser();
                                    // 获取Quartz执行的基础信息
                                    Map<String, String> quartzBasic = getQuartzBasic(kJob);
                                    // 获取Quartz的参数
                                    Map<String, Object> quartzParameter = getQuartzParameter(kJob);
                                    Date nextExecuteTime = null;
                                    // 添加任务
                                    // 判断转换执行类型
                                    try {
                                        if (new Integer(1).equals(kJob.getJobQuartz())) {//如果是只执行一次
                                            nextExecuteTime = QuartzManager.addOnceJob(quartzBasic.get("jobName"), quartzBasic.get("jobGroupName"),
                                                    quartzBasic.get("triggerName"), quartzBasic.get("triggerGroupName"),
                                                    JobQuartz.class, quartzParameter);
                                        } else {// 如果是按照策略执行
                                            //添加任务
                                            nextExecuteTime = QuartzManager.addJob(quartzBasic.get("jobName"), quartzBasic.get("jobGroupName"),
                                                    quartzBasic.get("triggerName"), quartzBasic.get("triggerGroupName"),
                                                    JobQuartz.class, quartzCron, quartzParameter);
                                        }
                                    } catch (Exception e) {
                                        System.out.println("在第" + i + "次时运行失败");
                                        if (i >= maxI) {
                                            kJob.setJobStatus(2);
                                            kJobDao.updateTemplateById(kJob);
                                        }
                                        flag = false;
                                        continue;
                                    }
                                    // 添加监控
                                    new JobService().addMonitor(userId, JobId, nextExecuteTime);
                                    kJob.setJobStatus(1);
                                    kJobDao.updateTemplateById(kJob);

                                }
                            }
                        }
                        // System.out.println("+++++++++++++jjjjjjjj++++end+++++++++++++++++");
                    } catch (Exception e) {
                        flag = false;
                        System.out.println("在第" + i + "次时运行失败");
                        // e.printStackTrace();
                    }
                }
                try {
                    Thread.yield();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    flag = false;
                    e.printStackTrace();
                } finally {
                    System.out.println("jjj运行了" + i + "次");
                    if(flag){
                        return;
                    }
                    i++;
                }
            }
        }
    }

    private void startT() {

        System.out.println("当前时间是"+ new Date());
        Thread.yield();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            int i = 1;
            int maxI = 9;
            boolean flag = true;
            while (i <= maxI) {
                flag = true;
                synchronized (this.getClass()) {
                    try {
                        System.out.println("+++++++++++++tttttttt++++start+++++++++++++++++");
                        //创建转换M对象 并 指定
                        KTransMonitor template = new KTransMonitor();
                        template.setMonitorStatus(1);
                        Thread.sleep(60);
                        //根据指定查询数据
                        List<KTransMonitor> kTransMonitorList = kTransMonitorDao.template(template);
                        //遍历数据，然后判断下次执行时间是否在当前时间之前，在当前时间之前启动转换
                        for (KTransMonitor kt : kTransMonitorList) {
                            //判断当前对象的时间是否为空
                            Date when = kt.getNextExecuteTime();
                            if (when != null) {
                                //创建当前时间对象
                                Date now = new Date();
                                //判断下次执行时间是否在当前时间之前，在当前时间之前启动转换
                                if (when.before(now)) {
                                    //启动转换
                                    Integer transId = kt.getMonitorTrans();
                                    // 获取到转换对象
                                    KTrans kTrans = kTransDao.unique(transId);
                                    // 获取到定时策略对象
                                    KQuartz kQuartz = kQuartzDao.unique(kTrans.getTransQuartz());
                                    // 定时策略
                                    String quartzCron = kQuartz.getQuartzCron();
                                    // 用户ID
                                    Integer userId = kTrans.getAddUser();
                                    // 获取Quartz执行的基础信息
                                    Map<String, String> quartzBasic = new TransService().getQuartzBasic(kTrans);
                                    // 获取Quartz的参数
                                    Map<String, Object> quartzParameter = new TransService().getQuartzParameter(kTrans);
                                    Date nextExecuteTime = null;
                                    // 添加任务
                                    // 判断转换执行类型
                                    try {
                                        if (new Integer(1).equals(kTrans.getTransQuartz())) {//如果是只执行一次
                                            nextExecuteTime = QuartzManager.addOnceJob(quartzBasic.get("jobName"), quartzBasic.get("jobGroupName"),
                                                    quartzBasic.get("triggerName"), quartzBasic.get("triggerGroupName"),
                                                    TransQuartz.class, quartzParameter);
                                        } else {// 如果是按照策略执行
                                            //添加任务
                                            nextExecuteTime = QuartzManager.addJob(quartzBasic.get("jobName"), quartzBasic.get("jobGroupName"),
                                                    quartzBasic.get("triggerName"), quartzBasic.get("triggerGroupName"),
                                                    TransQuartz.class, quartzCron, quartzParameter);
                                        }
                                    } catch (Exception e) {
                                        System.out.println("在第" + i + "次时运行失败");
                                        if (i >= maxI) {
                                            kTrans.setTransStatus(2);
                                            kTransDao.updateTemplateById(kTrans);
                                        }
                                        flag = false;
                                        e.printStackTrace();
                                        continue;
                                    }
                                    // 添加监控
                                    new TransService().addMonitor(userId, transId, nextExecuteTime);
                                    kTrans.setTransStatus(1);
                                    kTransDao.updateTemplateById(kTrans);

                                }
                            }
                        }
                        // System.out.println("+++++++++++++tttttttt++++end+++++++++++++++++");
                    } catch (Exception e) {
                        flag = false;
                        System.out.println("在第" + i + "次时运行失败");
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.yield();
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    flag = false;
                    e.printStackTrace();
                } finally {
                    System.out.println("当前时间是"+ new Date());
                    System.out.println("ttt运行了" + i + "次");
                    if(flag){
                        return;
                    }
                    i++;
                }
            }
        }
    }

*/

/*

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("------" + "over" +
                "------");  System.out.println("------" + "over" +
                "------");  System.out.println("------" + "over" +
                "------");  System.out.println("------" + "over" +
                "------");  System.out.println("------" + "over" +
                "------");  System.out.println("------" + "over" +
                "------");  System.out.println("------" + "over" +
                "------");  System.out.println("------" + "over" +
                "------");  System.out.println("------" + "over" +
                "------");
    }
*/

}
