package com.jcca.dataProcessing.manager;

import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.quartz.alarm.AlarmJobService;
import com.jcca.dataProcessing.Entity.*;
import com.jcca.dataProcessing.support.*;
import com.jcca.web.alarm.controller.AlarmInfoController;
import com.jcca.web.alarm.controller.AlarmRepositoryController;
import com.jcca.web.asset.controller.ApiThresholdAssetController;
import com.jcca.web.asset.controller.ThresholdProcessController;
import com.jcca.web.common.controller.ApiCollectSyslogController;
import com.jcca.web.db.controller.ApiManageDbController;
import com.jcca.web.event.controller.AlarmEventGroupController;
import com.jcca.web.event.controller.AlarmEventTypeController;
import com.jcca.web2.controller.AlarmEventControllerV2;
import com.jcca.web2.controller.AlarmWhitelistControllerV2;
import com.jcca.web2.controller.ProcessConfigControllerV2;
import com.jcca.web2.controller.ThresholdControllerV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 监控信息管理程序
 *
 * @author Zhaozheng
 * @description TODO
 * @className DataProcessManager
 * @date 2023/10/20 11:54
 * @since 2.1.0.0
 */
@Component("dataProcessManager")
@Slf4j
public class DataProcessManager {

    @Resource(name = "beanManager")
    private BeanManager beanManager;

    private IListener eventInfoListener;
    private IListener alarmListener;

    private Map<String, IAdapter> adapters;
    private IFilterHandler aixSystemMsgHandler;
    private IFilterHandler collectCluster;
    private IFilterHandler cascoLinkHandler;

    private IFilterHandler donghuanHandler;

    private IFilterHandler cascoMasterHandler;
    private IFilterHandler cascoThresholdHandler;
    private IFilterHandler cascoVersionHandler;


    private IFilterHandler tikeWorkStateHandler;
    private IFilterHandler tikeWorkChannelLinkHandler;
    private IFilterHandler tikeVersionHandler;
    private IFilterHandler tikeSecureLinkHandler;

    private IFilterHandler beiYangWorkHandler;
    private IFilterHandler beiYangLinkHandler;
    private IFilterHandler beiYangVersionHandler;
    private IFilterHandler xdhyHandler;


    private IFilterHandler congXingHandler;
    private IFilterHandler connectHandler;
    private IFilterHandler cpuHandler;
    private IFilterHandler cpuLoadHandler;
    private IFilterHandler dbHandler;
    private IFilterHandler dbTableHandler;
    private IFilterHandler dbAlarmHandler;
    private IFilterHandler diskHandler;
    private IFilterHandler memoryHandler;
    private IFilterHandler processHandler;
    private IFilterHandler processGroupHandler;
    private IFilterHandler portNumberHandler;

    private IFilterHandler netHandler;
    private IFilterHandler interfaceHander;
    private IFilterHandler raidHandler;

    private IFilterHandler raidDsBaseHandler;
    private IFilterHandler raidDsHandler;
    private IFilterHandler raidDsLogHandler;
    private IFilterHandler raidVLogHandler;
    private IFilterHandler raidBaseHandler;

    private IFilterHandler ipmiHandler;
    private IFilterHandler sensorHandler;

    private IFilterHandler snmpHandler;
    private IFilterHandler syslogHandler;

    private IFilterHandler centerSystemTimeHandler;
    private IFilterHandler stationSystemTimeHandler;

    private IFilterHandler pingHandler;
    private IFilterHandler pingGeneralHandler;

    private IFilterHandler pcbHandler;
    private IFilterHandler opticalHandler;

    private IFilterHandler mqHandler;
    private IFilterHandler eventInfoHandler;
    private IFilterHandler alarmInfoHandler;

    private IFilterHandler customHandler;

    private IFilterHandler collectNodeStatusHandler;

    private IFilterHandler collectBhmCpuHandler;
    private IFilterHandler collectBhmFanHandler;
    private IFilterHandler collectBhmMemoryHandler;
    private IFilterHandler collectBhmPcieHandler;
    private IFilterHandler collectBhmPowerHandler;
    private IFilterHandler collectBhmStorageHandler;
    private IFilterHandler collectBhmTempHandler;


    /**
     * 创建执行器
     *
     * @param handlerClassList
     * @param needAddEventHandlerClassList
     * @param eventInfoListener
     * @return
     */
    private IFilterHandler createHandler(List<String> handlerClassList, List<String> needAddEventHandlerClassList, IListener eventInfoListener) {
        IFilterHandler beginHandler = null;
        IFilterHandler filterHandler = null;
        for (String className : handlerClassList) {
            IFilterHandler itemHandler = this.getIFilterHandler(className);
            if (Objects.nonNull(filterHandler)) {
                filterHandler.setNextFilter(itemHandler);
            } else {
                beginHandler = itemHandler;
            }
            filterHandler = itemHandler;

            if (needAddEventHandlerClassList.contains(className)) {

                List<IListener> list = itemHandler.getListeners();
                boolean isContain = false;
                for (IListener listener : list) {
                    if (listener.equals(eventInfoListener)) {
                        isContain = true;
                    }
                }
                //如果不存在此监听器，就添加
                if (isContain == false) {
                    itemHandler.addDataSourceListener(eventInfoListener);
                }

            }
        }

        return beginHandler;
    }

    public void initBaseListener() {
        AlarmRepositoryController alarmRepositoryController = SpringContextUtil.getBean(AlarmRepositoryController.class);
        AlarmEventTypeController alarmEventTypeController = SpringContextUtil.getBean(AlarmEventTypeController.class);
        AlarmEventControllerV2 alarmEventController = SpringContextUtil.getBean(AlarmEventControllerV2.class);
        IListener alramRepositoryListener = this.getListener("alarmRepoListener");
        alarmRepositoryController.addDataSourceListener(alramRepositoryListener);
        alarmEventTypeController.addDataSourceListener(alramRepositoryListener);
        alarmEventController.addDataSourceListener(alramRepositoryListener);

        ThresholdProcessController thresholdProcessController = SpringContextUtil.getBean(ThresholdProcessController.class);
        IListener thresholdListener = this.getListener("thresholdListener");
        thresholdProcessController.addDataSourceListener(thresholdListener);
        ApiThresholdAssetController apiThresholdAssetController = SpringContextUtil.getBean(ApiThresholdAssetController.class);
        apiThresholdAssetController.addDataSourceListener(thresholdListener);


        AlarmJobService alarmJobService = SpringContextUtil.getBean(AlarmJobService.class);
        IListener eventListener = this.getListener("eventInfoListener");
        alarmJobService.addDataSourceListener(eventListener);

        IListener cacheEventListener = this.getListener("cacheEventListener");
        AlarmInfoController alarmInfoController = SpringContextUtil.getBean(AlarmInfoController.class);
        alarmInfoController.addDataSourceListener(cacheEventListener);

        IListener customEventListenerReceiver = this.getListener("customEventListenerReceiver");
        ApiCollectSyslogController apiCollectSyslogController = SpringContextUtil.getBean(ApiCollectSyslogController.class);
        apiCollectSyslogController.addDataSourceListener(customEventListenerReceiver);
        //白名单
        AlarmWhitelistControllerV2 alarmWhitelistControllerV2 = SpringContextUtil.getBean(AlarmWhitelistControllerV2.class);
        alarmWhitelistControllerV2.addDataSourceListener(cacheEventListener);
        //V2阈值
        ThresholdControllerV2 thresholdControllerV2 = SpringContextUtil.getBean(ThresholdControllerV2.class);
        thresholdControllerV2.addDataSourceListener(thresholdListener);

        //v2进程管理
        ProcessConfigControllerV2 processControllerV2 = SpringContextUtil.getBean(ProcessConfigControllerV2.class);
        processControllerV2.addDataSourceListener(thresholdListener);


        ApiManageDbController apiManageDbController = SpringContextUtil.getBean(ApiManageDbController.class);
        apiManageDbController.addDataSourceListener(thresholdListener);

        AlarmEventGroupController alarmEventGroupController = SpringContextUtil.getBean(AlarmEventGroupController.class);
        alarmEventGroupController.addDataSourceListener(thresholdListener);

    }

    /**
     * 适配器、过滤器初始化
     */
    public void init() {
        initBaseListener();

        Map<String, IAdapter> beanMap = this.getAdapters();
        adapters = new HashMap<>(beanMap.size());
        for (Map.Entry<String, IAdapter> entry : beanMap.entrySet()) {
            if (entry.getValue().getCode() != null && !adapters.containsKey(entry.getValue().getCode())) {
                adapters.put(entry.getValue().getCode(), entry.getValue());
            }
        }
        //初始化监听事件
        eventInfoListener = this.getListener("eventInfoListener");
        alarmListener = this.getListener("alarmListener");


        //2025-09-01 服务器BHM的CPU信息
        List<String> bhmCpuHandlerList = Arrays.asList("bhmCpuSaveHandler");
        IFilterHandler bhmCpuHandler = createHandler(bhmCpuHandlerList, new ArrayList<>(), eventInfoListener);
        collectBhmCpuHandler = bhmCpuHandler;
        //2025-09-01 服务器BHM的Fan信息
        List<String> bhmFanHandlerList = Arrays.asList("bhmFanSaveHandler");
        IFilterHandler bhmFanHandler = createHandler(bhmFanHandlerList, new ArrayList<>(), eventInfoListener);
        collectBhmFanHandler = bhmFanHandler;
        //2025-09-01 服务器BHM的Memory信息
        List<String> bhmMemoryHandlerList = Arrays.asList("bhmMemorySaveHandler");
        IFilterHandler bhmMemoryHandler = createHandler(bhmMemoryHandlerList, new ArrayList<>(), eventInfoListener);
        collectBhmMemoryHandler = bhmMemoryHandler;
        //2025-09-01 服务器BHM的PCIE信息
        List<String> bhmPcieHandlerList = Arrays.asList("bhmPcieSaveHandler");
        IFilterHandler bhmPcieHandler = createHandler(bhmPcieHandlerList, new ArrayList<>(), eventInfoListener);
        collectBhmPcieHandler = bhmPcieHandler;
        //2025-09-01 服务器BHM的Power信息
        List<String> bhmPowerHandlerList = Arrays.asList("bhmPowerSaveHandler");
        IFilterHandler bhmPowerHandler = createHandler(bhmPowerHandlerList, new ArrayList<>(), eventInfoListener);
        collectBhmPowerHandler = bhmPowerHandler;
        //2025-09-01 服务器BHM的存储信息
        List<String> bhmStorageHandlerList = Arrays.asList("bhmStorageSaveHandler");
        IFilterHandler bhmStorageHandler = createHandler(bhmStorageHandlerList, new ArrayList<>(), eventInfoListener);
        collectBhmStorageHandler = bhmStorageHandler;
        //2025-09-01 服务器BHM的温度信息
        List<String> bhmTempHandlerList = Arrays.asList("bhmTempSaveHandler");
        IFilterHandler bhmTempHandler = createHandler(bhmTempHandlerList, new ArrayList<>(), eventInfoListener);
        collectBhmTempHandler = bhmTempHandler;


        List<String> cpuHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "cpuSaveFilterHandler",
                "cpuFilterHandler",
                "cpuSectionFilterHandler",
                "cpuStageOneFilterHandler",
                "cpuStageTwoFilterHandler",
                "cpuStageThreeFilterHandler",
                "CpuTop5FilterHandler",
                //通用缓存保存fitlerHandler
                "saveFilterHandler");
        //需要添加内容event监听的filterHandler;
        List<String> eventList = Arrays.asList(
                "cpuFilterHandler",
                "cpuSectionFilterHandler",
                "cpuStageOneFilterHandler",
                "cpuStageTwoFilterHandler",
                "cpuStageThreeFilterHandler",
                "CpuTop5FilterHandler");

        //CPU阈值
        IFilterHandler cpuHandlerImpl = createHandler(cpuHandlerList, eventList, eventInfoListener);
        cpuHandler = cpuHandlerImpl;


        // CPU负载
        List<String> cpuLoadHandlerList = Arrays.asList(
                "cpuLoadSaveFilterHandler",
                "cpuLoadThresholdFilterHandler",
                "appServerLinkSaveFilterHandler",
                "saveFilterHandler");
        List<String> cpuLoadEventList = Arrays.asList(
                "cpuLoadThresholdFilterHandler",
                "appServerLinkSaveFilterHandler");
        cpuLoadHandler = createHandler(cpuLoadHandlerList, cpuLoadEventList, eventInfoListener);


        List<String> stationSystemHandlerList = Arrays.asList(
                "stationSystemRunTimeFilterHandler",
                "stationSystemTimeFilterHandler",
                "stationSystemTimeSaveFilterHandler",
                "commonSystemRestartFilterHandler");
        List<String> stationSystemList = Arrays.asList(
                "stationSystemRunTimeFilterHandler",
                "stationSystemTimeFilterHandler",
                "commonSystemRestartFilterHandler");
        //车站时间处理
        IFilterHandler stationTimeHandlerImpl = createHandler(stationSystemHandlerList, stationSystemList, eventInfoListener);
        stationSystemTimeHandler = stationTimeHandlerImpl;


        //内存
        List<String> memoryHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "memoryFilterHandler",
                "memorySaveFilterHandler",
                "memoryInfoFilterHandler",
                "memorySectionFilterHandler",
                "memoryStageOneFilterHandler",
                "memoryStageTwoFilterHandler",
                "memoryStageThreeFilterHandler",
                "memoryTop5FilterHandler",
                //通用缓存保存fitlerHandler
                "saveFilterHandler");
        List<String> memoryEventHandlerList = Arrays.asList(
                "memoryFilterHandler",
                "memoryInfoFilterHandler",
                "memorySectionFilterHandler",
                "memoryStageOneFilterHandler",
                "memoryStageTwoFilterHandler",
                "memoryStageThreeFilterHandler",
                "memoryTop5FilterHandler");
        memoryHandler = createHandler(memoryHandlerList, memoryEventHandlerList, eventInfoListener);
        ;


        //磁盘
        List<String> diskHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "diskSaveFilterHandler",
                "diskFilterHandler",
                "diskStageSectionFilterHandler",
                "diskStageOneFilterHandler",
                "diskStageTwoFilterHandler",
                "diskStageThreeFilterHandler",
                //通用缓存保存fitlerHandler
                "saveFilterHandler");
        List<String> diskEventHandlerList = Arrays.asList(
                "diskFilterHandler",
                "diskStageSectionFilterHandler",
                "diskStageOneFilterHandler",
                "diskStageTwoFilterHandler",
                "diskStageThreeFilterHandler");
        diskHandler = createHandler(diskHandlerList, diskEventHandlerList, eventInfoListener);


        //网卡
        List<String> netHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "netSaveFilterHandler",
                "disableNetInfoFilterHandler",
                "netInfoFilterHandler",
                "netStateFilterHandler",
                "saveFilterHandler");
        List<String> netEventHandlerList = Arrays.asList(
                "netStateFilterHandler");
        netHandler = createHandler(netHandlerList, netEventHandlerList, eventInfoListener);


        //系统运行时间、时间偏差、系统时间
        List<String> runTimeHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "centerSystemTimeSaveFilterHandler",
                "centerSystemRunTimeFilterHandler",
                "centerSystemTimeFilterHandler",
                "commonSystemRestartFilterHandler",
                "saveFilterHandler");
        List<String> runTimeEventHandlerList = Arrays.asList(
                "centerSystemRunTimeFilterHandler",
                "centerSystemTimeFilterHandler",
                "commonSystemRestartFilterHandler");
        centerSystemTimeHandler = createHandler(runTimeHandlerList, runTimeEventHandlerList, eventInfoListener);


        //端口占用情况
        List<String> portNumberHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "portNumberSaveFilterHandler",
                "portNumberTcpFilterHandler",
                "portNumberUdpFilterHandler",
                "saveFilterHandler");
        List<String> portNumberEventHandlerList = Arrays.asList(
                "portNumberTcpFilterHandler",
                "portNumberUdpFilterHandler");
        portNumberHandler = createHandler(portNumberHandlerList, portNumberEventHandlerList, eventInfoListener);


        //连接数
        List<String> connectHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "connectSaveFilterHandler",
                "connectFilterHandler",
                "saveFilterHandler");
        List<String> connectEventHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "connectSaveFilterHandler",
                "connectFilterHandler");
        connectHandler = createHandler(connectHandlerList, connectEventHandlerList, eventInfoListener);


        //进程
        List<String> processHandlerList = Arrays.asList(
                //数据库保存filterHandler
                "processSaveFilterHandler",
                "ProcessIdFilterHandler",
                "processStateFilterHandler",
                "processCpuFilterHandler",
                "processMemoryFilterHandler",
                "saveFilterHandler");
        List<String> processEventHandlerList = Arrays.asList(
                "processStateFilterHandler",
                "processCpuFilterHandler",
                "processMemoryFilterHandler");
        processHandler = createHandler(processHandlerList, processEventHandlerList, eventInfoListener);
        //进程组
        List<String> processGroupHandlerList = Arrays.asList(
                "processSaveGroupFilterHandler",
                "processChangeFilterHandler",
                "processGroupSingleStateFilterHandler",
                "ProcessGroupDoubleStateFilterHandler",
                "ProcessAloneStateFilterHandler",
                "saveFilterHandler"
        );
        List<String> processGroupEventHandlerList = Arrays.asList(
                "processChangeFilterHandler",
                "processGroupSingleStateFilterHandler",
                "ProcessGroupDoubleStateFilterHandler",
                "ProcessAloneStateFilterHandler");
        processGroupHandler = createHandler(processGroupHandlerList, processGroupEventHandlerList, eventInfoListener);

        //IPMI管理口
        List<String> ipmiHandlerList = Arrays.asList(
                "ipmiSaveFilterHandler",
                "ipmiFanFilterHandler",
                "ipmiPowerFilterHandler",
                "ipmiTemperatureFilterHandler",
                "ipmiTemperatureStatusFilterHandler",
                "ipmiCPUFilterHandler",
                "ipmiLedFilterHandler",
                "ipmiLogFilterHandler",
                "ipmiManuFilterHandler",
                "sensorTemperatureStageOneFilterHandler",
                "sensorTemperatureStageTwoFilterHandler",
                "sensorTemperatureStageThreeFilterHandler",
                "saveFilterHandler");
        List<String> ipmiEventHandlerList = Arrays.asList(
                "ipmiFanFilterHandler",
                "ipmiPowerFilterHandler",
                "ipmiTemperatureFilterHandler",
                "ipmiTemperatureStatusFilterHandler",
                "ipmiCPUFilterHandler",
                "ipmiLedFilterHandler",
                "ipmiLogFilterHandler",
                "sensorTemperatureStageOneFilterHandler",
                "sensorTemperatureStageTwoFilterHandler",
                "sensorTemperatureStageThreeFilterHandler",
                "ipmiManuFilterHandler");
        ipmiHandler = createHandler(ipmiHandlerList, ipmiEventHandlerList, eventInfoListener);

        //aix小机
        List<String> aixHandlerList = Arrays.asList(
                "aixSystemMsgSaveIFilterHandlerHandler",
                "aixSystemMsgIFilterHandlerHandler",
                "saveFilterHandler");
        List<String> aixEventHandlerList = Arrays.asList();
        aixSystemMsgHandler = createHandler(aixHandlerList, aixEventHandlerList, eventInfoListener);


        //添加自律机集群相关事件
        List<String> clusterHandlerList = Arrays.asList(
                "clusterStateSaveFilterHandler",
                "clusterAbStatusFilterHandler",
                "clusterNodeStatusFilterHandler",
                "clusterStateFilterHandler",
                "saveFilterHandler");
        List<String> clusterEventHandlerList = Arrays.asList(
                "clusterStateSaveFilterHandler",
                "clusterAbStatusFilterHandler",
                "clusterNodeStatusFilterHandler",
                "clusterStateFilterHandler",
                "saveFilterHandler");
        collectCluster = createHandler(clusterHandlerList, clusterEventHandlerList, eventInfoListener);


        //DS存储基础信息
        List<String> raidDsBaseHandlerList = Arrays.asList(
                "raidDsSaveFilterHandler",
                "raidDsStorageBaseInfoFilterHandler",
                "saveFilterHandler");
        List<String> raidDsBaseEventHandlerList = Arrays.asList();
        raidDsBaseHandler = createHandler(raidDsBaseHandlerList, raidDsBaseEventHandlerList, eventInfoListener);
        //DS存储其他信息

        List<String> raidHandlerList = Arrays.asList(
                "raidDsStorageControllerFitlerHandler",
                "raidDsStorageArrayFitlerHandler",
                "raidDsStorageDriverFilterHandler",
                "raidDsStorageLogicDriverFilterHandler",
                "saveFilterHandler"
        );
        List<String> raidEventHandlerList = Arrays.asList(
                "raidDsStorageControllerFitlerHandler",
                "raidDsStorageArrayFitlerHandler",
                "raidDsStorageDriverFilterHandler",
                "raidDsStorageLogicDriverFilterHandler",
                "saveFilterHandler"
        );

        raidDsHandler = createHandler(raidHandlerList, raidEventHandlerList, eventInfoListener);
        //存储日志
        raidDsLogHandler = this.getIFilterHandler("raidCommonStorageLogFilterHandler");
        IFilterHandler raidDsLogSave = this.getIFilterHandler("saveFilterHandler");
        raidDsLogHandler.setNextFilter(raidDsLogSave);


        //V系列存储日志
        raidVLogHandler = this.getIFilterHandler("raidVLogSaveFilterHandler");
        IFilterHandler raidLogInfo = this.getIFilterHandler("raidCommonStorageLogFilterHandler");
        raidVLogHandler.setNextFilter(raidLogInfo);

        //V系列存储
        List<String> raidVHandlerList = Arrays.asList(
                "raidVSaveFilterHandler",
                "raidStorageBaseInfoFilterHandler",
                "raidStorageDriverFitlerHandler",
                "raidStorageGroupFilterHandler",
                "raidStorageMdiskFilterHandler",
                "raidStorageVidskFitlerHandler",
                "raidStorageStateFilterHandler",
                "saveFilterHandler"
        );
        List<String> raidVEventHandlerList = Arrays.asList(
                "raidStorageStateFilterHandler"
        );
        raidHandler = createHandler(raidVHandlerList, raidVEventHandlerList, eventInfoListener);

        //数据库
        List<String> dbHandlerList = Arrays.asList(
                "dbInfoSaveFilterHandler",
                "dbInfoFilterHandler",
                "dBConnectFilterHandler",
                "saveFilterHandler"
        );
        List<String> dbEventHandlerList = Arrays.asList(
                "dBConnectFilterHandler"
        );
        dbHandler = createHandler(dbHandlerList, dbEventHandlerList, eventInfoListener);
        //表空间
        List<String> tabSpaceHandlerList = Arrays.asList(
                "dbTableSpaceInfoFilterHandler",
                "dBTableSpaceFilterHandler",
                "dBTableSpaceStageOneFilterHandler",
                "dBTableSpaceStageTwoFilterHandler",
                "dbTableSpaceStageThreeFilterHandler",
                "saveFilterHandler"
        );
        List<String> tabSpaceEventHandlerList = Arrays.asList(
                "dBTableSpaceFilterHandler",
                "dBTableSpaceStageOneFilterHandler",
                "dBTableSpaceStageTwoFilterHandler",
                "dbTableSpaceStageThreeFilterHandler"
        );

        dbTableHandler = createHandler(tabSpaceHandlerList, tabSpaceEventHandlerList, eventInfoListener);

        //数据库告警
        dbAlarmHandler = this.getIFilterHandler("dBAlarmConnectFilterHandler");
        IFilterHandler dbAlarmSave = this.getIFilterHandler("saveFilterHandler");
        dbAlarmHandler.addDataSourceListener(eventInfoListener);
        dbAlarmHandler.setNextFilter(dbAlarmSave);


        //端口
        List<String> interfaceHandlerList = Arrays.asList(
                "interfaceInfoFilterHandler",
                "disableFitlerInterfaceFilterHandler",
                "interfaceSaveInfoFilterHandler",
                "disableInterfaceFilterHandler",
                "interfaceUpDownFilterHandler",

                "interfaceErrorCodeInFilterHandler",
                "interfaceErrorCodeInSectionFilterHandler",
                "InterfaceErrorCodeInStageOneFilterHandler",
                "interfaceErrorCodeInStageTwoFilterHandler",
                "interfaceErrorCodeInStageThreeFilterHandler",

                "interfaceErrorCodeOutFilterHandler",
                "interfaceErrorCodeOutSectionFilterHandler",
                "interfaceErrorCodeOutStageOneFilterHandler",
                "interfaceErrorCodeOutStageTwoFilterHandler",
                "interfaceErrorCodeOutStageThreeFilterHandler",

                "interfaceLosePacketsInFilterHandler",
                "interfaceLosePacketsInSectionFilterHandler",
                "interfaceLosePacketsInStageOneFilterHandler",
                "interfaceLosePacketsInStageTwoFilterHandler",
                "interfaceLosePacketsInStageThreeFilterHandler",

                "interfaceLosePacketsOutFilterHandler",
                "interfaceLosePacketsOutSectionFilterHandler",
                "interfaceLosePacketsOutStageOneFilterHandler",
                "interfaceLosePacketsOutStageTwoFilterHandler",
                "interfaceLosePacketsOutStageThreeFilterHandler",

                "interfacePortInFilterHandler",
                "interfacePortInSectionFilterHandler",
                "interfacePortInStageOneFilterHandler",
                "interfacePortInStageTwoFilterHandler",
                "interfacePortInStageThreeFilterHandler",

                "interfacePortOutFilterHandler",
                "interfacePortOutSectionFilterHandler",
                "interfacePortOutStageOneFilterHandler",
                "interfacePortOutStageTwoFilterHandler",
                "interfacePortOutStageThreeFilterHandler",

                "interfaceRxPowerStageFilterHandler",
                "interfaceTxPowerStageFilterHandler",
                "interfaceRxPowerOneFilterHandler",
                "interfaceRxPowerTwoFilterHandler",
                "interfaceRxPowerThreeFilterHandler",
                "interfaceTxPowerOneFilterHandler",
                "interfaceTxPowerTwoFilterHandler",
                "interfaceTxPowerThreeFilterHandler",
                "saveFilterHandler"
        );
        List<String> interfaceEventHandlerList = Arrays.asList(
                //根据类型过滤，非可用类型的不上告警！！！！
                "disableFitlerInterfaceFilterHandler",

                "interfaceUpDownFilterHandler",

                "interfaceErrorCodeInFilterHandler",
                "interfaceErrorCodeInSectionFilterHandler",
                "InterfaceErrorCodeInStageOneFilterHandler",
                "interfaceErrorCodeInStageTwoFilterHandler",
                "interfaceErrorCodeInStageThreeFilterHandler",

                "interfaceErrorCodeOutFilterHandler",
                "interfaceErrorCodeOutSectionFilterHandler",
                "interfaceErrorCodeOutStageOneFilterHandler",
                "interfaceErrorCodeOutStageTwoFilterHandler",
                "interfaceErrorCodeOutStageThreeFilterHandler",

                "interfaceLosePacketsInFilterHandler",
                "interfaceLosePacketsInSectionFilterHandler",
                "interfaceLosePacketsInStageOneFilterHandler",
                "interfaceLosePacketsInStageTwoFilterHandler",
                "interfaceLosePacketsInStageThreeFilterHandler",

                "interfaceLosePacketsOutFilterHandler",
                "interfaceLosePacketsOutSectionFilterHandler",
                "interfaceLosePacketsOutStageOneFilterHandler",
                "interfaceLosePacketsOutStageTwoFilterHandler",
                "interfaceLosePacketsOutStageThreeFilterHandler",

                "interfacePortInFilterHandler",
                "interfacePortInSectionFilterHandler",
                "interfacePortInStageOneFilterHandler",
                "interfacePortInStageTwoFilterHandler",
                "interfacePortInStageThreeFilterHandler",

                "interfacePortOutFilterHandler",
                "interfacePortOutSectionFilterHandler",
                "interfacePortOutStageOneFilterHandler",
                "interfacePortOutStageTwoFilterHandler",
                "interfacePortOutStageThreeFilterHandler",


                "interfaceRxPowerStageFilterHandler",
                "interfaceRxPowerOneFilterHandler",
                "interfaceRxPowerTwoFilterHandler",
                "interfaceRxPowerThreeFilterHandler",
                "interfaceTxPowerStageFilterHandler",
                "interfaceTxPowerOneFilterHandler",
                "interfaceTxPowerTwoFilterHandler",
                "interfaceTxPowerThreeFilterHandler"
        );
        interfaceHander = createHandler(interfaceHandlerList, interfaceEventHandlerList, eventInfoListener);

        //ping组 事件
        List<String> pingHandlerList = Arrays.asList(
                "pingSaveFilterHandler",
                "pingGroupFilterHandler",
                "pingGroupAllDownFilterHandler",
                "pingGroupOtherDownFilterHandler",
                "pingNoGroupFilterHandler",
                "saveFilterHandler"
        );

        List<String> pingEventHandlerList = Arrays.asList(
                "pingGroupFilterHandler",
                "pingGroupAllDownFilterHandler",
                "pingGroupOtherDownFilterHandler",
                "pingNoGroupFilterHandler"
        );


        pingHandler = createHandler(pingHandlerList, pingEventHandlerList, eventInfoListener);
        //ping 事件
        List<String> pingGeneralHandlerList = Arrays.asList(
                "pingGeneralSaveFilterHandler",
                "pingGeneralFilterHandler",
                "saveFilterHandler"
        );
        List<String> pingGeneralEventHandlerList = Arrays.asList(
                "pingGeneralSaveFilterHandler",
                "pingGeneralFilterHandler");

        pingGeneralHandler = createHandler(pingGeneralHandlerList, pingGeneralEventHandlerList, eventInfoListener);

        //板卡
        IFilterHandler pcb1 = this.getIFilterHandler("pCBSaveFilterHandler");
        pcbHandler = pcb1;
        //光纤交换机

        List<String> opticalHandlerList = Arrays.asList(
                "opticalSaveFilterHandler",
                "opticalInterfaceUpDownFilterHandler",
                "opticalBandWithFilterHandler",
                "opticalFanFilterHandler",
                "opticalTemperatureFilterHandler",
                "opticalTemperatureStageOneFilterHandler",
                "opticalTemperatureStageTwoFilterHandler",
                "opticalTemperatureStageThreeFilterHandler",
                "opticalPowerFilterHandler",
                "opticalVoMapFilterHandler",
                "saveFilterHandler"
        );
        List<String> opticalEventHandlerList = Arrays.asList(
                "opticalInterfaceUpDownFilterHandler",
                "opticalBandWithFilterHandler",
                "opticalFanFilterHandler",
                "opticalTemperatureFilterHandler",
                "opticalTemperatureStageOneFilterHandler",
                "opticalTemperatureStageTwoFilterHandler",
                "opticalTemperatureStageThreeFilterHandler",
                "opticalPowerFilterHandler",
                "opticalVoMapFilterHandler"
        );

        opticalHandler = createHandler(opticalHandlerList, opticalEventHandlerList, eventInfoListener);

        //传感器
        List<String> sensorHandlerList = Arrays.asList(
                "sensorSaveFilterHandler",
                "sensorFanFilterHandler",
                "sensorPowerFilterHandler",
                "sensorTemperatureFilterHandler",
                "sensorTemperatureStageOneFilterHandler",
                "sensorTemperatureStageTwoFilterHandler",
                "sensorTemperatureStageThreeFilterHandler",
                "saveFilterHandler"
        );
        List<String> sensorEventHandlerList = Arrays.asList(
                "sensorFanFilterHandler",
                "sensorPowerFilterHandler",
                "sensorTemperatureFilterHandler",
                "sensorTemperatureStageOneFilterHandler",
                "sensorTemperatureStageTwoFilterHandler",
                "sensorTemperatureStageThreeFilterHandler"
        );
        sensorHandler = createHandler(sensorHandlerList, sensorEventHandlerList, eventInfoListener);


        //北洋事件
        List<String> beiYangLinkHandlerList = Arrays.asList(
                "softLinkFitlerHandler",
                "saveFilterHandler"
        );
        List<String> beiYangVersionHandlerList = Arrays.asList(
                "commonVersionFilterHandler",
                "saveFilterHandler"
        );
        List<String> beiYangWorkstateHandlerList = Arrays.asList(
                "commonWorkStateFilterHandler",
                "saveFilterHandler"
        );

        List<String> beiYangLinkEventHandlerList = Arrays.asList(
                "softLinkFitlerHandler"
        );
        List<String> beiYangVersionEventHandlerList = Arrays.asList(
                "commonVersionFilterHandler"
        );
        List<String> beiYangWorkStateEventHandlerList = Arrays.asList(
                "commonWorkStateFilterHandler"
        );
        beiYangWorkHandler = createHandler(beiYangWorkstateHandlerList, beiYangWorkStateEventHandlerList, eventInfoListener);
        beiYangLinkHandler = createHandler(beiYangLinkHandlerList, beiYangLinkEventHandlerList, eventInfoListener);
        beiYangVersionHandler = createHandler(beiYangVersionHandlerList, beiYangVersionEventHandlerList, eventInfoListener);

        //卡斯柯业务事件
        IFilterHandler cascoLink = this.getIFilterHandler("cascoLinkFitlerHandler");
        cascoLink.addDataSourceListener(eventInfoListener);
        IFilterHandler cascoLinkSave = this.getIFilterHandler("saveFilterHandler");
        cascoLink.setNextFilter(cascoLinkSave);
        cascoLinkHandler = cascoLink;


        //动环业务事件
        IFilterHandler dongHuan = this.getIFilterHandler("dongHuanNotifyHandler");
        //IFilterHandler dongHuanPower = this.getIFilterHandler("DongHuanPowerHandler");
        dongHuan.addDataSourceListener(eventInfoListener);
        donghuanHandler = dongHuan;


        //采集器掉线事件
        IFilterHandler nodeStatusHandler = this.getIFilterHandler("collectNodeStatusHandler");
        nodeStatusHandler.addDataSourceListener(eventInfoListener);
        collectNodeStatusHandler = nodeStatusHandler;


        IFilterHandler cascoMaster = this.getIFilterHandler("commonMasterFilterHandler");
        cascoMaster.addDataSourceListener(eventInfoListener);
        IFilterHandler cascoMasterSave = this.getIFilterHandler("saveFilterHandler");
        cascoMaster.setNextFilter(cascoMasterSave);
        cascoMasterHandler = cascoMaster;

        IFilterHandler cascoThreshold = this.getIFilterHandler("cascoThresholdFilterHandler");
        cascoThreshold.addDataSourceListener(eventInfoListener);
        IFilterHandler cascoThresholdSave = this.getIFilterHandler("saveFilterHandler");
        cascoThreshold.setNextFilter(cascoThresholdSave);
        cascoThresholdHandler = cascoThreshold;

        IFilterHandler cascoVersion = this.getIFilterHandler("commonVersionFilterHandler");
        cascoVersion.addDataSourceListener(eventInfoListener);
        IFilterHandler cascoVersionSave = this.getIFilterHandler("saveFilterHandler");
        cascoVersion.setNextFilter(cascoVersionSave);
        cascoVersionHandler = cascoVersion;


        //铁科
        IFilterHandler tiekeWorkSate = this.getIFilterHandler("commonWorkStateFilterHandler");
        tiekeWorkSate.addDataSourceListener(eventInfoListener);
        IFilterHandler tiekeMaster = this.getIFilterHandler("commonMasterFilterHandler");
        tiekeMaster.addDataSourceListener(eventInfoListener);
        IFilterHandler tiekeSave = this.getIFilterHandler("saveFilterHandler");
        tiekeWorkSate.setNextFilter(tiekeMaster);
        tiekeMaster.setNextFilter(tiekeSave);
        tikeWorkStateHandler = tiekeWorkSate;

        IFilterHandler tiekeChannelLinkSate = this.getIFilterHandler("softLinkFitlerHandler");
        tiekeChannelLinkSate.addDataSourceListener(eventInfoListener);
        IFilterHandler tiekeChannelLinkSateSateSave = this.getIFilterHandler("saveFilterHandler");
        tiekeChannelLinkSate.setNextFilter(tiekeChannelLinkSateSateSave);
        tikeWorkChannelLinkHandler = tiekeChannelLinkSate;

        IFilterHandler tiekeVersion = this.getIFilterHandler("commonVersionFilterHandler");
        tiekeVersion.addDataSourceListener(eventInfoListener);
        IFilterHandler tiekeVersionSave = this.getIFilterHandler("saveFilterHandler");
        tiekeVersion.setNextFilter(tiekeVersionSave);
        tikeVersionHandler = tiekeVersion;


        IFilterHandler tiekeSecureLink = this.getIFilterHandler("softLinkFitlerHandler");
        tiekeSecureLink.addDataSourceListener(eventInfoListener);
        IFilterHandler tiekeSecureLinkSave = this.getIFilterHandler("saveFilterHandler");
        tiekeSecureLink.setNextFilter(tiekeSecureLinkSave);
        tikeSecureLinkHandler = tiekeSecureLink;

        //从兴
        IFilterHandler congXingStatus = this.getIFilterHandler("congXingFilterHandler");
        congXingStatus.addDataSourceListener(eventInfoListener);
        IFilterHandler congxingLinkSave = this.getIFilterHandler("saveFilterHandler");
        congXingStatus.setNextFilter(congxingLinkSave);
        congXingHandler = congXingStatus;


        xdhyHandler = this.getIFilterHandler("xinDHYFilterHandler");
        xdhyHandler.addDataSourceListener(eventInfoListener);
        IFilterHandler xdhySave = this.getIFilterHandler("saveFilterHandler");
        xdhyHandler.setNextFilter(xdhySave);

        //snmp事件信息处理
        IFilterHandler snmpIBM = this.getIFilterHandler("snmpIBMinfoFilterHnadler");
        snmpIBM.addDataSourceListener(eventInfoListener);
        IFilterHandler SnmpNetTime = this.getIFilterHandler("snmpNetTimeinfoFilterHnadler");
        SnmpNetTime.addDataSourceListener(eventInfoListener);
        snmpIBM.setNextFilter(SnmpNetTime);
        snmpHandler = snmpIBM;


        //syslog事件信息处理
        List<String> syslogHandlerList = Arrays.asList(
                "syslogPowerSupplyFilterHnadler",
                "sysloglevel3FilterHnadler",
                "syslogCiscoinfoFilterHnadler",
                "syslogHuaWeiSwitchFilterHandler",
                "syslogIBMinfoFilterHnadler",
                "syslogOtherFilterHandler"
        );

        List<String> syslogEventHandlerList = Arrays.asList(
                "syslogPowerSupplyFilterHnadler",
                "sysloglevel3FilterHnadler",
                "syslogCiscoinfoFilterHnadler",
                "syslogHuaWeiSwitchFilterHandler",
                "syslogIBMinfoFilterHnadler",
                "syslogOtherFilterHandler"
        );

        syslogHandler = createHandler(syslogHandlerList, syslogEventHandlerList, eventInfoListener);

        //MQ事件信息处理
        IFilterHandler MQ1 = this.getIFilterHandler("mqQueueFilterHandler");
        MQ1.addDataSourceListener(eventInfoListener);
        IFilterHandler MQ2 = this.getIFilterHandler("mqstatusFilterHandler");
        MQ2.addDataSourceListener(eventInfoListener);
        IFilterHandler MQ3 = this.getIFilterHandler("saveFilterHandler");
        MQ1.setNextFilter(MQ2);
        MQ2.setNextFilter(MQ3);
        mqHandler = MQ1;


        //外部日志，第三方日志
        customHandler = this.getIFilterHandler("customEventFilterHnadler");

        customHandler.addDataSourceListener(eventInfoListener);


        //-----------------以下为事件信息处理程序----------------------------------------------------------------------
        //事件配置
        IFilterHandler eventIsConfigAlarmHandler = this.getIFilterHandler("eventIsConfigAlarmHandler");
        eventIsConfigAlarmHandler.addDataSourceListener(alarmListener);
        //事件保存
        IFilterHandler eventSaveAlarmHandler = this.getIFilterHandler("eventSaveAlarmHandler");
        eventIsConfigAlarmHandler.setNextFilter(eventSaveAlarmHandler);

        eventInfoHandler = eventIsConfigAlarmHandler;


        //-----------------以下为告警信息处理程序----------------------------------------------------------------------
        //巡检处理
        IFilterHandler eventXunjianHandler = this.getIFilterHandler("eventXunjianHandler");
        IFilterHandler alarmFilterHandler = this.getIFilterHandler("alarmFilterHandler");
        eventXunjianHandler.setNextFilter(alarmFilterHandler);
        IFilterHandler alarmEventHandler = this.getIFilterHandler("alarmEventHandler");
        alarmFilterHandler.setNextFilter(alarmEventHandler);
        IFilterHandler eventXunjianAlarmHandler = this.getIFilterHandler("eventXunjianAlarmHandler");
        alarmEventHandler.setNextFilter(eventXunjianAlarmHandler);
        alarmInfoHandler = eventXunjianHandler;

    }


    public IListener getListener(String listenerName) {
        return (IListener) this.beanManager.findBean(listenerName, IListener.class);
    }

    public IFilterHandler getIFilterHandler(String filterHandlerName) {
        return (IFilterHandler) this.beanManager.findBean(filterHandlerName, IFilterHandler.class);
    }

    public IAdapter getAdapater(String name) {
        return (IAdapter) this.beanManager.findBean(name, IAdapter.class);
    }

    public IAdapter getAdapter(String adapterName) {
        return (IAdapter) this.adapters.get(adapterName);
    }

    private Map<String, IAdapter> getAdapters() {
        return (Map<String, IAdapter>) this.beanManager.findBeansAnnotated(IAdapter.class);
    }


    /**
     * @param collectAixSystemFattenEntity
     */
    public void aixSystemMsgHandlerRequest(CollectAixSystemFattenEntity collectAixSystemFattenEntity) throws Exception {
        aixSystemMsgHandler.handleRequest(collectAixSystemFattenEntity, true);

    }

    /**
     * 集群事件处理
     *
     * @param list
     */
    public void collectClusterHandlerRequest(CollectClusterEntity list) throws Exception {
        collectCluster.handleRequest(list, true);

    }

    /**
     * 连接数事件处理
     *
     * @param entity
     */
    public void connectHandlerRequest(CollectConnectEntity entity) throws Exception {
        connectHandler.handleRequest(entity, true);

    }

    /**
     * CPU数据
     *
     * @param entity
     */
    public void cpuHandlerRequest(CollectCpuEntity entity) throws Exception {
        cpuHandler.handleRequest(entity, true);
    }

    /**
     * CPU负载数据
     *
     * @param entity
     * @throws Exception
     */
    public void cpuLoadHandlerRequest(CollectCpuLoadBean entity) throws Exception {
        cpuLoadHandler.handleRequest(entity, true);
    }

    /**
     * 数据库
     *
     * @param db
     */
    public void dbHandlerRequest(CollectDBEntity db) throws Exception {
        dbHandler.handleRequest(db, true);

    }

    public void dbTableHandlerRequest(CollectTablespaceEntity db) throws Exception {
        dbTableHandler.handleRequest(db, true);

    }

    public void dbAlarmHandlerRequest(CollectDBEntity db) throws Exception {
        dbAlarmHandler.handleRequest(db, true);

    }

    /**
     * 磁盘阈值
     *
     * @param disks
     */
    public void diskHandlerRequest(CollectDiskEntity disks) throws Exception {
        diskHandler.handleRequest(disks, true);
    }


    /**
     * 端口流量阈值 普通阈值
     *
     * @param list
     */
    public void interfaceHandlerRequest(CollectInterfaceEntity list) throws Exception {
        interfaceHander.handleRequest(list, true);

    }

    public void ipmiHandlerRequest(CollectSensorEntity collectSensorEntity) throws Exception {
        ipmiHandler.handleRequest(collectSensorEntity, true);
    }

    /**
     * 内存
     *
     * @param entity
     */
    public void memoryHandlerRequest(CollectMemoryEntity entity) throws Exception {
        memoryHandler.handleRequest(entity, true);
    }

    /**
     * 网卡
     *
     * @param entity
     */
    public void networkHandlerRequest(CollectNetworkCardEntity entity) throws Exception {
        netHandler.handleRequest(entity, true);
    }

    /**
     * 光交
     *
     * @param
     */
    public void opticalHandlerRequest(OpticalSwitchEntity entity) throws Exception {
        opticalHandler.handleRequest(entity, true);
    }

    public void PCBHandlerRequest(List<CollectPcbEntity> list) throws Exception {
        pcbHandler.handleRequest(list, true);
    }

    public void portNumberHandlerRequest(CollectPortUsedNumberEntity collectPortUsedNumberEntity) throws Exception {
        portNumberHandler.handleRequest(collectPortUsedNumberEntity, true);

    }

    public void processHandlerRequest(CollectProcessEntity collectProcessEntity) throws Exception {
        processHandler.handleRequest(collectProcessEntity, true);
    }

    public void processGroupHandlerRequest(ProcessGroupEntity processGroupEntity) throws Exception {
        processGroupHandler.handleRequest(processGroupEntity, true);
    }

    public void raidInfoHandlerRequest(DiskEntity diskEntity) throws Exception {
        raidHandler.handleRequest(diskEntity, true);

    }

    public void raidBaseInfoHandlerRequest(CollectRaidSystemFattenEntity collectRaidSystemFattenEntity) throws Exception {
        raidBaseHandler.handleRequest(collectRaidSystemFattenEntity, true);

    }

    public void raidDsInfoHandlerRequest(DSEntity dsEntity) throws Exception {
        raidDsHandler.handleRequest(dsEntity, true);
    }

    public void raidDsLogHandlerRequest(RaidCommonLogEntity dsEntity) throws Exception {
        raidDsLogHandler.handleRequest(dsEntity, true);
    }

    public void raidVLogHandlerRequest(RaidCommonLogEntity dsEntity) throws Exception {
        raidVLogHandler.handleRequest(dsEntity, true);
    }

    public void raidDsBaseInfoHandlerRequest(DsSystemFattenEntity dsSystemFattenEntity) throws Exception {
        raidDsBaseHandler.handleRequest(dsSystemFattenEntity, true);
    }

    public void sensorHandlerRequest(CollectSensorEntity entity) throws Exception {
        sensorHandler.handleRequest(entity, true);
    }

    public void stationSystemTimeHandlerRequest(CollectStationSystemTimeEntity collectStationSystemTimeEntity) throws Exception {
        stationSystemTimeHandler.handleRequest(collectStationSystemTimeEntity, true);
    }

    public void systemTimeHandlerRequest(CollectSystemTimeEntity collectSystemTimeEntity) throws Exception {
        centerSystemTimeHandler.handleRequest(collectSystemTimeEntity, true);

    }

    public void vlanHandlerRequest(List<CollectVlanEntity> list) {

    }

    /**
     * 北羊事件处理
     *
     * @param itsmQueue
     */
    public void beiYangWorkStateHandlerRequest(ItsmQueueEntity itsmQueue) throws Exception {
        beiYangWorkHandler.handleRequest(itsmQueue, true);
    }

    public void beiYangVersionHandlerRequest(ItsmQueueEntity itsmQueue) throws Exception {
        beiYangVersionHandler.handleRequest(itsmQueue, true);
    }

    public void beiYangLinkHandlerRequest(ItsmQueueEntity itsmQueue) throws Exception {
        beiYangLinkHandler.handleRequest(itsmQueue, true);
    }

    /**
     * 卡斯柯业务事件处理
     *
     * @param itsmQueueReq
     */
    public void cascoLinkHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        cascoLinkHandler.handleRequest(itsmQueueReq, true);
    }

    public void cascoMasterHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        cascoMasterHandler.handleRequest(itsmQueueReq, true);

    }

    public void donghuanHandlerRequest(DongHuanEntity dongHuanEntity) throws Exception {
        donghuanHandler.handleRequest(dongHuanEntity, true);
    }

    public void cascoThreshOldHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        cascoThresholdHandler.handleRequest(itsmQueueReq, true);

    }

    public void cascoVersionHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        cascoVersionHandler.handleRequest(itsmQueueReq, true);

    }

    public void crscClockHandlerRequest(ItsmQueueEntity itsmQueueReq) {

    }

    public void crscLinkHandlerRequest(ItsmQueueEntity itsmQueueReq) {

    }

    public void crscMasterHandlerRequest(ItsmQueueEntity itsmQueueReq) {

    }

    public void crscProcessHandlerRequest(ItsmQueueEntity itsmQueueReq) {

    }

    public void crscStateHandlerRequest(ItsmQueueEntity itsmQueueReq) {

    }

    public void crscVersionHandlerRequest(ItsmQueueEntity itsmQueueReq) {

    }

    public void tiekeChannelLinkHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        tikeWorkChannelLinkHandler.handleRequest(itsmQueueReq, true);
    }

    public void tiekeSecureLinkHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        tikeSecureLinkHandler.handleRequest(itsmQueueReq, true);
    }

    public void tiekeVersionHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        tikeVersionHandler.handleRequest(itsmQueueReq, true);
    }

    public void tiekeWorkStateHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        tikeWorkStateHandler.handleRequest(itsmQueueReq, true);

    }

    public void xinDHYHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        xdhyHandler.handleRequest(itsmQueueReq, true);
    }

    /**
     * 从兴
     *
     * @param itsmQueueReq
     */
    public void congXingHandlerRequest(ItsmQueueEntity itsmQueueReq) throws Exception {
        congXingHandler.handleRequest(itsmQueueReq, true);

    }

    public void pingHandlerRequest(ReceiveAlarmEntity alarmDto) throws Exception {
        pingHandler.handleRequest(alarmDto, true);

    }

    public void pingGeneralHandlerRequest(ReceiveAlarmEntity alarmDto) throws Exception {
        pingGeneralHandler.handleRequest(alarmDto, true);

    }

    public void evntInfoHandlerRequest(IEvent info) throws Exception {
        eventInfoHandler.handleRequest(info, true);
    }


    public void alarmInfoHandlerRequest(IEvent info) throws Exception {
        alarmInfoHandler.handleRequest(info, true);

    }

    //mq信息处理
    public void mqHandlerRequest(MQMonitorEntity info) throws Exception {
        if (mqHandler != null) {
            mqHandler.handleRequest(info, true);
        }


    }

    //snmp事件处理
    public void snmpEventHandlerRequest(SnmpEventInfoEntity infoEntity) throws Exception {
        snmpHandler.handleRequest(infoEntity, true);
    }

    //syslog事件处理
    public void syslogEventHandlerRequest(SyslogEventInfoEntity infoEntity) throws Exception {
        syslogHandler.handleRequest(infoEntity, true);

    }

    //自定义事件处理
    public void customEventHandlerRequest(CustomEvent infoEntity) throws Exception {
        customHandler.handleRequest(infoEntity, true);

    }

    public void collectNodeStatus(CollectNodeEntity infoEntity) throws Exception {
        collectNodeStatusHandler.handleRequest(infoEntity, true);

    }

    /**
     * 处理BHM的cpu信息
     * @param list
     */
    public void bhmCpuHandlerRequest(List<CollectBhmCpuEntity> list) throws Exception {
        collectBhmCpuHandler.handleRequest(list, true);
    }
    /**
     * 处理BHM的风扇信息
     * @param item
     */
    public void bhmFanHandlerRequest(List<CollectBhmFanEntity> item) throws Exception {
        collectBhmFanHandler.handleRequest(item, true);
    }

    /**
     * 处理BHM的内存信息
     * @param item
     */
    public void bhmMemoryHandlerRequest(List<CollectBhmMemoryEntity> item) throws Exception {
        collectBhmMemoryHandler.handleRequest(item, true);
    }

    /**
     * 处理BHM的PCIE信息
     * @param item
     */
    public void bhmPcieHandlerRequest(List<CollectBhmPcieEntity> item) throws Exception {
        collectBhmPcieHandler.handleRequest(item, true);
    }
    /**
     * 处理BHM的电源信息
     * @param item
     */
    public void bhmPowerHandlerRequest(List<CollectBhmPowerEntity> item) throws Exception {
        collectBhmPowerHandler.handleRequest(item, true);
    }
    /**
     * 处理BHM的存储信息
     * @param item
     */
    public void bhmStorageHandlerRequest(List<CollectBhmStorageEntity> item) throws Exception {
        collectBhmStorageHandler.handleRequest(item, true);
    }
    /**
     * 处理BHM的温度信息
     * @param item
     */
    public void bhmTempHandlerRequest(List<CollectBhmTempEntity> item) throws Exception {
        collectBhmTempHandler.handleRequest(item, true);
    }
}
