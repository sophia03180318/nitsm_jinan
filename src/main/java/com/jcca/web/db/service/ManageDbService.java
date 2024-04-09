package com.jcca.web.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.db.entity.ManageDb;


/**
 * 数据库管理
 *
 * @author Lvyp
 */
public interface ManageDbService extends IService<ManageDb> {

    /**
     * 保存
     *
     * @param dbEntity
     */
    void saveDB(ManageDb dbEntity, String assetOrgId);

    /**
     * 删除
     *
     * @param dbEntity
     */
    void removeDBById(ManageDb dbEntity, String assetId);

    /**
     * 更新DB
     *
     * @param dbEntity
     * @param asset
     */
    void updateDb(ManageDb dbEntity, Asset asset);


}
