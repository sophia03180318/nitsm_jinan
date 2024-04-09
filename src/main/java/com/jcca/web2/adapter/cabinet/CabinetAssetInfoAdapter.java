package com.jcca.web2.adapter.cabinet;

import com.jcca.web2.vo.CabinetAssetInfoVo;

import java.util.List;

/**
 * @description: 设备机柜详情的适配器
 * @author: Lvyp
 * @create: 2023/10/26 11:32
 */
public interface CabinetAssetInfoAdapter {

    /**
     * 适配器需要适配的型号
     *
     * @return
     */
    public List<Integer> getModel();

    /**
     * 补充完善机柜内设备详情信息
     *
     * @param baseInfo
     * @return
     */
    public CabinetAssetInfoVo fattenInfo(CabinetAssetInfoVo baseInfo);
}
