package com.jcca.component.client;


import java.util.List;

/**
 * 小机ASMI管理口接口
 * FW860.11 (SV860_063)
 * Server-8286-41A-SN21D4BEW
 */
public interface AsmiAgent {

    /**
     * 获取 ASMI 管理口的 cookie
     * @param assetId
     * @return
     */
    String getCookie(String assetId) throws Exception;

    /**
     * 获取Url
     * @param assetId
     * @return
     * @throws Exception
     */
    String getUrl(String assetId) throws Exception;

    /**
     * 获取事件
     * @param cookie
     * @return html页面
     */
    String getEventLog(String cookie,String assetId);

    /**
     * 获取日志详情
     * @param cookie
     * @param eventId
     * @param assetId
     * @return html页面
     */
    String getDetail(String cookie, List<String> eventId, String csrfToken, String assetId);
}
