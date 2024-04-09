package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.exception.asset.AssetNightBoardHaveMoreEnable;
import com.jcca.common.exception.common.DbEntityNotFound;
import com.jcca.web2.dto.BoardsAddDto;
import com.jcca.web2.dto.BoardsUpdateDto;
import com.jcca.web2.dto.UpdateBoardsConfigDto;
import com.jcca.web2.entity.NightBoard;
import com.jcca.web2.entity.NightBoardPayload;

/**
 * @description: 图标模板管理
 * @author: Lvyp
 * @create: 2023/11/10 17:42
 */
public interface NightBoardService extends IService<NightBoard> {

    /**
     * 创建
     *
     * @param req
     * @return entity
     */
    NightBoard saveBoards(BoardsAddDto req);

    /**
     * 更新配置
     *
     * @param req
     * @throws DbEntityNotFound
     */
    void updateConfig(UpdateBoardsConfigDto req) throws DbEntityNotFound;

    /**
     * 更新模板
     *
     * @param req
     * @throws DbEntityNotFound
     */
    void updateBoard(BoardsUpdateDto req) throws DbEntityNotFound, AssetNightBoardHaveMoreEnable;

    /**
     * 查询配置信息
     *
     * @param boardId
     * @return
     */
    NightBoardPayload getConf(String boardId);

    /**
     * 查询设备类型对应的配置信息
     *
     * @param assetId
     * @return
     */
    NightBoardPayload getAssetEnableConf(String assetId);
}
