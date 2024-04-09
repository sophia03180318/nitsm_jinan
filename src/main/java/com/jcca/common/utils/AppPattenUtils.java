package com.jcca.common.utils;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具
 *
 * @author Lvyp
 */
public class AppPattenUtils {


    private static Pattern OIDPATTERN = Pattern.compile("\\.*(\\d+)\\s*$");
    private static Pattern NUMBERPATTERN = Pattern.compile("^[0-9]*$");
    private static Pattern DOUBLEPATTERN = Pattern.compile("^(\\d|[1-9]\\d|100)(\\.\\d{1,2})?$");
    private static Pattern IPPATTERN = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
    private static Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}:\\d{1,2}:\\d{1,2})");
    private static Pattern CISCO_PATTERN = Pattern.compile("%(\\w*_*-*\\w*_*-*)+-\\d-(\\w*_*-*\\w*_*)+:");


    /**
     * 正则判断OID
     *
     * @param oid
     * @return
     */
    public static Boolean isOid(String oid) {
        Matcher matcher = OIDPATTERN.matcher(oid);
        return matcher.find();
    }

    /**
     * 正则判断是否正整数
     *
     * @param oid
     * @return
     */
    public static Boolean isNumber(String number) {
        if (StrUtil.isEmpty(number)) {
            return false;
        }
        Matcher matcher = NUMBERPATTERN.matcher(number);
        return matcher.find();
    }

    /**
     * 是否为小数
     *
     * @param param
     * @return
     */
    public static Boolean isDouble(String param) {
        if (StrUtil.isEmpty(param)) {
            return false;
        }
        if (isNumber(param)) {
            return true;
        }
        Matcher matcher = DOUBLEPATTERN.matcher(param);
        return matcher.find();
    }

    /**
     * 正则判断是否IP
     *
     * @param oid
     * @return
     */
    public static Boolean isIp(String ip) {
        Matcher matcher = IPPATTERN.matcher(ip);
        return matcher.find();
    }

    /**
     * 去除字符串中的时间
     * 必须是含有HH:mm:ss的字符串
     *
     * @param reqStr
     * @return
     */
    public static String removeDateStr(String reqStr) {
        if (StrUtil.isEmpty(reqStr)) {
            return reqStr;
        }

        Matcher matcher = TIME_PATTERN.matcher(reqStr);
        String newReqStr = reqStr;
        String beginStr = "";

        if (matcher.find()) {
            String group = matcher.group(0);

            int timeIndex = reqStr.indexOf(group) + group.length();
            newReqStr = reqStr.substring(timeIndex, reqStr.length());
        } else {
            return newReqStr;
        }

        if (newReqStr.startsWith(".")) {
            String millisecondStr = newReqStr.substring(1, 4);
            String[] split = millisecondStr.split("");
            String substring = newReqStr.substring(4, newReqStr.length());
            newReqStr = substring;

            for (String item : split) {
                if (NumberUtil.isNumber(item)) {
                    continue;
                }
                beginStr += item;
            }
        }

        return beginStr + newReqStr;
    }


    public static List<Integer> getNumbers(String str) {
        ArrayList<Integer> resultList = new ArrayList<Integer>();
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        String result = m.replaceAll(" ").trim();
        if (StrUtil.isEmpty(result)) {
            return resultList;
        }
        String[] strArr = result.split(" ");
        for (int i = 0; i < strArr.length; i++) {
            String trim = strArr[i].trim();
            if (StrUtil.isEmpty(trim)) {
                continue;
            }
            resultList.add(Integer.valueOf(trim));
        }

        return resultList;
    }

}
