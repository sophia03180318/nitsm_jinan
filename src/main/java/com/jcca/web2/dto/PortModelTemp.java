package com.jcca.web2.dto;

import com.jcca.web2.entity.PortTemp;
import lombok.Data;

import java.util.List;

/**
 * @description: 端口配置
 * @author: sophia
 * @create: 2024/01/09 11:33
 **/
@Data
public class PortModelTemp {
    //类型ID
    private String id;

    //类型名称
    private String name;

    //类型图片地址
    private String path;

    //占用U位
    private int unit=1;

    //模板列表
    private List<PortTemp> portTempDtoList;
}