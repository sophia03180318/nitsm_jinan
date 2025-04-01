package com.jcca.web.asset.vo;

import lombok.Data;

/**
 * @ClassName CabinetVo
 * @Description 机柜
 * @Date 2020/4/27 10:34
 * @Author hanwone
 */
@Data
public class CabinetVo {
    private String id;
    private String name;
    private String orgId;
    private String roomId;
    private String rowIndex;
    private String columnIndex;
}
