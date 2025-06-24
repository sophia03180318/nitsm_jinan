package com.jcca.component.quartz.dh;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.dataProcessing.Entity.DongHuanEntity;
import com.jcca.dataProcessing.dataAdpater.DongHuanAdapter;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetMsgVo;
import com.jcca.web.common.entity.Alarm;
import com.jcca.web.common.service.DhAlarmService;
import lombok.extern.log4j.Log4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 动环状态
 *
 * @author sophia
 */
@Log4j
@Service
@DisallowConcurrentExecution
public class QuartzDhStatusJob extends QuartzJobBean {
    @Resource
    private DataProcessManager dataProcessManager;
    @Resource
    private DhAlarmService alarmService;
    @Resource
    private AssetService assetServ;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        QueryWrapper<Alarm> qw = new QueryWrapper();
        qw.orderByDesc("OCCURRENCE_TIME");
        qw.likeRight("DEVICE_ID", "1");
        List<Alarm> alarmLists = this.alarmService.list(qw);
        int size = 10;
        if (alarmLists.size() < 10) {
            size = alarmLists.size();
        }
        for (int i = 0; i < size; ++i) {
            Alarm alarm = (Alarm) alarmLists.get(i);
            if (alarm.getLevell() != 318) {
                if (ObjectUtil.isNotNull(alarm.getDeviceId())) {
                    AssetMsgVo asset = this.assetServ.findMsgById(alarm.getDeviceId());
                    if (Objects.isNull(asset)) {
                        log.error("动环告警收到未录入数据，资产ID不存在：" + JSONUtil.toJsonStr(alarm));
                    } else {
                        try {
                            DongHuanEntity dongHuanEntity = new DongHuanEntity();
                            dongHuanEntity.setAssetId(alarm.getDeviceId());
                            dongHuanEntity.setFlag(alarm.getAlarmId());
                            dongHuanEntity.setCreateTime(alarm.getOccurrenceTime());
                            dongHuanEntity.setOriginalMsg("动环告警: " + alarm.getDescc());
                            dongHuanEntity.setAssetName(asset.getAssetName());
                            log.info("动环推送告警: " + JSONUtil.toJsonStr(dongHuanEntity));
                            DongHuanAdapter dhAdapter = (DongHuanAdapter) this.dataProcessManager.getAdapater("dongHuanAdapter");
                            dhAdapter.dispose(dongHuanEntity);
                        } catch (Exception e2) {
                            log.error("接收动环推送设备告警失败: " + e2.toString());
                        }
                    }
                }

                alarm.setLevell(318);
                this.alarmService.updateById(alarm);
            }
        }
    }
}



