package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * 进程阈值
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("THRESHOLD_PROCESS")
@Component("ThresholdProcess")
public class ThresholdProcess extends Model<ThresholdProcess> {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 进程配置名称
     */
    @TableField("PROCESS_NAME")
    private String processName;
    /**
     * 进程ID
     */
    @TableField("PROCESS_ID")
    private String processId;
    /**
     * cpu阈值
     */
    @TableField("THRESHOLD_CPU")
    private String thresholdCpu;
    /**
     * 内存阈值
     */
    @TableField("THRESHOLD_MEMORY")
    private String thresholdMemory;
    /**
     * 进程状态
     * StatusEnum
     * 1正常，0不正常
     */
    @TableField("COLLECT_STATUS")
    private Byte collectStatus;
    /**
     * 当前cpu使用率
     */
    @TableField("CPU_RATE")
    private String cpuRate;
    /**
     * 当前内存使用率
     */
    @TableField("MEMORY_RATE")
    private String memoryRate;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;

    @TableField("REMARK")
    private String remark;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    /**
     * 模式 1双击单活 2双机双活 其他普通
     */
    @TableField(value = "HOST_MODE")
    private Integer hostMode;

    @TableField(exist = false)
    private String collectStatusStr;

    /**
     * 判定进程是否是普通模式
     *
     * @return
     */
    public boolean hostModeIsPub() {
        if (Objects.isNull(hostMode)) {
            return true;
        }
        if (1 == hostMode || 2 == hostMode) {
            return false;
        }
        return true;
    }

}
