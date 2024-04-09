package com.jcca.web.broken.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName BrokenRecordOpinionVo
 * @Description 处理意见实体
 * @Date 2020/6/28 18:06
 * @Author hanwone
 */
@Data
public class BrokenRecordOpinionVo {
    /**
     * 处理意见ID
     */
    private String id;
    /**
     * 处理意见
     */
    private String opinion;
    /**
     * 创建日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 修改日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;

    private String creator;
}
