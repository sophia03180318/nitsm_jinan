package com.jcca.web.common.service.bean;


import com.jcca.admin.system.entity.PerformanceTarget;
import com.jcca.web.asset.entity.Asset;
import lombok.Data;

import java.util.List;

@Data
public class TestPerformanceTarget {

    private Asset asset;

    private List<PerformanceTarget> targetList;

}
