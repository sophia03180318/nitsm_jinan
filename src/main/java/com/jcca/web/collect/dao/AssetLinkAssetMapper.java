package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.AssetLinkAsset;
import com.jcca.web.common.service.bean.ThreeDLinkReq;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanhw
 * @description 设备对端连接信息
 * @className AssetLinkAssetMapper
 * @date 2023/4/27 9:40
 * @since 2.0.3.0
 */
public interface AssetLinkAssetMapper extends BaseMapper<AssetLinkAsset> {
    @Select("select l.ASSET_ID as assetAId ,PORT_INDEX as portAId,LINK_ASSET_ID as assetBId,LINK_PORT as portBId FROM (select asset_id from ASSET_ATTACH where ROOM_ID=#{roomId1} or ROOM_ID =#{roomId2})a  join ASSET_LINK_ASSET l on  l.ASSET_ID = a.ASSET_ID where LINK_ASSET_ID is not null")
    List<ThreeDLinkReq> getThreeDLink(String roomId1, String roomId2);
}
