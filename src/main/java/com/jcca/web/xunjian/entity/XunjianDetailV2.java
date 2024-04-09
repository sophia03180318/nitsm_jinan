package com.jcca.web.xunjian.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.Objects;


/**
 * 巡检V2版本详情
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("xunjian_detail_v2")
public class XunjianDetailV2  extends Model<XunjianDetailV2> implements java.io.Serializable {

    /**
     * 正常标记
     */
    public static final Integer NORMAL_FLAG = 0;
    public static final String NORMAL_FLAG_STR = "正常";
    /**
     * 异常标记
     */
    public static final Integer EXCEPTION_FLAG = 1;
    public static final String EXCEPTION_FLAG_STR = "异常";
    /**
     * 需二次确认标记
     */
    public static final Integer UN_KNOW_FLAG = 2;
    public static final String UN_KNOW_FLAG_STR = "二次确认";
    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 巡检记录ID
     */
    @TableField("XUNJIAN_RECORD_ID")
    private String xunjianRecordId;
    /**
     * 设备ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 设备型号
     */
    @TableField("ASSET_MODE")
    private Integer assetMode;
    /**
     * 设备名称
     */
    @TableField("ASSET_NAME")
    private String assetName;
    /**
     * 巡检指标项
     * XunJianTargetEnum
     */
    @TableField("XUNJIAN_TARGET_ITEM")
    private String xunjianTargetItem;
    /**
     * 巡检适配器
     */
    @TableField("XUNJIAN_ADAPTER")
    private String xunjianAdapter;
    /**
     * 有无异常，
     * 需要展示的字段
     */
    @TableField("NORMAL_FLAG_Str")
    private String normalFlagStr;
    /**
     * 有无异常，标记位
     * 0正常 1异常 2需二次确认
     */
    @TableField("NORMAL_FLAG")
    private Integer normalFlag;
    /**
     * 巡检抓取到的异常项
     */
    @TableField(value = "INPUT_ERROR_STR",updateStrategy=FieldStrategy.IGNORED,jdbcType = JdbcType.VARCHAR)
    private String inputErrorStr;
    /**
     * 使用的命令
     */
    @TableField("COMMAND")
    private String command;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;

    /**
     * 巡检输原始出项
     */
    @TableField(exist = false)
    private String inputOrgStr;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;

    public static String getNormalFlagStr(Integer normalFlag){
        if(Objects.isNull(normalFlag)){
            return null;
        }
        if(normalFlag == NORMAL_FLAG){
            return NORMAL_FLAG_STR;
        }else if(normalFlag == EXCEPTION_FLAG){
            return EXCEPTION_FLAG_STR;
        }else if(normalFlag == UN_KNOW_FLAG){
            return UN_KNOW_FLAG_STR;
        }

        return null;
    }

}
