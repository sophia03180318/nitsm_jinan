package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 9:52 2021/8/20
 * @ Description:
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SPEC_DICT")
public class SpecDictionary extends Model<SpecDictionary> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 设备类型
     * 183：主机，42：路由器，201：交换机，263：oracle数据库
     */
    @TableField("ASSET_MODE")
    @NotEmpty(message = "适配型号不可为空")
    private Integer assetMode;


    /**
     * 设备型号
     */
    @TableField("ASSET_IMAGE")
    @NotEmpty(message = "适配类型不可为空")
    private String assetImage;

    /**
     * 厂商
     */
    @TableField("MANUFACTURER_ID")
    @NotEmpty(message = "适配厂家不可为空")
    private String manufacturerId;

    /**
     * 采集类型
     * 0:linux,1:window,2:aix,-1其他
     */
    @TableField("SYSTEM_TYPE")
    @NotEmpty(message = "操作系统不能为空")
    private Integer systemType;

    /**
     * 执行代码
     */
    @TableField("SPEC_ID")
    @NotEmpty(message = "执行代码不可为空")
    private Integer specId;


    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_DATE", fill = FieldFill.INSERT)
    private Date createDate;


    public String toData() {
        return assetMode + ",," + assetImage + ",," + manufacturerId + ",, " + systemType + ",," + specId;
    }
}
