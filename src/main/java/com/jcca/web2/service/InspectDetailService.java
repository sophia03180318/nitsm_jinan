package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.dto.xunjian.InspectTargetDetailInfoVo;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.vo.InspectRecordListVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectService
 * @date 2023/11/14 17:31
 * @since 2.1.0.0
 */
public interface InspectDetailService extends IService<InspectDetail> {

    List<InspectRecordListVo> recordList();

    List<InspectRecordListVo> findRecordDetail(String inspectCode);

    List<InspectDetail> findAssetDetail(String assetId, String inspectCode);

    void deleteRecord(String inspectCode);

    void exportAssetRecord(String inspectCode, String assetId, HttpServletResponse response);


    Map<String, Object> getRecordDetail(String id);

    InspectTargetDetailInfoVo getTargetDetail(String inspectCode, String assetId);

    Map<String, Object> getReport1(String inspectCode);

    Map<String, Object> report1Down(String inspectCode);
}
