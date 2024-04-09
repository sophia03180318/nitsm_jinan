package com.jcca.web2.vo;

import com.jcca.web.asset.detail.bean.DetailProcess;
import com.jcca.web.collect.service.bean.AssetDiskVo;
import lombok.Data;

import java.util.List;


/**
 * @description: 服务器组件信息
 * @author: Lvyp
 * @create: 2023/12/28 13:20
 */
@Data
public class ServerModuleVo {

    /**
     * 磁盘信息包含磁盘列表
     */
    private AssetDiskVo assetDiskVo;
    /**
     * cpuTop5
     */
    private List<DetailProcess> processCpuTop;
    /**
     * mem top5
     */
    private List<DetailProcess> processMemTop;

}
