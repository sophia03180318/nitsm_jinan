package com.jcca.admin.system.entity;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.Cabinet;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 机房内设备信息
 */
@Data
public class SysRoomAssetMsgBean implements Serializable {

    private String roomId;

    private String cabinetId;
    /**
     * 机柜信息
     */
    private Cabinet cabinet;
    /**
     * 资产信息
     */
    private List<Asset> assetList;
}
