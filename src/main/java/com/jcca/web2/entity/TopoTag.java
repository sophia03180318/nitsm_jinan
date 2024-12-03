package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author sophia
 * @description: 拓扑页签列表
 * @date 2020-04-23 15:52:15
 **/
@Data
public class TopoTag {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 页签类型
     * "net_topo= 网络拓扑图
     * cabinet_topo= 机柜拓扑图
     * biz_topo= 业务拓扑图
     * pc_topo= 调度台拓扑
     * wan_top=广域网拓扑图"
     * <p>
     * TopoCategoryEnum
     */
    @TableField("CATEGORY")
    private String category;

    /**
     * 页签名称
     */
    @TableField("NAME")
    @Length(max = 60, message = "标签名称不能超过60个字符")
    private String name;

    /**
     * 所属组织
     */
    @TableField("ORG_ID")
    private String orgId;

    /**
     * 备注信息
     */
    @TableField("REMARK")
    private String remark;

}