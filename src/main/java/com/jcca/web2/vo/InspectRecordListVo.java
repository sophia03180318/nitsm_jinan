package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @author HanHW
 * @description 巡检列表标题
 * @className InspectRecordVo
 * @date 2024/1/15 14:46
 * @since 2.1.0.0
 */
@Data
public class InspectRecordListVo {

    String id;
    String assetId;
    String assetName;
    String inspectCode;
    String createTime;
    String inspectState;
    String remark;
    String resultPath;

    List<InspectRecordListVo> list;
}
