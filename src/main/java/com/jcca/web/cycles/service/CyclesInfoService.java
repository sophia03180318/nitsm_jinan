package com.jcca.web.cycles.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.cycles.entity.CyclesInfo;
import com.jcca.web.cycles.service.bean.CyclesException;

import java.util.Date;

/**
 * @description: 周期信息
 * @author: Lvyp
 * @create: 2024/11/20 10:04
 */
public interface CyclesInfoService extends IService<CyclesInfo> {


    /**
     * 保存信息
     *
     * @param req
     */
    void saveInfo(CyclesInfo req) throws CyclesException;

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @param name
     * @return
     */
    IPage pageQuery(Integer page, Integer size, String name);

    /**
     * 更新
     *
     * @param updateReq
     */
    void updateInfo(CyclesInfo updateReq) throws CyclesException;

    /**
     * 通过ID删除
     *
     * @param id
     * @throws CyclesException
     */
    void deleteById(String id) throws CyclesException;

    /**
     * 验证是否是天窗
     * true 是天窗   false 不是天窗
     *
     * @param assetId
     * @param occurTime
     */
    boolean verifyIsBlank(Asset assetId, Date occurTime);

    /**
     * 通过ID查询
     *
     * @param id
     * @return
     */
    CyclesInfo queryInfoById(String id);
}
