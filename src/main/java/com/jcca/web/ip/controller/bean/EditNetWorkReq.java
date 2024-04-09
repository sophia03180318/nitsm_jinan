package com.jcca.web.ip.controller.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 编辑网络信息
 *
 * @author lyp
 */
@Data
public class EditNetWorkReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    @NotEmpty(message = "要修改的网络Id空")
    private String id;

    /**
     * 网络名称
     */
    @NotEmpty(message = "请输入网络名称")
    private String name;

    /**
     * 组织ID
     */
    @NotEmpty(message = "组织ID不能空")
    private String orgId;

    /**
     * 备注
     */
    private String remark;
}
