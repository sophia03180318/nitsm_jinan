package com.jcca.web2.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.annotation.DevLog;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetTelnet;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.vo.AssetBelong;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.collect.service.AssetLinkAssetService;
import com.jcca.web2.vo.AssetStatisticsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HanHW
 * @description: 资产相关接口
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

    @Resource
    private AssetLinkAssetService linkAssetService;

    @GetMapping("/im")
    public void im(HttpServletResponse response) throws Exception {
        InputStream in = Files.newInputStream(new File("C:\\Users\\oamwh\\Desktop\\excel2003.xlsx").toPath());
        ExcelReader reader = ExcelUtil.getReader(in);
        Sheet sheet = reader.getSheets().get(0);
        int lastRowNum = sheet.getLastRowNum();

        QueryWrapper<Asset> query = null;
        Map<String, Asset> map = new HashMap<>(128);
        for (int i = 1; i < lastRowNum; i++) {
            Row row = sheet.getRow(i);
            AssetLinkAsset linkAsset = new AssetLinkAsset();

            String assetName = row.getCell(0).getStringCellValue();
            Asset asset = this.getAsset(map, assetName, query);
            if (asset == null) {
                System.out.println("未查到资产名称：" + assetName);
                continue;
            }

            Cell cell1 = row.getCell(1);
            if (cell1 != null) {
                String linkAssetName = cell1.getStringCellValue();
                if (!StringUtils.isEmpty(linkAssetName)) {
                    Asset link = this.getAsset(map, linkAssetName, query);
                    if (link != null) {
                        linkAsset.setLinkAssetId(link.getId());
                        linkAsset.setLinkAssetName(link.getName());
                    }
                }
            }

            Cell cell9 = row.getCell(9);
            if (cell9 != null) {
                String atName = cell9.getStringCellValue();
                if (!StringUtils.isEmpty(atName)) {
                    Asset at = this.getAsset(map, atName, query);
                    if (at != null) {
                        linkAsset.setAtAssetId(at.getId());
                    }
                    linkAsset.setAtName(atName);
                }
            }

            linkAsset.setId(MyIdUtil.getId());
            linkAsset.setAssetId(asset.getId());
            linkAsset.setPortIndex(Objects.nonNull(row.getCell(2)) ? row.getCell(2).getStringCellValue() : "");
            linkAsset.setPortIp(Objects.nonNull(row.getCell(3)) ? row.getCell(3).getStringCellValue() : "");
            linkAsset.setPortIndexRank(Objects.nonNull(row.getCell(5)) ? row.getCell(5).getStringCellValue() : "");
            linkAsset.setMaOrAt(Objects.nonNull(row.getCell(6)) ? row.getCell(6).getStringCellValue() : "");
            linkAsset.setLinkAssetIp(Objects.nonNull(row.getCell(7)) ? row.getCell(7).getStringCellValue() : "");
            linkAsset.setLinkPort(Objects.nonNull(row.getCell(8)) ? row.getCell(8).getStringCellValue() : "");
            linkAsset.setAtNetAddress(Objects.nonNull(row.getCell(10)) ? row.getCell(10).getStringCellValue() : "");
            linkAsset.setAtPortIndexName(Objects.nonNull(row.getCell(11)) ? row.getCell(11).getStringCellValue() : "");
            linkAssetService.save(linkAsset);
        }

    }


    private Asset getAsset(Map<String, Asset> map, String assetName, QueryWrapper<Asset> query) {
        Asset asset = map.get(assetName);
        if (asset == null) {
            query = Wrappers.query();
            query.eq("IS_DEL", 1);
            query.eq("NAME", assetName);
            List<Asset> list1 = assetServ.list(query);
            if (list1.isEmpty()) {
                return asset;
            }
            asset = list1.get(0);
            map.put(assetName, asset);
        }
        return asset;
    }
}
