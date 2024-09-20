package com.jcca.web2.service.notify;

import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web2.dto.ThresholdManageQuery;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.service.AssetNotifyService;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.service.ThresholdManageService;
import com.jcca.web2.vo.ThresholdManageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 资产变动通知巡检
 * @className NotifyCollectorImpl
 * @date 2024/01/22 09:38
 * @since 2.1.0.0
 */
@Service
public class NotifyInspectImpl implements AssetNotifyService {

    @Resource
    private InspectRecordService inspectRecordService;
    @Resource
    private ThresholdManageService thresholdManageService;
    @Resource
    private SysOrgService orgService;

    /**
     * OutConst
     * 0新增，1删除，2修改
     *
     * @param asset 变动的资产
     * @param state 0新增，1删除，2修改
     */
    @Override
    public void assetChange(Asset asset, Integer state) {
        if (OutConst.ALL_ASSET_UPDATE.intValue() == state) {
            return;
        }

        if (OutConst.MODIFY_ASSET.intValue() == state) {
            this.modifyInspect(asset);
        }

        if (OutConst.ADD_ASSET.intValue() == state) {
            this.addInspect();

            this.addThreshold(asset);
        }

    }

    private void addThreshold(Asset asset) {
        ThresholdManageQuery query = new ThresholdManageQuery();
        query.setOrgId(asset.getOrgId());
        query.setAssetDesk(asset.getDesk());
        List<ThresholdManageVo> list = this.getList(query);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        List<ThresholdManage> list1 = new ArrayList<>();
        for (ThresholdManageVo manage : list) {
            ThresholdManage m = new ThresholdManage();
            BeanUtils.copyProperties(manage, m);
            m.setId(MyIdUtil.getId());
            m.setAssetId(asset.getId());
            m.setAssetDesk(asset.getDesk());
            m.setOrgId(asset.getOrgId());
            m.setServiceTypeId(asset.getServiceTypeId());
            list1.add(m);
        }
        thresholdManageService.saveBatch(list1);
    }

    private List<ThresholdManageVo> getList(ThresholdManageQuery manage) {
        String orgId = manage.getOrgId();
        if (!StringUtils.isEmpty(orgId)) {
            SysOrg org = orgService.getById(orgId);
            if (OrgTypeConst.LINE == org.getType()) {
                Set<SysOrg> children = orgService.getChildrenById(org.getId());
                List<String> orgIds = children.stream().map(SysOrg::getId).collect(Collectors.toList());
                manage.setOrgIds(orgIds);
                manage.setOrgId(null);
            }
        }

        List<ThresholdManageVo> result = new ArrayList<>();
        Set<String> set = new HashSet<>();
        List<ThresholdManageVo> list = thresholdManageService.getList(manage);
        for (ThresholdManageVo vo : list) {
            String category = vo.getCategory();
            String assetDesk = vo.getAssetDesk();
            if (set.contains(category + "_" + assetDesk)) {
                continue;
            }
            set.add(category + "_" + assetDesk);
            result.add(vo);
        }

        return result;
    }

    private void addInspect() {
        inspectRecordService.checkRecord();
    }

    private void modifyInspect(Asset asset) {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("assetId", asset.getId());
        map.put("status", asset.getWatch() + "");
        list.add(map);
        inspectRecordService.modifyAsset(list);
    }
}
