package com.jcca.web.alarm.service.data;

import lombok.Data;

/**
 * @description: 异常设备查询条件
 * @author: Lvyp
 * @create: 2023/12/07 15:59
 */
@Data
public class AbnormalAssetQuery {

    /**
     * 查询逻辑关系枚举
     */
    public enum StatusLogicalEnum {
        /**
         * 与关系
         */
        AND(1),
        /**
         * 或关系
         */
        OR(2);

        private Integer code;

        public Integer getCode() {
            return code;
        }

        StatusLogicalEnum(Integer code) {
            this.code = code;
        }

    }


    /**
     * 告警大分类Code
     * 必传
     */
    private String eventCategory;
    /**
     * 确认状态
     * AlarmStatusEnum
     * 1未确认  2已确认
     */
    private Integer status;
    /**
     * 告警状态
     * AlarmStateEnum
     * 1告警  2恢复
     */
    private Integer alarmState;
    /**
     * 告警确认状态筛选关系
     * 1and关系
     * 2or关系
     */
    private Integer statusLogical;
    /**
     * 天窗告警标记，1正常时段告警，2天窗时段告警
     * AlarmBlankConst
     */
    private Byte blank;

    /**
     * 是否显示JCCA设备
     * 1显示
     * 2不显示
     */
    private Integer showJcca;

}
