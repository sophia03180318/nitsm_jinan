package com.jcca.web.xunjian.adapter.v1.util;

/**
 * 报告生成
 *
 * @author Lvyp
 */
public class TemplateUtil {

    /**
     * 获取阈值类型报告
     *
     * @param threshold
     * @param xunjianValue
     * @return
     */
    public static String getThresholdTemp(Double threshold, Double xunjianValue, Boolean isMore, Boolean havaBF) {
        String result = " 异常！";
        if (threshold > xunjianValue) {
            result = " 正常！";
        }
        String thresholdStr = threshold + "";
        String xunjianValueStr = xunjianValue + "";
        if (havaBF) {
            thresholdStr = thresholdStr + "%";
            xunjianValueStr = xunjianValueStr + "%";
        }
        if (isMore) {
            return String.format("【设定阈值】：%s,【当前最大值】：%s。<br/>【结果】：%s", thresholdStr, xunjianValueStr, result);
        }
        return String.format("【设定阈值】：%s,【当前值】：%s。<br/>【结果】：%s", thresholdStr, xunjianValueStr, result);
    }

    /**
     * 网卡
     *
     * @param b
     * @return
     */
    public static String getNetWorkStatusTemp(boolean b) {
        if (b) {
            return "该设备网卡状态正常！";
        } else {
            return "该设备存在断开状态网卡，异常！";
        }

    }

}
