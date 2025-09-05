package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * king日志配置采集
 *
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_DB_LOG_SETTING")
public class CollectDbLogSetting extends Model<CollectDbLogSetting> {


    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("COLLECT_DB_ID")
    private String collectDbId;
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 配置名称
     * archive_mode:控制是否启用 WAL 日志归档
     * 取值：off（关闭归档）、on（仅在归档时运行）、always（始终运行，即使在恢复模式）。
     * archive_command:定义归档执行的具体命令，直接反映归档方式（本地 / 远程 / 工具调用）
     * 自定义命令字符串（如'cp %p /archive/%f'），%p表示当前 WAL 文件路径，%f表示文件名。
     * archive_timeout:若指定时间内无新 WAL 片段生成，则强制归档当前日志（用于避免长时间不归档）。
     * 取值：整数（单位秒，默认 0 表示关闭）。
     * archive_library:指定用于归档的外部库（较少使用，通常通过archive_command配置）。
     * 动态库路径（如用于第三方归档插件）。
     */
    @TableField("SETTING_NAME")
    private String settingName;
    /**
     * 配置值
     */
    @TableField("SETTING_VALUE")
    private String settingValue;
    /**
     * 单位
     */
    @TableField("UNIT")
    private String unit;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
