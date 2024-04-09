package com.jcca.common.utils;

import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.util.Objects;


/**
 * 常用运算
 *
 * @author Lvyp
 */
public class AppMathUtil {

    /**
     * 百分比计算
     *
     * @return 百分比单位的小数
     */
    public static String percentage(Long dv1, Long dv2, Integer scale) {
        if (dv1.longValue() == 0 || dv2.longValue() == 0) {
            return "0";
        }
        BigDecimal usedRate = new BigDecimal(dv1).divide(new BigDecimal(dv2), scale, BigDecimal.ROUND_HALF_UP);
        return usedRate.multiply(new BigDecimal(100)).doubleValue() + "";
    }

    /**
     * 百分比计算
     *
     * @return 百分比单位的小数
     */
    public static Double percentageDouble(Long dv1, Long dv2, Integer scale) {
        if (Objects.isNull(dv1)||Objects.isNull(dv2)||dv1.longValue() == 0 || dv2.longValue() == 0) {
            return 0d;
        }
        BigDecimal usedRate = new BigDecimal(dv1).divide(new BigDecimal(dv2), scale, BigDecimal.ROUND_HALF_UP);
        return usedRate.multiply(new BigDecimal(100)).doubleValue();
    }

    public static String div(Long dv1, Long dv2, Integer scale) {
        Long retunFlgh = 0L;
        if (retunFlgh.equals(dv1) || retunFlgh.equals(dv2)) {
            return "0";
        }
        BigDecimal div = new BigDecimal(dv1).divide(new BigDecimal(dv2), scale, BigDecimal.ROUND_HALF_UP);
        return div.toString();
    }

    /**
     * 比较大小
     *
     * @return V1大于 V2 true
     * 否则 false
     */
    public static Boolean compare(String v1, String v2) {
        if (StrUtil.isEmpty(v1) || StrUtil.isEmpty(v2)) {
            return false;
        }

        int result = new BigDecimal(v1).compareTo(new BigDecimal(v2));
        return result == 1;
    }

    /**
     * 计算减法
     * @param subtrahend
     * @param minuend
     * @return
     */
    public static Long sub(String subtrahend,String minuend){
        BigDecimal subtract = new BigDecimal(subtrahend).subtract(new BigDecimal(minuend));
        return subtract.longValue();
    }
}
