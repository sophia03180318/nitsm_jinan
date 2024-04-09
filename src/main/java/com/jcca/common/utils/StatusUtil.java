package com.jcca.common.utils;

import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.exception.ResultException;

/**
 * 数据状态工具
 *
 * @date 2019/2/19
 */
public class StatusUtil {

    /**
     * 获取状态StatusEnum对象
     *
     * @param param 状态字符参数
     */
    public static StatusEnum getStatusEnum(String param) {
        try {
            return StatusEnum.valueOf(param.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResultException(ResultEnum.STATUS_ERROR);
        }
    }
}
