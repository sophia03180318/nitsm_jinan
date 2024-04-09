package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 设备的生命周期，包含
 *
 * @description: 设备生命周期
 * @author: Lvyp
 * @create: 2023/12/11 12:00
 */
@Data
public class AssetLifeLineVo {

    /**
     * 生命周期类型
     */
    public enum LifeLineType {

        /**
         * 上架
         */
        UP(1, "上架"),
        /**
         * 下架
         */
        DOWN(2, "下架"),
        /**
         * 故障记录
         */
        MALFUNCTION(3, "故障记录"),
        /**
         * 硬件更换记录
         */
        FIX_LOG(4, "硬件更换记录");

        private Integer code;

        private String msg;

        LifeLineType(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }


    /**
     * 标题
     */
    private String title;
    /**
     * 类型
     * LifeLineType
     */
    private Integer type;
    /**
     * 关联的ID
     */
    private String linkId;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
