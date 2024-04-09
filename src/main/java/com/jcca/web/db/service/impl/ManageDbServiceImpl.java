package com.jcca.web.db.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.AssetManufacturerEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.common.entity.BizManage;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web.common.service.OutService;
import com.jcca.web.db.dao.ManageDbMapper;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 数据库
 *
 * @author
 */
@Service
public class ManageDbServiceImpl extends ServiceImpl<ManageDbMapper, ManageDb> implements ManageDbService {

    @Resource
    private BizManageService bizService;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private AssetService assetService;
    @Resource
    private OutService outService;
    @Resource
    private AlarmInfoService alarmServ;

    @Override
    @Transactional
    public void saveDB(ManageDb dbEntity, String assetOrgId) {
        String id = MyIdUtil.getId();
        dbEntity.setId(id);

        bizService.saveByBizAndOrg(assetOrgId, id, BizManageConstant.M_DB);
        save(dbEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDBById(ManageDb dbEntity, String assetId) {
        List<ManageDb> list = new ArrayList<ManageDb>();
        if (StrUtil.isNotEmpty(assetId)) {
            QueryWrapper<ManageDb> query = new QueryWrapper<ManageDb>();
            query.eq("ASSET_ID", assetId);
            list = list(query);
        }
        if (Objects.nonNull(dbEntity)) {
            list.add(dbEntity);
        }
        List<String> assetList = new ArrayList<String>();
        for (ManageDb manageDb : list) {
            bizService.remove(manageDb.getId(), BizManageConstant.M_DB);
            // 通知外部应用
            this.notifyOnChange(manageDb, OutConst.DEL_ASSET);
            //
            assetList.add(manageDb.getId());
            removeById(manageDb.getId());
            //删除数据库相关告警信息
            alarmServ.delDbAlarm(manageDb.getAssetId());
        }

        // 删除附属信息
        if (!assetList.isEmpty()) {
            assetAttachService.removeByIds(assetList);
        }
    }

    private void notifyOnChange(ManageDb db, Integer optFlag) {
        Asset a = assetService.getById(db.getAssetId());

        Asset asset = new Asset();
        asset.setOrgId(a.getOrgId());
        asset.setId(db.getId());
        asset.setName(db.getDbName());
        asset.setIp(a.getIp());
        asset.setPort(db.getPort());
        asset.setOsUser(db.getUsername());
        asset.setOsPassword(db.getPassword());
        asset.setAssetMode(AssetModeConst.ORACLE_DB);
        asset.setCollectionType(a.getCollectionType());
        asset.setManufacturerId(AssetManufacturerEnum.ORACLE.getCode());
        asset.setAssetImage(a.getAssetImage());
        asset.setNtpFlag(StatusConst.NO);
        outService.notifyOnChange(optFlag, asset);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateDb(ManageDb dbEntity, Asset asset) {
        List<BizManage> bizManages = bizService.listObjByBizId(dbEntity.getId(), BizManageConstant.M_DB);
        for (BizManage bizManage : bizManages) {
            bizManage.setManageId(asset.getOrgId());
        }
        if (!bizManages.isEmpty()) {
            bizService.updateBatchById(bizManages);
        }

        this.updateById(dbEntity);
        // 修改附属信息
        AssetAttach attach = new AssetAttach();
        attach.setAssetId(dbEntity.getId());
        attach.setOrgId(asset.getOrgId());
        attach.setRoomId(asset.getId());
        assetAttachService.updateById(attach);
    }

}
