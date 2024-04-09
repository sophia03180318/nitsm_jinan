package com.jcca.dataProcessing.DataFilter.netInterface;

import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectInterfaceEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 端口保存处理类
 * @className InterfaceUpDownFilterHandler
 * @date 2023/10/27 9:36
 * @since 2.1.0.0
 */
@Component("interfaceSaveInfoFilterHandler")
public class InterfaceSaveInfoFilterHandler extends IFilterHandler<CollectInterfaceEntity> {

    @Resource
    private CollectInterfacesService interfaceServ;

    @Override
    public boolean handler(CollectInterfaceEntity info) {
        Date date = new Date();
        date.setTime(info.getCollectTime());
        CollectInterfaces copy = EntityBeanUtil.copy(info, CollectInterfaces.class);
        copy.setId(MyIdUtil.getId());
        copy.setCollectCode(info.getCollectCode());
        copy.setCollectTime(date);
        copy.setPortSpeed(info.getPortSpeed());
        copy.setPortInSpeed(info.getPortInSpeed());
        copy.setPortOutSpeed(info.getPortOutSpeed());
        copy.setLosePacketsOutRate(info.getLosePacketsOutRate());
        copy.setLosePacketsInRate(info.getLosePacketsInRate());
        copy.setErroCodeOutRate(info.getErroCodeOutRate());
        copy.setErroCodeInRate(info.getErroCodeInRate());
        copy.setPortIndexRank(info.getPortIndexRank());
        // 采集器portIn,errorCodeOut...存总值 itsm portIn,errorCodeOut... 存差值
        copy.setPortInCount(info.getPortIn());
        copy.setPortOutCount(info.getPortOut());
        copy.setErrorCodeInCount(info.getErrorCodeIn());
        copy.setErrorCodeOutCount(info.getErrorCodeOut());
        copy.setDiscardPacketsInCount(info.getDiscardPacketsIn());
        copy.setDiscardPacketsOutCount(info.getDiscardPacketsOut());
        if (info.getTxPower() != null) {
            copy.setTxPower(String.valueOf(info.getTxPower()));
        }
        if (info.getRxPower() != null) {
            copy.setRxPower(String.valueOf(info.getRxPower()));

        }
        // 保存采集数据
        interfaceServ.save(copy);

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
