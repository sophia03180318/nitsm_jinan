package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author HanHW
 * @description 巡检管理实体
 * @className InspectAsset
 * @date 2025/5/19 11:38
 * @since 2.1.6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("INSPECT_ASSET")
public class InspectAsset extends Model<InspectAsset> implements Serializable {

    private static final long serialVersionUID = -2265325783573462871L;


    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    private String jobId;
    private String assetId;
    private String assetName;
    // 事件类型ID
    private String eventTypeId;
    private String eventTypeName;
    private String assetIp1;
    private String assetIp2;
    private String orgId;
    private String orgName;
    private String roomId;
    private String roomName;
    private String cabinetId;
    private String cabinetName;
    // 巡检指标项
    private String targetItem;
    // 巡检指标名称
    private String targetName;
    // 1待巡检，2正在巡检，3巡检正常，4巡检异常  Web2Const
    private String inspectState;
    // 阈值设定值
    private String thresholdValue;
    // 巡检结果值
    private String inspectValue;
    // 巡检结果描述
    private String resultMsg;
    // 1设备巡检，2指标巡视
    private Integer inspectType;
    // 巡检命令
    private String command;
    private String remark;
    // 资产小类型
    private Integer assetDesk;
    // 资产小类型名称
    private String deskName;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(value = "MODIFY_TIME", fill = FieldFill.UPDATE)
    private Date modifyTime;

    @TableField(value = "MODIFIER", fill = FieldFill.UPDATE)
    private String modifier;

    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    @TableField(exist = false)
    private String inspectRecordId;
    @TableField(exist = false)
    private String alarmId;
    @TableField(exist = false)
    private String eventCategory;
    @TableField(exist = false)
    private Byte ntpFlag;
}
