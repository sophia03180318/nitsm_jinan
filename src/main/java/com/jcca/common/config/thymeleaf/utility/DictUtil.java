package com.jcca.common.config.thymeleaf.utility;

import com.jcca.admin.system.entity.SysDict;
import com.jcca.admin.system.service.SysDictService;
import com.jcca.common.utils.EhCacheUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.asset.utils.NullFieldException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 字典提取工具对象
 *
 * @author hanwone
 * @date 2018/8/14
 */
public class DictUtil {

    private static Cache dictCache = EhCacheUtil.getDictCache();

    /**
     * 获取字典值集合
     *
     * @param name 字典标识
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> value(String name) {
        Map<String, String> value = null;
        Element dictEle = dictCache.get(name);
        if (dictEle != null) {
            value = (Map<String, String>) dictEle.getObjectValue();
        } else {
            SysDictService dictService = SpringContextUtil.getBean(SysDictService.class);
            SysDict dict = dictService.getByNameOk(name);
            if (dict != null) {
                String dictValue = dict.getValue();
                String[] outerSplit = dictValue.split(",");
                value = new LinkedHashMap<>();
                for (String osp : outerSplit) {
                    String[] split = osp.split(":");
                    if (split.length > 1) {
                        value.put(split[0], split[1]);
                    }
                }
                dictCache.put(new Element(dict.getName(), value));
            }
        }
        return value;
    }

    public static String getValue(String label) {
        String value = "";
        SysDictService dictService = SpringContextUtil.getBean(SysDictService.class);
        SysDict dict = dictService.getByNameOk(label);
        if (dict != null) {
            value = dict.getValue();
        }
        return value;
    }

    public static String getTitle(String name) {
        String value = "";
        SysDictService dictService = SpringContextUtil.getBean(SysDictService.class);
        SysDict dict = dictService.getByName(name);
        if (dict != null) {
            value = dict.getTitle();
        }
        return value;
    }


    /**
     * 根据选项编码获取选项值
     *
     * @param label 字典标识 其实是name,这个工具写的和数据库对不上……服了
     * @param code  选项编码
     */
    public static String keyValue(String label, String code) {
        Map<String, String> list = DictUtil.value(label);
        if (list != null) {
            return list.get(code);
        } else {
            return "";
        }
    }

    /**
     * 封装数据状态字典
     *
     * @param status 状态
     */
    public static String dataStatus(Byte status) {
        String label = "DATA_STATUS";
        return DictUtil.keyValue(label, String.valueOf(status));
    }

    /**
     * 清除缓存中指定的数据
     *
     * @param name 字典标识
     */
    public static void clearCache(String name) {
        Element dictEle = dictCache.get(name);
        if (dictEle != null) {
            dictCache.remove(name);
        }
    }


    /**
     * 根据字典标识和value值获取key值
     */
    public static String getKey(String name, String value) {
        Map<String, String> map = DictUtil.value(name);
        String key = "";
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (value.equals(entry.getValue())) {
                    key = entry.getKey();
                }
            }
        }
        return key;
    }

    /**
     * 根据字典标识和value值获取key值
     */
    public static String getKeyNotNull(String name, String value) throws NullFieldException {
        Map<String, String> map = DictUtil.value(name);
        String key = "";
        boolean b = false;
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (value.equals(entry.getValue())) {
                    key = entry.getKey();
                    b = true;
                }
            }
        }
        if (b) {
            return key;
        } else {
            throw new NullFieldException();
        }

    }


    /**
     * 获取选项值(取不出返回 "")
     *
     * @param name 字典标识
     * @param code  选项编码
     */
    public static String getValue(String name, String code) {
        try {
            Map<String, String> list = DictUtil.value(name);
            if (list != null) {
                return list.get(code) + "";
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }

    }

}
