package com.jcca.common.bean.constant;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 厂商明称标记
 * 用来判断是不是此厂商
 */
public class AssetManufacturerNameFlagConst {

    /**
     * 思科厂商关键字
     */
    public static final List<String> CISCO = Arrays.asList("思科","cisco");
    /**
     * 华为关键字
     */
    public static final List<String> HUA_WEI = Arrays.asList("华为","huawei");
    /**
     * 惠普关键字
     */
    public static final List<String> HUI_PU = Arrays.asList("惠普","huipu","hp");
    /**
     * IBM关键字
     */
    public static final List<String> IBM = Arrays.asList("ibm","International Business Machines Corporation","国际商业机器公司","internationalbusinessmachinescorporation");
    /**
     * 华三
     */
    public static final List<String> H3C = Arrays.asList("h3c","新华三","华三");
    /**
     * 研华
     */
    public static final Collection<String> YAN_HUA = Arrays.asList("研华","advantech");
}

