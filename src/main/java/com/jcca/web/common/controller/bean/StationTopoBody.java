package com.jcca.web.common.controller.bean;

import com.jcca.web.graph.entity.*;
import lombok.Data;

import java.util.List;

/**
 * 车站TOPO
 *
 * @author lyp
 */
@Data
public class StationTopoBody {

    private List<TopoAssetGroup> groupList;

    private List<TopoAssetMark> markList;

    private List<TopoEdge> edgeList;

    private List<TopoPoints> pointsList;

    private List<TopoVertex> vertexList;

}
