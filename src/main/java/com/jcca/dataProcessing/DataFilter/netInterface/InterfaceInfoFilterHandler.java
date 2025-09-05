package com.jcca.dataProcessing.DataFilter.netInterface;

import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectInterfaceEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 端口通断过滤处理类
 * @className InterfaceUpDownFilterHandler
 * @date 2023/10/27 9:36
 * @since 2.1.0.0
 */
@Component("interfaceInfoFilterHandler")
public class InterfaceInfoFilterHandler extends IFilterHandler<CollectInterfaceEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private RedisService redisServ;


    /**
     * 封装INFO MAP
     * @param redisKey
     * @param mapKey
     * @param value
     * @param info
     */
    private void putInfoMap(String redisKey, String mapKey, Object value,CollectInterfaceEntity info,boolean isThresholdTag){
        boolean flag= eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey,value);
        if(isThresholdTag){
            ChangeInfo changeInfo= this.createChangeInfo(value,redisKey,mapKey);
            changeInfo.setIsChange(flag);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            Map<String, ChangeInfo> maps = info.getMaps();
            maps.put(mapKey, changeInfo);
            info.setMaps(maps);
            return ;
        }
        if(flag){
            ChangeInfo changeInfo= this.createChangeInfo(value,redisKey,mapKey);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            Map<String, ChangeInfo> maps = info.getMaps();
            maps.put(mapKey, changeInfo);
            info.setMaps(maps);
        }

    }


    @Override
    public boolean handler(CollectInterfaceEntity info) {
        if (info.getPortIndex().contains(".")) {
            info.setDiscardPacketsOut(0L);
            info.setErrorCodeIn(0L);
            info.setLosePacketsOutRate(0.0);
            info.setErroCodeInRate(0.0);
        }

        //兼容车站，中心计算已经挪至采集器端
        if(Objects.isNull(info.getPortInRate())||Objects.isNull(info.getPortOutRate())){
            // 带宽
            Long portSpeed = info.getPortSpeed();
            //带宽 单位为B   乘8之后变成bit  与portIn 单位相同
            BigDecimal speedBig = new BigDecimal(portSpeed).multiply(new BigDecimal(8));
            //端口流入每秒速率 bit/s
            Long portInSpeed = info.getPortInSpeed();

            double portInRate = AppMathUtil.percentageDouble(portInSpeed,speedBig.longValue(),2);
            double portOutRate = AppMathUtil.percentageDouble(info.getPortOutSpeed(),speedBig.longValue(),2);

            info.setPortInRate(portInRate);
            info.setPortOutRate(portOutRate);
        }

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_interface.getCode() + ":" + info.getPortName();
        //状态
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_up_down.getCode(),info.getStatus(),info,true);
        //端口流出
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portOut.getCode(),info.getPortOut(),info,false);
        //端口流出率
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portOutRate.getCode(),info.getPortOutRate(),info,true);
        //端口流入
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portIn.getCode(),info.getPortIn(),info,false);
        //端口流入率
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portInRate.getCode(),info.getPortInRate(),info,true);
        //端口流入丢包数
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_discardPacketsIn.getCode(),info.getDiscardPacketsIn(),info,false);
        //端口流入丢包率
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_losePacketInRate.getCode(),info.getLosePacketsInRate(),info,true);
        //端口流出丢包数
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_discardPacketsOut.getCode(),info.getDiscardPacketsOut(),info,false);
        //端口流出丢包率状态
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_losePacketOutRate.getCode(),info.getLosePacketsOutRate(),info,true);
        //流入误码数
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_errorCodeIn.getCode(),info.getErrorCodeIn(),info,false);
        //流出误码
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_errorCodeOut.getCode(),info.getErrorCodeOut(),info,false);
        //流入误码率
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_errorCodeInRate.getCode(),info.getErroCodeInRate(),info,true);
        //端口流出误码率
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_errorCodeOutRate.getCode(),info.getErroCodeOutRate(),info,true);
        //别名
        String portAlias = info.getPortAlias() == null ? "--" : info.getPortAlias();
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portAlias.getCode(),portAlias,info,false);
        //端口类型
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portType.getCode(),info.getPortType(),info,false);
        //端口索引
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portIndex.getCode(),info.getPortIndex(),info,false);
        //端口序号
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portIndexRank.getCode(),info.getPortIndexRank(),info,false);
        //链接类型
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portLinkType.getCode(),info.getPortLinkType(),info,false);
        //链接IP
        String linkIp=info.getLinkIp()==null?"--":info.getLinkIp();
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_linkIp.getCode(),linkIp,info,false);
        //链接掩码
        String linkMask=info.getLinkMask()==null?"--":info.getLinkMask();
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_linkMask.getCode(),linkMask,info,false);
        //对端地址
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_linkPhyAddress.getCode(),info.getLinkPhyAddress(),info,false);
        //端口非单播流入量
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_noUnicastPacketsIn.getCode(),info.getNoUnicastPacketsIn(),info,false);
        //端口非单播流出量
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_noUnicastPacketsOut.getCode(),info.getNoUnicastPacketsOut(),info,false);
        //端口单播流入量
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_unicastPacketsIn.getCode(),info.getUnicastPacketsIn(),info,false);
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_unicastPacketsOut.getCode(),info.getUnicastPacketsOut(),info,false);
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portSpeed.getCode(),info.getPortSpeed(),info,false);
        putInfoMap(redisKey,StatusInfoChangeTypeEnum.status_interface_portInSpeed.getCode(),info.getPortInSpeed(),info,false);

        if(Objects.nonNull(info.getPortInSpeed()) && info.getPortInSpeed()!=0 ){
            BigDecimal speed = new BigDecimal(info.getPortInSpeed()).divide(new BigDecimal(8000), 2, BigDecimal.ROUND_HALF_DOWN);
            putInfoMap(redisKey, StatusInfoChangeTypeEnum.status_interface_portInSpeedBps.getCode(), speed.doubleValue(), info, true);
        }
        if (Objects.nonNull(info.getPortOutSpeed()) && info.getPortOutSpeed() != 0) {
            BigDecimal speed = new BigDecimal(info.getPortOutSpeed()).divide(new BigDecimal(8000), 2, BigDecimal.ROUND_HALF_DOWN);
            putInfoMap(redisKey, StatusInfoChangeTypeEnum.status_interface_portOutSpeedBps.getCode(), speed.doubleValue(), info, true);
        }

        putInfoMap(redisKey, StatusInfoChangeTypeEnum.status_interface_portOutSpeed.getCode(), info.getPortOutSpeed(), info, false);
        putInfoMap(redisKey, StatusInfoChangeTypeEnum.status_interface_crcErrors.getCode(), info.getCrcErrors(), info, false);
        if (info.getTxPower() != null) {
            putInfoMap(redisKey, StatusInfoChangeTypeEnum.status_interface_txPower.getCode(), info.getTxPower(), info, true);

        }
        if (info.getRxPower() != null) {
            putInfoMap(redisKey, StatusInfoChangeTypeEnum.status_interface_rxPower.getCode(), info.getRxPower(), info, true);
        }


        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

    private ChangeInfo  createChangeInfo(Object value,String redisKey, String mapKey){
            ChangeInfo changeInfo=new ChangeInfo();
            changeInfo.setValue(value);
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            return changeInfo;
    }



}
