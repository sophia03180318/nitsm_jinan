package com.jcca.web.asset.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import com.jcca.web.asset.dao.CabinetMapper;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.vo.CabinetVo;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web.graph.vo.GraphStatusVo;
import com.jcca.web2.dto.CabinetBaseInfoQueryDto;
import com.jcca.web2.vo.CabinetBaseInfoVo;
import com.jcca.web2.vo.CabinetTopoDetailVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author hanwone
 * @date 2020-04-26 15:33
 **/
@Service
public class CabinetServiceImpl extends ServiceImpl<CabinetMapper, Cabinet> implements CabinetService {

    @Resource
    private CabinetMapper cabinetMapper;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private RoomService roomService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private AssetService   assetService;

    /**
     * 根据机房ID获取机柜列表
     *
     * @param roomId
     * @return
     */
    @Override
    public List<CabinetVo> listByRoomId(String roomId) {
        return cabinetMapper.listByRoomId(roomId);
    }

    /**
     * 查询组织下的所有机柜
     *
     * @param rowIndex
     * @param orgId
     * @return
     */
    @Override
    public List<Cabinet> findByOrgId(Integer rowIndex, String orgId) {
        return cabinetMapper.findByOrgId(rowIndex, orgId);
    }

    @Override
    public List<Cabinet> findByOrgId(String orgId) {
        return cabinetMapper.findCabinetByOrgId(orgId);
    }

    /**
     * 查询组织下的所有机柜
     *
     * @param orgId
     * @return
     */
    @Override
    public List<CabinetTopoDetailVo> findByOrgIdV2(String orgId) {
        return cabinetMapper.findByOrgIdV2(orgId);
    }

    /**
     * 根据ID删除机柜
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> delById(String id) {
        // 查看机柜内有无设备
        QueryWrapper<AssetAttach> query = Wrappers.query();
        query.eq("cabinet_id", id);
        List<AssetAttach> cabinetAssets = assetAttachService.list(query);
        if (CollectionUtil.isNotEmpty(cabinetAssets)) {
            return ResultVoUtil.error("请先删除机柜内资产");
        }

        // 删除机柜
        cabinetMapper.deleteById(id);

        return ResultVoUtil.success("成功");
    }

    /**
     * 获取机柜状态
     *
     * @param orgId
     * @return
     */
    @Override
    public List<GraphStatusVo> findStatusByOrgId(String orgId) {
        return cabinetMapper.findStatusByOrgId(orgId);
    }

    /**
     * 获取机柜详情
     *
     * @param id
     * @return
     */
    @Override
    public List<DetailCabinetVo> findDetailById(String id) {
        return cabinetMapper.findDetailById(id);
    }

    /**
     * 通过资产ID获取机柜
     *
     * @param assetId
     * @return
     */
    @Override
    public Cabinet findByAssetId(String assetId) {
        return cabinetMapper.findByAssetId(assetId);
    }

    /**
     * 获取机柜内设备告警信息
     *
     * @param id
     * @return
     */
    @Override
    public List<AlarmUnconfirmVo> findAlarmByCabinetId(String id) {
        return cabinetMapper.findAlarmByCabinetId(id);
    }

    /**
     * 查询名字
     */
    @Override
    public List<Cabinet> findAllByName(String content) {
        QueryWrapper<Cabinet> queryWrapper = new QueryWrapper<Cabinet>();
        queryWrapper.eq("NAME", content);
        return cabinetMapper.selectList(queryWrapper);
    }


    @Override
    public CabinetBaseInfoVo findCabinetBaseInfoV2(CabinetBaseInfoQueryDto query) {
        return cabinetMapper.selectCabinetBaseInfoV2(query);
    }

    /**
     * 保存机柜
     *
     * @param cabinet
     */
    @Override
    public void saveCabinetV2(Cabinet cabinet) {
        if (StrUtil.isNotEmpty(cabinet.getName()) && cabinet.getName().getBytes(StandardCharsets.UTF_8).length > 64) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "机柜名称字符超长,最长64个字符");
        }
        Room room = roomService.getById(cabinet.getRoomId());
        if (ObjectUtil.isNull(room)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "请选择机房!");
        }

        if (Objects.nonNull(cabinet.getQrCodeNum()) && StrUtil.isNotEmpty(cabinet.getQrCodeNum().trim())) {
            QueryWrapper<Cabinet> qw = Wrappers.query();
            qw.eq("QR_CODE_NUM", cabinet.getQrCodeNum());
            Cabinet c = this.getOne(qw);
            if (Objects.nonNull(c) && !c.getId().equals(cabinet.getId())) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "该识别号已被[" + c.getName() + "]使用，请您更换");
            }
        }

        QueryWrapper<Cabinet> query = Wrappers.query();
        query.eq("NAME", cabinet.getName());
        if (!StringUtils.isEmpty(cabinet.getId())) {
            query.ne("ID", cabinet.getId());
        }
        List<Cabinet> list = this.list(query);
        if (!list.isEmpty()) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "机柜名称不能重复");
        }

        query = Wrappers.query();
        query.eq("CODE", cabinet.getCode());
        query.eq("ROOM_ID", cabinet.getRoomId());
        if (!StringUtils.isEmpty(cabinet.getId())) {
            query.ne("ID", cabinet.getId());
        }
        list = this.list(query);
        if (!list.isEmpty()) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "机柜编号不能重复");
        }

        QueryWrapper<Cabinet> cabinetQuery = Wrappers.query();
        cabinetQuery.eq("ROOM_ID", cabinet.getRoomId());
        cabinetQuery.eq("ROW_INDEX", cabinet.getRowIndex());
        cabinetQuery.eq("COLUMN_INDEX", cabinet.getColumnIndex());
        if (Objects.nonNull(cabinet.getId())) {
            cabinetQuery.ne("ID", cabinet.getId());
        }
        Cabinet one = this.getOne(cabinetQuery);
        if (Objects.nonNull(one)) {
            throw new ResultException(ResultEnum.DATA_DELETE.getCode(), "该机房内机柜坐标(" + cabinet.getRowIndex() + "," + cabinet.getColumnIndex() + ")已存在");
        }

        this.saveOrUpdate(cabinet);
    }

    /**
     * 机柜列表
     *
     * @param cabinet
     */
    @Override
    public List<Cabinet> getCabinetListV2(Cabinet cabinet) {
        QueryWrapper<Cabinet> query = Wrappers.query();
        if (!StringUtils.isEmpty(cabinet.getName())) {
            query.like("NAME", cabinet.getName());
        }
        if (!StringUtils.isEmpty(cabinet.getRoomId())) {
            query.eq("ROOM_ID", cabinet.getRoomId());
        }

        return this.list(query);
    }

    @Override
    public List<String> topoCabinetByAssetStr(String roomId, String keyword) {

      return   cabinetMapper.topoCabinetByAssetStr( roomId,  keyword);

    }

}