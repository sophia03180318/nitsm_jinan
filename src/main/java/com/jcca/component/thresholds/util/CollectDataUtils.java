package com.jcca.component.thresholds.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ValidatorUtils;
import com.jcca.common.utils.constants.AppLogHead;
import lombok.extern.slf4j.Slf4j;

/**
 * 采集器数据校验
 *
 * @author Lvyp
 */
@Slf4j
public class CollectDataUtils {

    /**
     * 校验采集入参数据
     *
     * @param reqData
     * @param type    {@link AppLogHead}
     * @return
     */
    public static Boolean verifyReq(Object reqData, String type) {
        String validateReq = ValidatorUtils.validateReq(reqData);
        if (StrUtil.isNotEmpty(validateReq)) {
            log.error(AppLogUtils.logStr(type, "推送数据校验出错" + validateReq,
                    JSONUtil.toJsonStr(reqData)));
            return false;
        }
        return true;
    }

}
