package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectPcb;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 板卡采集信息
 *
 * @author Lvyp
 */
@Mapper
public interface CollectPcbMapper extends BaseMapper<CollectPcb> {

    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    @Select("SELECT * FROM COLLECT_PCB b WHERE b.COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code FROM COLLECT_PCB WHERE ASSET_ID=#{assetId})")
    List<CollectPcb> selectRealTimeData(String assetId);

    @Select("select ID,COLLECT_TIME,COLLECT_CODE,ASSET_ID,NAME,MODEL_NAME,TYPE,SERIAL_NUMBER_NAME,SOFTWARE_VERSION,HARDWATE_VERSION,DESC_STR,OS_VERSION,PCB_INDEX,CREATE_TIME,ENT_PHYSICAL_INDEX,ENT_PHYSICAL_CONTAINED_IN,ENT_PHYSICAL_PAR_PELPOS,ENT_PHYSICAL_IS_FRU,CASE WHEN SERIAL_NUMBER ='--' THEN '' else SERIAL_NUMBER END AS SERIAL_NUMBER from  COLLECT_PCB where  COLLECT_CODE= (select MAX(COLLECT_CODE)AS CODE FROM COLLECT_PCB where ASSET_ID=#{assetId}) AND  ASSET_ID=#{assetId} order by ENT_PHYSICAL_CONTAINED_IN ,ENT_PHYSICAL_PAR_PELPOS")
    List<CollectPcb> getPcdInfoByAsset(String assetId);
}
