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
 * @description 巡检管理详情
 * @className InspectRecord
 * @date 2023/11/16 11:38
 * @since 2.1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("INSPECT_DETAIL")
public class InspectDetail extends Model<InspectDetail> implements Serializable {

    private static final long serialVersionUID = -2265325783573462872L;


    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    private String assetId;
    private String assetName;
    // 巡检设备类型
    private String modeType;
    // 巡检类型名称
    private String modeName;
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
    // 巡检时间
    private Date inspectTime;
    // 1自动巡检，2日常巡视
    private Integer inspectType;
    // 巡检命令
    private String command;
    // 一次巡检一个代码
    private String inspectCode;
    // 资产选中状态，0未选中，1选中
    private Integer assetStatus;
    // 指标选中状态，0未选中，1选中
    private Integer targetStatus;
    private String remark;
    // 资产小类型
    private Integer assetDesk;
    // 资产小类型名称
    private String deskName;
    // 巡检结果文件路径
    private String resultPath;
    // 告警ID
    private String alarmId;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(value = "MODIFY_TIME", fill = FieldFill.UPDATE)
    private Date modifyTime;

    @TableField(value = "MODIFIER", fill = FieldFill.UPDATE)
    private String modifier;

    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;


}
