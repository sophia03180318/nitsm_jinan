package com.jcca.web2.dto.xunjian;


import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.enums.xunjian.InspectionStatus;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author lifp
 * @version 1.0
 * @description: 用于封装 type 参数解析结果的 DTO
 * @date 2025-10-29 星期三 14:33:56
 */
@Data
public class TypeParseResult {
    private final String deskCode;      // 前半部分：如 "183"
    private final String totalType;     // 后半部分：如 "warningTotal （告警）", "abNormalTotal(异常)"
    private final String queryStatus;   // 根据 totalType 转换的状态码

    public TypeParseResult(String deskCode, String totalType, String queryStatus) {
        this.deskCode = deskCode;
        this.totalType = totalType;
        this.queryStatus = queryStatus;
    }

    // 静态工厂方法
    public static TypeParseResult empty() {
        return new TypeParseResult(null, Web2Const.TOTAL_TYPE_ALL, "-1");
    }

    /**
     * 解析 type 参数（格式：deskCode_totalType）
     *
     * @param type 为 null - 全部 、"183_all", "183_abnormal"
     * @return 解析结果
     */
    public static TypeParseResult parseTypeParam(String type) {
        if (!StringUtils.hasText(type)) {
            return TypeParseResult.empty();
        }

        String[] parts = type.split("_", 2); // 最多分两段
        String deskCode = parts[0];
        String totalType;
        String queryStatus;

        if (parts.length == 2) {
            totalType = parts[1];
        } else {
            // abNormalTotal 异常
            totalType = deskCode;
        }

        queryStatus = parseStatusFromTotalType(totalType);

        return new TypeParseResult(deskCode, totalType, queryStatus);
    }

    /**
     * 根据 totalType 获取查询状态码
     */
    private static String parseStatusFromTotalType(String totalType) {
        switch (totalType) {
            case Web2Const.TOTAL_TYPE_ABNORMAL:
                return InspectionStatus.INSPECT_ERROR.getCode();
            case Web2Const.TOTAL_TYPE_WARNING:
                return InspectionStatus.INSPECT_ALARM.getCode();
            default:
                return "-1"; // 查询全部
        }
    }
}