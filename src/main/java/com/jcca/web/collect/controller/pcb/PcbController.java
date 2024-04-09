package com.jcca.web.collect.controller.pcb;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.collect.entity.CollectPcb;
import com.jcca.web.collect.service.CollectPcbService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "板卡展示查询")
@RestController
@RequestMapping("/api/pcb")
public class PcbController {

    @Autowired
    private CollectPcbService pcbServ;

    @SuppressWarnings("rawtypes")
    @GetMapping("/queryList")
    ResultVo queryList(String assetId) {
        List<CollectPcb> pcbList = pcbServ.getPcdInfoByAsset(assetId);
        return ResultVoUtil.success(pcbList);
    }

}
