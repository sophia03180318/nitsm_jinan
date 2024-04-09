package com.jcca.web2.vo;

import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.vo.DetailCabinetVo;
import lombok.Data;

import java.util.List;

/**
 * @Description 机柜详情
 *  * @Author sophia
 * @Date 2023/10/24 17:12
 */
@Data
public class CabinetTopoDetailVo {
    /**
     * 机柜ID
     * */
    private String id;

    /**
     * 机柜名称
     * */
    private String name;

    /**
     *机房名称
     * */
    private  String roomName;

    /**
     * 机房ID
     * */
    private  String roomId;

    /**
     * 机柜中资产列表
     * */
    private List<DetailCabinetVo> assetList;
}
