package com.jcca.web.config.vo;

import com.jcca.common.bean.constant.AlarmLevelConst;
import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.web2.dto.DialogsAlarmListDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ZYH
 * @ Author：sophia
 * @ Date：Created in 10:13 2023/6/28
 * @ Description:
 */
@Data
public class SysConfig {
    private static final long serialVersionUID = 1L;

    public static final String YES = "yes";
    public static final String NO = "no";
    public static final Byte zero = 0;

    /**
     * 是否开启声音
     */
    private String broadcast = "yes";

    /**
     * 是否显示中航设备
     */
    private String showJcca = "yes";

    /**
     * 是否开启连续播报
     */
    private String continuous = "no";

    /**
     * 是否告警弹窗
     */
    private String popup = "yes";

    /**
     * 确认状态
     */
    private String affirmStatus = "yes";

    /**
     * 恢复状态
     */
    private String recoveredStatus = "yes";

    /**
     * 是否播报一级告警告
     */
    private String firstLevel = "yes";

    /**
     * 是否播报二级告警告
     */
    private String secondLevel = "no";

    /**
     * 是否播报三级告警告
     */
    private String thirdLevel = "no";

    private List<Integer> level;


    /**
     * 获取需要查询的告警等级
     *
     * @return
     */
    public List<Byte> getConfigAlarmLevelList() {
        List<Byte> levelList = new ArrayList<>();
        if (YES.equals(firstLevel)) {
            levelList.add(AlarmLevelEnum.LEVEL_ONE.getCode());
        }
        if (YES.equals(secondLevel)) {
            levelList.add(AlarmLevelEnum.LEVEL_TWO.getCode());
        }
        if (YES.equals(thirdLevel)) {
            levelList.add(AlarmLevelEnum.LEVEL_THREE.getCode());
        }
        if (levelList.isEmpty()) {
            return null;
        }
        return levelList;
    }

    /**
     * 判定当前是否可以播放告警
     *
     * @param level       告警级别
     * @param status      确认状态
     * @param alarmStatus 告警状态
     * @param isJcca      是否是jcca厂家设备
     * @return
     * @auth Lvyp
     */
    public boolean canPlay(int level, int status, int alarmStatus, boolean isJcca) {

        if (YES.equals(affirmStatus)) {
            if (AlarmStatusEnum.UNCONFIRM.getCode().intValue() != status) {
                return false;
            }
        }
        if (YES.equals(recoveredStatus)) {
            if (AlarmStateEnum.ALARM.getCode() != alarmStatus) {
                return false;
            }
        }
        if (!YES.equals(showJcca) && isJcca) {
            //配置不推送jcca告警 当前是jcca的设备
            return false;
        }

        if (AlarmLevelConst.SERIOUS_LEVEL == level) {
            return YES.equals(firstLevel);
        }
        if (AlarmLevelConst.MIDDLE_LEVEL == level) {
            return YES.equals(secondLevel);
        }
        if (AlarmLevelConst.LIGHT_LEVEL == level) {
            return YES.equals(thirdLevel);
        }

        return false;
    }

    public Boolean showJcca(){
        return YES.equals(this.showJcca);
    }

    /**
     * 查询告警筛选条件
     *
     * @return
     * @auth Lvyp
     */
    public DialogsAlarmListDto getAlarmQueryDto() {
        DialogsAlarmListDto dto = new DialogsAlarmListDto();
        dto.setStatusLogical(1);
        if (YES.equals(affirmStatus)) {
            dto.setStatus(AlarmStatusEnum.UNCONFIRM.getCode().intValue());
        }
        if (YES.equals(recoveredStatus)) {
            dto.setAlarmState((int) AlarmStateEnum.ALARM.getCode());
        }
        if (!YES.equals(affirmStatus) && !YES.equals(recoveredStatus)) {
            //一个也没开启，随便放入一个值让查出来是空列表
            dto.setAlarmState(99);
        }
        if (YES.equals(showJcca)) {
            dto.setShowJcca(1);
        } else {
            dto.setShowJcca(2);
        }
        List<Byte> configAlarmLevelList = getConfigAlarmLevelList();
        if (Objects.isNull(configAlarmLevelList)) {
            configAlarmLevelList = new ArrayList<>();
        }
        configAlarmLevelList.add(zero);

        dto.setAlarmLevelList(configAlarmLevelList);


        return dto;
    }

    /**
     * 判定是否可以告警
     *
     * @return
     */
    public Boolean canPoup() {

        if (!YES.equals(firstLevel) && !YES.equals(secondLevel) && !YES.equals(thirdLevel)) {
            return false;
        }
        if (!YES.equals(affirmStatus) && !YES.equals(recoveredStatus)) {
            return false;
        }

        return true;
    }

}
