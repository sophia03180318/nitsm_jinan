package com.jcca.web.ibmMQ.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.ibmMQ.command.impl.InquireQMgrCommand;
import com.jcca.web.ibmMQ.common.ErrorConstants;
import com.jcca.web.ibmMQ.common.MetadataAccessNotAllowedException;
import com.jcca.web.ibmMQ.domain.Connection;
import com.jcca.web.ibmMQ.domain.Monitor;
import com.jcca.web.ibmMQ.domain.StatisticalData;
import com.jcca.web.ibmMQ.domain.vo.MonitorVo;
import com.jcca.web.ibmMQ.domain.vo.ReqMonitorVo;
import com.jcca.web.ibmMQ.domain.vo.WarningVo;
import com.jcca.web.ibmMQ.entity.*;
import com.jcca.web.ibmMQ.health.HealthRuleException;
import com.jcca.web.ibmMQ.schedule.impl.MonitorScheduler;
import com.jcca.web.ibmMQ.service.*;
import com.jcca.web.ibmMQ.service.impl.MetadataService;
import com.jcca.web.ibmMQ.util.Strings;
import com.jcca.web.ibmMQ.vo.MQObject;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/23 10:07
 */
@RestController
@RequestMapping("/api/mqMonitor")
@Slf4j
@ApiOperation(value = "新建业务组")
public class MonitorController {
    @Resource
    private MonitorService monitorService;
    @Resource
    private StatisticalDataService statisticalDataManager;
    @Resource
    ConnectionService connectionService;
    @Resource
    MetadataService metadataService;
    @Resource
    private PCFMessageService pcfMessageService;
    @Resource
    private ChannelDataService channelDataService;
    @Resource
    private ListenerDataService listenerDataService;
    @Resource
    private QueueDataService queueDataService;
    @Resource
    private TopicDataService topicDataService;
    @Resource
    private IBMMonitorService ibmMonitorService;
    @Resource
    private IBMGroupService ibmGroupService;

    @PostMapping("/createGroup")
    @ApiOperation(value = "新建业务组")
    @ActionLog(name = "新建业务组", title = "中间件", key = LogTypeConstant.ADD)
    public ResultVo createGroup(@Valid @RequestBody IBMGroup ibmGroup) {
        if (ObjectUtil.isNull(ibmGroup.getName())) {
            return ResultVoUtil.warning("业务名称不可为空");
        }
        String id = ibmGroupService.selectIdByName(ibmGroup.getName(), ibmGroup.getConnectId());
        if (id.length() > 0 && !ibmGroup.getId().equals(id)) {
            return ResultVoUtil.warning("业务名称重复,请更换");
        }
        if (ObjectUtil.isNull(ibmGroup.getId())) {
            ibmGroup.setId(MyIdUtil.getId());
        }

        ibmGroupService.saveOrUpdate(ibmGroup);
        return ResultVoUtil.success();
    }

    @PostMapping("/getGroups/{connectId}")
    @ApiOperation(value = "获取所有业务组")
    public ResultVo getGroups(@PathVariable("connectId") String connectId) {
        QueryWrapper<IBMGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("CONNECT_ID", connectId);
        return ResultVoUtil.success(ibmGroupService.list(wrapper));
    }


    @PostMapping("/removeGroup/{id}")
    @ApiOperation(value = "移除业务组")
    @ActionLog(name = "移除业务组", title = "中间件", key = LogTypeConstant.REMOVEE)
    public ResultVo removeGroup(@PathVariable("id") String groupId) {
        if (ibmMonitorService.getMonitorById(groupId).size() > 0) {
            return ResultVoUtil.warning("请先删除业务组内监视器");
        }
        ibmGroupService.removeById(groupId);
        return ResultVoUtil.success("删除成功");
    }

    @PostMapping("/getQueueDetail/{id}")
    @ApiOperation(value = "查询监视器属性详情")
    public ResultVo getQueueDetail(@PathVariable("id") String id) {
        IBMMonitor monitor = ibmMonitorService.getById(id);
        if (ObjectUtil.isNotNull(monitor)) {
            return ResultVoUtil.success(monitor);
        }
        return ResultVoUtil.success();
    }

    @PostMapping("/setWarn")
    @ApiOperation(value = "设置队列深度阈值")
    @ActionLog(name = "设置队列尝试阈值", title = "中间件", key = LogTypeConstant.MODIFY)
    public ResultVo setWarn(@RequestBody WarningVo warningVo) {
        IBMMonitor monitor = monitorService.getById(warningVo.getId());
        IBMConnection connection = connectionService.getById(monitor.getConnectionId());
        IBMMonitor ibmMonitor = monitorService.findMonitor(connection.getConnectName(), Monitor.MonitorNameType.QMGR.getValue());
        Monitor connectMonitor = Monitor.getMonitor(ibmMonitor);
        StatisticalData data = this.statisticalDataManager.queryMonitorDetails(connectMonitor);
        if (ObjectUtil.isNull(data.getHealthState()) || !data.getHealthState().getValue().equals("OK")) {
            return ResultVoUtil.warning("该连接状态异常,采集中断.暂不能设置阈值~");
        }

        if (ObjectUtil.isNotNull(warningVo.getCurrentQDepth()) && warningVo.getCurrentQDepth().length() > 0) {
            String rule = "error(currentQDepth>" + warningVo.getCurrentQDepth() + ")";
            try {
                monitorService.validateWarn(rule);
            } catch (HealthRuleException e) {
                return ResultVoUtil.warning("阈值设置不符合规则");
            }
            monitor.setHealthRule(rule);
            monitorService.updateWarn(monitor.getId(), rule);
        } else {
            monitorService.cleanWarn(monitor.getId());
            monitor.setHealthRule(null);
        }

        //重启 monitor监控任务

        monitor.setHost(connection.getConnectHost());
        monitor.setPort(connection.getConnectPort());
        monitor.setConnectionName(connection.getConnectName());
        monitor.setConnectionId(connection.getId());
        monitor.setConnectionDescription(connection.getDescription());
        monitor.setChannelName(connection.getChannelName());
        monitor.setUserId(connection.getUserId());
        Monitor monitor2 = Monitor.getMonitor(monitor);

        monitorService.restartTask(monitor2);


        return ResultVoUtil.success("阈值配置成功");
    }

    @PostMapping("/getWarn/{id}")
    @ApiOperation(value = "获取队列深度阈值")
    public ResultVo getWarn(@PathVariable("id") String id) {
        IBMMonitor monitor = monitorService.getById(id);
        String r = "";
        if (ObjectUtil.isNotNull(monitor.getHealthRule()) && monitor.getHealthRule().length() > 0) {
            String rule = monitor.getHealthRule();//
            r = rule.substring(0, rule.length() - 1).split(">")[1];
        }
        ResultVo<Object> success = ResultVoUtil.success("");
        success.setData(r);
        return success;
    }

    @PostMapping("/createMonitor")
    @ApiOperation(value = "创建监视器")
    @ActionLog(name = "创建监视器", title = "中间件", key = LogTypeConstant.ADD)
    public ResultVo createMonitor(@RequestBody MonitorVo monitorVo) {

        Monitor monitor = new Monitor();
        String connectionName = monitorVo.getConnectionName();//远程队列管理器名称
        monitor.setHealthRule(monitorVo.getHealthRule());

        monitor.setGroupId(monitorVo.getGroupId());
        monitor.setName(monitorVo.getObjectName());//监视器名称
        monitor.setCategory(monitorVo.getCategory());
        monitor.setObjectType(monitorVo.getObjectType());//类型
        monitor.setUsage(monitorVo.getUsage());//本地队列还是传输队列 
        monitor.setObjectName(monitorVo.getObjectName());//对象名称
        monitor.setDescription(monitorVo.getDescription());//备注
        monitor.setPollingInterval("5m");//刷新时长
        monitor.setDataExpirationTime("1h");
        if (monitor.getObjectType().equals("Local")) {
            monitor.addMeasurement("currentQDepth");
            monitor.addMeasurement("openInputCount");
            monitor.addMeasurement("openOutputCount");
            monitor.addMeasurement("maxQDepth");
            monitor.addMeasurement("occupiedPercent");
        } else if (monitor.getObjectType().equals("Remote")) {
            monitor.addMeasurement("occupiedPercent");
        }

        if (monitor.getCategory().getValue().equals("Channel")) {
            monitor.addMeasurement("bytesReceived");
            monitor.addMeasurement("bytesSent");
            monitor.addMeasurement("channelStatus");
        }
        monitor.setViewType(Monitor.ViewType.BarChart);//队列视图方式
        monitor.setScope(Monitor.Scope.NORMAL);
        monitor.setState(Monitor.State.Active);


        if (log.isDebugEnabled()) {
            log.debug("Creating monitor '{}' in connection '{}'", monitor, connectionName);
        }
        if (monitor.isMQTT()) {
            return ResultVoUtil.warning("不允许MQTT监视器访问!");
        }
        Connection connection = this.connectionService.findConnection(connectionName);
        monitor.setConnection(connection);
/*        if (monitorService.existsMonitor(monitor.getConnection().getName(), monitor.getName())) {
            return ResultVoUtil.warning("录入的名称已经存在，请填写其他名称");
        }*/
        try {
            this.monitorService.createMonitor(monitor);
        } catch (Exception e) {
            return ResultVoUtil.warning(e.getMessage());
        }

        return ResultVoUtil.CREATE_SUCCESS;
    }

    @PostMapping("/connects")
    @ApiOperation(value = "获取连接")
    @RequiresPermissions({"api:mqMonitor:connects"})
    public ResultVo queryConnects() {
        return ResultVoUtil.success(connectionService.listConnections());
    }

    /**
     * 查询监控队列状态
     *
     * @param
     * @return
     */
    @PostMapping("/monitorDetails")
    @ApiOperation(value = "获取队列管理器状态详情")
    public ResultVo queryMonitorDetails(@RequestBody ReqMonitorVo reqMonitorVo) {
        IBMMonitor ibmMonitor = monitorService.findMonitor(reqMonitorVo.getConnectionName(), Monitor.MonitorNameType.QMGR.getValue());
        Monitor monitor = Monitor.getMonitor(ibmMonitor);
        StatisticalData data = this.statisticalDataManager.queryMonitorDetails(monitor);
        return ResultVoUtil.success(data);
    }

    /**
     * 创建连接
     *
     * @param connection
     * @return
     */
    @PostMapping("/createConnection")
    @ApiOperation(value = "创建连接")
    @RequiresPermissions({"api:mqMonitor:createConnection"})
    @ActionLog(name = "创建连接", title = "中间件", key = LogTypeConstant.ADD)
    public ResultVo createConnection(@Validated @RequestBody Connection connection) {
        if (connection.getPort() < 0 || connection.getPort() > 65535) {
            return ResultVoUtil.warning("端口范围为:[1-65535]!");
        }
        //给予连接通道默认值
        if (Strings.isNullOrEmpty(connection.getChannelName())) {
            connection.setChannelName("SYSTEM.DEF.SVRCONN");
        }


        List<IBMConnection> list = connectionService.findByHostAndPort(connection.getHost(), connection.getPort());
        if (list != null && list.size() > 0) {
            return ResultVoUtil.warning("新建的队列连接与" + list.get(0).getConnectName() + "重复,无法添加！");
        } else if (pcfMessageService.existsConnection(connection.getName())) {
            return ResultVoUtil.warning("对象已经存在，无法重复录入");
        } else {
            try {
                this.connectionService.createConnection(connection);
            } catch (Exception e) {
                /*连接 'Connection [id=null,name=队列管理器,channelName=SYSTEM.DEF.SVRCONN,host=192.168.51.18,port=1414,userId=mqm,description=null]' 不可达，错误码： 2538(MQRC_HOST_NOT_AVAILABLE)!*/
                if (e.getMessage().contains("MQRC_HOST_NOT_AVAILABLE")) {
                    return ResultVoUtil.warning("无法连接,请校验您的连接信息");
                }
                /*用户 'Connection [id=null,name=队列B号,channelName=SYSTEM.DEF.SVRCONN,host=192.168.51.140,port=40001,userId=mq,description=null]' 没有授权，错误码： 2035(MQRC_NOT_AUTHORIZED)!*/
                else if (e.getMessage().contains("MQRC_NOT_AUTHORIZED")) {
                    return ResultVoUtil.warning("此用户名无授权,请校验您的用户名");
                }

                return ResultVoUtil.warning(e.getMessage());
            }

            return ResultVoUtil.CREATE_SUCCESS;
        }

    }


    @PostMapping("/removeConnection")
    @ApiOperation(value = "删除连接")
    @RequiresPermissions({"api:mqMonitor:removeConnection"})
    @ActionLog(name = "删除连接", title = "中间件", key = LogTypeConstant.REMOVEE)
    public ResultVo removeConnection(@RequestBody ReqMonitorVo reqMonitorVo) {
        QueryWrapper<IBMGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("CONNECT_ID", reqMonitorVo.getConnectionId());
        List<IBMGroup> list = ibmGroupService.list(queryWrapper);
        if (!list.isEmpty()) {
            return ResultVoUtil.warning("请先清空队列管理器!");
        }

        if (log.isDebugEnabled()) {
            log.debug("Removing conneciton '{}'", reqMonitorVo.getConnectionName());
        }

        this.connectionService.removeConnection(reqMonitorVo.getConnectionId(), reqMonitorVo.getConnectionName());
        //如果连接不同，删除连接变化缓存
        InquireQMgrCommand.change.remove(reqMonitorVo.getConnectionId());
        if (log.isDebugEnabled()) {
            log.debug("Removed conneciton '{}'", reqMonitorVo.getConnectionName());
        }


        return ResultVoUtil.REMOVE_SUCCESS;
    }


    @PostMapping("/removeMonitor")
    @ApiOperation(value = "删除监控项")
    @RequiresPermissions({"api:mqMonitor:removeMonitor"})
    @ActionLog(name = "删除监控项", title = "中间件", key = LogTypeConstant.REMOVEE)
    public ResultVo removeMonitor(@RequestBody ReqMonitorVo reqMonitorVo) {
        IBMMonitor ibmMonitor = this.monitorService.findMonitor(reqMonitorVo.getConnectionName(), reqMonitorVo.getMonitorName());
        Monitor monitor = Monitor.getMonitor(ibmMonitor);
        this.tryForbidMQTTMonitorAccess(monitor);

        //删除此队列已产生的事件
        String objectName = monitor.getObjectName();
        this.monitorService.removeEvent(objectName);

        this.monitorService.removeMonitor(monitor);
        removeRelateInfo(monitor.getId(), monitor.getCategory().getValue());
        if (log.isDebugEnabled()) {
            log.debug("Removed monitor '{}' in connection '{}'", reqMonitorVo.getMonitorName(), reqMonitorVo.getConnectionName());
        }


        return ResultVoUtil.REMOVE_SUCCESS;
    }

    /**
     * 获取监控种类
     *
     * @return
     */
    @GetMapping("/categories")
    @ApiOperation(value = "获取监控类别")
    public ResultVo findCategories() {
        return ResultVoUtil.success(this.metadataService.queryCategories());
    }

    @GetMapping("/categoryTypes")
    @ApiOperation(value = "获取监视器类型")
    public ResultVo findObjectTypes(String category) {
        return ResultVoUtil.success(this.metadataService.queryObjectTypes(category));
    }

    @GetMapping("/typeMeasurements")
    @ApiOperation(value = "获取监视器属性")
    public ResultVo listMeasurements(String category, String objectType) {
        if (log.isDebugEnabled()) {
            log.debug("Query all measurements for category '{}' with object type '{}'", category, objectType);
        }

        List<String> measurements = this.metadataService.queryMeasurements(category, objectType);
        if (log.isDebugEnabled()) {
            log.debug("Found {} measurements {} for category '{}' with object type '{}'", measurements.size(), Arrays.toString(measurements.toArray()), category, objectType);
        }

        return ResultVoUtil.success(measurements);
    }


    @GetMapping("/connectionCategroyMonitorInfo")
    @ApiOperation(value = "获取监控对象")
    public ResultVo listAllChannels(String connectionName, String name, String type, String category) {
        ResultVo result = ResultVoUtil.warning("未获取相关监控对象信息");
        try {
            List<MQObject> mqObjects = null;
            switch (category) {
                case "QueueManager":
                    break;
                case "Channel":
                    mqObjects = listAllChannels(connectionName, name, type);
                    break;
                case "Queue":
                    mqObjects = listAllQueues(connectionName, name, type);

                    break;
                case "Topic":
                    mqObjects = listAllTopics(connectionName, name, type);
                    break;
                case "Listener":
                    mqObjects = listAllListeners(connectionName, name);
                    break;
            }
            if (mqObjects != null) {
                return ResultVoUtil.success(mqObjects);
            } else {
                return result;
            }
        } catch (Exception e) {
            log.error("MQ监控获取监控对象异常", e);
            return result;
        }
    }

    @PostMapping("/clearTask")
    @ApiOperation(value = "清空task任务")
    @ActionLog(name = "清空task任务", title = "中间件", key = LogTypeConstant.REMOVEE)
    public ResultVo clearTask(String monitorId, String connectionName) {


/*        IBMMonitor monitor = monitorService.getById(monitorId);
        IBMConnection connection = connectionService.getById(monitor.getConnectionId());

        monitor.setHost(connection.getConnectHost());
        monitor.setPort(connection.getConnectPort());
        monitor.setConnectionName(connection.getConnectName());
        monitor.setConnectionId(connection.getId());
        monitor.setConnectionDescription(connection.getDescription());
        monitor.setChannelName(connection.getChannelName());
        monitor.setUserId(connection.getUserId());
        Monitor monitor2 = Monitor.getMonitor(monitor);*/
        Monitor monitor = new Monitor();
        Connection connection = new Connection();
        connection.setName(connectionName);
        monitor.setId(monitorId);
        monitor.setConnection(connection);
        new MonitorScheduler().unscheduleMonitor(monitor);
        return ResultVoUtil.success("成功了");
    }


    @GetMapping("/queryDetailData")
    @ApiOperation(value = "查询详情信息")
    @RequiresPermissions({"api:mqMonitor:queryDetailData"})
    public ResultVo listAllChannels(String category, String monitorId) {
        ResultVo result = ResultVoUtil.warning("未获取相关监控对象详情信息");
        String strDateFormat = "HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        Object mqObjects = null;
        Map<String, Object> map = new HashMap<>();
        List<Object> title = new ArrayList<>();
        List<Object> dataList = new ArrayList<>();
        List<Object> captureTime = new ArrayList<>();
        switch (category) {
            case "QueueManager":
                break;
            case "Channel":
                List<IBMChannelData> ibmChannelDataList = channelDataService.queryDetailData(monitorId);
                Collections.reverse(ibmChannelDataList);
                List<Object> bytesSent = new ArrayList<>();
                title.add("发送字节数");
                dataList.add(bytesSent);
                List<Object> bytesReceived = new ArrayList<>();
                title.add("接收字节数");
                dataList.add(bytesReceived);
                List<Object> buffersSent = new ArrayList<>();
                title.add("发送字符数");
                dataList.add(buffersSent);
                List<Object> buffersReceived = new ArrayList<>();
                title.add("接收字符数");
                dataList.add(buffersReceived);
                List<Object> messagesTransferred = new ArrayList<>();
                title.add("消息转换数");
                dataList.add(messagesTransferred);

                for (IBMChannelData ibmChannelData : ibmChannelDataList) {
                    bytesSent.add(ibmChannelData.getBytesSent());
                    bytesReceived.add(ibmChannelData.getBytesReceived());
                    buffersSent.add(ibmChannelData.getBuffersSent());
                    buffersReceived.add(ibmChannelData.getBuffersReceived());
                    messagesTransferred.add(ibmChannelData.getMessagesTransferred());
                    captureTime.add(sdf.format(ibmChannelData.getCaptureTime()));
                }
                map.put("captureTime", captureTime);
                map.put("title", title);
                map.put("dataList", dataList);
                mqObjects = map;
                break;
            case "Queue":
                List<IBMQueueData> list = queueDataService.queryDetailData(monitorId);
                Collections.reverse(list);
                List<Object> currentQDepth = new ArrayList<>();
                title.add("当前队列深度");
                dataList.add(currentQDepth);
                List<Object> openInputCount = new ArrayList<>();
                title.add("打开输入个数");
                dataList.add(openInputCount);
                List<Object> openOutputCount = new ArrayList<>();
                title.add("打开输出个数");
                dataList.add(openOutputCount);
                for (IBMQueueData ibmQueueData : list) {
                    currentQDepth.add(ibmQueueData.getCurrentQDepth());
                    openInputCount.add(ibmQueueData.getOpenInputCount());
                    openOutputCount.add(ibmQueueData.getOpenOutputCount());
                    captureTime.add(sdf.format(ibmQueueData.getCaptureTime()));
                }

                map.put("captureTime", captureTime);
                map.put("title", title);
                map.put("dataList", dataList);
                mqObjects = map;

                break;
            case "Topic":
                List<IBMTopicData> ibmTopicDataList = topicDataService.queryDetailData(monitorId);
                Collections.reverse(ibmTopicDataList);
                List<Object> publishedMessagesCount = new ArrayList<>();
                title.add("发布消息个数");
                dataList.add(publishedMessagesCount);
                List<Object> publishersCount = new ArrayList<>();
                title.add("发布个数");
                dataList.add(publishersCount);
                List<Object> subscriberscount = new ArrayList<>();
                title.add("子消息个数");
                dataList.add(subscriberscount);
                for (IBMTopicData ibmTopicData : ibmTopicDataList) {
                    publishedMessagesCount.add(ibmTopicData.getPublishedMessagesCount());
                    publishersCount.add(ibmTopicData.getPublishersCount());
                    subscriberscount.add(ibmTopicData.getSubscriberscount());
                    captureTime.add(sdf.format(ibmTopicData.getCaptureTime()));
                }
                Collections.reverse(dataList);
                map.put("captureTime", captureTime);
                map.put("title", title);
                map.put("dataList", dataList);
                mqObjects = map;

                break;
            case "Listener":
                List<IBMListenerData> ibmListenerDataList = listenerDataService.queryDetailData(monitorId);
                Collections.reverse(ibmListenerDataList);
                List<Object> listenerStatus = new ArrayList<>();
                title.add("监听状态");
                dataList.add(listenerStatus);
                for (IBMListenerData ibmListenerData : ibmListenerDataList) {
                    listenerStatus.add(ibmListenerData.getListenerStatus());
                    captureTime.add(sdf.format(ibmListenerData.getCaptureTime()));
                }
                Collections.reverse(dataList);
                map.put("captureTime", captureTime);
                map.put("title", title);
                map.put("dataList", dataList);
                mqObjects = map;
                break;
        }
        if (mqObjects != null) {
            return ResultVoUtil.success(mqObjects);
        } else {
            return result;
        }


    }


    public List<MQObject> listAllChannels(String connectionName, String channelName, String channelType) {
        if (log.isDebugEnabled()) {
            log.debug("Query all channels in connection '{}'", connectionName);
        }
        Connection connection = this.connectionService.findConnection(connectionName);
        if (log.isDebugEnabled()) {
            log.debug("Found connection '{}'", connection);
        }
        List<MQObject> objects = this.metadataService.queryChannelNames(connection, channelName, channelType);
        if (log.isDebugEnabled()) {
            log.debug("Found {} channels {} in connection '{}'", new Object[]{objects.size(), Arrays.toString(objects.toArray()), connectionName});
        }
        return objects;
    }


    public List<MQObject> listAllQueues(String connectionName, String queueName, String queueType) {
        Connection connection = this.connectionService.findConnection(connectionName);
        if (log.isDebugEnabled()) {
            log.debug("Found connection '{}'", connection);
        }

        List<MQObject> objects = this.metadataService.queryQueueNames(connection, queueName, queueType);
        if (log.isDebugEnabled()) {
            log.debug("Found {} queues {} in connection '{}'", new Object[]{objects.size(), Arrays.toString(objects.toArray()), connectionName});
        }
        List<MQObject> list = new ArrayList<>();
        for (MQObject mqObject : objects) {
            if (!(mqObject.getName() != null && mqObject.getName().contains("AMQ"))) {
                list.add(mqObject);
            }
        }
        if (list.size() > 0) {
            return list;
        }

        return objects;
    }


    public List<MQObject> listAllTopics(String connectionName, String topicName, String topicType) {
        Connection connection = this.connectionService.findConnection(connectionName);
        List<MQObject> objects = this.metadataService.queryTopicNames(connection, topicName, topicType);
        if (log.isDebugEnabled()) {
            log.debug("Found {} topics {} in connection '{}'", new Object[]{objects.size(), Arrays.toString(objects.toArray()), connectionName});
        }
        return objects;
    }


    public List<MQObject> listAllListeners(String connectionName, String listenerName) {
        Connection connection = this.connectionService.findConnection(connectionName);
        List<MQObject> objects = this.metadataService.queryListenerNames(connection, listenerName);
        if (log.isDebugEnabled()) {
            log.debug("Found {} listeners {} in connection '{}'", new Object[]{objects.size(), Arrays.toString(objects.toArray()), connectionName});
        }
        return objects;
    }


    @PostMapping("/listMonitors/{id}")
    @ApiOperation(value = "查询当前连接下的监控对象")
    @ActionLog(name = "查看当前连接下的监控对象", title = "中间件", key = LogTypeConstant.QUERY)
    public ResultVo listMonitors(@PathVariable("id") String id) {

        QueryWrapper<IBMGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("CONNECT_ID", id);
        List<IBMGroup> groupList = ibmGroupService.list(wrapper);

        List<IBMGroup> groups = new ArrayList<>();
        for (IBMGroup ibmGroup : groupList) {
            List<IBMMonitor> monitors = ibmMonitorService.getMonitorById(ibmGroup.getId());
            List<IBMMonitor> monitors2 = new ArrayList<>();
            //注入现深度和最大深度
            for (IBMMonitor monitor : monitors) {
                if (monitor.getCategory().equals("Queue")) {
                    IBMMonitor QueueStatus = ibmMonitorService.getQueueStatus(monitor.getId());
                    String maxQDepth = QueueStatus.getMaxQDepth();
                    String currentQDepth = QueueStatus.getCurrentQDepth();
                    if (ObjectUtil.isNull(maxQDepth) || Strings.isNullOrEmpty(maxQDepth)) {
                        maxQDepth = "999";
                    }
                    if (ObjectUtil.isNull(currentQDepth) || Strings.isNullOrEmpty(currentQDepth)) {
                        currentQDepth = "0";
                    }
                    monitor.setMaxQDepth(maxQDepth);
                    monitor.setCurrentQDepth(currentQDepth);
                }

                if (monitor.getCategory().equals("Channel")) {
                    IBMMonitor channelStatus = ibmMonitorService.getChannelStatus(monitor.getId());
                    monitor.setSentStr(bytes2kb(channelStatus.getBytesSent()));
                    monitor.setReceivedStr(bytes2kb(channelStatus.getBytesReceived()));
                }
                monitors2.add(monitor);
            }

            ibmGroup.setMonitorList(monitors2);
            groups.add(ibmGroup);
        }

        return ResultVoUtil.success(groups);
    }

    @PostMapping("/connectionMonitorExist")
    public ResultVo existMonitor(String connectionName, String monitorName) {
        boolean result = this.monitorService.existsMonitor(connectionName, monitorName);
        if (log.isDebugEnabled()) {
            log.debug("Monitor '{}' exists in connection '{}' is '{}'", new Object[]{connectionName, monitorName, result});
        }
        return ResultVoUtil.success(result);
    }


    private void tryForbidMQTTMonitorAccess(Monitor monitor) {
        if (monitor.isMQTT()) {
            throw new MetadataAccessNotAllowedException(40311, ErrorConstants.Message.MSG_MQTT_MONITOR_ACCESS_NOT_ALLOWED, new Object[]{monitor});
        }
    }


    private void removeRelateInfo(String monitorId, String category) {
        switch (category) {
            case "QueueManager":
                queueDataService.removeStatistics(monitorId);
                break;
            case "Channel":
                channelDataService.removeStatistics(monitorId);
                break;
            case "Queue":
                queueDataService.removeStatistics(monitorId);
                break;
            case "Topic":
                topicDataService.removeStatistics(monitorId);
                break;
            case "Listener":
                listenerDataService.removeStatistics(monitorId);
                break;
        }


    }

    private Map<String, String> getMap(String str) {//匹配出括号中的字符串
        String regex = "(?<=\\()[^\\(\\)]*(?=\\))";
        Pattern pat = Pattern.compile(regex);
        Matcher mat = pat.matcher(str);
        HashMap<String, String> map = new HashMap<>();
        int i = 0;
        while (mat.find()) {
            String[] kv = mat.group().split(">");
            map.put(kv[0], kv[1]);
        }
        return map;
    }

    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1)
            return (returnValue + "MB");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "KB");
    }


}
