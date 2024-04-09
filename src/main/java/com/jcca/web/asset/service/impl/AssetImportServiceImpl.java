package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.dao.AssetImportMapper;
import com.jcca.web.asset.entity.*;
import com.jcca.web.asset.service.AssetImportService;
import com.jcca.web.asset.vo.AssetInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ Author：sophia
 * @ Date：Created in 11:25 2021/7/8
 * @ Description:
 */
@Service
public class AssetImportServiceImpl extends ServiceImpl<AssetImportMapper, AssetImportRecord> implements AssetImportService {
    @Resource
    private AssetImportMapper assetImportMapper;

    @Override
    public List<AssetImportRecord> selectAllAsset() {
        return assetImportMapper.selectAllAsset();
    }

    @Override
    public void deleteAllAsset() {
        assetImportMapper.delectAllAsset();
    }

    @Override
    public int countAll() {
        return assetImportMapper.countAll();

    }

    @Override
    public void saveErrorAsset(AssetImportRecord assetImportRecord) {
        assetImportMapper.insert(assetImportRecord);
    }

    @Override
    public ResultVo estimateTemplateEmpty(List<Object> titleList) {
        //有则提取赋值,无则报错 name:资产名称,assetImage:资产型号,orgId:组织机构,roomId:机房
        Map<String, String> emptyMap = new HashMap<>();
        emptyMap.put("name", "资产名称");
        emptyMap.put("assetImage", "资产型号");
        emptyMap.put("orgId", "组织机构");
        emptyMap.put("roomId", "机房");
        ResultVo resul = ResultVoUtil.success();
        if (!titleList.containsAll(emptyMap.keySet())) {
            resul.setCode(ResultEnum.ERROR.getCode());
            resul.setMsg("模板不符合要求!请重新下载模板!");
            return resul;
        }
        return resul;
    }

    @Override
    public AssetInfoVo estimateDouble(String orgName, String roomName) {
        AssetInfoVo assetInfoVo = new AssetInfoVo();
        List<Org> orgs = assetImportMapper.estimateOrg(orgName);
        List<Room> rooms = assetImportMapper.estimateRoom(roomName);
        if (!(orgs.isEmpty() || rooms.isEmpty())) {
            String orgId = orgs.get(0).getId();
            List<Room> estimate = assetImportMapper.estimateDouble(orgId, roomName);
            if (!estimate.isEmpty()) {
                Room room = estimate.get(0);
                Asset asset = new Asset();
                asset.setOrgId(orgId);
                asset.setRoomId(room.getId());
                assetInfoVo.setJudge(true);
                assetInfoVo.setAsset(asset);
                return assetInfoVo;
            }
        }
        assetInfoVo.setInfo("组织关系和机房关系不成立");
        return assetInfoVo;
    }

    @Override
    public boolean estimateModeAndImage(int assetMode, String assetImage) {
        AssetInfoVo assetInfoVo = new AssetInfoVo();
        boolean b = false;
        String mode = DictUtil.keyValue("ASSET_IMAGE", assetImage.trim());
        assetInfoVo.setInfo("资产型号填写错误!");
        if (!mode.isEmpty()) {
            if (mode.equals(assetMode + "")) {
                assetInfoVo.setJudge(true);
                return true;
            }
            assetInfoVo.setInfo("资产型号与资产类型不匹配!");
        }

        return b;
    }

    @Override
    public AssetInfoVo estimateTreble(String orgName, String roomName, String cabinetName) {

        AssetInfoVo assetInfoVo = new AssetInfoVo();

        List<Org> orgs = assetImportMapper.estimateOrg(orgName);
        List<Room> rooms = assetImportMapper.estimateRoom(roomName);
        List<Cabinet> cabinets = assetImportMapper.estimateCabinet(cabinetName);
        if (!(orgs.isEmpty() || rooms.isEmpty() || cabinets.isEmpty())) {
            String orgId = orgs.get(0).getId();
            List<Cabinet> estimate = assetImportMapper.estimateTreble(orgId, roomName, cabinetName);
            if (!estimate.isEmpty()) {
                Cabinet cabinet = estimate.get(0);
                assetInfoVo.setJudge(true);
                Asset asset = new Asset();
                asset.setOrgId(orgId);
                asset.setRoomId(cabinet.getRoomId());
                asset.setCabinetId(cabinet.getId());
                assetInfoVo.setAsset(asset);
                return assetInfoVo;
            }
        }
        assetInfoVo.setInfo("组织,机房,机柜关系不成立");
        return assetInfoVo;
    }


}