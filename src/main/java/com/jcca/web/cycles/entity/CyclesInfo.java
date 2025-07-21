package com.jcca.web.cycles.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @description: 周期计划信息
 * @author: Lvyp
 * @create: 2024/11/20 09:34
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cycles_info")
public class CyclesInfo extends Model<CyclesInfo> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 周期id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 周期名称
     */
    private String cyclesName;
    /**
     * 备注
     */
    private String remark;
    /**
     * 1 启用
     * -1 禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 计划包含的任务
     */
    @TableField(exist = false)
    private List<CyclesOrg> orgList;
    /**
     * 包含的施工时间
     */
    @TableField(exist = false)
    private List<CyclesTimes> timesList;

}
