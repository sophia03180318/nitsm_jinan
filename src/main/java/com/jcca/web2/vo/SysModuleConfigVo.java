package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * 系统配置
 */
@Data
public class SysModuleConfigVo {

    /**
     * 配置
     */
    private String title;
    /**
     * 唯一KEY
     */
    private String key;
    /**
     * 默认值
     */
    private String defaultValue;
    /**
     * 配置类型
     * radio 单选 数值形式保存
     * input 输入 数值形式保存
     *
     * radioGroup 单选组 json形式保存
     */
    private String type;
    /**
     * radio 选项 返回value
     */
    private List<RadioVo>  radioVo;


}
