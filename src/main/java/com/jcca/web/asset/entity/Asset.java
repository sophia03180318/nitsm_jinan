package com.jcca.web.asset.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.web.asset.utils.enums.ManufacturersEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 资产表
 *
 * @author hanwone
 * @date 2020-04-20 15:37:48
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset")
public class Asset extends Model<Asset> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 资产id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产别名
     */
    @TableField("NAME")
    @NotEmpty(message = "请输入资产名称")
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
    @NotNull(message = "请选择资产小类型")
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
     * 网络设备登录用户名
     * 网络设备OSUSER存的是团体名
     */
    @TableField("LOGIN_NAME")
    private String loginName;
    /**
     * 网络设备登录密码
     * 网络设备OSUSER存的是团体名
     */
    @TableField("LOGIN_PWD")
    private String loginPwd;
    /**
     * 服务器的登录端口取这个字段！！！！
     * 登录端口
     */
    @TableField(value = "LOGIN_PORT", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
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
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库，318:存储)
     */
    @TableField("ASSET_MODE")
    private Integer assetMode;
    /**
     * 设备状态(0：离线，1：在线，2：不监控,4:未知)
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 序列号
     */
    @TableField("SERIAL_NUMBER")
    private String serialNumber;

    /**
     * 二维码识别码
     */
    @TableField("QR_CODE_NUM")
    private String qrCodeNum;
    /**
     * 运行模式
     * AssetRunModelEnum
     */
    @TableField("RUN_MODEL")
    private String runModel;
    /**
     * 电源模块型号
     */
    @TableField("POWER_MODEL")
    private String powerModel;
    /**
     * 电源模块数量
     */
    @TableField("POWER_TOTAL")
    private Integer powerTotal;
    /**
     * 内存
     */
    @TableField("MEMORY")
    private String memory;
    /**
     * 是否是核心设备
     */
    @TableField("SHOW_CORE")
    private String showCore;
    /**
     * 磁盘数量
     */
    @TableField("DISK_TOTAL")
    private Integer diskTotal;
    /**
     * 单磁盘容量
     */
    @TableField("DISK_CAPACITY")
    private String diskCapacity;
    /**
     * 生产厂商ID
     */
    @TableField("MANUFACTURER_ID")
    private Integer manufacturerId;
    /**
     * 资产供货商
     * ManufacturersEnum
     */
    @TableField("ASSET_SUPPLIER")
    private String assetSupplier;
    /**
     * 组织ID
     */
    @TableField("ORG_ID")
    @NotEmpty(message = "请选择资产所属组织")
    private String orgId;
    /**
     * 资产位置
     */
    @TableField(exist = false)
    private String assetSite;
    /**
     * 操作系统名称
     */
    @TableField("OPERATION_SYSTEM")
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
     * 采集操作系统类型  SystemTypeEnum
     * 0:linux,1:windows,2:aix,-1:网络设备
     */
    @TableField("COLLECTION_TYPE")
    private Integer collectionType;
    /***
     * 采集协议 0linux  1windows
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
    private String assetImage;
    /**
     * CPU个数
     */
    @TableField("CPU_NUMBER")
    private Integer cpuNumber;
    /**
     * CPU核数
     */
    @TableField("CPU_CORE_NUMBER")
    private Integer cpuCoreNumber;
    /**
     * CPU型号
     */
    @TableField("CPU_MODEL")
    private String cpuModel;
    /**
     * CPU主频
     */
    @TableField("CPU_FREQUENCY")
    private String cpuFrequency;
    /**
     * Ip地址
     */
    @TableField("IP2")
    private String ip2;
    /**
     * AB机标识（0=A机,1=B机，2=集群）
     */
    @TableField("A_B_FLAG")
    private Byte aBFlag;
    /**
     * 数据状态
     * 1-正常，3已删除
     * StatusConst
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
     * AssetWatchStatusEnum
     */
    @TableField("WATCH")
    private Byte watch;
    /**
     * 质保期限
     */
    @TableField("VALIDITY_DATE")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date validityDate;
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

    /**
     * 设备当前健康度
     */
    @TableField(value = "HEALTH_DEGREE")
    private Double healthDegree;

    /**
     * 设备版本号
     */
    @TableField(value = "ASSET_VERSION")
    private String assetVersion;
    /**
     * 备注
     */
    @TableField(value = "REMARK")
    private String remark;


    /**
     * 监控状态
     * 0异常
     * 1正常
     * 2未知
     * 3不监控
     * <p>
     * AssetMonitorEnum
     */
    @TableField(value = "MONITOR")
    private Integer monitor;

    /**
     * 业务类型ID(BUSINESSSERVICE_SERVICE_TYPE 表ID)
     */
    @TableField(value = "SERVICE_TYPE_ID")
    private String serviceTypeId;

    /**
     * 机房ID
     */
    @TableField(exist = false)
    private String roomId;
    /**
     * 机房名
     */
    @TableField(exist = false)
    private String roomName;
    /**
     * 组织名
     */
    @TableField(exist = false)
    private String orgName;
    /**
     * 机柜ID
     */
    @TableField(exist = false)
    private String cabinetId;
    /**
     * 机柜名
     */
    @TableField(exist = false)
    private String cabinetName;
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
     * 资产在机柜中占用U位数
     */
    @TableField(exist = false)
    private Integer assetUnit;
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
    /**
     * 阈值状态 syt
     * 资产拆分后要显示的字段
     */
    @TableField(exist = false)
    private String threshold;

    /**
     * 监控资产添加时是否有先行校验
     * yes已校验,保存时不再次校验  no未校验,保存时需要进行校验
     * syt
     */
    @TableField(exist = false)
    private String beforeVerify;

    /**
     * 监控状态
     * true是  false否
     * syt
     */
    @TableField(exist = false)
    private String monitorStatus;

    /**
     * 是否是使用模板导入
     */
    @TableField(exist = false)
    private boolean template;

    /**
     * 采集方式（snmp\ssh）
     */
    @TableField(exist = false)
    private String protocolTypeStr;

    @TableField(exist = false)
    private String assetPosition;


    /**
     * 判定是不是主机
     * @return
     */
    public Boolean isServer(){
        if(Objects.isNull(this.assetMode)){
            return false;
        }
        return this.assetMode == 183;
    }

    /**
     * 获取默认端口
     *
     * @return
     */
    public static Integer getDefaultPort(Integer collectionType) {
        List<Integer> linux = Arrays.asList(0, 3);
        List<Integer> aix = Arrays.asList(2, 4);
        if (Objects.isNull(collectionType)) {
            return null;
        }
        if (linux.contains(collectionType)) {
            return 22;
        } else if (aix.contains(collectionType)) {
            return 23;
        }
        return null;
    }

    /**
     * 判定是不是中航设备
     * @author LVYP
     * @return
     */
    public boolean isJccaAsset() {
        if (StrUtil.isEmpty(this.assetSupplier)) {
            return false;
        }
        if (ManufacturersEnum.JCCA.name().equals(this.assetSupplier)) {
            return true;
        }

        return false;
    }


    /**
     * 是否是DS存储
     * 现在DS和其他存储存放的表不再一起
     * @author LVYP
     * @return
     */
    public Boolean isDSRaid(){
        if(StrUtil.isEmpty(this.assetImage)){
            return null;
        }
        return assetImage.startsWith("DS");
    }

    public boolean isNetAsset() {
        if(Objects.isNull(this.assetMode)){
            return false;
        }
        return this.assetMode == 42 || this.assetMode == 201;
    }
}
