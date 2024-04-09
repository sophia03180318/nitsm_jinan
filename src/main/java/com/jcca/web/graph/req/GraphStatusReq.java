package com.jcca.web.graph.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @ClassName GraphStatusReq
 * @Description 拓扑图状态请求数据
 * @Date 2020/6/15 15:07
 * @Author hanwone
 */
@Data
public class GraphStatusReq implements Serializable {

    @NotEmpty(message = "拓扑图种类不能为空")
    private String category;
    private String orgId;
}
