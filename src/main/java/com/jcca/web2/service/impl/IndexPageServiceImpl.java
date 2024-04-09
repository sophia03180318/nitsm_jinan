package com.jcca.web2.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.quartz.warn.bean.KeepAlarmVo;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.auth.memory.TempMemory;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.websocket.WebSocketServer;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dto.DialogsAlarmListDto;
import com.jcca.web2.service.IndexPageService;
import com.jcca.web2.vo.AlarmSocketVo;
import com.jcca.web2.vo.DialogsAlarmListVo;
import com.jcca.web2.vo.OptionSocketVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @description: 大屏推送实现
 * @author: Lvyp
 * @create: 2023/11/16 10:57
 */
@Slf4j
@Service
public class IndexPageServiceImpl implements IndexPageService {

    public static final String ROOT = "root";

    @Resource
    private SysModuleConfigService configServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private AlarmInfoService alarmInfoService;

    @Override
    public void sendAlarmMsg(AlarmSocketVo vo) {
        vo.setCode(Web2Const.STATISTICS_TOP_MSG_ALARM);
        Set<String> usernames = WebSocketServer.sessionPool.keySet();
        if (CollectionUtils.isEmpty(usernames)) {
            return;
        }
        SysConfig sysConfig = configServ.getSysConfig();
        //此告警等级是否需要推送语音播报 如果传入了空的告警级别 则不在判定是否可以播放
        if (Objects.nonNull(vo.getAlarmLevel()) && !sysConfig.canPlay(vo.getAlarmLevel(), vo.getStatus(), vo.getAlarmState(), vo.getIsJcca())) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.ALARM_POP, JSONUtil.toJsonStr(vo), "当前告警等级系统配置了不推送语音");
            return;
        }
        //是否需要弹框
        if (Objects.isNull(vo.getPopup())) {
            vo.setPopup(SysConfig.YES.equals(sysConfig.getPopup()) ? 1 : 2);
        }

        for (String username : usernames) {
            //用户是否具有此告警设备的权限
            if (!ROOT.equals(username)) {
                List<String> assetIds = assetServ.listIdByUserNameV2(username, 1);
                if (Objects.isNull(assetIds) || !assetIds.contains(vo.getAssetId())) {
                    continue;
                }
            }
            WebSocketServer.sendMessage(username, JSONUtil.toJsonStr(vo));
            AppLogUtils.buildLogDebug(LogFunctionEnum.ALARM_POP, JSONUtil.toJsonStr(vo), "用户" + username + "推送成功");
            TempMemory.USER_ID_MAP_V2.put(username, username);
        }

    }

    @Override
    public void sendAlarmMsgV1(AlarmSocketVo vo) {
        Set<String> usernames = WebSocketServer.sessionPool.keySet();
        if (CollectionUtils.isEmpty(usernames)) {
            return;
        }
        SysConfig sysConfig = configServ.getSysConfig();

        //此告警等级是否需要推送语音播报 如果传入了空的告警级别 则不在判定是否可以播放
        if (Objects.nonNull(vo.getAlarmLevel()) && !sysConfig.canPlay(vo.getAlarmLevel(), vo.getStatus(), vo.getAlarmState(), vo.getIsJcca())) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.ALARM_POP, JSONUtil.toJsonStr(vo), "当前告警等级系统配置了不推送语音");
            return;
        }
        KeepAlarmVo keepAlarmVo = new KeepAlarmVo();
        //设置ws类型
        keepAlarmVo.setWsType("1");
        //告警弹框控制的只是新告警弹出不弹出  默认不打开弹窗
        keepAlarmVo.setPopup(true);
        //是否需要弹框
        if (Objects.isNull(vo.getPopup())) {
            keepAlarmVo.setPopup(SysConfig.YES.equals(sysConfig.getPopup()));
        } else {
            keepAlarmVo.setPopup(vo.getPopup() == 1);
        }
        for (String username : usernames) {
            //用户是否具有此告警设备的权限
            if (!ROOT.equals(username)) {
                List<String> assetIds = assetServ.listIdByUserNameV2(username, 1);
                if (Objects.isNull(assetIds) || !assetIds.contains(vo.getAssetId())) {
                    continue;
                }
            }

            DialogsAlarmListDto query = new DialogsAlarmListDto();
            if (SysConfig.YES.equals(sysConfig.getAffirmStatus())) {
                query.setStatus(AlarmStatusEnum.UNCONFIRM.getCode().intValue());
            }
            if (SysConfig.YES.equals(sysConfig.getRecoveredStatus())) {
                query.setAlarmState(AlarmStateEnum.ALARM.getCode().intValue());
            }
            query.setShowJcca(SysConfig.YES.equals(sysConfig.getShowJcca()) ? 1 : 2);
            query.setStatusLogical(1);
            query.setBlank(AlarmBlankConst.NORMARL);
            query.setUserName(username);
            query.setAlarmLevelList(sysConfig.getConfigAlarmLevelList());
            if (StrUtil.isNotEmpty(vo.getAssetId())) {
                query.setAssetId(vo.getAssetId());
            }

            List<DialogsAlarmListVo> dialogsAlarmListVos = alarmInfoService.queryDialogsVoListV2(query);

            if (dialogsAlarmListVos.isEmpty()) {
                continue;
            }

            List<AlarmInfo> copyList = new ArrayList<AlarmInfo>();
            for (DialogsAlarmListVo item : dialogsAlarmListVos) {
                AlarmInfo copy = EntityBeanUtil.copy(vo, AlarmInfo.class);
                copy.setId(item.getAlarmId());
                copy.setOccurTime(item.getOccurTime());
                copy.setCreateTime(item.getOccurTime());
                copy.setAlarmLevel(item.getAlarmLevel());
                copyList.add(copy);
            }
            //放置告警列表
            keepAlarmVo.setAlarmInfoList(copyList);
            // 赋予系统中的告警数量
            keepAlarmVo.setCount(copyList.size());
            WebSocketServer.sendMessage(username, JSONUtil.toJsonStr(keepAlarmVo));
            if (LogInputUtils.inputInfo(ServerTypeEnum.JOB_QUARTZ)) {
                log.info("向" + username + "推送" + keepAlarmVo.getCount() + "条告警~   弹窗:" + keepAlarmVo.isPopup());
            }
            TempMemory.USER_ID_MAP_V1.put(username, username);
        }
    }

    @Override
    public void sendNotify(OptionSocketVo vo) {
        Set<String> usernames = TempMemory.USERNAME_SET;
        if (CollectionUtils.isEmpty(usernames)) {
            return;
        }
        for (String username : usernames) {
            WebSocketServer.sendMessage(username, JSONUtil.toJsonStr(vo));
        }
    }

}
