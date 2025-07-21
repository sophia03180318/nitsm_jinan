package com.jcca.web.asset.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.AssetHardwareTypeEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.controller.bean.AssetHardwareFixReq;
import com.jcca.web.asset.dao.AssetHardwareFixMapper;
import com.jcca.web.asset.entity.AssetHardwareFix;
import com.jcca.web.asset.service.AssetHardwareFixService;
import com.jcca.web.asset.vo.AssetHardwareFixExportVo;
import com.jcca.web.asset.vo.AssetHardwareFixVo;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import com.jcca.web2.vo.AssetLifeLineVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-07-16 18:05
 **/
@Service
public class AssetHardwareFixServiceImpl extends ServiceImpl<AssetHardwareFixMapper, AssetHardwareFix> implements AssetHardwareFixService {

    @Resource
    private BizManageService bizManageService;
    @Resource
    private AssetHardwareFixMapper assetHardwareFixMapper;


    /**
     * 保存记录
     *
     * @param hardwareFix
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEntity(AssetHardwareFix hardwareFix) {
        String id = MyIdUtil.getId();
        hardwareFix.setId(id);
        this.save(hardwareFix);

        bizManageService.save(id, BizManageConstant.HARDWARE_FIX);
    }

    /**
     * 分页查询更换记录
     *
     * @param req
     * @return
     */
    @Override
    public List<AssetHardwareFixVo> findByPage(AssetHardwareFixReq req) {
        return assetHardwareFixMapper.findByPage(req);
    }

    /**
     * 按条件导出硬件更换记录
     *
     * @param req
     * @return
     */
    @Override
    public List<AssetHardwareFixExportVo> findExport(AssetHardwareFixReq req) {
        return assetHardwareFixMapper.findExport(req);
    }

    /**
     * 查找数量
     *
     * @param req
     * @return
     */
    @Override
    public Long countItem(AssetHardwareFixReq req) {
        return assetHardwareFixMapper.countItem(req);
    }

    /**
     * 查询设备硬件更换记录
     *
     * @param assetId
     * @return
     */
    @Override
    public List<AssetLifeLineVo> getLifeLineV2(String assetId) {
        QueryWrapper<AssetHardwareFix> query = new QueryWrapper<AssetHardwareFix>();
        query.eq("ASSET_ID", assetId);
        List<AssetHardwareFix> assetHardwareFixeList = assetHardwareFixMapper.selectList(query);

        List<AssetLifeLineVo> voList = new ArrayList<AssetLifeLineVo>();
        for (AssetHardwareFix assetHardwareFix : assetHardwareFixeList) {
            AssetLifeLineVo vo = new AssetLifeLineVo();
            vo.setTitle("更换" + AssetHardwareTypeEnum.getDecrip(assetHardwareFix.getHardwareType()));
            vo.setLinkId(assetHardwareFix.getId());
            vo.setType(AssetLifeLineVo.LifeLineType.FIX_LOG.getCode());
            vo.setCreateTime(assetHardwareFix.getFixTime());
            voList.add(vo);
        }
        return voList;
    }


    @Override
    public AssetHardwareFix getByAssetId(String assetId) {
        List<AssetHardwareFix> list = assetHardwareFixMapper.getByListAssetId(assetId);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}