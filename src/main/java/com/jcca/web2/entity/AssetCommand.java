package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.common.utils.MyIdUtil;
import lombok.Data;

import java.util.Date;

/**
 * @author HanHW
 * @description 资产采集命令
 * @className AssetCommand
 * @date 2024/3/7 14:37
 * @since 2.1.0.0
 */
@Data
@TableName("ASSET_COMMAND")
public class AssetCommand {

    public AssetCommand() {
    }

    public AssetCommand(PerformanceTarget target, AssetModel model, String assetImage) {
        this.id = MyIdUtil.getId();
        this.assetMode = target.getAssetMode();
        this.assetImage = assetImage;
        this.targetDescription = target.getTargetDescription();
        this.targetHandle = target.getTargetHandle();
        this.cronExpress = target.getCronExpress();
//        this.execution = target.getExecution();
        this.systemType = model.getCollectionType();
        this.manufacturerId = model.getManufacturerId();
    }

    @TableId(value = "ID")
    private String id;

    private Integer assetMode;
    private String assetImage;
    private Long manufacturerId;
    private Integer systemType;
    private String targetDescription;
    private String targetHandle;
    private String cronExpress;
    private String execution;
    private Integer isAvailable;
    private String remark;

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
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;
}
