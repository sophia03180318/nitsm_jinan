package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ Author：sophia
 * @ Date：Created in 14:48 2021/7/13
 * @ Description:资产导入失败记录导出模板
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ASSET_IMPORT_RECORD")
public class AssetImportRecord extends Model<AssetImportRecord> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * AB机标识
     */
    @TableField("ABFLAG")
    private String aBFlag;
    /**
     * 是否采集系统时间
     */
    @TableField("NTPFLAG")
    private String ntpFlag;
    /**
     * 是否topo显示
     */
    @TableField("SHOWTOPO")
    private String showTopo;

    /**
     * 是否是核心设备
     */
    @TableField("SHOW_CORE")
    private String showCore;

    /**
     * 是否监控
     */
    @TableField("WATCH")
    private String watch;
    /**
     * 下架时间
     */
    @TableField("DOWNLINETIME")
    private String downlineTime;
    /**
     * 上架时间
     */
    @TableField("ONLINETIME")
    private String onlineTime;
    /**
     * 质保期限
     */
    @TableField("VALIDITYDATE")
    private String validityDate;
    /**
     * 资产类型
     */
    @TableField("ASSETMODE")
    private String assetMode;
    /**
     * 采集类型
     */
    @TableField("COLLECTIONTYPE")
    private String collectionType;
    /**
     * CPU核数
     */
    @TableField("CPUCORENUMBER")
    private String cpuCoreNumber;
    /**
     * CPU个数
     */
    @TableField("CPUNUMBER")
    private String cpuNumber;
    /**
     * 硬盘个数
     */
    @TableField("DISKTOTAL")
    private String diskTotal;
    /**
     * 结束位置
     */
    @TableField("ENDPOSITION")
    private String endPosition;
    /**
     * 登录端口
     */
    @TableField("LOGINPORT")
    private String loginPort;
    /**
     * 资产厂商
     */
    @TableField("MANUFACTURERID")
    private String manufacturerId;
    /**
     * 电源模块个数
     */
    @TableField("POWERTOTAL")
    private String powerTotal;
    /**
     * 起始位置
     */
    @TableField("STARTPOSITION")
    private String startPosition;
    /**
     * 资产编号
     */
    @TableField("ASSETCODE")
    private String assetCode;
    /**
     * 资产型号
     */
    @TableField("ASSETIMAGE")
    private String assetImage;
    /**
     * 供货商
     */
    @TableField("ASSETSUPPLIER")
    private String assetSupplier;
    /**
     * 机柜
     */
    @TableField("CABINETID")
    private String cabinetId;
    /**
     * CPU主频
     */
    @TableField("CPUFREQUENCY")
    private String cpuFrequency;
    /**
     * CPU型号
     */
    @TableField("CPUMODEL")
    private String cpuModel;
    /**
     * 单硬盘容量
     */
    @TableField("DISKCAPACITY")
    private String diskCapacity;
    /**
     * 视频线接口类型
     */
    @TableField("DISPLAYERPORTMODEL")
    private String displayerPortModel;
    /**
     * 连接显示器数量
     */
    @TableField("DISPLAYERTOTAL")
    private String displayerTotal;
    /**
     * 主机编号
     */
    @TableField("HOSTNUMBER")
    private String hostNumber;
    /**
     * ip地址1
     */
    @TableField("IP")
    private String ip;
    /**
     * ip地址2
     */
    @TableField("IP2")
    private String ip2;
    /**
     * 管理口ip
     */
    @TableField("IPMIIP")
    private String ipmiIp;
    /**
     * 管理口密码
     */
    @TableField("IPMIPWD")
    private String ipmiPwd;
    /**
     * 管理口账号
     */
    @TableField("IPMIUSER")
    private String ipmiUser;
    /**
     * 登录用户名/团体名
     */
    @TableField("OSUSER")
    private String osUser;
    /**
     * 登录密码
     */
    @TableField("OSPASSWORD")
    private String osPassword;
    /**
     * 内存
     */
    @TableField("MEMORY")
    private String memory;
    /**
     * 资产名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 操作系统版本
     */
    @TableField("OPERATIONSYSTEM")
    private String operationSystem;
    /**
     * 组织机构
     */
    @TableField("ORGID")
    private String orgId;
    /**
     * 电源模块型号
     */
    @TableField("POWERMODEL")
    private String powerModel;
    /**
     * 机房
     */
    @TableField("ROOMID")
    private String roomId;
    /**
     * 主机运行方式
     */
    @TableField("RUNMODEL")
    private String runModel;
    /**
     * 序列号
     */
    @TableField("SERIALNUMBER")
    private String serialNumber;

    /**
     * 业务类型
     */
    @TableField(value = "SERVICE_TYPE_ID")
    private String serviceTypeId;

    /**
     * 错误日志
     */
    @TableField("STATUS")
    private String status;


    /**
     * 错误日志
     */
    @TableField("ERRORLOG")
    private String errorLog;

    /**
     * 监控状态
     */
    @TableField(exist = false)
    private String monitorStatus;
}
