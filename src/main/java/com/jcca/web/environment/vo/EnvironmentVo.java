package com.jcca.web.environment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName EnvironmentVo
 * @Description TODO
 * @Date 2020/5/18 18:07
 * @Author hanwone
 */
@Data
public class EnvironmentVo {

    /**
     * 数据ID
     */
    private String id;
    /**
     * 环境状态
     */
    private Byte status;
    /**
     * 监测目标名字
     */
    private String name;
    /**
     * 监测描述
     */
    private String descript;
    /**
     * 监测日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date date;
}
