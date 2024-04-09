package com.jcca.common.utils;

import cn.hutool.core.util.StrUtil;
import com.jcca.admin.biz.entity.Station;
import lombok.extern.slf4j.Slf4j;

/**
 * url 工具
 */
@Slf4j
public class UrlUtil {

    /**
     * 获取车站url前缀
     *
     * @param station
     * @return
     */
    public static String getStationUrlPrefix(Station station) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotEmpty(station.getProxyUrl())) {
            sb.append(station.getProxyUrl()).append("/").append(station.getIp()).append(":")
                    .append(station.getPort());
        } else {
            sb.append(station.getIp()).append(":")
                    .append(station.getPort());
        }
        return sb.toString();
    }

    /**
     * 获取车站url前缀
     *
     * @param
     * @return
     */
    public static String getStationUrlPrefix(String proxyUrl,String ip,String port) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        if (StrUtil.isNotEmpty(proxyUrl)) {
            sb.append(proxyUrl).append("/").append(ip).append(":")
                    .append(port);
        } else {
            sb.append(ip).append(":").append(port);
        }
        return sb.toString();
    }
}
