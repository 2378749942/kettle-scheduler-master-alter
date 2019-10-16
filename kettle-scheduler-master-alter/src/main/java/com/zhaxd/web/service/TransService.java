package com.zhaxd.web.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.beetl.sql.core.DSTransactionManager;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.db.KeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zhaxd.common.toolkit.Constant;
import com.zhaxd.core.dto.BootTablePage;
import com.zhaxd.core.mapper.KQuartzDao;
import com.zhaxd.core.mapper.KRepositoryDao;
import com.zhaxd.core.mapper.KTransDao;
import com.zhaxd.core.mapper.KTransMonitorDao;
import com.zhaxd.core.model.KQuartz;
import com.zhaxd.core.model.KRepository;
import com.zhaxd.core.model.KTrans;
import com.zhaxd.core.model.KTransMonitor;
import com.zhaxd.web.quartz.QuartzManager;
import com.zhaxd.web.quartz.TransQuartz;
import com.zhaxd.web.quartz.model.DBConnectionModel;
import com.zhaxd.web.utils.CommonUtils;

import javax.annotation.PostConstruct;

@Service
//@Component
public class TransService {

    private SQLManager sqlManager;

    @Autowired
    private KTransDao kTransDao;

    @Autowired
    private KQuartzDao kQuartzDao;

    @Autowired
    private KRepositoryDao KRepositoryDao;

    @Autowired
    private KTransMonitorDao kTransMonitorDao;

    @Value("${kettle.log.file.path}")
    private String kettleLogFilePath;

    @Value("${kettle.file.repository}")
    private String kettleFileRepository;

    @Value("${jdbc.driver}")
    private String jdbcDriver;

    @Value("${jdbc.url}")
    private String jdbcUrl;

    @Value("${jdbc.username}")
    private String jdbcUsername;

    @Value("${jdbc.password}")
    private String jdbcPassword;

    /**
     * @param uId 用户ID
     * @return List<KTrans>
     * @Title getList
     * @Description 获取列表
     */
    public List<KTrans> getList(Integer uId) {
        KTrans template = new KTrans();
        template.setAddUser(uId);
        template.setDelFlag(1);
        return kTransDao.template(template);
    }

    /**
     * @param start 其实行数
     * @param size  获取数据的条数
     * @param uId   用户ID
     * @return BootTablePage
     * @Title getList
     * @Description 获取列表
     */
    public BootTablePage getList(Integer start, Integer size, Integer CategoryId, String transName, Integer uId) {
        KTrans template = new KTrans();
        template.setAddUser(uId);
        template.setDelFlag(1);
        if (CategoryId != null) {
            template.setCategoryId(CategoryId);
        }
        if (StringUtils.isNotEmpty(transName)) {
            template.setTransName(transName);
        }
//		List<KTrans> kTransList = kTransDao.template(template, start, size);
//		Long allCount = kTransDao.templateCount(template);
        List<KTrans> kTransList = kTransDao.pageQuery(template, start, size);
        Long allCount = kTransDao.allCount(template);
        BootTablePage bootTablePage = new BootTablePage();
        bootTablePage.setRows(kTransList);
        bootTablePage.setTotal(allCount);
        return bootTablePage;
    }

    /**
     * @param kTransId 转换ID
     * @return void
     * @Title delete
     * @Description 删除转换
     */
    public void delete(Integer kTransId) {
        KTrans kTrans = kTransDao.unique(kTransId);
        kTrans.setDelFlag(0);
        kTransDao.updateById(kTrans);
    }

    /**
     * @param repositoryId 资源库ID
     * @param kTransPath   转换路径信息
     * @param uId          用户ID
     * @return boolean
     * @Title check
     * @Description 检查转换是否添加过
     */
    public boolean check(Integer repositoryId, String kTransPath, Integer uId) {
        KTrans template = new KTrans();
        template.setDelFlag(1);
        template.setAddUser(uId);
        template.setTransRepositoryId(repositoryId);
        template.setTransPath(kTransPath);
        List<KTrans> kTransList = kTransDao.template(template);
        if (null != kTransList && kTransList.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param uId       用户ID
     * @param transFile 需要保存的转换文件
     * @return String
     * @throws IOException
     * @Title saveFile
     * @Description 保存上传的转换文件
     */
    public String saveFile(Integer uId, MultipartFile transFile) throws IOException {
        return CommonUtils.saveFile(uId, kettleFileRepository, transFile);
    }

    /**
     * @param kTrans        转换对象
     * @param uId           用户ID
     * @param customerQuarz 用户自定义的定时策略
     * @return void
     * @throws SQLException
     * @Title insert
     * @Description 添加转换到数据库
     */
    public void insert(KTrans kTrans, Integer uId, String customerQuarz) throws SQLException {
        DSTransactionManager.start();
        //补充添加作业信息
        //作业基础信息
        kTrans.setAddUser(uId);
        kTrans.setAddTime(new Date());
        kTrans.setEditUser(uId);
        kTrans.setEditTime(new Date());
        //作业是否被删除
        kTrans.setDelFlag(1);
        //作业是否启动
        kTrans.setTransStatus(2);
        if (StringUtils.isNotBlank(customerQuarz)) {
            //添加任务执行的调度策略
            KQuartz kQuartz = new KQuartz();
            kQuartz.setAddUser(uId);
            kQuartz.setAddTime(new Date());
            kQuartz.setEditUser(uId);
            kQuartz.setEditTime(new Date());
            kQuartz.setDelFlag(1);
            kQuartz.setQuartzCron(customerQuarz);
            kQuartz.setQuartzDescription(kTrans.getTransName() + "的定时策略");
            KeyHolder kQuartzKey = kQuartzDao.insertReturnKey(kQuartz);
            //插入调度策略
            kTrans.setTransQuartz(kQuartzKey.getInt());
        } else if (StringUtils.isBlank(customerQuarz) && new Integer(0).equals(kTrans.getTransQuartz())) {
            kTrans.setTransQuartz(1);
        } else if (StringUtils.isBlank(customerQuarz) && kTrans.getTransQuartz() == null) {
            kTrans.setTransQuartz(1);
        }
        kTransDao.insert(kTrans);
        DSTransactionManager.commit();
    }

    /**
     * @param transId 转换ID
     * @return KTrans
     * @Title getTrans
     * @Description 获取转换对象
     */
    public KTrans getTrans(Integer transId) {
        return kTransDao.single(transId);
    }

    /**
     * @param kTrans        转换对象
     * @param customerQuarz 用户自定义的定时策略
     * @param uId           用户ID
     * @return void
     * @Title update
     * @Description 更新转换信息
     */
    public void update(KTrans kTrans, String customerQuarz, Integer uId) {
        if (StringUtils.isNotBlank(customerQuarz)) {
            Integer transQuartzId = kTrans.getTransQuartz();
            KQuartz kQuartz = kQuartzDao.single(transQuartzId);
            if (kQuartz.getAddUser() == uId) {// 如果更新前选择的是自定义的，这一步要更新
                kQuartz.setQuartzCron(customerQuarz);
                kQuartzDao.updateTemplateById(kQuartz);
            } else {// 如果更新前选择的是默认的定时策略，这一步要新增一个定时策略
                KQuartz kQuartzTemeplate = new KQuartz();
                kQuartzTemeplate.setAddUser(uId);
                kQuartzTemeplate.setAddTime(new Date());
                kQuartzTemeplate.setEditUser(uId);
                kQuartzTemeplate.setEditTime(new Date());
                kQuartzTemeplate.setDelFlag(1);
                kQuartzTemeplate.setQuartzCron(customerQuarz);
                kQuartzTemeplate.setQuartzDescription(kTrans.getTransName() + "的定时策略");
                KeyHolder kQuartzKey = kQuartzDao.insertReturnKey(kQuartzTemeplate);
                //插入调度策略
                kTrans.setTransQuartz(kQuartzKey.getInt());
            }
        }
        kTransDao.updateTemplateById(kTrans);
    }

    /**
     * @param transId 转换ID
     * @return void
     * @Title start
     * @Description 启动转换
     */
    public void start(Integer transId) {
        // 获取到转换对象
        KTrans kTrans = kTransDao.unique(transId);
        // 获取到定时策略对象
        KQuartz kQuartz = kQuartzDao.unique(kTrans.getTransQuartz());
        // 定时策略
        String quartzCron = kQuartz.getQuartzCron();
        // 用户ID
        Integer userId = kTrans.getAddUser();
        // 获取Quartz执行的基础信息
        Map<String, String> quartzBasic = getQuartzBasic(kTrans);
        // 获取Quartz的参数
        Map<String, Object> quartzParameter = getQuartzParameter(kTrans);
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
            kTrans.setTransStatus(2);
            kTransDao.updateTemplateById(kTrans);
            return;
        }
        // 添加监控
        addMonitor(userId, transId, nextExecuteTime);
        kTrans.setTransStatus(1);
        kTransDao.updateTemplateById(kTrans);
    }

    /**
     * @param transId 转换ID
     * @return void
     * @Title stop
     * @Description 停止转换
     */
    public void stop(Integer transId) {
        // 获取到作业对象
        KTrans kTrans = kTransDao.unique(transId);
        // 用户ID
        Integer userId = kTrans.getAddUser();
        // 获取Quartz执行的基础信息
        Map<String, String> quartzBasic = getQuartzBasic(kTrans);
        // 移除任务
        if (new Integer(1).equals(kTrans.getTransQuartz())) {//如果是只执行一次
            // 一次性执行任务，不允许手动停止
            QuartzManager.removeJob(quartzBasic.get("jobName"), quartzBasic.get("jobGroupName"),
                    quartzBasic.get("triggerName"), quartzBasic.get("triggerGroupName"));
        } else {// 如果是按照策略执行
            QuartzManager.removeJob(quartzBasic.get("jobName"), quartzBasic.get("jobGroupName"),
                    quartzBasic.get("triggerName"), quartzBasic.get("triggerGroupName"));
        }
        // 移除监控
        removeMonitor(userId, transId);
        // 更新任务状态
        kTrans.setTransStatus(2);
        kTransDao.updateTemplateById(kTrans);
    }

    /**
     * @param kTrans 转换对象
     * @return Map<String, String> 任务调度的基础信息
     * @Title getQuartzBasic
     * @Description 获取任务调度的基础信息
     */
    private Map<String, String> getQuartzBasic(KTrans kTrans) {
        Integer userId = kTrans.getAddUser();
        Integer transRepositoryId = kTrans.getTransRepositoryId();
        String transPath = kTrans.getTransPath();
        Map<String, String> quartzBasic = new HashMap<String, String>();
        // 拼接Quartz的任务名称
        StringBuilder jobName = new StringBuilder();
        jobName.append(Constant.JOB_PREFIX).append(Constant.QUARTZ_SEPARATE)
                .append(transRepositoryId).append(Constant.QUARTZ_SEPARATE)
                .append(transPath);
        // 拼接Quartz的任务组名称
        StringBuilder jobGroupName = new StringBuilder();
        jobGroupName.append(Constant.JOB_GROUP_PREFIX).append(Constant.QUARTZ_SEPARATE)
                .append(userId).append(Constant.QUARTZ_SEPARATE)
                .append(transRepositoryId).append(Constant.QUARTZ_SEPARATE)
                .append(transPath);
        // 拼接Quartz的触发器名称
        String triggerName = StringUtils.replace(jobName.toString(), Constant.JOB_PREFIX, Constant.TRIGGER_PREFIX);
        // 拼接Quartz的触发器组名称
        String triggerGroupName = StringUtils.replace(jobGroupName.toString(), Constant.JOB_GROUP_PREFIX, Constant.TRIGGER_GROUP_PREFIX);
        quartzBasic.put("jobName", jobName.toString());
        quartzBasic.put("jobGroupName", jobGroupName.toString());
        quartzBasic.put("triggerName", triggerName);
        quartzBasic.put("triggerGroupName", triggerGroupName);
        return quartzBasic;
    }

    /**
     * @param kTrans 转换对象
     * @return Map<String, Object>
     * @Title getQuartzParameter
     * @Description 获取任务调度的参数
     */
    private Map<String, Object> getQuartzParameter(KTrans kTrans) {
        // Quartz执行时的参数
        Map<String, Object> parameter = new HashMap<String, Object>();
        // 资源库对象
        Integer transRepositoryId = kTrans.getTransRepositoryId();
        KRepository kRepository = null;
        if (transRepositoryId != null) {// 这里是判断是否为资源库中的转换还是文件类型的转换的关键点
            kRepository = KRepositoryDao.single(transRepositoryId);
        }
        // 资源库对象
        parameter.put(Constant.REPOSITORYOBJECT, kRepository);
        // 数据库连接对象
        parameter.put(Constant.DBCONNECTIONOBJECT, new DBConnectionModel(jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword));
        // 转换ID
        parameter.put(Constant.TRANSID, kTrans.getTransId());
        parameter.put(Constant.JOBTYPE, 2);
        String transPath = kTrans.getTransPath();
        if (transPath.contains("/")) {
            int lastIndexOf = StringUtils.lastIndexOf(transPath, "/");
            String path = transPath.substring(0, lastIndexOf);
            // 转换在资源库中的路径
            parameter.put(Constant.TRANSPATH, StringUtils.isEmpty(path) ? "/" : path);
            // 转换名称
            parameter.put(Constant.TRANSNAME, transPath.substring(lastIndexOf + 1, transPath.length()));
        }
        // 用户ID
        parameter.put(Constant.USERID, kTrans.getAddUser());
        // 转换日志等级
        parameter.put(Constant.LOGLEVEL, kTrans.getTransLogLevel());
        // 转换日志的保存位置
        parameter.put(Constant.LOGFILEPATH, kettleLogFilePath);
        return parameter;
    }

    /**
     * @param userId  用户ID
     * @param transId 转换ID
     * @return void
     * @Title addMonitor
     * @Description 添加监控
     */
    private void addMonitor(Integer userId, Integer transId, Date nextExecuteTime) {
        KTransMonitor template = new KTransMonitor();
        template.setAddUser(userId);
        template.setMonitorTrans(transId);
        KTransMonitor templateOne = kTransMonitorDao.templateOne(template);
        if (null != templateOne) {
            templateOne.setMonitorStatus(1);
            StringBuilder runStatusBuilder = new StringBuilder();
            runStatusBuilder.append(templateOne.getRunStatus())
                    .append(",").append(new Date().getTime()).append(Constant.RUNSTATUS_SEPARATE);
            templateOne.setRunStatus(runStatusBuilder.toString());
            templateOne.setNextExecuteTime(nextExecuteTime);
            kTransMonitorDao.updateTemplateById(templateOne);
        } else {
            KTransMonitor kTransMonitor = new KTransMonitor();
            kTransMonitor.setMonitorTrans(transId);
            kTransMonitor.setAddUser(userId);
            kTransMonitor.setMonitorSuccess(0);
            kTransMonitor.setMonitorFail(0);
            StringBuilder runStatusBuilder = new StringBuilder();
            runStatusBuilder.append(new Date().getTime()).append(Constant.RUNSTATUS_SEPARATE);
            kTransMonitor.setRunStatus(runStatusBuilder.toString());
            kTransMonitor.setMonitorStatus(1);
            kTransMonitor.setNextExecuteTime(nextExecuteTime);
            kTransMonitorDao.insert(kTransMonitor);
        }
    }

    /**
     * @param userId  用户ID
     * @param transId 转换ID
     * @return void
     * @Title removeMonitor
     * @Description 移除监控
     */
    private void removeMonitor(Integer userId, Integer transId) {
        KTransMonitor template = new KTransMonitor();
        template.setAddUser(userId);
        template.setMonitorTrans(transId);
        KTransMonitor templateOne = kTransMonitorDao.templateOne(template);
        templateOne.setMonitorStatus(2);
        StringBuilder runStatusBuilder = new StringBuilder();
        runStatusBuilder.append(templateOne.getRunStatus())
                .append(new Date().getTime());
        templateOne.setRunStatus(runStatusBuilder.toString());
        kTransMonitorDao.updateTemplateById(templateOne);
    }

    public void startAll(Integer CategoryId, String transName, Integer uId) {
        KTrans template = new KTrans();
        template.setAddUser(uId);
        template.setDelFlag(1);
        template.setTransStatus(2);
        if (CategoryId != null) {
            template.setCategoryId(CategoryId);
        }
        if (StringUtils.isNotEmpty(transName)) {
            template.setTransName(transName);
        }
        List<KTrans> jobList = kTransDao.queryByCondition(template);
        for (KTrans KTrans : jobList) {
            start(KTrans.getTransId());
        }
    }

    public void stopAll(Integer CategoryId, String transName, Integer uId) {
        KTrans template = new KTrans();
        template.setAddUser(uId);
        template.setDelFlag(1);
        template.setTransStatus(1);//将正在运行的停止
        if (CategoryId != null) {
            template.setCategoryId(CategoryId);
        }
        if (StringUtils.isNotEmpty(transName)) {
            template.setTransName(transName);
        }
        List<KTrans> jobList = kTransDao.queryByCondition(template);
        for (KTrans KTrans : jobList) {
            stop(KTrans.getTransId());
        }
    }

    public Long getStartTaskCount(Integer CategoryId, String transName, Integer uId) {
        KTrans template = new KTrans();
        template.setAddUser(uId);
        template.setDelFlag(1);
        template.setTransStatus(1);
        if (CategoryId != null) {
            template.setCategoryId(CategoryId);
        }
        if (StringUtils.isNotEmpty(transName)) {
            template.setTransName(transName);
        }
        Long startTaskCount = kTransDao.allCount(template);
        return startTaskCount;
    }

    public Long getStopTaskCount(Integer CategoryId, String transName, Integer uId) {
        KTrans template = new KTrans();
        template.setAddUser(uId);
        template.setDelFlag(1);
        template.setTransStatus(2);
        if (CategoryId != null) {
            template.setCategoryId(CategoryId);
        }
        if (StringUtils.isNotEmpty(transName)) {
            template.setTransName(transName);
        }
        Long stopTaskCount = kTransDao.allCount(template);
        return stopTaskCount;
    }

    public String getTransRunState(Integer transId) {
        // 获取到作业对象
        KTrans kTrans = kTransDao.unique(transId);
        // 获取Quartz执行的基础信息
        Map<String, String> quartzBasic = getQuartzBasic(kTrans);

        return QuartzManager.getTriggerState(quartzBasic.get("triggerName"), quartzBasic.get("triggerGroupName"));
    }


    /**
     * @return void
     * @Title startT
     * @Description 服务启动时自动执行的方法 ， 负责启动应该启动的转换
     */
    private void initStr(){

        if (kettleLogFilePath==null||"".equals(kettleLogFilePath)){
            //获取配置文件中的时间
            Properties props = null;
            String timing = "resource/kettle.properties";
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

            kettleLogFilePath = props.getProperty("kettle.log.file.path");
            kettleFileRepository =props.getProperty("kettle.file.repository");


            timing = "resource/db.properties";
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
            jdbcDriver =props.getProperty("jdbc.driver");
            jdbcUrl =props.getProperty("jdbc.url");
            jdbcUsername =props.getProperty("jdbc.username");
            jdbcPassword =props.getProperty("jdbc.password");
            String name = jdbcUrl.split(":")[1];
            sqlManager = SQLManager.getSQLManagerByName(name);
            kTransDao = sqlManager.getMapper(KTransDao.class);
            kTransMonitorDao = sqlManager.getMapper(KTransMonitorDao.class);
            kQuartzDao = sqlManager.getMapper(KQuartzDao.class);

        }
    }

    //    @PostConstruct
    public void startT() {
        initStr();
        Thread.yield();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            int i = 1;
            int maxI = 3;
            boolean flag = true;
            while (i <= maxI) {
                flag = true;
                synchronized (this.getClass()) {
                    try {
                        // System.out.println("+++++++++++++tttttttt++++start+++++++++++++++++");
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
                                    Thread.sleep(60);
                                    // 获取到转换对象
                                    KTrans kTrans = kTransDao.unique(transId);
                                    Thread.sleep(60);
                                    // 获取到定时策略对象
                                    KQuartz kQuartz = kQuartzDao.unique(kTrans.getTransQuartz());
                                    // 定时策略
                                    String quartzCron = kQuartz.getQuartzCron();
                                    // 用户ID
                                    Integer userId = kTrans.getAddUser();
                                    // 获取Quartz执行的基础信息
                                    Map<String, String> quartzBasic = getQuartzBasic(kTrans);
                                    // 获取Quartz的参数
                                    Map<String, Object> quartzParameter = getQuartzParameter(kTrans);
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
                                        continue;
                                    }
                                    // 添加监控
                                    addMonitor(userId, transId, nextExecuteTime);
                                    kTrans.setTransStatus(1);
                                    kTransDao.updateTemplateById(kTrans);

                                }
                            }
                        }
                        // System.out.println("+++++++++++++tttttttt++++end+++++++++++++++++");
                    } catch (Exception e) {
                        flag = false;
                        System.out.println("在第" + i + "次时运行失败");
                        // e.printStackTrace();
                    }

                }
                try {
                    Thread.yield();
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    flag = false;
                    e.printStackTrace();
                } finally {
                    System.out.println("Trans的自动执行 运行了" + i + "次");
                    if(flag){
                        return;
                    }
                    i++;
                }
            }
        }
    }

}