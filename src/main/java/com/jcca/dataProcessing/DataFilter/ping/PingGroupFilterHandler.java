package com.jcca.dataProcessing.DataFilter.ping;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.other.bean.PingAssetStatus;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ReceiveAlarmEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 组ping模式
 * @className pingGroupFilterHandler
 * @date 2023/10/27 9:51
 * @since 2.1.0.0
 */
@Component("pingGroupFilterHandler")
public class PingGroupFilterHandler extends IFilterHandler<ReceiveAlarmEntity> {

    private static final Integer MIN_GROUP_SIZE = 2;

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public boolean handler(ReceiveAlarmEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "组ping模式过滤", info.getAssetIp());
        String content = info.getContent();
        if (StrUtil.isEmpty(content) || !JSONUtil.isJsonArray(content)) {
            //异常数据
            return true;
        }
        List<PingAssetStatus> pingAssetStatusList = JSONUtil.toList(JSONUtil.parseArray(content),
                PingAssetStatus.class);
        if (Objects.isNull(pingAssetStatusList) || pingAssetStatusList.size() < MIN_GROUP_SIZE) {
            //单机
            return true;
        }
        if (StrUtil.isNotEmpty(info.getAssetIp())) {
            //单机
            return true;
        }


        for (PingAssetStatus item : pingAssetStatusList) {
            String redisKey = item.getAsset().getIp() + ":" + item.getAsset().getId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
            String mapKey = StatusInfoChangeTypeEnum.group_single_status_ping.getCode();
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getFlag());
            if (flag) {//将ping信息
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(item.getCurrStatus());
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date());
                info.getMaps().put(mapKey + "_" + item.getAsset().getIp(), changeInfo);
            }
        }
        return true;
    }


    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
