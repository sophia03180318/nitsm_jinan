package com.jcca.component.quartz.alarm;

import com.jcca.dataProcessing.support.IListener;
import com.jcca.web.asset.entity.Asset;

/**
 * 告警相关定时任务业务接口
 */
public interface AlarmJobService {


    /**
     * Ping 检测IPMI管理口
     *
     * @param asset
     */
    void pingIpmiPort(Asset asset);

    public void addDataSourceListener(IListener IListener);

}
