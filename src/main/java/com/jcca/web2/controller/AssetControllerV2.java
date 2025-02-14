package com.jcca.web2.controller;

import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.annotation.DevLog;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetTelnet;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web2.vo.AssetStatisticsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @description: 资产相关接口
 * @author HanHW
 * @create: 2023/11/01 16:42
 */
@RestController
@RequestMapping("/api/v2/asset")
@Api(tags = "资产相关接口V2")
public class AssetControllerV2 {

    @Resource
    private AssetService assetServ;

    @ApiOperation("资产字段详细")
    @GetMapping("/content/{id}")
    public ResultVo<Object> content(@PathVariable String id) {

        Asset asset = assetServ.getById(id);

        AssetBelong belong = assetServ.findAssetBelongById(id);
        if (Objects.isNull(belong)) {
            throw new ResultException(ResultEnum.CANNOT_FIND.getCode(), "未查询到资产附属信息");
        }
        asset.setRoomId(belong.getRoomId());
        asset.setCabinetId(belong.getCabinetId());
        asset.setStartPosition(belong.getStartPosition());
        asset.setEndPosition(belong.getEndPosition());
        asset.setOsPassword(EncryptUtil.aesDecryptStr(asset.getOsPassword()));
        asset.setLoginPwd(EncryptUtil.aesDecryptStr(asset.getLoginPwd()));
        asset.setIpmiPwd(EncryptUtil.aesDecryptStr(asset.getIpmiPwd()));
        if (!StringUtils.isEmpty(belong.getCabinetId())) {
            asset.setAssetUnit(asset.getEndPosition() - asset.getStartPosition() + 1);
        }

        return ResultVoUtil.success(asset);
    }

    @ApiOperation("按类型统计")
    @GetMapping("/mode")
    public ResultVo<Object> mode(@RequestParam Map<String, Object> map) {

        List<AssetStatisticsVo> list = assetServ.countModeV2(map);

        return ResultVoUtil.success(list);
    }

    @ApiOperation("按型号统计")
    @GetMapping("/model")
    public ResultVo<Object> model(@RequestParam Map<String, Object> map) {

        List<AssetStatisticsVo> list = assetServ.countModelV2(map);

        return ResultVoUtil.success(list);
    }


    @ApiOperation("保存资产")
    @PostMapping("/save")
    @RequiresPermissions("api:v2:asset:save")
    @DevLog(title = "资产管理", name = "保存/修改资产", dev = DevLogConstant.ASSET_MODIFY, key = LogTypeConstant.DEV)
    public ResultVo<Object> add(@Validated @RequestBody Asset asset) throws Exception {

        assetServ.saveAssetV2(asset);

        return ResultVoUtil.success();
    }

    /**
     * 资产IP地址ping测试
     */
    @PostMapping("/assetPing")
    @ApiOperation(value = "资产IP地址ping测试")
    @RequiresPermissions("api:v2:asset:assetPing")
    @ActionLog(name = "资产PING测试", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo<Object> assetPing(@RequestBody List<String> ids) {

        return ResultVoUtil.success(assetServ.assetPingV2(ids));
    }

    /**
     * telnet测试主机端口是否启用
     */
    @PostMapping("/assetTelnet")
    @ApiOperation(value = "资产telnet测试主机端口是否启用")
    @RequiresPermissions("api:v2:asset:assetTelnet")
    @ActionLog(name = "资产telnet测试", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo<Object> assetTelnet(@RequestBody List<AssetTelnet> req) {

        return ResultVoUtil.success(assetServ.assetTelnetV2(req));
    }

}
