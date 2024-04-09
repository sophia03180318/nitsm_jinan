package com.jcca.common.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class TelnetUtil {

    private static final String ENTER_COMMAND_ARROW = ">";
    private static final String ENTER_COMMAND_ARROW_N7K = "#";
    private static final String ENTER_COMMAND_ARROW_HUAWEI = "]";
    private static final String ENTER_COMMAND_COLON = ":";
    private static final String MORE_FLAG = "more";
    public static String ERROR_FLAG = "COLLECT_ERROR";
    public static final String COMMAND_SPECIAL_FLAG = "TPWDT";

    private TelnetClient telnet = new TelnetClient("VT100");

    private InputStream in;

    private PrintStream out;

    private static final int timeout = 20000;

    private Integer port;
    private String username;
    private String password;
    private String ip;


    /**
     * 批量执行telnet
     *
     * @param ip
     * @param userName
     * @param pwd
     * @param port
     * @param commandList
     * @return
     */
    public static List<String> exeTelNet(String ip, String userName, String pwd, String enablePassword, Integer port, List<String> commandList) {
        List<String> respList = new LinkedList<>();
        TelnetUtil telnet = new TelnetUtil(ip, userName, pwd, port);
        boolean login = false;
        int i = 0;
        for (String command : commandList) {
            StringBuilder resp = new StringBuilder("");
            if (command.contains("mib采集")) {
                respList.add(resp.toString());
                continue;
            }
            //光交采集三次之后会失败，应该是命令太频繁
            if (i > 2) {
                telnet.disconnect();
                login = false;
            }
            i++;
            try {
                if (!login) {
                    telnet.opticalConnect();
                    telnet.pubReadUntil(ENTER_COMMAND_COLON, "", "", true, false);
                    if (StrUtil.isNotEmpty(userName)) {
                        telnet.write(userName);
                        telnet.pubReadUntil(ENTER_COMMAND_COLON, ENTER_COMMAND_ARROW, ENTER_COMMAND_ARROW_HUAWEI, true, false);
                    }
                    if (StrUtil.isNotEmpty(pwd) || StringUtils.isNotEmpty(enablePassword)) {
                        telnet.write(StrUtil.isEmpty(pwd) ? enablePassword : pwd);
                        telnet.pubReadUntil(ENTER_COMMAND_ARROW, ENTER_COMMAND_ARROW_N7K, ENTER_COMMAND_ARROW_HUAWEI, true, false);
                    }
                }
                login = true;
                boolean isHuawei = false;

                //判定命令类型：含有|PWD|的话走特权采集
                if (command.contains(COMMAND_SPECIAL_FLAG)) {
                    String[] commandArray = command.split(COMMAND_SPECIAL_FLAG);
                    telnet.write(commandArray[0]);
                    isHuawei = commandArray[0].equals("sys");
                    String msg = telnet.pubReadUntil(ENTER_COMMAND_COLON, ENTER_COMMAND_ARROW_N7K, ENTER_COMMAND_ARROW_HUAWEI, false, isHuawei);
                    if (msg.contains(ENTER_COMMAND_COLON)) {
                        //需要密码
                        telnet.write(StrUtil.isEmpty(enablePassword) ? pwd : enablePassword);
                        telnet.pubReadUntil(ENTER_COMMAND_ARROW, ENTER_COMMAND_ARROW_N7K, ENTER_COMMAND_ARROW_HUAWEI, true, isHuawei);
                    }
                    telnet.write(commandArray[1]);
                } else {
                    telnet.write(command);
                }

                String msg = telnet.pubReadUntil(ENTER_COMMAND_ARROW, ENTER_COMMAND_ARROW_N7K, ENTER_COMMAND_ARROW_HUAWEI, false, isHuawei);
                resp.append(msg);
                //循环获取more报文
                if (msg.toLowerCase().contains(MORE_FLAG)) {
                    while (true) {
                        telnet.write("");
                        String msgStr = telnet.pubReadUntil(ENTER_COMMAND_ARROW, ENTER_COMMAND_ARROW_N7K, ENTER_COMMAND_ARROW_HUAWEI, false, isHuawei);
                        resp.append("\r\n");
                        resp.append(msgStr);
                        if (!msgStr.toLowerCase().contains(MORE_FLAG)) {
                            //强制重新登录一次
                            telnet.disconnect();
                            login = false;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("【设备TELNET采集异常：" + ip + "】:" + e.getMessage(), e);
                resp.append(ERROR_FLAG);
            } finally {
                respList.add(resp.toString().replace("More", "").replace("--", ""));
            }
        }
        telnet.disconnect();
        return respList;
    }

    /**
     * 判定采集结果是否正常
     *
     * @param result
     * @return
     */
    public static Boolean verifyResult(List<String> result) {
        return !result.isEmpty() && !result.contains(ERROR_FLAG);
    }

    /**
     * 执行telnet 用
     *
     * @param pattern1
     * @param pattern2
     * @param pattern3
     * @param isLogin
     * @param isHuawei
     * @return
     * @throws IOException
     */
    public String pubReadUntil(String pattern1, String pattern2, String pattern3, boolean isLogin, boolean isHuawei) throws IOException {
        if (ENTER_COMMAND_ARROW_N7K.equals(pattern1) && isHuawei) {
            pattern1 = "";
        }
        if (ENTER_COMMAND_ARROW_N7K.equals(pattern2) && isHuawei) {
            pattern2 = "";
        }
        if (ENTER_COMMAND_ARROW_N7K.equals(pattern3) && isHuawei) {
            pattern3 = "";
        }

        char lastChar = pattern1.charAt(pattern1.length() - 1);
        StringBuilder sb = new StringBuilder();
        char ch = (char) in.read();
        while (true) {
            sb.append(ch);
            if (sb.toString().toLowerCase().contains("sorry") && isLogin) {
                throw new IOException("Sorry! Max remote sessions for login:admin is 2");
            }
            if (sb.length() > 10240 && isLogin) {
                throw new IOException("Sorry! More than 10240 bytes have been read");
            }
            if (ch == lastChar) {
                if (sb.toString().endsWith(pattern1)) {
                    byte[] temp = sb.toString().getBytes(StandardCharsets.ISO_8859_1);
                    return new String(temp, StandardCharsets.UTF_8);
                }
            }
            if (StrUtil.isNotEmpty(pattern2)) {
                if (ch == pattern2.charAt(pattern2.length() - 1) && sb.toString().endsWith(pattern2)) {
                    byte[] temp = sb.toString().getBytes(StandardCharsets.ISO_8859_1);
                    return new String(temp, StandardCharsets.UTF_8);
                }
            }
            if (StrUtil.isNotEmpty(pattern3)) {
                if (ch == pattern3.charAt(pattern3.length() - 1) && sb.toString().endsWith(pattern3)) {
                    byte[] temp = sb.toString().getBytes(StandardCharsets.ISO_8859_1);
                    return new String(temp, StandardCharsets.UTF_8);
                }
            }
            if (sb.toString().toLowerCase().contains(MORE_FLAG)) {
                byte[] temp = sb.toString().getBytes(StandardCharsets.ISO_8859_1);
                return new String(temp, StandardCharsets.UTF_8);
            }

            ch = (char) in.read();
        }
    }


    public TelnetUtil(String ip, String userName, String password, Integer port) {
        this.ip = ip;
        this.username = userName;
        this.password = password;
        if (Objects.isNull(port)) {
            this.port = 23;
        } else {
            this.port = port;
        }
    }

    public void connect() throws Exception {

        telnet.setDefaultTimeout(timeout);
        telnet.connect(ip, port);
        in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());
        telnet.setKeepAlive(true);
        if (StrUtil.isNotEmpty(username)) {
            write(username);
        }
        write(password);
    }

    public void opticalConnect() throws Exception {
        telnet.setDefaultTimeout(timeout);
        //telnet.setConnectTimeout(connectTimeout);
        telnet.connect(ip, port);
        in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());
        telnet.setKeepAlive(true);
    }


    public String opticalReadUntil(String pattern, String command) throws IOException {
        char lastChar = pattern.charAt(pattern.length() - 1);
        StringBuilder sb = new StringBuilder();
        char ch = (char) in.read();
        while (true) {
            sb.append(ch);
            if (sb.toString().toLowerCase().contains("sorry")) {
                throw new IOException("Sorry! Max remote sessions for login:admin is 2");
            }
            if (!"errdump".equals(command) && sb.length() > 10240) {
                throw new IOException("Sorry! More than 10240 bytes have been read");
            }
            if (ch == lastChar) {
                if (sb.toString().endsWith(pattern)) {
                    return sb.toString();
                }
            }
            ch = (char) in.read();
        }
    }

    public String n7kReadUntil(String pattern) throws IOException {
        char lastChar = pattern.charAt(pattern.length() - 1);
        StringBuilder sb = new StringBuilder();
        char ch = (char) in.read();
        while (true) {
            sb.append(ch);
            if (ch == lastChar) {
                if (sb.toString().endsWith(pattern)) {
                    return sb.toString();
                }
            }
            ch = (char) in.read();
        }
    }

    public void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
