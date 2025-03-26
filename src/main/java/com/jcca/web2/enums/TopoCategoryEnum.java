package com.jcca.web2.enums;

import com.jcca.common.bean.constant.OrgTypeConst;

/**
 * @author HanHW
 * @description 拓扑图类型
 * @className TopoCategoryEnum
 * @date 2024/3/1 13:34
 * @since 2.1.0.0
 */
public enum TopoCategoryEnum {
    /**
     * 页签类型
     * "net_topo = 网络拓扑图
     * cabinet_topo = 机柜拓扑图
     * biz_topo = 业务拓扑图
     * pc_topo = 调度台拓扑
     * wan_topo = 广域网拓扑图"
     */

    NET_TOPO("net_topo", "网络拓扑"),
    CABINET_TOPO("cabinet_topo", "机柜拓扑"),
    BIZ_TOPO("biz_topo", "业务拓扑"),
    PC_TOPO("pc_topo", "调度台拓扑"),
    WAN_TOPO("wan_topo", "广域网拓扑"),
    NET_WORKASSET_TOPO("netWorkAsset_topo", "资产连线拓扑"),
    ALL_TOPO("all_topo", "全局topo"),
    ;

    public String category;
    public String tagName;

    TopoCategoryEnum(String category, String tagName) {
        this.category = category;
        this.tagName = tagName;
    }

    public static Boolean containsType(Integer orgType, String category) {
        if (OrgTypeConst.CENTER == orgType) {
            return NET_TOPO.category.equals(category) || CABINET_TOPO.category.equals(category)
                    || BIZ_TOPO.category.equals(category) || PC_TOPO.category.equals(category)
                    || WAN_TOPO.category.equals(category)||ALL_TOPO.category.equals(category);
        }
        if (OrgTypeConst.LINE == orgType) {
            return NET_TOPO.category.equals(category) || BIZ_TOPO.category.equals(category);
        }
        if (OrgTypeConst.STATION == orgType) {
            return NET_TOPO.category.equals(category) || CABINET_TOPO.category.equals(category);
        }
        return false;
    }
}
