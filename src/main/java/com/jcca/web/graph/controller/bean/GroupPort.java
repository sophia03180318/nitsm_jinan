package com.jcca.web.graph.controller.bean;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 20:44 2021/9/1
 * @ Description:
 */
@Data
public class GroupPort {
    private String groupId;
    private String portName;

    public GroupPort(String groupId, String portName) {
        this.groupId = groupId;
        this.portName = portName;
    }

}
