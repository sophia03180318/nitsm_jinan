package com.jcca.web.common.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.component.client.AsmiAgent;
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
