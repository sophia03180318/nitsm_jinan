package com.jcca.admin.biz.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/8/20 18:49
 */
@Data
public class TopoVlanVo {
    private String assetId;
    private String pcbId;
    private List<TopoNodePortVo> topoNodePortVos = new ArrayList<>();
}
