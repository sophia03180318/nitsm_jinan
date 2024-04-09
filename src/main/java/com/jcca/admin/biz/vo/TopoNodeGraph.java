package com.jcca.admin.biz.vo;

import com.jcca.web.graph.entity.TopoEdge;
import com.jcca.web.graph.entity.TopoPoints;
import com.jcca.web.graph.entity.TopoVertex;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-30 15:53:07
 **/
@Data
public class TopoNodeGraph implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "拓扑图种类不能为空")
    private String category;
    private String orgId;
    private String assetId;
    private List<TopoPoints> points;
    private List<TopoEdge> edges;
    private List<TopoVertex> nodes;
    private List<String> groups;
    private List<String> marks;
}