package com.jcca.common.utils;

import com.jcca.admin.biz.enums.AlarmTemplateEnum;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;

import java.util.List;

/**
 * 告警是否显示'[已恢复]'工具类
 *
 * @author syt
 * @date 2021/5/17
 */
public class AlarmUtils {

    /**
     * 单条数据判断是否显示'[已恢复]'字样,
     * 根据告警分类{@code alarmCategory}、告警级别{@code alarmLevel}、告警状态{@code alarmState}去判断
     *
     * @param alarmCategory: 告警类型
     * @param alarmLevel:    告警级别
     * @param alarmState:    恢复状态
     * @return: java.lang.Integer
     * @Author: syt
     * @Date: 2021/5/17
     */
    public static Integer isShowRecover(String alarmCategory, Integer alarmLevel, Byte alarmState) {
        // 卡斯柯主备不显示(必须同时符合卡斯柯主备和信息通知两个条件)
        if (AlarmTemplateEnum.CASCO_MASTER_CHANGE.getCode().equals(alarmCategory)
                & AlarmLevelEnum.LEVEL_MSG.getCode().equals(alarmLevel) ) {
            return 0;
        }
        // 卡斯柯版本变更不显示
        if (AlarmTemplateEnum.CASCO_CHANGE_VERSION.getCode().equals(alarmCategory)) {
            return 0;
        }
        // snmp不显示
        if (AlarmTemplateEnum.SNMP.getCode().equals(alarmCategory)) {
            return 0;
        }
        // 未恢复的告警不显示
        if (AlarmStateEnum.ALARM.getCode() == alarmState) {
            return 0;
        }
        return 1;
    }

    /**
     * 判断特定集合:{@code List<AlarmUnconfirmVo>}是否显示'[已恢复]'字样
     *
     * @param list:List<AlarmUnconfirmVo> 告警未确认集合
     * @Author: syt
     * @Date: 2021/5/17
     */
    public static List<AlarmUnconfirmVo> isShowRecover(List<AlarmUnconfirmVo> list) {
        if (list.isEmpty() || list.size() <= 0) {
            return list;
        }
        return list;
    }
}
