package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.dto.xunjian.InspectAssetDetailInfo;
import com.jcca.web2.dto.xunjian.InspectReport1;
import com.jcca.web2.dto.xunjian.InspectTargetDetailInfo;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.vo.InspectRecordListVo;
import com.jcca.web2.vo.ItemVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author HanHW
 * @description 巡检详情
 * @className InspectDetailMapper
 * @date 2024/1/19 11:33
 * @since 2.1.0.0
 */
public interface InspectDetailMapper extends BaseMapper<InspectDetail> {

    List<InspectRecordListVo> findRecordList();

    List<InspectRecordListVo> findDownList(String inspectCode);

    List<InspectRecordListVo> findRecordDetail(String inspectCode);

    List<InspectDetail> findAssetDetail(String assetId, String inspectCode);

    void deleteRecord(String inspectCode);

    List<String> totalAsset(String inspectCode);

    List<ItemVo> deskList(String inspectCode);

    Integer totalDesk(String inspectCode, Integer assetDesk);

    Integer stateDesk(String inspectCode, Integer assetDesk, int inspectState);

    Integer abnormalAsset(String inspectCode, int inspectState);

    List<InspectAssetDetailInfo> getAssetDetail(String inspectCode, String desks);

    List<InspectTargetDetailInfo> getTargetDetail(String inspectCode, String assetId);

    List<InspectReport1> getReport1(String inspectCode);
}
