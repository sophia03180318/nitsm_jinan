package com.jcca.web.ip.util;

import cn.hutool.core.util.StrUtil;

import java.util.*;

/**
 * ip相关的计算操作
 *
 * @author lyp
 */
public class AppIpUtil {

    private static Map<Integer, String> ipMap;

    static {
        ipMap = new HashMap<Integer, String>();
        List<String> baseList = Arrays.asList("128.0.0.0", "192.0.0.0", "224.0.0.0", "240.0.0.0", "248.0.0.0",
                "252.0.0.0", "254.0.0.0", "255.0.0.0", "255.128.0.0", "255.192.0.0", "255.224.0.0", "255.240.0.0",
                "255.248.0.0", "255.252.0.0", "255.254.0.0", "255.255.0.0", "255.255.128.0", "255.255.192.0",
                "255.255.224.0", "255.255.240.0", "255.255.248.0", "255.255.252.0", "255.255.254.0", "255.255.255.0",
                "255.255.255.128", "255.255.255.192", "255.255.255.224", "255.255.255.240", "255.255.255.248",
                "255.255.255.252", "255.255.255.254", "255.255.255.255");
        for (int i = 1; i < 33; i++) {
            ipMap.put(i, baseList.get(i - 1));
        }

    }


    /**
     * 查询所有的ip(并判断 传入网关是否占用网络地址或广播地址)
     */
    public static List<String> getIPList(String startIp, String endIp) throws Exception {

        String[] startList = startIp.split("\\.");
        String[] endList = endIp.split("\\.");
        String subStart = startList[0] + "." + startList[1] + "." + startList[2] + ".";
        String subEnd = endList[0] + "." + endList[1] + "." + endList[2] + ".";
        startIp = subStart + (Integer.parseInt(startList[3]) + 1);
        endIp = subEnd + (Integer.parseInt(endList[3]) - 1);


        return parseIpRange(startIp, endIp);

    }

    /**
     * 计算ip的起始ip
     *
     * @param ip
     * @param mask
     * @return
     */
    public static String getBeginIpStr(String ip, String mask) {
        return getIpFromLong(getBeginIpLong(ip, mask));
    }

    /**
     * 根据 ip/掩码位 计算IP段的起始IP 如 IP串 218.240.38.69/30
     *
     * @param ip   给定的IP，如218.240.38.69
     * @param mask 给定的掩码位，如30
     * @return 终止IP的字符串表示
     */
    public static String getEndIpStr(String ip, String mask) {
        return getIpFromLong(getEndIpLong(ip, mask));
    }

    /**
     * 根据 ip/掩码位 计算IP段的终止IP 如 IP串 218.240.38.69/30
     *
     * @param ip   给定的IP，如218.240.38.69
     * @param mask 给定的掩码位，如30
     * @return 终止IP的长整型表示
     */
    private static Long getEndIpLong(String ip, String mask) {
        return getBeginIpLong(ip, mask) + ~getIpFromString(mask);
    }

    /**
     * 把long类型的Ip转为一般Ip类型：xx.xx.xx.xx
     *
     * @param ip
     * @return
     */
    private static String getIpFromLong(Long ip) {
        String s1 = String.valueOf((ip & 4278190080L) / 16777216L);
        String s2 = String.valueOf((ip & 16711680L) / 65536L);
        String s3 = String.valueOf((ip & 65280L) / 256L);
        String s4 = String.valueOf(ip & 255L);
        return s1 + "." + s2 + "." + s3 + "." + s4;
    }

    private static Long getIpFromString(String ip) {
        Long ipLong = 0L;
        String ipTemp = ip;
        ipLong = ipLong * 256 + Long.parseLong(ipTemp.substring(0, ipTemp.indexOf('.')));
        ipTemp = ipTemp.substring(ipTemp.indexOf('.') + 1, ipTemp.length());
        ipLong = ipLong * 256 + Long.parseLong(ipTemp.substring(0, ipTemp.indexOf('.')));
        ipTemp = ipTemp.substring(ipTemp.indexOf(".") + 1, ipTemp.length());
        ipLong = ipLong * 256 + Long.parseLong(ipTemp.substring(0, ipTemp.indexOf('.')));
        ipTemp = ipTemp.substring(ipTemp.indexOf('.') + 1, ipTemp.length());
        ipLong = ipLong * 256 + Long.parseLong(ipTemp);
        return ipLong;
    }

    /**
     * 根据 ip/掩码位 计算IP段的起始IP 如 IP串 218.240.38.69/30
     *
     * @param ip   给定的IP，如218.240.38.69
     * @param mask 给定的掩码
     * @return 起始IP的长整型表示
     */
    private static Long getBeginIpLong(String ip, String mask) {
        return getIpFromString(ip) & getIpFromString(mask);
    }

    /**
     * 根据掩码位获取掩码
     *
     * @param maskBit 掩码位数，如"28"、"30"
     * @return
     */
    public static String getMaskByMaskBit(Integer maskBit) {
        if (Objects.isNull(maskBit)) {
            return "error, maskBit is null !";
        }
        return getMaskMap(maskBit);
    }

    private static String getMaskMap(Integer maskBit) {
        String baseIp = ipMap.get(maskBit);
        if (StrUtil.isEmpty(baseIp)) {
            return "-1";
        }
        return baseIp;
    }

    private static List<String> parseIpRange(String ipfrom, String ipto) {
        List<String> ips = new ArrayList<String>();
        String[] ipfromd = ipfrom.split("\\.");
        String[] iptod = ipto.split("\\.");
        int[] int_ipf = new int[4];
        int[] int_ipt = new int[4];
        for (int i = 0; i < 4; i++) {
            int_ipf[i] = Integer.parseInt(ipfromd[i]);
            int_ipt[i] = Integer.parseInt(iptod[i]);
        }
        for (int A = int_ipf[0]; A <= int_ipt[0]; A++) {
            for (int B = (A == int_ipf[0] ? int_ipf[1] : 0); B <= (A == int_ipt[0] ? int_ipt[1] : 255); B++) {
                for (int C = (B == int_ipf[1] ? int_ipf[2] : 0); C <= (B == int_ipt[1] ? int_ipt[2] : 255); C++) {
                    for (int D = (C == int_ipf[2] ? int_ipf[3] : 0); D <= (C == int_ipt[2] ? int_ipt[3] : 255); D++) {
                        ips.add(A + "." + B + "." + C + "." + D);
                    }
                }
            }
        }
        return ips;
    }


    /**
     * 根据掩码获取掩码位数
     *
     * @param mask 掩码 255.255.255.0
     * @return
     */
    public static int getMaskBitByMask(String mask) {
        int maskNum = 0;
        switch (mask) {
            case "255.255.240.0":
                maskNum = 20;
                break;
            case "255.255.248.0":
                maskNum = 21;
                break;
            case "255.255.252.0":
                maskNum = 22;
                break;
            case "255.255.254.0":
                maskNum = 23;
                break;
            case "255.255.255.0":
                maskNum = 24;
                break;
            case "255.255.255.128":
                maskNum = 25;
                break;
            case "255.255.255.192":
                maskNum = 26;
                break;
            case "255.255.255.224":
                maskNum = 27;
                break;
            case "255.255.255.240":
                maskNum = 28;
                break;
            case "255.255.255.248":
                maskNum = 29;
                break;
            case "255.255.255.252":
                maskNum = 30;
                break;

        }
        return maskNum;
    }
}
