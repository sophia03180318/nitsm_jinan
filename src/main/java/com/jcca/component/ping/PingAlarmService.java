package com.jcca.component.ping;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.other.AlarmAdapter;
import com.jcca.component.other.bean.PingAssetStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @ClassName PingAlarmService
 * @Description PING告警处理
 * @Date 2020/6/22 14:32
 * @Author hanwone
 */
//@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class
PingAlarmService implements AlarmAdapter {

    @Autowired
    private GroupPingAlarmService groupPingAlarmService;
    @Autowired
    private NoGroupPingAlarmService noGroupPingAlarmService;


    @Override
    public String getCode() {
        return ReceiveAlarmTypeEnum.PING.getCode();
    }

    @Override
    public void handle(ReceiveAlarmDto alarmData) {
        log.info("【PING告警】：{}", JSONUtil.toJsonStr(alarmData));

        String content = alarmData.getContent();

        Boolean isGroup = false;
        if (StrUtil.isNotEmpty(content) && JSONUtil.isJsonArray(content)) {
            List<PingAssetStatus> pingAssetStatusList = JSONUtil.toList(JSONUtil.parseArray(content),
                    PingAssetStatus.class);
            if (Objects.nonNull(pingAssetStatusList) && pingAssetStatusList.size() > 1) {
                isGroup = true;
            } else if (Objects.nonNull(pingAssetStatusList) && pingAssetStatusList.size() == 1) {
                //组形式回来的单机ping
                PingAssetStatus pingAssetStatus = pingAssetStatusList.get(0);
                alarmData.setAssetIp(pingAssetStatus.getAsset().getIp());
                alarmData.setFlag(pingAssetStatus.getCurrStatus());
            }
        }

        // 非组ping服务的告警ip不为空,组ping服务的告警ip为空
        if (StrUtil.isEmpty(alarmData.getAssetIp()) && isGroup) {
            groupPingAlarmService.handle(alarmData);
        } else {
            noGroupPingAlarmService.handle(alarmData);
        }
    }

}
