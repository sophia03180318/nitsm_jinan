package com.jcca.web.common.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.client.AsmiAgent;
import com.jcca.dataProcessing.Entity.SyslogEventInfoEntity;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * ASMI 管理口交互
 */
@RestController
@RequestMapping("/api/free/asmi")
@Slf4j
public class ApiAsmiController {

    @Resource
    private AsmiAgent asmiAgent;
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Resource
    private AssetService assetService;

    /*@GetMapping("/test")
    public String test() throws Exception {
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
    }*/

    @GetMapping("/getCookie")
    ResultVo getCookie(String assetId) {

        String cookie = "";
        String url = "";
        try {
            cookie = asmiAgent.getCookie(assetId);
            url = asmiAgent.getUrl(assetId);
        } catch (Exception e) {
            log.error("管理口交互-获取cookie异常，参数：{}", assetId, e);
            return ResultVoUtil.warning(e.getMessage());
        }

        JSONObject respJson = new JSONObject();
        respJson.put("cookie", cookie);
        respJson.put("url", url);

        return ResultVoUtil.success(respJson);
    }

    @GetMapping("/getEventLogs")
    String getEventLogs(String cookie, String assetId) {
        if (StrUtil.isEmpty(cookie)) {
            return "";
        }
        return asmiAgent.getEventLog(cookie, assetId);
    }

    @GetMapping("/getEventLogDetail")
    String getEventLogDetail(String cookie, String assetId, @RequestParam("eventIds") List<String> eventIds, String csrfToken) {
        if (StrUtil.isEmpty(cookie)) {
            return "";
        }
        return asmiAgent.getDetail(cookie, eventIds, csrfToken, assetId);
    }

}
