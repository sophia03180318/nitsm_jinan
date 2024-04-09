package com.jcca.web.asset.controller.bean;

import com.jcca.web.collect.entity.CollectRaid;
import lombok.Data;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 11:47 2022/12/20
 * @ Description:
 */
@Data
public class RaidControllerVo {
    private String code;
    private List<CollectRaid> drivers;
    private String status;
    private String name;

}
