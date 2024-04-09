package com.jcca.web.common.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 进程变动通知
 *
 * @author Lvyp
 */
@Data
public class ProcessOnChangeVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 变动标识
     * 0:增,1:删 2 更新
     */
    @NotNull(message = "变动标识不能空")
    private Integer optFlag;
    /**
     * 资产ID
     * 进程所属资产ID
     */
    @NotEmpty(message = "资产ID不能空")
    private String assetId;
    /**
     * 进程名称列表
     */
    @NotNull(message = "进程名称列表不能空")
    @Size(min = 1, message = "至少要存在1个进程名称")
    private List<String> processNameList;


}
