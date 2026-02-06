package com.jcca.common.bean.constant;


/**
 * @author lifp
 * @version 1.0
 * @description: 板卡状态
 * @date 2025-12-30 星期二 14:15:22
 */
public class PcbStatus {
    //  (未知)
    public static final String UNKNOWN = "1";
    //  (正常工作)
    public static final String UP = "2";
    //  (禁用)
    public static final String DISABLED = "3";
    // (诊断失败但基本功能正常)
    public static final String OK_BUT_DIAG_FAILED = "4";

    // 判断是否属于“异常”状态（非 UP）
    public static boolean isAbnormal(String status) {
        return !UP.equals(status);
    }
}
