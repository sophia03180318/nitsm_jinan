package com.jcca.common.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.StringUtils;
import com.jcca.common.exception.common.DbEntityNotFound;
import com.jcca.poi.hssf.record.cf.Threshold;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP测试工具
 *
 * @author lyp
 */
@Slf4j
public class TestIpUtil {

    private static final String WINDOWS_FLG = "WINDOWS";

    /**
     * ping 操作
     *
     * @param ip
     * @param timeOutSec 超时时间 ：秒
     * @return 通/断
     */
    public static Boolean ping(String ip, Integer timeOutSec) throws IOException {
        return InetAddress.getByName(ip).isReachable(timeOutSec * 1000);
    }


    /**
     * 测试主机端口是否启用
     *
     * @param ip
     * @param port
     * @param timeOutSec 超时时间秒
     * @return
     */
    public static Boolean telnet(String ip, Integer port, Integer timeOutSec) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, port), timeOutSec * 1000);
            return socket.isConnected();
        } catch (IOException e) {
            log.error("【telnet测试" + ip + ":" + port + "失败】：" + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        return false;
    }

    /**
     * 获取IP的MAC地址
     *
     * @param ip
     * @return
     */
    public static String getMacAddress(String ip, Boolean needPing) {
        String mac = "";
        String property = System.getProperty("os.name");
        if (StrUtil.isEmpty(property)) {
            return mac;
        }
        try {
            if (property.toUpperCase().contains(WINDOWS_FLG)) {
                String[] cmd = {"cmd", "/c", "ping " + ip};
                String[] another = {"cmd", "/c", "nbtstat  -A " + ip};
                String cmdResult = callCmd(cmd, another);
                if (StringUtils.isEmpty(cmdResult)) {
                    return mac;
                }
                mac = filterMacAddress(ip, cmdResult, "-");
            } else {
                mac = getMac(ip, needPing);
            }
            if (mac.contains("NO ENTRY")) {
                mac = "";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return mac.toUpperCase();
    }

    /**
     * 路由追踪
     *
     * @param ip
     * @return 返回所有途径的IP
     */
    public static List<String> traceroute(String ip) {
        List<String> ipList = new ArrayList<String>();
        String osName = System.getProperty("os.name");
        try {
            if (osName.toUpperCase().contains(WINDOWS_FLG)) {
                String[] cmd = {"cmd", "/c", "tracert -d " + ip};
                String callCmd = callCmd(cmd, null);
                tracerouteAnalyze(callCmd, ipList);
            } else {
                Process process = Runtime.getRuntime().exec("traceroute -d " + ip);
                InputStreamReader ir = new InputStreamReader(process.getInputStream(), "GBK");
                LineNumberReader input = new LineNumberReader(ir);
                String line;
                StringBuffer s = new StringBuffer();
                while ((line = input.readLine()) != null) {
                    s.append(line);
                }
                tracerouteAnalyze(s.toString(), ipList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (ipList.size() > 0) {
            ipList.remove(0);
        }
        return ipList;
    }

    /**
     * 提取字符串中的ip地址
     *
     * @param result
     * @param ipList
     */
    private static void tracerouteAnalyze(String result, List<String> ipList) {
        log.info(result);
        String regEx = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(result);
        while (m.find()) {
            String ip = m.group();
            ipList.add(ip);
        }
    }

    /**
     * Linux获取MAC地址
     *
     * @param ip
     * @return
     * @throws IOException
     */
    private static String getMac(String ip, Boolean ping) throws Exception {
        String mac = "";
        Runtime runtime = Runtime.getRuntime();
        if (ping) {
            Process process = runtime.exec("ping " + ip + " c4");
            process.waitFor();
        }
        Process process = runtime.exec("arp " + ip);
        InputStreamReader ir = new InputStreamReader(process.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);
        String line;
        StringBuffer s = new StringBuffer();
        while ((line = input.readLine()) != null) {
            s.append(line);
        }
        mac = s.toString();
        if (StrUtil.isNotEmpty(mac)) {
            try {
                mac = mac.substring(mac.indexOf(":") - 2, mac.lastIndexOf(":") + 3);
            } catch (Exception e) {
                log.error(AppLogUtils.logStr("IP-TEST", "取回MAC格式错误(取:前两位到后三位之间的字符串)：" + s.toString(), e.getMessage()));
            }
        }
        return mac;
    }


    /**
     * windows 处理mac地址
     *
     * @param ip
     * @param sourceString
     * @param macSeparator
     * @return
     */
    private static String filterMacAddress(final String ip, final String sourceString, final String macSeparator) {
        String result = "";
        String regExp = "((([0-9,A-F,a-f]{1,2}" + macSeparator + "){1,5})[0-9,A-F,a-f]{1,2})";
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(sourceString);
        while (matcher.find()) {
            result = matcher.group(1);
            // 因计算机多网卡问题，截取紧靠IP后的第一个mac地址
            int num = sourceString.indexOf(ip) - sourceString.indexOf(": " + result + " ");
            if (num > 0 && num < 300) {
                break;
            }
        }
        return result;
    }

    /**
     * 运行windows CMD命令
     *
     * @param cmd
     * @param another
     * @return
     * @throws Exception
     */
    private static String callCmd(String[] cmd, String[] another) throws Exception {
        String line = "";
        Runtime rt = Runtime.getRuntime();
        // 执行第一个命令
        Process proc = rt.exec(cmd);
        // 执行第二个命令
        if (Objects.nonNull(another)) {
            proc.waitFor();
            proc = rt.exec(another);
        }
        StringBuilder result = new StringBuilder();
        InputStreamReader is = new InputStreamReader(proc.getInputStream(), "GBK");
        BufferedReader br = new BufferedReader(is);
        while ((line = br.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }
}
