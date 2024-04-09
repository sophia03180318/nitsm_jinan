package com.jcca.common.bean.constant;

/**
 * @ClassName AlarmToRecordConst
 * @Description 告警转故障记录状态
 * @Date 2020/6/9 15:51
 * @Author hanwone
 */
public interface AlarmToRecordConst {

    /**
     * 告警未转为故障记录
     */
    byte UNTRANSFORM = 0;
    /**
     * 告警已转为故障记录
     */
    byte TRANSFORMED = 1;
}
