package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.controller.bean.AssetHardwareFixReq;
import com.jcca.web.asset.entity.AssetHardwareFix;
import com.jcca.web.asset.vo.AssetHardwareFixExportVo;
import com.jcca.web.asset.vo.AssetHardwareFixVo;
import com.jcca.web2.vo.AssetLifeLineVo;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-07-16 18:05:21
 **/
public interface AssetHardwareFixService extends IService<AssetHardwareFix> {


    /**
     * 保存记录
     *
     * @param hardwareFix
     */
    void saveEntity(AssetHardwareFix hardwareFix);

    /**
     * 分页查询更换记录
     *
     * @param req
     * @return
     */
    List<AssetHardwareFixVo> findByPage(AssetHardwareFixReq req);

    /**
     * 按条件导出硬件更换记录
     *
     * @param req
     * @return
     */
    List<AssetHardwareFixExportVo> findExport(AssetHardwareFixReq req);

    /**
     * 查找数量
     *
     * @param req
     * @return
     */
    Long countItem(AssetHardwareFixReq req);

    /**
     * 生命周期、硬件更换记录
     *
     * @param assetId
     * @return
     */
    List<AssetLifeLineVo> getLifeLineV2(String assetId);

    /**
     *
     * 获取指定资产的最新记录
     * */
    AssetHardwareFix getByAssetId(String assetId);
}
