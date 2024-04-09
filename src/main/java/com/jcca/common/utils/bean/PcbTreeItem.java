package com.jcca.common.utils.bean;

import lombok.Data;

import java.util.List;

/**
 * 板卡树
 *
 * @author Lvyp
 */
@Data
public class PcbTreeItem {

    private String id;

    private String name;

    private String type;

    private String model;

    private List<PcbTreeItem> sunList;

}
