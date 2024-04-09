package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 区间阈值
 * 权重：区间阈值>资产阈值>通用阈值
 *
 * @author lyp
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("THRESHOLD_SECTION")
public class ThresholdSection extends Model<ThresholdSection> {

    private static final long serialVersionUID = 1L;
    /**
     * 区间阈值ID
     */
    @TableId(value = "SECTION_ID", type = IdType.ID_WORKER_STR)
    private Long sectionId;
    /**
     * 阈值类型
     * ThresholdSectionEnum
     */
    @NotEmpty(message = "前端问题：接口缺少阈值类型值")
    @TableField("TYPE")
    private String type;
    /**
     * 资产ID
     */
    @NotEmpty(message = "前端问题：接口缺少资产ID")
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 标识
     * 端口索引之类的
     */
    @TableField("FLAG")
    private String flag;
    /**
     * 设定的最小值
     */
    @NotNull(message = "前端问题：接口设定的最小值")
    @TableField("MIN_PRICE")
    private Double minPrice;
    /**
     * 设定的最大值
     */
    @NotNull(message = "前端问题：接口缺少设定的最大值")
    @TableField("MAX_PRICE")
    private Double maxPrice;
    /**
     * 1启用
     * -1禁用
     */
    @TableField("STATUS")
    private Integer status;
    /**
     * 创建时间
     */
    @TableField("CREATE_DATE")
    private Date createDate;
    /**
     * 更新时间
     */
    @TableField("UPDATE_DATE")
    private Date updateDate;


}
