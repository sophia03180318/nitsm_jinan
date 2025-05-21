package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.vo.*;

import java.util.List;
import java.util.Map;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectService
 * @date 2023/11/14 17:31
 * @since 2.1.0.0
 */
public interface InspectRecordService extends IService<InspectRecord> {
    /**
     * @description: 分组查询组织机柜
     * @author: HanHW
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.InspectVo>
     **/
    List<InspectVo> getOrgCabinet(String inspectCode);

    /**
     * @description: 获取设备所有采集指标信息
     * @author: HanHW
     * @param: [assetId]
     * @return: com.jcca.web2.vo.InspectionTargetVo
     **/
    InspectTargetVo getAssetTarget(String assetId);

    /**
     * @description: 获取设备指标状态
     * @author: HanHW
     * @param: [assetId]
     * @return: com.jcca.web2.vo.InspectionResult
     **/
    List<InspectResultVo> getTargetState(String assetId);

    /**
     * @description: 按设备类型获取设备列表
     * @author: HanHW
     * @param: [modeType]
     * @return: java.util.List<com.jcca.web2.vo.InspectVo>
     **/
    List<InspectVo> getModeAsset(String modeType);

    /**
     * @description: 开始巡检
     * @author: HanHW
     * @param: []
     * @return: void
     **/
    void start(String inspectType);

    /**
     * @description: 查询上一次巡检记录
     * @author: HanHW
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.InspectVo>
     **/
    List<InspectVo> getNextRecords(String inspectCode);

    /**
     * @description: 增减巡检指标
     * @author: HanHW
     * @param: [modeType, targetId, status]
     * @return: void
     **/
    void modifyTarget(String modeType, String targetItem, String status);

    /**
     * @description: 增减巡检资产
     * @author: HanHW
     * @param: [assetId, status]
     * @return: void
     **/
    void modifyAsset(List<Map<String, String>> list);

    /**
     * @description: 按机柜ID查询机柜内设备信息
     * @author: HanHW
     * @param: [cabinetId]
     * @return: java.util.List<com.jcca.web.asset.vo.DetailCabinetVo>
     **/
    List<DetailCabinetVo> findByCabinetId(String cabinetId);

    /**
     * @description: 获取正在巡检采集记录 INSPECT_CODE
     * @author: HanHW
     * @param: []
     * @return: java.lang.String
     **/
    String getLastInspectCode();

    /**
     * @description: 查询当前巡检中异常指标
     * @author: HanHW
     * @param: [assetId]
     * @return: java.util.List<com.jcca.web2.entity.InspectRecord>
     **/
    List<InspectRecord> findAssetAbnormalTarget(String assetId);

    /**
     * 查询正常指标项
     *
     * @param assetId
     * @return
     */
    List<InspectRecord> findAssetNormalTarget(String assetId);

    /**
     * @description: 暂停巡检
     * @author: HanHW
     * @param: []
     * @return: void
     **/
    void pause();

    /**
     * @description: 结束巡检
     * @author: HanHW
     * @param: []
     * @return: void
     **/
    void end(String msg);

    /**
     * 获取巡检状态
     */
    String getState();

    /**
     * 删除已删除设备却未巡检的记录
     */
    void deleteInspectByAssetId(String assetId);

    /**
     * 查询指标巡检状态
     *
     * @param assetId
     * @param targetItem
     * @param inspectCode
     * @return
     */
    InspectRecord findTargetState(String assetId, String targetItem, String modeType, String inspectCode);

    /**
     * 查询资产状态
     *
     * @param assetId
     * @return
     */
    InspectRecord findAssetState(String assetId);

    /**
     * 查询机柜状态
     *
     * @param cabinetId
     * @return
     */
    InspectRecord findCabinetState(String cabinetId);

    /**
     * 查询待巡检资产ID
     *
     * @param
     * @return
     */
    List<String> findAssetIdList();


    InspectRecord findOneByAssetAndTarget(String assetId, String modeType, String targetItem);

    /**
     * 准备巡检数据
     */
    void prepareRecord();

    void checkRecord();

    /**
     * 按组织 资产类型 统计资产
     *
     * @return InspectOrgAssetVo
     */
    List<InspectOrgAssetVo> getOrgAsset();

    /**
     * 更新巡检类型
     *
     * @param inspectType 1资产巡检，2指标巡检
     */
    void updateInspect(String inspectType);

    /**
     * 按照任务ID获取巡检记录
     *
     * @param scheduleId 任务ID
     * @return
     */
    List<ItemVo> findBySchuduleId(String scheduleId);
}
