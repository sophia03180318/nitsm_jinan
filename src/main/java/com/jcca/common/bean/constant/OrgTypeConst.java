package com.jcca.common.bean.constant;

/**
 * @ClassName OrgTypeConst
 * @Description 组织类型常量
 * @Date 2020/6/2 11:52
 * @Author hanwone
 */
public interface OrgTypeConst {

    /**
     * 铁路总公司
     */
    byte PARENT = 0;
    /**
     * 铁路局
     */
    byte GROUP = 1;
    /**
     * 中心
     */
    byte CENTER = 2;
    /**
     * 铁路线
     */
    byte LINE = 3;
    /**
     * 车站
     */
    byte STATION = 4;
    /**
     * 设备
     */
    byte ASSET = 96;
    /**
     * 机柜列
     */
    byte ROW = 97;
    /**
     * 机房
     */
    byte ROOM = 98;
    /**
     * 机柜
     */
    byte CABINET = 99;
}
