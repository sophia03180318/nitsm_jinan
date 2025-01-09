package com.jcca.web2.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.vo.AssetTargetVo;
import com.jcca.admin.biz.vo.SysTopoGraph;
import com.jcca.admin.biz.vo.TopoNodeGraph;
import com.jcca.admin.system.entity.SysModuleConfig;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.TopoAssetPort;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.enums.OrgTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.controller.route.bean.AssetLinkConst;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web.collect.service.CollectClusterService;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectRouteService;
import com.jcca.web.graph.entity.TopoAssetGroup;
import com.jcca.web.graph.entity.TopoAssetMark;
import com.jcca.web.graph.entity.TopoEdge;
import com.jcca.web.graph.service.*;
import com.jcca.web.graph.vo.TopoVertexVo;
import com.jcca.web2.enums.TopoCategoryEnum;
import com.jcca.web2.vo.NetTopoVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @ClassName MXGraphController
 * @Description 画图工具
 * @Date 2020/4/27 14:35
 * @Author hanwone
 */

@Controller
@RequestMapping("/api/graph2")
@Slf4j
public class GraphController2 {
    @Value("${project.upload.file-path}")
    private String path;

    @Resource
    private TopoVertexService topoVertexService;
    @Resource
    private TopoPointsService topoPointsService;
    @Resource
    private TopoEdgeService topoEdgeService;

    @Resource
    private TopoAssetGroupService topoAssetGroupService;
    @Resource
    private TopoAssetMarkService topoAssetMarkService;
    @Resource
    private AssetLinkAssetService linkAssetService;

    @PostMapping("/saveNetWorkTopoFile")
    @ApiOperation(value = "保存网络资产拓扑文件")
    @ResponseBody
    public ResultVo saveNetWorkTopoFile(@RequestBody NetTopoVo netTopoVo) {
        String msg = netTopoVo.getMsg();
        String filePath = path + "/NetTopo/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filePath + netTopoVo.getOrgId());
            fileWriter.write(netTopoVo.getMsg());
            fileWriter.close();

            return ResultVoUtil.success("保存成功");
        } catch (IOException e) {
            if (ObjectUtil.isNotNull(fileWriter)) {
                try {
                    fileWriter.close();
                } catch (IOException ex) {
                }
            }
            return ResultVoUtil.error(e.toString());
        }
        //return null;
    }


}
