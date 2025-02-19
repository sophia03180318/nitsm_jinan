package com.jcca.web.common.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.dataProcessing.Entity.ProcessAlarmQueueEntity;
import com.jcca.dataProcessing.Entity.ProcessGroupEntity;
import com.jcca.dataProcessing.Entity.SyslogEventInfoEntity;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: Lvyp
 * @create: 2025/02/19 15:25
 */
@RestController
@RequestMapping("/api/free/mock")
@Slf4j
public class MockController {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private AssetService assetService;

    @GetMapping("/syslog")
    public String syslog() throws Exception {
            SyslogEventInfoEntity syslogEventInfoEntity = new SyslogEventInfoEntity();
            Asset asset = assetService.findOneByIp("192.168.73.88");
            if (asset != null) {
                syslogEventInfoEntity.setAssetId(asset.getId());
                syslogEventInfoEntity.setAssetIp(asset.getIp());
            }
            syslogEventInfoEntity.setIp("192.168.73.88");
            syslogEventInfoEntity.setMessage("1 2025-02-14T09:36:04-00:00 XCC-7X04-J900V142 XCC-LOG - - - \n\tServer MTM: 7X04CTO1WW\n\n\n\tAlert Text: Security: Userid: USERID using default authentication had 1 login failures from WEB client at IP address 220.0.1.250.\n\tType of Alert: System - Remote Login\n\n\tSeverity: 4\n\tDate(m/d/y): 02/14/2025\n\tTime(h:m:s): 09:36:03\n\n\tContact: jcca\n\n\tLocation: jcca\n\tBMC Text ID: UnknownBMC\n\tBMC Serial Number: J900V142\n\tBMC UUID: 725FE55C404211EEB22472E2842E61EF\n\tEvent ID: 4000001000000000\n\tServiceable Event Indicator: Not Serviceable\n\tFRU list: Not available\n\tRoom ID: Not available\n\tRack ID: Not available\n\tLowest U-position: 1\n\tBlade Bay: Not available\n\tTest Alert: no\n\tAuxiliary Data: Not available\n\tCommon Event ID: FQXSPSE4002I\n\tEvent Type: 0\n\tReport Chain: XCC");
            syslogEventInfoEntity.setLevel(6);
            dataProcessManager.syslogEventHandlerRequest(syslogEventInfoEntity);

            return "send ok";
        }

    @GetMapping("/process")
    public String process(String status) throws Exception {
        ProcessGroupEntity processGroupEntity = new ProcessGroupEntity();
        processGroupEntity.setAssetIp("192.168.20.80");
        ProcessAlarmQueueEntity processAlarmQueueEntity = new ProcessAlarmQueueEntity();
        processAlarmQueueEntity.setProcessStatus(StrUtil.isEmpty(status)?true:false);
        processAlarmQueueEntity.setAssetIp("192.168.20.80");
        processAlarmQueueEntity.setProcessName("sublime_text.exe");
        processAlarmQueueEntity.setProcessId("12848");
        processAlarmQueueEntity.setHostMode(3);
        processAlarmQueueEntity.setThresholdId("1892086660787871744");

        processGroupEntity.setQueueObj(Arrays.asList(processAlarmQueueEntity));

        Asset asset = assetService.findOneByIp("192.168.20.80");
        if (asset != null) {
            processGroupEntity.setAssetId(asset.getId());
            processGroupEntity.setAssetIp(asset.getIp());
        }
        dataProcessManager.processGroupHandlerRequest(processGroupEntity);

        return "send ok";
    }
}
