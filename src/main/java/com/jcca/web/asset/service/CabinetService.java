package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.common.bean.ResultVo;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.vo.CabinetVo;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web.graph.vo.GraphStatusVo;
import com.jcca.web2.dto.CabinetBaseInfoQueryDto;
import com.jcca.web2.entity.FileRelate;
import com.jcca.web2.vo.CabinetBaseInfoVo;
import com.jcca.web2.vo.CabinetTopoDetailVo;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-26 15:33:54
 **/
public interface CabinetService extends IService<Cabinet> {


    /**
     * 根据机房ID获取机柜列表
     *
     * @param roomId
     * @return
     */
    List<CabinetVo> listByRoomId(String roomId);

    /**
     * 查询组织下的所有机柜
     *
     * @param rowIndex
     * @param orgId
     * @return
     */
    List<Cabinet> findByOrgId(Integer rowIndex, String orgId);

    /**
     * 通过组织ID查询机柜
     * @param orgId
     * @return
     */
    List<Cabinet> findByOrgId( String orgId);

    /**
     * 查询组织下的所有机柜
     *
     * @param orgId
     * @return
     */
    List<CabinetTopoDetailVo> findByOrgIdV2(String orgId);

    /**
     * 根据ID删除机柜
     *
     * @param id
     * @return
     */
    ResultVo<Object> delById(String id);

    /**
     * 获取机柜状态
     *
     * @param orgId
     * @return
     */
    List<GraphStatusVo> findStatusByOrgId(String orgId);

    /**
     * 获取机柜详情
     *
     * @param id
     * @return
     */
    List<DetailCabinetVo> findDetailById(String id);

    /**
     * 通过资产ID获取机柜
     *
     * @param assetId
     * @return
     */
    Cabinet findByAssetId(String assetId);

    /**
     * 获取机柜内设备告警信息
     *
     * @param id
     * @return
     */
    List<AlarmUnconfirmVo> findAlarmByCabinetId(String id);

    /**
     * 查询通过名字
     *
     * @param content
     * @return
     */
    List<Cabinet> findAllByName(String content);

    /**
     * V2版本接口用来获取机柜基础信息
     *
     * @param query 查询参数
     * @return 返回机柜基础信息 可能会有空返回
     * @author Lvyp
     */
    CabinetBaseInfoVo findCabinetBaseInfoV2(CabinetBaseInfoQueryDto query);

    /**
     * 保存机柜
     *
     * @param cabinet
     */
    void saveCabinetV2(Cabinet cabinet);

    /**
     * 机柜列表
     *
     * @param cabinet
     */
    List<Cabinet> getCabinetListV2(Cabinet cabinet);

    /**
     * 筛选  包含这个名称或IP资产的机柜 ID
     * */
    List<String> topoCabinetByAssetStr(String roomId, String keyword);

    List<FileRelate> findAssetInCabinet(String cabinetId);
}
