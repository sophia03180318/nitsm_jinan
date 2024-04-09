package com.jcca.common.utils;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Singleton;

/**
 * @ClassName MyIdUtil
 * @Description ID生成器
 * @Date 2020/4/7 16:02
 * @Author hanwone
 */
public class MyIdUtil {

    /**
     * 返回唯一数字ID
     *
     * @return
     */
    public static String getId() {
        return getSnowFlake().nextIdStr();
    }

    /**
     * 返回ID创建时的毫秒数
     *
     * @param id
     * @return
     */
    public static Long getCreateMills(String id) {
        Assert.notNull(id);
        return getSnowFlake().getGenerateDateTime(Long.parseLong(id));
    }

    /**
     * 多机部署时每台机器上的两个参数不能相同
     * 正例：A机 workerId=1,datacenterId=1
     * B机 workerId=2,datacenterId=2
     *
     * @return
     */
    public static MySnowflake getSnowFlake() {
        return Singleton.get(MySnowflake.class, 2L, 2L);
    }
}
