package com.jcca.web2.controller;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.log.annotation.FieldLogAnno;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.enums.AssetPlaceEnum;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    @GetMapping("/sync")
    public ResultVo syncItsmAsset() {
        try {
            Map<String, AssetResponse> stationAssetMap = new HashMap<String, AssetResponse>();

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
                List<Asset> assetList = assetService.lambdaQuery().eq(Asset::getOrgId, sysOrg.getId()).list();
                AssetResponse assetResponse = new AssetResponse();
                assetResponse.setOrgLineName(sysOrg.getTitle());

                List<AssetVo> assetData = new ArrayList<AssetVo>();
                assetList.forEach(i -> assetData.add(toEntity(i)));

                assetResponse.setAssetData(assetData);
                stationAssetMap.put(sysOrg.getId(), assetResponse);
            }

            return ResultVoUtil.success(stationAssetMap);
        } catch (Exception e) {
            logger.error("", e);
            return ResultVoUtil.error("数据处理异常" + e.getMessage());
        }
    }


    private AssetVo toEntity(Asset asset) {
        return EntityBeanUtil.copy(asset, AssetVo.class);
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
    }
}
