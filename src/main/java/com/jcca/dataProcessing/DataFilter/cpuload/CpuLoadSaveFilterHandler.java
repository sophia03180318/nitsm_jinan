package com.jcca.dataProcessing.DataFilter.cpuload;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.CollectCpuLoadBean;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.CollectCpuLoad;
import com.jcca.web.asset.service.CollectCpuLoadService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author: hhw
 * @description: CpuLoadSaveFilterHandler 主要是用来处理应用服务器CPU负载数据
 * @date: 2025-07-17  16:15
 * @since: 2.1.8.0
 */
@Component("cpuLoadSaveFilterHandler")
public class CpuLoadSaveFilterHandler extends IFilterHandler<CollectCpuLoadBean> {

    @Resource
    private CollectCpuLoadService collectCpuLoadService;

    /**
     * 处理方法
     *
     * @param info
     * @return 返回值true表示可以进入下一层处理，放回false
     * 表示整个处理结束，不会进入下层处理，不会保存缓存
     */
    @Override
    public boolean handler(CollectCpuLoadBean info) throws Exception {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "CPU负载处理类", info);
        String cpuLoadOne = info.getCpuLoadOne();
        String cpuLoadFive = info.getCpuLoadFive();
        String cpuLoadFifteen = info.getCpuLoadFifteen();
        if (StringUtils.isEmpty(cpuLoadOne) || StringUtils.isEmpty(cpuLoadFive) || StringUtils.isEmpty(cpuLoadFifteen)) {
            return false;
        }
        String assetId = info.getAssetId();

        CollectCpuLoad collectCpuLoad = new CollectCpuLoad();
        collectCpuLoad.setId(MyIdUtil.getId());
        collectCpuLoad.setAssetId(assetId);
        collectCpuLoad.setCollectCode(info.getCollectCode());
        collectCpuLoad.setLoadOne(Double.valueOf(info.getCpuLoadOne()));
        collectCpuLoad.setLoadFive(Double.valueOf(info.getCpuLoadFive()));
        collectCpuLoad.setLoadFifteen(Double.valueOf(info.getCpuLoadFifteen()));
        collectCpuLoad.setCollectTime(new Date(info.getCollectTime()));
        collectCpuLoadService.save(collectCpuLoad);
        return true;
    }

    /**
     * 直接控制下层处理
     *
     * @param flag
     * @return 返回true则需要下层处理，返回false不需要下层处理，并且不会保存缓存
     */
    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
