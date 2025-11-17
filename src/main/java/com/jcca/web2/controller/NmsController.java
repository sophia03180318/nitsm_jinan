package com.jcca.web2.controller;

import com.baomidou.mybatisplus.annotation.*;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetPlaceEnum;
import com.jcca.web.asset.utils.enums.ProductTypeEnum;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lifp
 * @version 1.0
 * @description: NMS 同步ISTM资产数据
 * @date 2025-10-30 星期四 14:02:47
 */
@RestController
@RequestMapping(value = "/api/nms/v1")
public class NmsController {

    private final static Logger logger = LoggerFactory.getLogger(NmsController.class);

    @Resource
    private SysOrgService sysOrgService;

    @Resource
    private AssetService assetService;

    @PostMapping("/sync")
    public ResultVo syncItsmAsset() {
        try {
            List<AssetResponse> assetResponses = new ArrayList<>();

            // 选择车站数
            List<String> stationList = sysOrgService.lambdaQuery()
                    .select(SysOrg::getId)
                    .eq(SysOrg::getType, AssetPlaceEnum.STATION.getType())
                    .list()
                    .stream()
                    .map(SysOrg::getId)
                    .collect(Collectors.toList());

            // 选择车站下面的所有线路
            List<SysOrg> lineList = sysOrgService.lambdaQuery().in(SysOrg::getId, stationList).list();
            for (SysOrg sysOrg : lineList) {
                SysOrg parentId = sysOrgService.getById(sysOrg.getPid());
                List<Asset> assetList = assetService.lambdaQuery()
                        .eq(Asset::getOrgId, sysOrg.getId())
                        .eq(Asset::getAssetMode, ProductTypeEnum.LYQ.getCode())
                        .list();
                if (assetList.isEmpty()) continue;
                AssetResponse assetResponse = new AssetResponse();
                assetResponse.setOrgLineName(parentId.getTitle());

                List<AssetVo> assetData = new ArrayList<AssetVo>();
                assetList.forEach(i -> assetData.add(toEntity(i)));

                assetResponse.setAssetData(assetData);
                assetResponses.add(assetResponse);
            }

            return ResultVoUtil.success(assetResponses);
        } catch (Exception e) {
            logger.error("", e);
            return ResultVoUtil.error("数据处理异常" + e.getMessage());
        }
    }


    private AssetVo toEntity(Asset asset) {
        AssetVo assetVo = EntityBeanUtil.copy(asset, AssetVo.class);
        assetVo.setSnmpVersion("V2");
        return assetVo;
    }


    @Data
    static class AssetResponse {
        private String orgLineName;
        private List<AssetVo> assetData;
    }

    @Data
    public static class AssetVo {

        /**
         * Ip地址
         */
        private String ip;
        /**
         * Ip地址
         */
        private String ip2;

        /**
         * 设备名称
         */
        private String name;

        /**
         * 设备登录账号
         */
        private String osUser;
        /**
         * 设备登录密码
         */
        private String osPassword;
        /**
         * 网络设备登录用户名
         * 网络设备OSUSER存的是团体名
         */
        private String loginName;
        /**
         * 网络设备登录密码
         * 网络设备OSUSER存的是团体名
         */
        private String loginPwd;
        /**
         * 服务器的登录端口取这个字段！！！！
         * 登录端口
         */
        private Integer loginPort;

        /**
         * 组织ID
         */
        private String orgId;

        /**
         * snmp 版本
         * V2/V3 默认V2
         */
        private String snmpVersion;
    }
}
