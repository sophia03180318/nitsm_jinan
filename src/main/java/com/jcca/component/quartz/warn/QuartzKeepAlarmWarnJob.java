package com.jcca.component.quartz.warn;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.quartz.warn.bean.KeepAlarmVo;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.auth.memory.TempMemory;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.websocket.WebSocketServer;
import com.jcca.web2.dto.DialogsAlarmListDto;
import com.jcca.web2.service.IndexPageService;
import com.jcca.web2.vo.AlarmSocketVo;
import com.jcca.web2.vo.DialogsAlarmListVo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 保持告警持续向前台推送
 *
 * @author 张雅惠
 */
@Slf4j
@Service
@DisallowConcurrentExecution
public class QuartzKeepAlarmWarnJob extends QuartzJobBean {

    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private SysModuleConfigService configService;
    @Resource
    private IndexPageService indexPageServ;
    @Resource
    private AssetService assetService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (LogInputUtils.inputInfo(ServerTypeEnum.JOB_QUARTZ)) {
            log.info("定时任务-告警管理中查看全局配置~");
        }
        SysConfig sysConfig = configService.getSysConfig();

        if (!sysConfig.canPoup()) {
            return;
        }

        for (String username : WebSocketServer.sessionPool.keySet()) {
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
            List<DialogsAlarmListVo> dialogsAlarmListVos = alarmInfoService.queryDialogsVoListV2(query);

            if (dialogsAlarmListVos.isEmpty()) {
                continue;
            }

            List<AlarmInfo> copyList = new ArrayList<AlarmInfo>();
            for (DialogsAlarmListVo vo : dialogsAlarmListVos) {
                AlarmInfo copy = EntityBeanUtil.copy(vo, AlarmInfo.class);
                copy.setId(vo.getAlarmId());
                copy.setOccurTime(vo.getOccurTime());
                copy.setCreateTime(vo.getOccurTime());
                copy.setAlarmLevel(vo.getAlarmLevel());
                copyList.add(copy);
            }

            //V1的告警数据
            KeepAlarmVo keepAlarmVo = new KeepAlarmVo();
            //设置ws类型
            keepAlarmVo.setWsType("1");
            //放置告警列表
            keepAlarmVo.setAlarmInfoList(copyList);
            // 赋予系统中的告警数量
            keepAlarmVo.setCount(copyList.size());
            //告警弹框控制的只是新告警弹出不弹出  默认不打开弹窗
            keepAlarmVo.setPopup(false);

            String userIdV1 = TempMemory.USER_ID_MAP_V1.get(username);
            String userIdV2 = TempMemory.USER_ID_MAP_V2.get(username);

            //已经弹出过新告警提示
            if (StrUtil.isNotEmpty(userIdV1)) {
                if (SysConfig.YES.equals(sysConfig.getContinuous())) {
                    //开启连续播报
                    WebSocketServer.sendMessage(username, JSONUtil.toJsonStr(keepAlarmVo));
                    if (LogInputUtils.inputInfo(ServerTypeEnum.JOB_QUARTZ)) {
                        log.info("向" + username + "推送" + keepAlarmVo.getCount() + "条告警~   弹窗:" + keepAlarmVo.isPopup());
                    }
                }
            } else {
                //未弹出过新告警提示
                keepAlarmVo.setPopup(true);
                WebSocketServer.sendMessage(username, JSONUtil.toJsonStr(keepAlarmVo));
                if (LogInputUtils.inputInfo(ServerTypeEnum.JOB_QUARTZ)) {
                    log.info("向" + username + "推送" + keepAlarmVo.getCount() + "条告警~   弹窗:" + keepAlarmVo.isPopup());
                }
                TempMemory.USER_ID_MAP_V1.put(username, username);
            }

            if (StrUtil.isNotEmpty(userIdV2)) {
                if (SysConfig.YES.equals(sysConfig.getContinuous())) {
                    sendToV2(dialogsAlarmListVos.get(0), 2, 2);
                }
            }else{
                //判定告警中包含未确认并且未恢复的时候才认为是新的
                List<DialogsAlarmListVo> haveNew = dialogsAlarmListVos.stream().filter(item -> (item.getStatus().intValue() == AlarmStatusEnum.UNCONFIRM.getCode().intValue()
                        && item.getAlarmState().intValue() == AlarmStateEnum.ALARM.getCode().intValue())).collect(Collectors.toList());
                sendToV2(dialogsAlarmListVos.get(0), null, haveNew.isEmpty() ? 2 : 1);

                TempMemory.USER_ID_MAP_V2.put(username, username);
            }
        }
    }

    /**
     * sendToV2
     * 向2.0大屏推送
     *
     * @param alarmInfo
     * @author Lvyp
     */
    private void sendToV2(DialogsAlarmListVo alarmInfo, Integer popUp, Integer newPopup) {
        //lvyp 向2.0大屏推送
        Asset asset = assetService.getById(alarmInfo.getAssetId());
        AlarmSocketVo vo = new AlarmSocketVo();
        vo.setPopup(popUp);
        vo.setNewAlarm(newPopup);
        vo.setAlarmLevel(alarmInfo.getAlarmLevel().intValue());
        vo.setAlarmTitle(alarmInfo.getTitle());
        vo.setStatus(alarmInfo.getStatus().byteValue());
        vo.setAlarmState(alarmInfo.getAlarmState().byteValue());
        vo.setAssetId(asset.getId());
        vo.setAssetName(asset.getName());
        vo.setIsJcca(asset.isJccaAsset());
        indexPageServ.sendAlarmMsg(vo);
    }

}
