package com.jcca.admin.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 车站版本变更记录
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("M_STATION_VERSION_LOG")
public class StationVersionLog extends Model<StationVersionLog> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.INPUT)
    private String id;

    @TableField("SYS_FILE_ID")
    private String sysFileId;

    @TableField("STATION_ID")
    private String stationId;

    @TableField("VERSION_MSG_ID")
    private String versionMsgId;
    /**
     * 当前的版本
     */
    @TableField("VERSION")
    private String version;
    /**
     * 当前的commitId
     */
    @TableField("COMMIT_ID")
    private String commitId;
    /**
     * 正在更新的版本
     */
    @TableField("FUTURE_VERSION")
    private String futureVersion;
    /**
     * 车站保存名称
     */
    @TableField("JAR_NAME")
    private String jarName;
    /**
     * 更新状态
     * StationVersionStatusEnum
     */
    @TableField("STATUS")
    private String status;
    /**
     * 更新进度
     */
    @TableField("UPDATE_RATE")
    private String updateRate;
    /**
     * 备注信息
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 限速大小 单位KB/s
     */
    @TableField("RATE_LIMI")
    private String rateLimi;
    /**
     * 上传间隔时常单位秒 单块数据上传完成后休眠秒数
     */
    @TableField("INTERVAL_TIME")
    private String intervalTime;
    /**
     * 完成的大小
     */
    @TableField("FINISH_SIZE")
    private Integer finishSize;
    /**
     * 保存路径
     */
    @TableField("SAVE_PATH")
    private String savePath;
    /**
     * 开始时间
     */
    @TableField("START_DATE")
    private Date startDate;
    /**
     * 结束时间
     */
    @TableField("END_DATE")
    private Date endDate;

}
