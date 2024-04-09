package com.jcca.web2.vo;

import com.jcca.web.ip.vo.NetWorkAddressVo;
import lombok.Data;

import java.util.List;

/**
 * @description: 线路ip展示
 * @author: sophia
 * @create: 2023/11/14 11:09
 **/
@Data
public class LineNetWorkVo {
    private static final long serialVersionUID = 1L;
    /**
     * 组织id
     * */
    private String orgId;
    /**
     * 组织名称
     * */
    private String orgName;
    /**
     * 组织下网段集合
     * */
    private List<NetWorkAddressVo> netWorkAddressList;
}