package com.jcca.web2.controller;


import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web2.dto.CommonOrganizationsDto;
import com.jcca.web2.vo.CommonOrganizationInfo;
import com.jcca.web2.vo.CommonAssetInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lifp
 * @version 1.0
 * @description: 通用组件接口
 * @date 2025-10-14 星期二 15:43:22
 */
@RestController
@RequestMapping(value = "/api/v2/common")
public class CommonController {

    @Resource
    private AssetService assetService;

    @Resource
    private SysOrgService orgService;

    /**
     * 根据组织机构id查询 所有的资产设备
     *
     * @param orgId
     * @return
     */
    @GetMapping(value = "/organize/{orgId}")
    public ResultVo<Object> getOrganizations(@PathVariable(value = "orgId") String orgId) {
        if (orgId == null || orgId.isEmpty()) {
            return ResultVoUtil.error("参数不能为空!");
        }
        try {
            ArrayList<CommonAssetInfo> organizations = new ArrayList<>();
            List<Asset> list = assetService.lambdaQuery()
                    .select(
                            Asset::getId,
                            Asset::getName,
                            Asset::getAssetMode,
                            Asset::getOrgId)
                    .eq(Asset::getOrgId, orgId)
                    .eq(Asset::getWatch, 1)
                    .list();

            for (Asset asset : list) {
                organizations.add(transfer(asset));
            }

            return ResultVoUtil.success(organizations);
        } catch (Exception e) {
            return ResultVoUtil.error("数据获取异常");
        }
    }

    /**
     * 批量查询组织机构下的设备数量
     *
     * @param organizations
     * @return
     */
    @PostMapping(value = "/organizations")
    public ResultVo<Object> getOrganizations(@RequestBody CommonOrganizationsDto organizations) {
        List<String> orgIdList = organizations.getOrgIdList();
        if (orgIdList == null || orgIdList.isEmpty()) {
            return ResultVoUtil.error("组织机构不允许为空");
        }

        List<CommonOrganizationInfo> organizationInfosAssetList = new ArrayList<>();
        try {
            for (String orgId : orgIdList) {
                SysOrg sysOrg = orgService.lambdaQuery().eq(SysOrg::getId, orgId).one();
                List<CommonAssetInfo> assetInfoData = new ArrayList<>();
                CommonOrganizationInfo organization = new CommonOrganizationInfo();
                List<Asset> list = assetService.lambdaQuery()
                        .select(
                                Asset::getId,
                                Asset::getName,
                                Asset::getAssetMode,
                                Asset::getOrgId)
                        .eq(Asset::getOrgId, orgId)
                        .eq(Asset::getWatch, 1)
                        .list();

                for (Asset asset : list) {
                    assetInfoData.add(transfer(asset));
                }

                organization.setId(orgId);
                organization.setOrgName(sysOrg.getTitle());
                organization.setChildren(assetInfoData);
                organization.setParentId(sysOrg.getPid());
                organizationInfosAssetList.add(organization);
            }
            return ResultVoUtil.success(organizationInfosAssetList);
        } catch (Exception e) {
            return ResultVoUtil.error("数据获取异常");
        }
    }

    /**
     * 批量查询设备数量
     *
     * @param organizations
     * @return
     */
    @PostMapping(value = "/assetInfo")
    public ResultVo<Object> getAssetInfoList(@RequestBody CommonOrganizationsDto organizations) {
        List<String> assetIdList = organizations.getAssetIdList();
        if (assetIdList == null || assetIdList.isEmpty()) {
            return ResultVoUtil.error("资产id不允许为空");
        }

        try {
            List<Asset> list = assetService.lambdaQuery().select(
                    Asset::getId,
                    Asset::getName,
                    Asset::getAssetMode,
                    Asset::getOrgId).in(Asset::getId, assetIdList).list();
            List<CommonAssetInfo> assetInfoData = new ArrayList<>();

            for (Asset asset : list) {
                assetInfoData.add(transfer(asset));
            }
            return ResultVoUtil.success(assetInfoData);
        } catch (Exception e) {
            return ResultVoUtil.error("数据获取异常");
        }
    }

    /**
     * 对象格式转换
     *
     * @param asset
     * @return
     */
    public CommonAssetInfo transfer(Asset asset) {
        CommonAssetInfo organizations = new CommonAssetInfo();
        organizations.setAssetName(asset.getName());
        organizations.setAssetMode(asset.getAssetMode());
        organizations.setOrgId(asset.getOrgId());
        organizations.setAssetId(asset.getId());
        return organizations;
    }
}
