package com.jcca.component.process;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.other.AlarmAdapter;
import com.jcca.component.process.bean.ProcessAlarmQueueBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 处理进程状态告警
 *
 * @author lyp
 */
@Slf4j
//@Service
public class ProcessAlarmAdapterImpl implements AlarmAdapter {

    @Resource
    private ProcessAlarmService processAlarmServ;
    @Resource
    private AssetService assetServ;

    @Override
    public String getCode() {

        return ReceiveAlarmTypeEnum.PROCESS.getCode();
    }

    @Override
    public void handle(ReceiveAlarmDto alarmDto) {
        log.info("【Process进程状态告警】：{}", JSONUtil.toJsonStr(alarmDto));
        String content = alarmDto.getContent();
        List<ProcessAlarmQueueBean> queueObj = JSONUtil.toList(JSONUtil.parseArray(content),
                ProcessAlarmQueueBean.class);

        if (Objects.isNull(queueObj) || queueObj.size() == 0) {
            log.error("进程告警处理ERROR-->空的进程参数 IP：{}", alarmDto.getAssetIp());
            return;
        }

        for (ProcessAlarmQueueBean processAlarmQueueBean : queueObj) {
            String assetIp = processAlarmQueueBean.getAssetIp();

            Asset asset = assetServ.findOneByIp(assetIp);

            if (Objects.isNull(asset)) {
                log.error("进程告警处理ERROR-->资产不存在IP：{}", alarmDto.getAssetIp());
                return;
            }
            if (AssetWatchStatusEnum.WATCH_STATUS_YES.getCode() != asset.getWatch()) {
                log.error("进程告警处理ERROR-->：非监控资产 IP：{}", asset.getIp());
                return;
            }

            processAlarmQueueBean.setAsset(asset);
        }

        String occurTime = alarmDto.getOccurTime();
        Date now = new Date();
        now.setTime(Long.parseLong(occurTime));

        String assetIp = alarmDto.getAssetIp();
        if (StrUtil.isNotEmpty(assetIp)) {
            // 无分组
            processAlarmServ.disposeNoGroupAlarm(queueObj.get(0), now);
        } else {
            // 分组
            processAlarmServ.disposeGroupAlarm(queueObj, now);
        }

    }

}
