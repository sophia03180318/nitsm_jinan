package com.jcca.web.topo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 网络设备自动发现
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("M_NETWORK_ASSET_MAP")
public class NetworkAssetMap extends Model<NetworkAssetMap> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 发起采集端设备IP
     */
    @TableField("LOCALHOST_IP")
    private String localhostIp;
    /**
     * 索引编号
     */
    @TableField("OID_INDEX")
    private String oidIndex;
    /**
     * 远程设备IP
     */
    @TableField("REMOTE_IP")
    private String remoteIp;
    /**
     * 远程设备端口
     */
    @TableField("REMOTE_PORT")
    private String remotePort;
    /**
     * 更新时间
     */
    @TableField("UPDATE_DATE")
    private Date updateDate;
    /**
     * 远端端口主机标识ID
     */
    @TableField("REMOTE_DEVICE_ID")
    private String remoteDeviceId;
    /**
     * 本地主机端口标识
     */
    @TableField("LOCAL_DEVICE_ID")
    private String localDeviceId;

}
