package com.jcca.web2.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.adapter.AdapterMissingException;
import com.jcca.common.exception.asset.AssetNightBoardHaveMoreEnable;
import com.jcca.common.exception.common.DbEntityNotFound;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web2.adapter.db.DbSourceHeader;
import com.jcca.web2.dto.*;
import com.jcca.web2.entity.NightBoard;
import com.jcca.web2.entity.NightBoardPayload;
import com.jcca.web2.service.CacheDataService;
import com.jcca.web2.service.NightBoardService;
import com.jcca.web2.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: Lvyp
 * @create: 2023/12/21 10:56
 */
@RestController
@RequestMapping("/api/v2/asset/detail")
@Api(tags = "资产详情相关接口V2")
public class AssetDetailControllerV2 {

    @Resource
    private AssetService assetServ;
    @Resource
    private DbSourceHeader sourceHeader;
    @Resource
    private NightBoardService nightBoardService;
    @Resource
    private CacheDataService cacheDataService;


    @ApiOperation("查询设备性能数据")
    @GetMapping("/getPerformanceData")
    public ResultVo<Object> getPerformanceData(String assetId) {
        if (StrUtil.isEmpty(assetId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        AssetPerformanceDataVo resp = assetServ.queryPerformanceDataV2(assetId);
        return ResultVoUtil.success(resp);
    }

    @ApiOperation("查询服务器组件信息")
    @GetMapping("/serverModule")
    public ResultVo<Object> serverModule(String assetId) {
        if (StrUtil.isEmpty(assetId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }

        ServerModuleVo vo = assetServ.serverModuleQueryV2(assetId);

        return ResultVoUtil.success(vo);
    }

    @ApiOperation("查询设备状态")
    @GetMapping("/getAssetStatusList")
    public ResultVo<Object> getAssetStatusList(String assetId) {
        List<AssetStatusItmVo> assetStatusItmVos1 = assetServ.queryBaseStatusItmV2(assetId);
        List<AssetStatusItmVo> assetStatusItmVos = cacheDataService.queryAssetTargetStatus(assetId);
        assetStatusItmVos1.addAll(assetStatusItmVos);
        return ResultVoUtil.success(assetStatusItmVos1);
    }

    @ApiOperation("查询设备状态明细")
    @GetMapping("/getAssetStatusDetail")
    public ResultVo<Object> getAssetStatusDetail(String assetId, String code) {
        if(AssetStatusItmVo.isBaseItem(code)){
            AssetStatusDetailVo resp = assetServ.getAssetStatusDetailV2(assetId,code);
            return ResultVoUtil.success(resp);
        }else{
            AssetStatusDetailVo resp = cacheDataService.getAssetStatusDetailV2(assetId, code);
            return ResultVoUtil.success(resp);
        }
    }

    @ApiOperation("查询设备生命周期")
    @GetMapping("/getLifeLine")
    public ResultVo<Object> getLifeLine(String assetId) {
        if (StringUtils.isEmpty(assetId) || "null".equals(assetId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        List<AssetLifeLineVo> voList = assetServ.getLifeLineV2(assetId);
        return ResultVoUtil.success(voList);
    }


    @ApiOperation("查询设备日志列表")
    @GetMapping("/logPage")
    public ResultVo<Object> logPage(String assetId) {

        return ResultVoUtil.success();
    }


    @ApiOperation("查询设备基础信息")
    @GetMapping("/baseInfo")
    public ResultVo<Object> baseInfo(String assetId) {
        if (StringUtils.isEmpty(assetId) || "null".equals(assetId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        AssetInfoBaseVo vo = assetServ.queryBaseInfoV2(assetId);

        return ResultVoUtil.success(vo);
    }


    @ApiOperation("查询设备适用的配置")
    @GetMapping("/getBoardConfByAsset")
    public ResultVo<Object> getBoardConfByAsset(String assetId) {
        if (StringUtils.isEmpty(assetId)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR);
        }
        NightBoardPayload assetEnableConf = nightBoardService.getAssetEnableConf(assetId);
        return ResultVoUtil.success(assetEnableConf);
    }


    @ApiOperation("更新配置")
    @PostMapping("/updateConfig")
    public ResultVo<Object> updateConfig(@Validated @RequestBody UpdateBoardsConfigDto req) {
        try {
            nightBoardService.updateConfig(req);
        } catch (DbEntityNotFound e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_DETAIL, JSONUtil.toJsonStr(req), e);
            return ResultVoUtil.error(e.getMessage());
        }
        return ResultVoUtil.success("更新成功");
    }

    @ApiOperation("查找图表配置")
    @PostMapping("/getBoardConf")
    public ResultVo<Object> getBoardConf(String boardId) {
        if (StrUtil.isEmpty(boardId)) {
            return ResultVoUtil.error("缺少模板ID");
        }
        NightBoardPayload conf = nightBoardService.getConf(boardId);
        return ResultVoUtil.success(conf);
    }


    @ApiOperation("更新模板")
    @PostMapping("/updateBoards")
    public ResultVo<Object> updateBoards(@Validated @RequestBody BoardsUpdateDto req) {
        try {
            nightBoardService.updateBoard(req);
        } catch (DbEntityNotFound | AssetNightBoardHaveMoreEnable e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_DETAIL, JSONUtil.toJsonStr(req), e);
            return ResultVoUtil.error(e.getMessage());
        }
        return ResultVoUtil.success("更新成功");
    }


    @ApiOperation("保存模板")
    @PostMapping("/addBoards")
    public ResultVo<Object> boards(@Validated @RequestBody BoardsAddDto req) {
        NightBoard nightBoard = nightBoardService.saveBoards(req);
        return ResultVoUtil.success(nightBoard);
    }

    @ApiOperation("分页查询模板")
    @GetMapping("/pageBoards")
    public ResultVo<Object> pageBoards(BoardsQueryDto req) {
        String name = req.getName();
        String tag = req.getTag();
        QueryWrapper<NightBoard> queryWrapper = new QueryWrapper<NightBoard>();
        if (StrUtil.isNotEmpty(name)) {
            queryWrapper.like("name", name);
        }
        if (StrUtil.isNotEmpty(tag)) {
            queryWrapper.like("TAGS", tag);
        }
        queryWrapper.eq("HIDE", 0);
        IPage<NightBoard> startPage = PagePlugin.startPageT(req.getPage(), req.getSize(), NightBoard.class);

        IPage<NightBoard> page = nightBoardService.page(startPage, queryWrapper);
        return ResultVoUtil.success(page);
    }


    @ApiOperation("执行资产查询SQL")
    @PostMapping("/exeQuery")
    public ResultVo<Object> exeQuery(@RequestBody ExeSqlQueryDto query) {
        try {
            List<List<AssetDataVo>> respList = new ArrayList<List<AssetDataVo>>();
            List<ExeSqlQuery> queries = query.getQueries();
            for (ExeSqlQuery exeSqlQuery : queries) {
                List<AssetDataVo> resp = sourceHeader.exeCommand(exeSqlQuery);
                respList.add(resp);
            }
            return ResultVoUtil.success(respList);
        } catch (AdapterMissingException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_DETAIL, JSONUtil.toJsonStr(query), e);
            return ResultVoUtil.success(new ArrayList<JSONObject>());
        } catch (Exception e){
            return ResultVoUtil.warning("输入的sql执行有误，请检查sql");
        }
    }


}
