package com.jcca.dataProcessing.DataFilter.netInterface;

import cn.hutool.core.util.StrUtil;
import com.jcca.admin.system.entity.TopoAssetPort;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectInterfaceEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 端口通断过滤处理类
 * @className InterfaceUpDownFilterHandler
 * @date 2023/10/27 9:36
 * @since 2.1.0.0
 */
@Component("interfaceUpDownFilterHandler")
public class InterfaceUpDownFilterHandler extends IFilterHandler<CollectInterfaceEntity> {


    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private AssetLinkAssetService assetLinkAssetServ;
    @Resource
    private TopoAssetPortService topoAssetPortService;

    @Override
    public boolean handler(CollectInterfaceEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_interface_up_down.getCode();
        String mapKey = info.getPortName();

        //过滤一下需要忽略的状态
        if(InterfaceStatus.needIgnore(info.getStatus())){
            return true;
        }

        Boolean compare = InterfaceStatus.isUp(info.getStatus());
        Integer status = compare ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();

        int portStatus = 1;
        if (status.equals(EventLevelEnum.ABNORMAL.getCode())) {
            portStatus = 2;
        }
        // 更新拓扑图端口状态
        if (Arrays.asList(6, 18, 22, 23, 39, 65).contains(info.getPortType())) {
            TopoAssetPort assetPort = topoAssetPortService.findAssetPort(info.getAssetId(), info.getPortIndex());
            if (Objects.nonNull(assetPort) && assetPort.getStatus().intValue() != status){
                topoAssetPortService.updatePortStatus(info.getAssetId(), info.getPortIndex(), portStatus);
            }
        }

        Boolean flag = eventInfoChangeManagerService.infoIschangeFirst(info.getInspectRecordId(),redisKey, mapKey, info.getStatus());

        if (Objects.isNull(flag) && status.equals(EventLevelEnum.ABNORMAL.getCode())) {
            return true;
        }

        if (Objects.isNull(flag) || flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getStatus());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey, changeInfo);
            this.addEventStatus(StatusInfoChangeTypeEnum.event_port_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), info.getPortName(), status, info, changeInfo);

            String eventRedisKey = StatusInfoChangeTypeEnum.event_port_state.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getPortName();


            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setCollectValue(status + "");
            tempReq.setFlag(info.getPortName());

            AssetLinkAsset linkAssetByAsset = null;
            String statusStr="端口恢复";
            //如果为异常事件，查询对端设备
            if (portStatus == 2) {
                statusStr="端口断开";
                linkAssetByAsset = assetLinkAssetServ.findLinkAssetByAsset(info.getAssetId(), info.getPortIndex());
            }
            //如果对端设备不为空
            String descStr = String.format(StatusInfoChangeTypeEnum.event_port_state.getDescr(), info.getPortName(),statusStr);
            if (linkAssetByAsset != null) {
                if (StrUtil.isNotEmpty(linkAssetByAsset.getLinkAssetName())) {
                    tempReq.setLinkAssetName(linkAssetByAsset.getLinkAssetName());
                    descStr = descStr + ",对端设备【" + linkAssetByAsset.getLinkAssetName() + "】";
                }
                if (StrUtil.isNotEmpty(linkAssetByAsset.getLinkAssetIp())) {
                    tempReq.setLinkAssetIp(linkAssetByAsset.getLinkAssetIp());
                    descStr = descStr + ",对端设备IP【" + linkAssetByAsset.getLinkAssetIp() + "】";
                }

                // 更新对端设备端口状态
                try {
                    linkAssetByAsset.setPortStatus(portStatus + "");
                    assetLinkAssetServ.updateById(linkAssetByAsset);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.ASSET_LINK_ASSET, info.getAssetIp(), e);
                }

            }

            tempReq.setOrgMsg(descStr);

            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, tempReq,info.getInspectRecordId());

            if (Objects.nonNull(event)) {
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
