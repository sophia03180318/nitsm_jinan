package com.jcca.component.quartz.dh;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.utils.MyIdUtil;
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
    @Resource
    private SysModuleConfigService sysModuleConfigService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        SysModuleConfig config = sysModuleConfigService.getSysModuleConfig("config:dongHuan");
        if (Objects.isNull(config)) {
            SysModuleConfig config1 = new SysModuleConfig();
            config1.setId(MyIdUtil.getId());
            config1.setName("config:dongHuan");
            config1.setValue("open");
            config1.setDescription("open：开启动环消息接收  close：关闭动环消息接收");
            config1.setOrgId("0");
            config1.setWebConf("{\"title\":\"动环消息\",\"type\":\"radio\",\"radioVo\": [{\"name\":\"接收\",\"value\":\"open\"},{\"name\":\"不接收\",\"value\":\"close\"}]}");
            config1.setServiceType(3);
            sysModuleConfigService.save(config1);
            config = config1;
        }
        if (!"open".equals(config.getValue())) {
            return;
        }

        List<Alarm> alarmLists = this.alarmService.getAssetAlarm();
        int size = 20;
        if (alarmLists.size() < 20) {
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
                            dongHuanEntity.setOriginalMsg(alarm.getDescc());
                            dongHuanEntity.setAssetName(asset.getAssetName());
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


    private  String parseDataId(String idStr) {
        // 将字符串转换为 long
        long id = Long.parseLong(idStr);

        long AA = (id >> 27) & 0x1F;   // CSC 内 LSC 编号（5 位）
        long BBB = (id >> 17) & 0x3FF;  // LSC 内站点编号（10 位）
        long CC = (id >> 11) & 0x3F;   // 站内对象编号（6 位）
        long DDD = id & 0x7FF;          // 对象下数据点编号（11 位）

        return String.format("%d.%d.%d.%d", AA, BBB, CC, DDD);
    }


}



