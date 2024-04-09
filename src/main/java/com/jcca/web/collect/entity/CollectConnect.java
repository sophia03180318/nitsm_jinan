package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author LuBan
 * @since 2021-04-28
 */
@TableName("COLLECT_CONNECT")
public class CollectConnect extends Model<CollectConnect> {

    private static final long serialVersionUID = 1L;

    @TableId("ID")
    private String id;

    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;

    /**
     * 设备id
     */
    @TableField("ASSET_ID")
    private String assetId;

    /**
     * 活动连接数
     */
    @TableField("ESTABLISHED_NUM")
    private Integer establishedNum;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(Date collectTime) {
        this.collectTime = collectTime;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public Integer getEstablishedNum() {
        return establishedNum;
    }

    public void setEstablishedNum(Integer establishedNum) {
        this.establishedNum = establishedNum;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "CollectConnect{" +
                "id=" + id +
                ", collectTime=" + collectTime +
                ", assetId=" + assetId +
                ", establishedNum=" + establishedNum +
                "}";
    }
}
