package com.jcca.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.net.InetAddress;
import java.util.Date;

/**
 * ntpudp获取服务器时间工具
 *
 * @author hanwone
 * @date 2019-12-16 19:19
 */
@Slf4j
public class NTPUDPUtil {

    private static NTPUDPClient timeClient = null;

    public static Date getRemoteDate(String host) {
        Date date;
        try {
            if (timeClient == null) {
                timeClient = new NTPUDPClient();
            }
            timeClient.setDefaultTimeout(3000); // 连接超时时间
            InetAddress timeServerAddress = InetAddress.getByName(host);
            TimeInfo timeInfo = timeClient.getTime(timeServerAddress, 123);
            TimeStamp timeStamp = timeInfo.getMessage().getTransmitTimeStamp();
            date = timeStamp.getDate();
            return date;
        } catch (Exception e) {
            log.error("NTPUDPUtil获取服务器时间异常" + e.getMessage());
        }
        return null;
    }

    /**
     * 将snmp采集到的时间16进制转为时间
     * yyyy-MM-dd HH:mm:ss SSS
     *
     * @param result
     * @return
     * @throws Exception
     */
    public static String getTimeStr(String result) {
        if (StrUtil.isEmpty(result)) {
            return null;
        }
        String[] split = result.split(":");
        if (split.length != 8) {
            return null;
        }
        Integer year = Integer.valueOf(split[0] + split[1], 16);
        Integer month = Integer.valueOf(split[2], 16);
        Integer day = Integer.valueOf(split[3], 16);
        Integer hours = Integer.valueOf(split[4], 16);
        Integer minute = Integer.valueOf(split[5], 16);
        Integer second = Integer.valueOf(split[6], 16);
        Integer millisecond = Integer.valueOf(split[7], 16);

        return year + "-" + month + "-" + day + " " + hours + ":" + minute + ":" + second + " " + millisecond;
    }

}
