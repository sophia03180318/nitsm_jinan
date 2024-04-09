package com.jcca.web2.controller;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web2.adapter.cabinet.CabinetAssetInfoHeader;
import com.jcca.web2.dto.CabinetBaseInfoQueryDto;
import com.jcca.web2.vo.CabinetAssetInfoVo;
import com.jcca.web2.vo.CabinetBaseInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 机柜相关接口V2版本
 * @author: Lvyp
 * @create: 2023/10/24 10:09
 */
@RestController
@RequestMapping("/api/v2/cabinet")
@Api(tags = "机柜相关接口V2")
public class CabinetControllerV2 {

    @Resource
    private CabinetService cabinetServ;
    @Resource
    private AssetService assetService;
    @Resource
    private CabinetAssetInfoHeader cabinetAssetHeader;


    @GetMapping("/queryBaseInfo")
    @ApiOperation("查询机柜基础信息")
    public ResultVo<Object> queryBaseInfo(CabinetBaseInfoQueryDto query) {
        CabinetBaseInfoVo cabinetBaseInfoV2 = cabinetServ.findCabinetBaseInfoV2(query);
        return ResultVoUtil.success(cabinetBaseInfoV2);
    }

    @GetMapping("/queryAssetInfo")
    @ApiOperation("查询机柜内设备信息")
    public ResultVo<Object> queryAssetInfo(String assetId) {
        CabinetAssetInfoVo vo = assetService.findCabinetAssetInfoV2(assetId);

        return ResultVoUtil.success(cabinetAssetHeader.fattenCabinetInfo(vo));
    }

    @PostMapping("/list")
    @ApiOperation("获取机柜列表")
    public ResultVo<Object> getList(@RequestBody Cabinet cabinet) {

        List<Cabinet> list = cabinetServ.getCabinetListV2(cabinet);

        return ResultVoUtil.success(list);
    }

    @PostMapping("/save")
    @RequiresPermissions("api:v2:cabinet:save")
    @ApiOperation("保存机柜")
    @ActionLog(name = "保存机柜", title = "机柜管理", key = LogTypeConstant.ADD)
    public ResultVo<String> save(@Validated Cabinet cabinet) {

        cabinetServ.saveCabinetV2(cabinet);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    @PostMapping("/del/{id}")
    @RequiresPermissions("api:v2:cabinet:del")
    @ApiOperation("删除机柜")
    @ActionLog(name = "删除机柜", title = "机柜管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> del(@PathVariable String id) {

        cabinetServ.delById(id);

        return ResultVoUtil.success();
    }

}
