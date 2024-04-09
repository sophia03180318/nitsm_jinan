package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author HanHW
 * @description 组织设备
 * @className InspectOrgAssetVo
 * @date 2024/1/26 18:06
 * @since 2.1.0.0
 */
@Data
public class InspectOrgAssetVo {

    // 无意义，前端使用
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String orgId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String orgName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer assetDesk;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String deskName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String assetId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String assetName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer assetStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<InspectOrgAssetVo> orgList;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<InspectOrgAssetVo> deskList;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<InspectOrgAssetVo> assetList;
}
