package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @description: 中心机房机柜拓扑信息
 * @author: sophia
 * @create: 2023/10/24 15:06
 **/
@Data
public class RoomTopoVo {
    private static final long serialVersionUID = 1L;
    /**
     * 机房ID
     */
    private String roomId;
    /**
     * 机房名称
     */
    private String roomName;
    /**
     * 机房内包含的机柜信息
     */
    private List<CabinetTopoVo> topoNode;
}