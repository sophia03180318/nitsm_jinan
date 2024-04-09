package com.jcca.web2.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.exception.asset.AssetNightBoardHaveMoreEnable;
import com.jcca.common.exception.common.DbEntityNotFound;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.dao.AssetMapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web2.dao.NightBoardMapper;
import com.jcca.web2.dao.NightBoardPayloadMapper;
import com.jcca.web2.dto.BoardsAddDto;
import com.jcca.web2.dto.BoardsUpdateDto;
import com.jcca.web2.dto.UpdateBoardsConfigDto;
import com.jcca.web2.entity.NightBoard;
import com.jcca.web2.entity.NightBoardPayload;
import com.jcca.web2.service.NightBoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * @description: 图标管理
 * @author: Lvyp
 * @create: 2023/11/10 17:42
 */
@Service
public class NightBoardServiceImpl extends ServiceImpl<NightBoardMapper, NightBoard> implements NightBoardService {

    @Resource
    private NightBoardMapper boardMapper;
    @Resource
    private NightBoardPayloadMapper boardPayloadMapper;
    @Resource
    private AssetMapper assetMapper;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public NightBoard saveBoards(BoardsAddDto req) {
        String configs = req.getConfigs();
        String name = req.getName();
        String tags = req.getTags();
        Date date = new Date();
        String boardId = MyIdUtil.getId();
        NightBoard board = new NightBoard();
        board.setId(boardId);
        board.setName(name);
        board.setIdent("");
        board.setTags(tags);
        board.setPub(0);
        board.setBuiltIn(0);
        board.setHide(0);
        board.setEnable(0);
        board.setCreateAt(date);
        board.setUpdateAt(date);
        board.setAssetMode(req.getAssetMode());

        NightBoardPayload payload = new NightBoardPayload();
        payload.setPayload(configs);
        payload.setBoardId(boardId);
        payload.setId(boardId);

        boardMapper.insert(board);
        boardPayloadMapper.insert(payload);

        return board;
    }

    @Override
    public void updateConfig(UpdateBoardsConfigDto req) throws DbEntityNotFound {
        NightBoardPayload boardPayload = boardPayloadMapper.selectById(req.getBoardId());
        if (Objects.isNull(boardPayload)) {
            throw new DbEntityNotFound(String.format("在表%s中未找到id是%s的记录", "NIGHT_BOARD_PAYLOAD", req.getBoardId()));
        }
        boardPayload.setPayload(req.getConfigs());
        boardPayloadMapper.updateById(boardPayload);
    }

    @Override
    public void updateBoard(BoardsUpdateDto req) throws DbEntityNotFound, AssetNightBoardHaveMoreEnable {
        NightBoard nightBoard = boardMapper.selectById(req.getBoardId());
        if (Objects.isNull(nightBoard)) {
            throw new DbEntityNotFound(String.format("在表%s中未找到id是%s的记录", "NIGHT_BOARD", req.getBoardId()));
        }
        nightBoard.setUpdateAt(new Date());
        if (Objects.nonNull(req.getAssetMode())) {
            nightBoard.setAssetMode(req.getAssetMode());
        }
        if (StrUtil.isNotEmpty(req.getTags())) {
            nightBoard.setTags(req.getTags());
        }
        if (StrUtil.isNotEmpty(req.getName())) {
            nightBoard.setName(req.getName());
        }
        if (Objects.nonNull(req.getEnable()) && Objects.nonNull(req.getAssetMode())) {
            //一个类型尽可打开一个模板
            QueryWrapper<NightBoard> queryWrapper = new QueryWrapper<NightBoard>();
            queryWrapper.eq("ENABLE", 1);
            queryWrapper.eq("ASSET_MODE", req.getAssetMode());
            NightBoard dbEntity = boardMapper.selectOne(queryWrapper);
            if (Objects.nonNull(dbEntity) && req.getEnable().equals(1)) {
                throw new AssetNightBoardHaveMoreEnable(String.format("设备类型%s已经拥有一个启用的模板", nightBoard.getAssetMode()));
            }
            nightBoard.setEnable(req.getEnable());
        }
        if (Objects.nonNull(req.getPub())) {
            nightBoard.setPub(req.getPub());
        }
        if (Objects.nonNull(req.getHide())) {
            nightBoard.setHide(req.getHide());
        }
        boardMapper.updateById(nightBoard);
    }

    @Override
    public NightBoardPayload getConf(String boardId) {
        return boardPayloadMapper.selectById(boardId);
    }

    @Override
    public NightBoardPayload getAssetEnableConf(String assetId) {
        Asset asset = assetMapper.selectById(assetId);
        Integer assetMode = asset.getAssetMode();
        Integer desk = asset.getDesk();
        QueryWrapper<NightBoard> queryWrapper = new QueryWrapper<NightBoard>();
        queryWrapper.eq("ENABLE", 1);
        queryWrapper.eq("ASSET_MODE", desk);
        NightBoard dbEntity = boardMapper.selectOne(queryWrapper);
        if (Objects.isNull(dbEntity)) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ENABLE", 1);
            queryWrapper.eq("ASSET_MODE", assetMode);
            dbEntity = boardMapper.selectOne(queryWrapper);
        }
        if (Objects.nonNull(dbEntity)) {
            return boardPayloadMapper.selectById(dbEntity.getId());
        }
        return null;
    }


}
