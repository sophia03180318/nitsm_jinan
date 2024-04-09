package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 资产模板表
 *
 * @author syt
 * @date 2021-04-29 15:51:48
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_template")
public class AssetTemplate extends Model<AssetTemplate> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 资产id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 用户id
     */
    @TableField("USER_ID")
    private String userId;
    /**
     * 资产别名
     */
    @TableField("NAME")
    @Length(max = 64, message = "设备名字不能超过60个字符")
    private String name;
    /**
     * 资产编号
     */
    @TableField("ASSET_CODE")
    private String assetCode;
    /**
     * 20210112hanwone
     * 用于区分设备类型的细分，在原服务器183后面追加数字1，2，3
     * 1终端，2小型机，3工控机
     * 最终存为1831，1832，1833
     * 原设备类型不变存入该字段
     */
    @TableField("DESK")
    private Integer desk;
    /**
     * 调度台
     * 主机编号
     */
    @TableField("HOST_NUMBER")
    private String hostNumber;
    /**
     * 调度台
     * 连接显示器数量
     */
    @TableField("DISPLAYER_TOTAL")
    private String displayerTotal;
    /**
     * 调度台
     * 视频接口类型
     */
    @TableField("DISPLAYER_PORT_MODEL")
    private String displayerPortModel;
    /**
     * Ip地址
     */
    @TableField("IP")
    //@Pattern(message = "ip地址格式不正确", regexp = "^((25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)($|(?!\\.$)\\.)){4}$")
    private String ip;
    /**
     * 设备登录账号
     */
    @TableField("OS_USER")
    private String osUser;
    /**
     * 设备登录密码
     */
    @TableField("OS_PASSWORD")
    private String osPassword;
    /**
     * 登录用户名
     */
    @TableField("LOGIN_NAME")
    private String loginName;
    /**
     * 登录密码
     */
    @TableField("LOGIN_PWD")
    private String loginPwd;
    /**
     * 登录端口
     */
    @TableField("LOGIN_PORT")
    private Integer loginPort;

    /**
     * 管理口IP
     */
    @TableField("IPMI_IP")
    private String ipmiIp;
    /**
     * 管理口用户
     */
    @TableField("IPMI_USER")
    private String ipmiUser;
    /**
     * 管理口密码
     */
    @TableField("IPMI_PWD")
    private String ipmiPwd;

    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    @TableField("ASSET_MODE")
    @NotNull(message = "设备类型不可为空")
    private Integer assetMode;
    /**
     * 设备状态(0：停用，1：启用)
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 序列号
     */
    @TableField(value = "SERIAL_NUMBER", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String serialNumber;
    /**
     * 运行模式
     * AssetRunModelEnum
     */
    @TableField(value = "RUN_MODEL", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String runModel;
    /**
     * 电源模块型号
     */
    @TableField(value = "POWER_MODEL", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String powerModel;
    /**
     * 电源模块数量
     */
    @TableField(value = "POWER_TOTAL", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private Integer powerTotal;
    /**
     * 内存
     */
    @TableField(value = "MEMORY", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String memory;
    /**
     * 磁盘数量
     */
    @TableField(value = "DISK_TOTAL", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private Integer diskTotal;
    /**
     * 单磁盘容量
     */
    @TableField(value = "DISK_CAPACITY", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String diskCapacity;
    /**
     * 生产厂商ID
     */
    @TableField("MANUFACTURER_ID")
    @NotNull(message = "生产厂商不可为空")
    private Integer manufacturerId;
    /**
     * 资产供货商
     * ManufacturersEnum
     */
    @TableField(value = "ASSET_SUPPLIER", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String assetSupplier;
    /**
     * 组织ID
     */
    @TableField("ORG_ID")
    private String orgId;
    /**
     * 资产位置
     */
    @TableField(exist = false)
    private String assetSite;
    /**
     * 操作系统名称
     */
    @TableField(value = "OPERATION_SYSTEM", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String operationSystem;
    /**
     * 上架时间
     */
    @TableField(value = "ONLINE_TIME", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date onlineTime;
    /**
     * 下架时间
     */
    @TableField(value = "DOWNLINE_TIME", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date downlineTime;
    /**
     * 跳转路径
     */
    @TableField("SKIP_URL")
    private String skipUrl;
    /**
     * 采集操作系统类型
     */
    @TableField("COLLECTION_TYPE")
    private Integer collectionType;
    /***
     * 采集协议
     */
    @TableField("PROTOCOL_TYPE")
    private Byte protocolType;
    /**
     * 主备信息(主:1,备:0)
     * AssetHostTypeConst
     */
    @TableField("INITATIVE")
    private Byte initative;
    /**
     * 设备图片
     */
    @TableField("ASSET_IMAGE")
    @NotEmpty(message = "设备型号不可为空")
    private String assetImage;
    /**
     * CPU个数
     */
    @TableField(value = "CPU_NUMBER", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private Integer cpuNumber;
    /**
     * CPU核数
     */
    @TableField(value = "CPU_CORE_NUMBER", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private Integer cpuCoreNumber;
    /**
     * CPU型号
     */
    @TableField(value = "CPU_MODEL", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String cpuModel;
    /**
     * CPU主频
     */
    @TableField(value = "CPU_FREQUENCY", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String cpuFrequency;
    /**
     * Ip地址
     */
    @TableField(value = "IP2", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String ip2;
    /**
     * AB机标识（1=B机 0=A机）
     */
    @TableField(value = "A_B_FLAG", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private Byte aBFlag;
    /**
     * 数据状态
     * 1-正常，3已删除
     */
    @TableField("IS_DEL")
    private Byte isDel;
    /**
     * 是否显示在拓扑图中
     * 0-不显示，1-显示
     */
    @TableField("SHOW_TOPO")
    private Byte showTopo;
    /**
     * 是否开启ntp采集
     * 0-不采集，1-采集
     */
    @TableField("NTP_FLAG")
    private Byte ntpFlag;
    /**
     * 设备是否监控
     * 0-不监控，1-监控
     */
    @TableField("WATCH")
    private Byte watch;
    /**
     * 质保期限
     */
    @TableField(value = "VALIDITY_DATE", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date validityDate;
    /**
     * 资产在机柜中起始位置
     */
    @TableField(exist = false)
    private Integer startPosition;
    /**
     * 资产在机柜中结束位置
     */
    @TableField(exist = false)
    private Integer endPosition;
    /**
     * 资产阈值设置类型
     * ThresholdAutoFlagEnum
     */
    @TableField(exist = false)
    private String autoFlag;
    /**
     * 端口
     */
    @TableField(exist = false)
    private Integer port;

    @TableField(exist = false)
    private String alarmMode;

}