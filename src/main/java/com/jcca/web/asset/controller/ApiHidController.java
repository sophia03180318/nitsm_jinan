package com.jcca.web.asset.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import com.jcca.web.collect.entity.CollectNetworkCard;
import com.jcca.web.collect.service.CollectInterfacesService;
import com.jcca.web.collect.service.CollectNetworkCardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 组件禁用接口
 */
@RestController
@RequestMapping("/api/hid")
@Api(tags = "组件禁用接口")
public class ApiHidController {

    @Resource
    private AssetHidConfService hidConfService;
    @Resource
    private CollectNetworkCardService netWorkCardServ;
    @Resource
    private CollectInterfacesService interfacesService;



    @GetMapping("/queryCardList")
    ResultVo queryCardList(String assetId){
        List<AssetHidConf> hideList = hidConfService.getFlagListByAsset(assetId, AssetHidConf.TypeEnum.NET_CARD.name());
        List<CollectNetworkCard> realTimeData = netWorkCardServ.getRealTimeData(assetId);

        List<String> hidNameList = hideList.stream().map(item -> item.getFlag()).collect(Collectors.toList());
        for (CollectNetworkCard realTimeDatum : realTimeData) {
            if(hidNameList.contains(realTimeDatum.getName())){
                realTimeDatum.setShow(false);
            }else{
                realTimeDatum.setShow(true);
            }
        }
        return ResultVoUtil.success(realTimeData);
    }


    @PostMapping("/updateNetStatus")
    ResultVo updateStatus(@RequestBody String req){
        JSONObject reqJson = JSONUtil.parseObj(req);
        String assetId = reqJson.getStr("assetId");
        Boolean status = reqJson.getBool("show");
        String name = reqJson.getStr("name");
        if(StrUtil.isEmpty(assetId)){
            return ResultVoUtil.error("缺少资产ID");
        }
        if(StrUtil.isEmpty(name)){
            return ResultVoUtil.error("缺少网卡名称");
        }
        if(Objects.isNull(status)){
            return ResultVoUtil.error("缺少状态");
        }
        AssetHidConf conf = new AssetHidConf();
        conf.setConfId(MyIdUtil.getId());
        conf.setAssetId(assetId);
        conf.setFlag(name);
        conf.setType(AssetHidConf.TypeEnum.NET_CARD.name());
        conf.setCreateTime(new Date());
        if(!status){
            //打开隐藏
            hidConfService.updateAssetNetCardConf(assetId, Arrays.asList(conf));
        }else{
            //删除隐藏
            QueryWrapper<AssetHidConf> confQuery = new QueryWrapper<>();
            confQuery.eq("asset_id",assetId);
            confQuery.eq("flag",name);
            confQuery.eq("type",AssetHidConf.TypeEnum.NET_CARD.name());
            hidConfService.remove(confQuery);
        }

        return ResultVoUtil.success();
    }

    /**
     * 获取网卡的隐藏配置
     * @param assetId
     * @return
     */
    @GetMapping("/getHideNetList")
    ResultVo getHideNetList(String assetId){
        List<AssetHidConf> hideList = hidConfService.getFlagListByAsset(assetId, AssetHidConf.TypeEnum.NET_CARD.name());
        return ResultVoUtil.success(hideList);
    }


    /**
     * 查询端口列表
     * @param assetId
     * @return
     */
/*
    @ApiOperation(value = "查询端口列表")
    @GetMapping("/queryPortList")
    ResultVo queryPortList(String assetId){
        List<CollectInterfaces> realTimeData = interfacesService.filterPort(assetId);
        List<AssetHidConf> hideList = hidConfService.getFlagListByAsset(assetId, AssetHidConf.TypeEnum.PORT.name());
        List<String> hidNameList = hideList.stream().map(item -> item.getFlag()).collect(Collectors.toList());
        for (CollectInterfaces realTimeDatum : realTimeData) {
            if(hidNameList.contains(realTimeDatum.getPortIndex())){
                realTimeDatum.setShow(false);
            }else{
                realTimeDatum.setShow(true);
            }
        }
        return ResultVoUtil.success(realTimeData);
    }
*/


    /**
     * 获取端口的隐藏配置
     * @param assetId
     * @return
     */
    @ApiOperation(value = "获取隐藏的端口")
    @GetMapping("/getHidePortList")
    ResultVo getHidePortList(String assetId){
        List<AssetHidConf> hideList = hidConfService.getFlagListByAsset(assetId, AssetHidConf.TypeEnum.PORT.name());
        return ResultVoUtil.success(hideList);
    }


    @PostMapping("/updatePortStatus")
    @ApiOperation(value = "更新")
    ResultVo updatePortStatus(@RequestBody String req){
        JSONObject reqJson = JSONUtil.parseObj(req);
        String assetId = reqJson.getStr("assetId");
        Boolean status = reqJson.getBool("show");
        String name = reqJson.getStr("name");
        if(StrUtil.isEmpty(assetId)){
            return ResultVoUtil.error("缺少资产ID");
        }
        if(StrUtil.isEmpty(name)){
            return ResultVoUtil.error("缺少端口名称");
        }
        if(Objects.isNull(status)){
            return ResultVoUtil.error("缺少状态");
        }
        AssetHidConf conf = new AssetHidConf();
        conf.setConfId(MyIdUtil.getId());
        conf.setAssetId(assetId);
        conf.setFlag(name);
        conf.setType(AssetHidConf.TypeEnum.PORT.name());
        conf.setCreateTime(new Date());
        if(!status){
            //打开隐藏
            hidConfService.updateAssetPortConf(assetId, Arrays.asList(conf));
        }else{
            //删除隐藏
            QueryWrapper<AssetHidConf> confQuery = new QueryWrapper<>();
            confQuery.eq("asset_id",assetId);
            confQuery.eq("flag",name);
            confQuery.eq("type",AssetHidConf.TypeEnum.PORT.name());
            hidConfService.remove(confQuery);
        }

        return ResultVoUtil.success();
    }



}
