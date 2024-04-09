package com.jcca.web.asset.controller.bean;

import com.jcca.web.collect.entity.CollectDS;
import lombok.Data;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:47 2022/12/20
 * @ Description:
 */
@Data
public class ControllerVo {
    private String code;
    private List<CollectDS> drivers;
    private String status;
    private String name;

}
