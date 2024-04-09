package com.jcca.common.utils;

import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * 通用方法工具类
 *
 * @date 2018/10/15
 */
public class ToolUtil {

    /**
     * 获取随机位数的字符串
     *
     * @param length 随机位数
     */
    public static String getRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            // 获取ascii码中的字符 数字48-57 小写65-90 大写97-122
            int range = random.nextInt(75) + 48;
            range = range < 97 ? (range < 65 ? (range > 57 ? 114 - range : range) : (range > 90 ? 180 - range : range)) : range;
            sb.append((char) range);
        }
        return sb.toString();
    }

    /**
     * 首字母转小写
     */
    public static String lowerFirst(String word) {
        if (Character.isLowerCase(word.charAt(0))) {
            return word;
        } else {
            return String.valueOf(Character.toLowerCase(word.charAt(0))) + word.substring(1);
        }
    }

    /**
     * 首字母转大写
     */
    public static String upperFirst(String word) {
        if (Character.isUpperCase(word.charAt(0))) {
            return word;
        } else {
            return String.valueOf(Character.toUpperCase(word.charAt(0))) + word.substring(1);
        }
    }


    /**
     * 获取项目不同模式下的根路径
     */
    public static String getProjectPath() {
        String filePath = ToolUtil.class.getResource("").getPath();
        String projectPath = ToolUtil.class.getClassLoader().getResource("").getPath();
        StringBuilder path = new StringBuilder();

        if (!filePath.startsWith("file:/")) {
            // 开发模式下根路径
            char[] filePathArray = filePath.toCharArray();
            char[] projectPathArray = projectPath.toCharArray();
            for (int i = 0; i < filePathArray.length; i++) {
                if (projectPathArray.length > i && filePathArray[i] == projectPathArray[i]) {
                    path.append(filePathArray[i]);
                } else {
                    break;
                }
            }
        } else if (!projectPath.startsWith("file:/")) {
            // 部署服务器模式下根路径
            projectPath = projectPath.replace("/WEB-INF/classes/", "");
            projectPath = projectPath.replace("/target/classes/", "");
            try {
                path.append(URLDecoder.decode(projectPath, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return projectPath;
            }
        } else {
            // jar包启动模式下根路径
            String property = System.getProperty("java.class.path");
            int firstIndex = property.lastIndexOf(System.getProperty("path.separator")) + 1;
            int lastIndex = property.lastIndexOf(File.separator) + 1;
            path.append(property, firstIndex, lastIndex);
        }

        File file = new File(path.toString());
        String rootPath = "/";
        try {
            rootPath = URLDecoder.decode(file.getAbsolutePath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return rootPath.replaceAll("\\\\", "/");
    }

    /**
     * 获取文件后缀名
     */
    public static String getFileSuffix(String fileName) {
        if (!fileName.isEmpty()) {
            int lastIndexOf = fileName.lastIndexOf(".");
            if (lastIndexOf == -1) {
                return "";
            }
            return fileName.substring(lastIndexOf);
        }
        return "";
    }

    /**
     * 将枚举转成List集合
     *
     * @param enumClass 枚举类
     */
    public static Map<Long, String> enumToMap(Class<?> enumClass) {
        Map<Long, String> map = new TreeMap<>();
        try {
            Object[] objects = enumClass.getEnumConstants();
            Method getCode = enumClass.getMethod("getCode");
            Method getMessage = enumClass.getMethod("getMessage");
            for (Object obj : objects) {
                Object iCode = getCode.invoke(obj);
                Object iMessage = getMessage.invoke(obj);
                map.put(Long.valueOf(String.valueOf(iCode)), String.valueOf(iMessage));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
        return map;
    }

    /**
     * 根据枚举code获取枚举对象
     *
     * @param enumClass 枚举类
     * @param code      code值
     */
    public static Object enumCode(Class<?> enumClass, Object code) {
        try {
            Object[] objects = enumClass.getEnumConstants();
            Method getCode = enumClass.getMethod("getCode");
            for (Object obj : objects) {
                Object iCode = getCode.invoke(obj);
                if (iCode.equals(code)) {
                    return obj;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        }
        return "";
    }


    /**
     * 中文标点符号转英文字标点符号
     *
     * @param str 原字符串
     * @return str 新字符串
     */
    public static final String cToe(String str) {
        String[] regs = {"！", "，", "。", "；", "~", "《", "》", "（", "）", "？",
                "”", "｛", "｝", "“", "：", "【", "】", "”", "‘", "’", "!", ",",
                ".", ";", "`", "<", ">", "(", ")", "?", "'", "{", "}", "\"",
                ":", "{", "}", "\"", "\'", "\'"};
        for (int i = 0; i < regs.length / 2; i++) {
            str = str.replaceAll(regs[i], regs[i + regs.length / 2]);
        }
        return str;
    }


    /**
     * 全角转半角:
     *
     * @param fullStr
     * @return
     */
    public static final String full2Half(String fullStr) {
        if (StrUtil.isEmpty(fullStr)) {
            return fullStr;
        }
        char[] c = fullStr.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] >= 65281 && c[i] <= 65374) {
                c[i] = (char) (c[i] - 65248);
            } else if (c[i] == 12288) { // 空格
                c[i] = (char) 32;
            }
        }
        return new String(c);
    }

    /**
     * 半角转全角
     *
     * @param halfStr
     * @return
     */
    public static final String half2Full(String halfStr) {
        if (StrUtil.isEmpty(halfStr)) {
            return halfStr;
        }
        char[] c = halfStr.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) {
                c[i] = (char) 12288;
            } else if (c[i] < 127) {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }

    /**
     * 将字符串中的中文标点转为英文标点
     */
    public static synchronized String chineseToEnglish(String chinese) {
        String sReturn = chinese;
        if (StrUtil.isEmpty(sReturn)) {
            return chinese;
        }
        try {
            sReturn = sReturn.toUpperCase();
            sReturn = sReturn.replace('，', ',');
            sReturn = sReturn.replace('。', '.');
            sReturn = sReturn.replace('；', ';');
            sReturn = sReturn.replace('！', '!');
            sReturn = sReturn.replace('？', '?');
            sReturn = sReturn.replace('：', ':');
            sReturn = sReturn.replace('"', '＂');
            sReturn = sReturn.replace('“', '＂');
            sReturn = sReturn.replace('”', '＂');
        } catch (Exception e) {
            return chinese;
        }
        return sReturn;
    }


}
