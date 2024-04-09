package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 小型机IO卡采集
 */
@TableName("COLLECT_AIX_ADAPTER")
public class CollectAIXAdapter extends Model<CollectAIXAdapter> {

    private static final long serialVersionUID = -8015583955701543025L;
    private String id;

    private String assetId;

    private String collectCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;

    /**
     * IO卡名称
     */
    private String adapterName;
    /**
     * 1网口，2光口，3串口，4PCI物理槽位
     */
    private String adapterType;
    /**
     * IO卡状态
     */
    private String adapterStat;
    /**
     * 链路状态
     */
    private String attentionType;
    /**
     * IO口槽位
     */
    private String adapterSlot;
    /**
     * 描述
     */
    private String description;
    /**
     * 光口wwn号
     */
    private String fcWwn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getCollectCode() {
        return collectCode;
    }

    public void setCollectCode(String collectCode) {
        this.collectCode = collectCode;
    }

    public Date getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(Date collectTime) {
        this.collectTime = collectTime;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getAdapterStat() {
        return adapterStat;
    }

    public void setAdapterStat(String adapterStat) {
        this.adapterStat = adapterStat;
    }

    public String getAttentionType() {
        return attentionType;
    }

    public void setAttentionType(String attentionType) {
        this.attentionType = attentionType;
    }

    public String getAdapterSlot() {
        return adapterSlot;
    }

    public void setAdapterSlot(String adapterSlot) {
        this.adapterSlot = adapterSlot;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFcWwn() {
        return fcWwn;
    }

    public void setFcWwn(String fcWwn) {
        this.fcWwn = fcWwn;
    }
}