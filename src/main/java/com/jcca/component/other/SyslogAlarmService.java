package com.jcca.component.other;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppPattenUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.thresholds.impl.DisposeInterfaceAdapterImpl;
import com.jcca.web.alarm.dao.AlarmRepositoryMapper;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import com.jcca.web.collect.entity.CollectRoute;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName SyslogAlarmService
 * @Description syslog告警处理
 * @Date 2020/6/22 14:34
 * @Author hanwone
 */
@Service
@Slf4j
public class SyslogAlarmService implements AlarmAdapter {

    public static final String SYS_LOG_FLAG = "THIS_IS_SYSLOG_EVENT";

    @Resource
    private AssetService assetService;
    @Resource
    private AlarmRepositoryMapper alarmRepositoryMapper;
    @Resource
    private TopoAssetPortService topoAssetPortService;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private RedisService redisService;
    @Resource
    private CollectRouteService collectRouteService;
    @Resource
    private AssetHidConfService hidConfServ;

    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.SYSLOG.getCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(ReceiveAlarmDto alarmData) {
        log.info("-------------------处理Syslog告警-------------------");
        Asset asset = assetService.getOneByAllIp(alarmData.getAssetIp());
        if (Objects.isNull(asset)) {
            log.error("IP为[{}]的资产不存在", alarmData.getAssetIp());
            return;
        }

        if (AssetWatchStatusEnum.WATCH_STATUS_NO.getCode() == asset.getWatch()) {
            log.error("IP为[{}]的资产不监控，不接收SYSLOG", alarmData.getAssetIp());
            return;
        }

        Integer manufacturerId = asset.getManufacturerId();
        Integer assetMode = asset.getAssetMode();

        String registerKey = String.format("config:syslog:register:%s:%s", manufacturerId, assetMode);
        String syslogConfig = configService.getSyslogConfig(registerKey, assetMode);

        String assetId = asset.getId();

        String eventJson = alarmData.getContent();
        JSONObject event = JSONUtil.parseObj(eventJson);

        String content = AppPattenUtils.removeDateStr(event.getStr("message"));

        CreateEventReq eventReq = new CreateEventReq();
        eventReq.setAssetId(assetId);
        String occurTime = alarmData.getOccurTime();
        DateTime parse = DateUtil.parse(occurTime, "yyyy-MM-dd HH:mm:ss");
        eventReq.setCreateTime(parse.toJdkDate());
        eventReq.setEventLevel(EventLevelEnum.NOTIFY.getCode());
        eventReq.setOriginalMsg(event.getStr("message"));

        String eventUniqueCode = content;

        // 正则匹配
        Pattern pattern = Pattern.compile(syslogConfig);
        Matcher matcher = pattern.matcher(content);
        // 端口正则
        boolean find = matcher.find();

        List<Integer> netAssetModeList = Arrays.asList(42, 201);
        if (netAssetModeList.contains(assetMode) && find) {
            eventUniqueCode = formatCode(matcher.group(0), "SWITCH");

            String portStr = "GigabitEthernet[0-9]*/[0-9]*(/[0-9])*\\w*";
            Pattern portPattern = Pattern.compile(portStr);
            Matcher portMatcher = portPattern.matcher(content);
            if (portMatcher.find()) {
                String portUp = "to up";
                String portDown = "to down";

                String portIndex = portMatcher.group(0);

                QueryWrapper<CollectRoute> collectRouteQueryWrapper = new QueryWrapper<>();
                collectRouteQueryWrapper.eq("ASSET_ID", assetId);
                collectRouteQueryWrapper.eq("PORT_INDEX_NAME", portIndex);
                List<CollectRoute> list = collectRouteService.list(collectRouteQueryWrapper);

                eventReq.setBaseValue("未识别设备");
                if (!list.isEmpty()){
                    String atNetAddress = list.get(0).getAtNetAddress();
                    if(StrUtil.isEmpty(atNetAddress)){
                        if(StrUtil.isNotEmpty(list.get(0).getAtAssetId())){
                            Asset atAsset = assetService.getById(list.get(0).getAtAssetId());
                            eventReq.setBaseValue(atAsset.getName()+":"+atAsset.getIp());
                        }
                    }else{
                        eventReq.setBaseValue(list.get(0).getAtNetAddress());
                    }
                }

                String cacheKey = DisposeInterfaceAdapterImpl.CACHE_KEY_STATUS + assetId + ":" + portIndex;
                if (content.contains(portUp)) {
                    // 删除端口处理时候的key 解决配置为同一个事件类型的时候不配置SYSLOG 告警先后顺序导致不恢复的问题
                    redisService.remove(cacheKey);

                    eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                    int status = InterfaceStatus.OK.getCode();
                    log.info("syslog匹配端口名称【{}】,状态【{}】", portIndex, status);
                    topoAssetPortService.updatePortStatus(assetId, portIndex, status);
                }
                if (content.contains(portDown)) {
                    // 删除端口处理时候的key 解决配置为同一个事件类型的时候不配置SYSLOG 告警先后顺序导致不恢复的问题
                    redisService.remove(cacheKey);

                    eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                    int status = InterfaceStatus.NO.getCode();
                    log.info("syslog匹配端口名称【{}】,状态【{}】", portIndex, status);
                    topoAssetPortService.updatePortStatus(assetId, portIndex, status);
                }

                eventReq.setGroupFlag(EventGroupConstant.INTERFACES_UP_DOWN);
                String portName = portIndex.replace("gabitEthernet", "");
                List<AssetHidConf> flagListByAsset = hidConfServ.getFlagListByAsset(assetId, AssetHidConf.TypeEnum.PORT.name());
                for (AssetHidConf assetHidConf : flagListByAsset) {
                    if (assetHidConf.getFlag().contains(portName)){
                        return;
                    }
                }
                eventReq.setFlag(portName);
            }

        } else if (matcher.find()) {
            eventUniqueCode = formatCode(matcher.group(0), "SERVER");
        }

        eventReq.setUniqueCode(eventUniqueCode);
        if (StrUtil.isEmpty(eventReq.getFlag())) {
            eventReq.setFlag(eventUniqueCode);
        }

        List<AlarmRepository> repos = alarmRepositoryMapper.selectOrgMsgContainStatusFlg(eventUniqueCode, content);

        if (Objects.nonNull(repos) && repos.size() > 0) {
            eventReq.setRepoMsg(repos.get(0).getDescStr());
        }else{
            //没有匹配到任何知识库 使用默认的匹配码去匹配知识库
            eventReq.setUniqueCode(SYS_LOG_FLAG);
        }

        try {
            if (StrUtil.isEmpty(eventReq.getGroupFlag())) {
                eventReq.setGroupFlag(MyIdUtil.getId());
            }

            eventReq.setRemark(SYS_LOG_FLAG);
            eventLogicServ.addEvent(eventReq);
        } catch (Exception e) {
            log.error("【SYSLOG添加事件处理失败】：" + e.getMessage(), e);
        }

    }

    /**
     * @param code
     * @param type SERVER 服务器 SWITCH 交换机
     * @return
     */
    private String formatCode(String code, String type) {
        code = code.trim();
        if ("SERVER".equals(type)) {
            String newCode = code.replace("Event ID", "");
            if (newCode.startsWith(":")) {
                return newCode.replaceFirst(":", "").trim();
            } else if (newCode.startsWith("：")) {
                return newCode.replaceFirst("：", "").trim();
            }
        } else if ("SWITCH".equals(type)) {
            if (code.endsWith(":") || code.endsWith("：")) {
                return code.substring(0, code.length() - 1);
            }
        }

        return code;
    }

}
