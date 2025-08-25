package com.jcca.dataProcessing.dataAdpater;

import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.dataProcessing.Entity.CollectHardwareBean;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: hhw
 * @description: HardwareLinuxAdapter 主要是用来
 * @date: 2025-08-25  13:59
 * @since: 2.1.9.0
 */
@Component("hardwareLinuxAdapter")
public class HardwareLinuxAdapter extends AssetIpAdd implements IAdapter<ReceiveAlarmDto> {

    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    /**
     * 处理数据
     *
     * @param alarmDto 接收到的数据
     */
    @Override
    public void dispose(ReceiveAlarmDto alarmDto) {
        String content = alarmDto.getContent();
        List<CollectHardwareBean> list = JSONUtil.toList(JSONUtil.parseArray(content), CollectHardwareBean.class);
        CollectHardwareBean bean = list.get(0);
        //事件监控分类
        eventInfoChangeManagerService.setStateValue(StatusInfoChangeTypeEnum.event_hardware.getCode(), "monitor", true);

        excutorService.submit(() -> {
            setAssetIp(bean);
            try {
                dataProcessManager.hardwareHandlerRequest(bean);
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "设备" + bean.getAssetIp() + "HardwareLinuxAdapter 抛出异常", e);
            }
            return 1;
        });
    }

    /**
     * 获取对应的KEY
     *
     * @return
     */
    @Override
    public String getCode() {
        return CollectConst.LINUX_HARDWARE_CONFIG;
    }

    /**
     * 可用户获取一些过程中的处理信息
     *
     * @return
     */
    @Override
    public String dataProcess() {
        return "当前硬件信息剩余处理数量：" + excutorService.getQueue().size();
    }
}
