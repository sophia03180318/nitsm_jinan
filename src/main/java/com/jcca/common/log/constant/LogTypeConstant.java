package com.jcca.common.log.constant;

/**
 * @author GodWone
 * @description 日志动作类型常量
 * @className LogTypeConstant
 * @date 2023/2/24 10:49
 * @since 2.0.0.1
 */
public interface LogTypeConstant {
    /**
     * 登录/退出
     */
    String LOGIN_OUT = "0";
    /**
     * 查看
     */
    String QUERY = "1";
    /**
     * 新增
     */
    String ADD = "2";
    /**
     * 修改
     */
    String MODIFY = "3";
    /**
     * 删除
     */
    String REMOVEE = "4";
    /**
     * 下载
     */
    String DOWNLOAD = "5";
    /**
     * 上传
     */
    String UPLOAD = "6";
    /**
     * 确认
     */
    String CONFIRM = "7";
    /**
     * 恢复
     */
    String RECOVER = "8";
    /**
     * 运维日志
     */
    String DEV = "9";

}
