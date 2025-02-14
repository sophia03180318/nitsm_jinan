package com.jcca.dataProcessing.DataFilter.net;

import cn.hutool.core.util.StrUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectNetworkCardEntity;
import com.jcca.dataProcessing.enums.StatusEnum;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 1:通、2：断、3：在测试模式下、4：未知、5：休眠 6：缺少组件 7：DOWN_DUE_TO_STATE_OF 由于状态而向下
 *
 * @author Zhaozheng
 * @description TODO 网卡状态信息过滤处理类
 * @className NetStateFilterHandler
 * @date 2023/10/27 9:45
 * @since 2.1.0.0
 */
@Component("netStateFilterHandler")
public class NetStateFilterHandler extends IFilterHandler<CollectNetworkCardEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private AssetLinkAssetService assetLinkAssetServ;
    @Autowired
    private AssetService assetService;

    @Override
    public boolean handler(CollectNetworkCardEntity info) {
        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_net_status.getCode());
        String eventRedisKey = StatusInfoChangeTypeEnum.event_net_state.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();

        Integer status = StatusEnum.status_net_2.getCode().equals(info.getStatus().toString()) ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
        Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(changeInfo.getRedisKey(), changeInfo.getMapKey(), changeInfo.getValue());
        if (Objects.isNull(flag) && status.equals(EventLevelEnum.ABNORMAL.getCode())) {
            return true;
        }
        //变动了
        if (Objects.isNull(flag) || flag) {

            //判断网卡A/B网
            Asset asset = assetService.getById(info.getAssetId());
            String name = "";
            if (StrUtil.isNotEmpty(info.getIp())) {
                if (info.getIp().equals(asset.getIp())) {
                    name = "【A网】";
                } else if (info.getIp().equals(asset.getIp2())) {
                    name = "【B网】";
                }
            }
            String descStr = name + String.format(StatusInfoChangeTypeEnum.event_net_state.getDescr(), info.getName() + "【" + info.getIp() + "】");
            AlarmTempReq tempReq = new AlarmTempReq();
            AssetLinkAsset linkAsset = assetLinkAssetServ.findAssetByLinkAsset(info.getAssetId(), info.getIp());
            if (Objects.nonNull(linkAsset)) {
                tempReq.setLinkAssetIp(linkAsset.getLinkAssetIp());
                tempReq.setLinkAssetName(linkAsset.getLinkAssetName());
                Asset intAsset = assetService.getById(linkAsset.getAssetId());
                descStr = descStr + "【" + info.getIp() + "】,对端设备【" + intAsset.getName() + "】,对端设备IP【" + intAsset.getIp() + "】";
            }
            tempReq.setOrgMsg(descStr);
            tempReq.setCollectValue(info.getStatus().toString());
            tempReq.setFlag("网卡");

            this.addEventStatus(StatusInfoChangeTypeEnum.event_net_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, tempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(descStr);
                this.dispatureEvent(event);
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
