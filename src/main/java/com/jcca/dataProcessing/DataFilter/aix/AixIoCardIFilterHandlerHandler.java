package com.jcca.dataProcessing.DataFilter.aix;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.AixIoCardEntity;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectAixSystemFattenEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * IFilterHandler处理流程作为数据处理类
 * IFilterHandler-------》IFilterHandler-------》IFilterHandler-------》支持组合式处理。
 * handler返回true，则到下一个IFilterHandler处理，返回false则无需下一个IFilterHandler进行处理
 * 如果有事件变动 ，使用this.dispatureEvent(event)将事件抛出，有其他监听器进行处理
 */

/**
 * @author Zhaozheng
 * @description TODO 小型机基础信息过滤处理类
 * @className AixSystemMsgIFilterHandlerHandler
 * @date 2023/10/27 9:21
 * @since 2.1.0.0
 */
@Slf4j
@Component("aixIoCardIFilterHandlerHandler")
public class AixIoCardIFilterHandlerHandler extends IFilterHandler<CollectAixSystemFattenEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectAixSystemFattenEntity info) {

        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "小型机IO卡过滤处理类", info.getAssetIp());

        List<AixIoCardEntity> beanList = JSONUtil.toList(new JSONArray(info.getIoCard()), AixIoCardEntity.class);
        beanList.size();
        for (int i = 0; i < beanList.size(); i++) {

            AixIoCardEntity aixIoCardEntity = beanList.get(i);
            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_aix_io.getCode() + ":" + aixIoCardEntity.getAdapterName();

            String mapKey = StatusInfoChangeTypeEnum.status_aix_type.getCode();
            String mapKey1 = StatusInfoChangeTypeEnum.status_aix_state.getCode();
            String mapKey2 = StatusInfoChangeTypeEnum.status_aix_attention_state.getCode();
            String mapKey3 = StatusInfoChangeTypeEnum.status_aix_adapterSlot.getCode();
            String mapKey4 = StatusInfoChangeTypeEnum.status_aix_io_description.getCode();
            String mapKey5 = StatusInfoChangeTypeEnum.status_aix_io_wwn.getCode();
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, aixIoCardEntity.getAdapterType());
            if (flag) {
                ChangeInfo changeInfo = this.createChangeInfo(aixIoCardEntity.getAdapterType(), redisKey, mapKey);
                info.getMaps().put(aixIoCardEntity.getAdapterName() + "_" + mapKey, changeInfo);
            }
            boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1, aixIoCardEntity.getAdapterStat());
            if (flag1) {
                ChangeInfo changeInfo = this.createChangeInfo(aixIoCardEntity.getAdapterStat(), redisKey, mapKey1);
                info.getMaps().put(aixIoCardEntity.getAdapterName() + "_" + mapKey1, changeInfo);
            }
            boolean flag2 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey2, aixIoCardEntity.getAttentionType());
            if (flag2) {
                ChangeInfo changeInfo = this.createChangeInfo(aixIoCardEntity.getAttentionType(), redisKey, mapKey2);
                info.getMaps().put(aixIoCardEntity.getAdapterName() + "_" + mapKey2, changeInfo);
            }

            boolean flag3 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey3, aixIoCardEntity.getAdapterSlot());
            if (flag3) {
                ChangeInfo changeInfo = this.createChangeInfo(aixIoCardEntity.getAdapterSlot(), redisKey, mapKey3);
                info.getMaps().put(aixIoCardEntity.getAdapterName() + "_" + mapKey3, changeInfo);
            }
            boolean flag4 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey4, aixIoCardEntity.getDescription());
            if (flag4) {
                ChangeInfo changeInfo = this.createChangeInfo(aixIoCardEntity.getDescription(), redisKey, mapKey4);
                info.getMaps().put(aixIoCardEntity.getAdapterName() + "_" + mapKey4, changeInfo);
            }
            boolean flag5 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey5, aixIoCardEntity.getFcWwn());
            if (flag5) {
                ChangeInfo changeInfo = this.createChangeInfo(aixIoCardEntity.getFcWwn(), redisKey, mapKey5);
                info.getMaps().put(aixIoCardEntity.getAdapterName() + "_" + mapKey5, changeInfo);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }



    private ChangeInfo createChangeInfo(Object value, String redisKey, String mapKey) {
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(value);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        return changeInfo;
    }

}
