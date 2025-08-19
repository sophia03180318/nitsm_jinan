package com.jcca.dataProcessing.Entity;


import lombok.Data;

/**
 * 数据库日志归档方式
 *
 */
@Data
public class DbLogSettingEntity {

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
    private String settingName;
    /**
     * 配置值
     */
    private String settingValue;
    /**
     * 单位
     */
    private String unit;

}
