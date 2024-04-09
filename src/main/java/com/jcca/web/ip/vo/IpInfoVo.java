package com.jcca.web.ip.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ip信息
 *
 * @author lyp
 */
@Data
public class IpInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * ip
     */
    private String ip;
    /**
     * mac 地址
     */
    private String mac;
    /**
     * 掩码
     */
    private String mask;
    /**
     * 网关
     */
    private String gateway;
    /**
     * 状态
     */
    private Byte status;
    /**
     * 状态 翻译后的内容
     */
    private String statusStr;
    /**
     * 审核状态
     */
    private Integer authStatus;

    /**
     * 审核状态翻译后
     */
    private String authStatusStr;
    /**
     * ping 占用状态
     */
    private Byte pingStatus;
    /**
     * ping占用状态翻译后的内容
     */
    private String pingStatusStr;
    /**
     * 资产名称
     */
    private String assetName;

    private Integer assetMode;

    /**
     * 上次ping 的时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date pingDate;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;


    /**
     * 审核人
     */
    private String authUserId;
    /**
     * 审核时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date authDate;


}
