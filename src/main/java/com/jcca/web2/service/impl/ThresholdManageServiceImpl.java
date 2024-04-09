package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.bean.constant.ThresholdAutoFlagConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web2.dao.ThresholdManageMapper;
import com.jcca.web2.dto.ThresholdAssetListDto;
import com.jcca.web2.dto.ThresholdDto;
import com.jcca.web2.dto.ThresholdManageQuery;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.enums.ThresholdCategoryEnum;
import com.jcca.web2.service.ThresholdManageService;
import com.jcca.web2.vo.AssetBaseInfoVo;
import com.jcca.web2.vo.ThresholdManageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 阈值管理
 * @className ThresholdManageServiceImpl
 * @date 2023/12/15 15:26
 * @since 2.1.0.0
 */
@Service
public class ThresholdManageServiceImpl extends ServiceImpl<ThresholdManageMapper, ThresholdManage> implements ThresholdManageService {

    @Resource
    private ThresholdManageMapper thresholdManageMapper;
    @Resource
    private AssetService assetService;
    @Resource
    private ManageDbService manageDbService;
    @Resource
    private SysOrgService orgService;

    private static HashMap<String, List<Integer>> map = new HashMap<String, List<Integer>>() {
        private static final long serialVersionUID = 1672395454281107107L;

        {
            put(ThresholdCategoryEnum.CPU.name(), Arrays.asList(42, 201, 183, 1831, 1832, 1833, 1834));
            put(ThresholdCategoryEnum.MEMORY.name(), Arrays.asList(42, 201, 183, 1831, 1832, 1833, 1834));
            put(ThresholdCategoryEnum.RUNNINGTIME_DEVIATION.name(), Arrays.asList(42, 201, 183, 1831, 1832, 1833, 1834));
            put(ThresholdCategoryEnum.DISK.name(), Arrays.asList(183, 1831, 1832, 1833, 1834));
            put(ThresholdCategoryEnum.TIME_DEVIATION.name(), Arrays.asList(183, 1831, 1832, 1833, 1834));
            put(ThresholdCategoryEnum.CONNECTION_NO.name(), Arrays.asList(183, 1831, 1832, 1833, 1834));
            put(ThresholdCategoryEnum.PACKET_LOSS_IN.name(), Arrays.asList(42, 201));
            put(ThresholdCategoryEnum.PACKET_LOSS_OUT.name(), Arrays.asList(42, 201));
            put(ThresholdCategoryEnum.CODE_ERROR_IN.name(), Arrays.asList(42, 201));
            put(ThresholdCategoryEnum.CODE_ERROR_OUT.name(), Arrays.asList(42, 201));
            put(ThresholdCategoryEnum.PORT_RATE_IN.name(), Arrays.asList(42, 201));
            put(ThresholdCategoryEnum.PORT_RATE_OUT.name(), Arrays.asList(42, 201));
            put(ThresholdCategoryEnum.TABLE_SPACE.name(), Collections.singletonList(263));
            put(ThresholdCategoryEnum.TEMPERATURE.name(), Arrays.asList(42, 201, 183, 1831, 1832, 1833, 1834));
            put(ThresholdCategoryEnum.SWITCH_OPTICAL_RX.name(), Arrays.asList(42, 201));
            put(ThresholdCategoryEnum.SWITCH_OPTICAL_TX.name(), Arrays.asList(42, 201));
        }
    };

    /**
     * 获取阈值管理列表
     *
     * @param manage 参数
     * @return ThresholdManage
     */
    @Override
    public List<ThresholdManageVo> getList(ThresholdManageQuery manage) {
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

        return thresholdManageMapper.getList(manage);
    }

    /**
     * 保存阈值数据
     *
     * @param manage 阈值数据
     */
    @Override
    public synchronized void add(ThresholdManage manage) {

        this.checkData(manage);

        Set<String> set = new HashSet<>();
        List<String> assetIds = manage.getAssetIds();
        String category = manage.getCategory();
        if (CollectionUtils.isEmpty(assetIds)) {
            if (CollectionUtils.isEmpty(manage.getOrgIds())) {
                manage.setOrgIds(null);
            }
            manage.setDesks(map.get(category));
            List<AssetBaseInfoVo> voList = assetService.getAssetIdListV2(manage);
            if (CollectionUtils.isEmpty(voList) && ThresholdCategoryEnum.TABLE_SPACE.name().equals(category)) {
                // 查找数据库软件
                if (!CollectionUtils.isEmpty(manage.getOrgIds())) {
                    QueryWrapper<Asset> query = Wrappers.query();
                    query.in("ORG_ID", manage.getOrgIds());
                    query.eq("IS_DEL", StatusConst.OK);
                    query.eq("WATCH", StatusConst.OK);
                    List<Asset> list = assetService.list(query);
                    Set<String> collect = list.stream().map(Asset::getId).collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(collect)) {
                        throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "组织下无设备或未选择设备");
                    }
                    QueryWrapper<ManageDb> query1 = Wrappers.query();
                    query1.in("ASSET_ID", collect);
                    List<ManageDb> list1 = manageDbService.list(query1);
                    if (!CollectionUtils.isEmpty(list1)) {
                        Set<String> collect1 = list1.stream().map(ManageDb::getAssetId).collect(Collectors.toSet());
                        set.addAll(collect1);
                    }
                }
            } else {
                set.addAll(voList.stream().map(AssetBaseInfoVo::getId).collect(Collectors.toList()));
            }
        } else {
            if (ThresholdCategoryEnum.TABLE_SPACE.name().equals(category)) {
                // 查看资产下有没有数据库
                QueryWrapper<ManageDb> query1 = Wrappers.query();
                query1.eq("ASSET_ID", assetIds.get(0));
                List<ManageDb> list1 = manageDbService.list(query1);
                if (CollectionUtils.isEmpty(list1)) {
                    throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "该设备没有对应的数据库");
                }
            }
            set.addAll(assetIds);
        }

        if (CollectionUtils.isEmpty(set)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "组织下无设备或未选择设备");
        }

        Integer reset = manage.getReset();
        List<ThresholdManage> newList = new ArrayList<>();
        List<ThresholdManage> oldList = new ArrayList<>();
        List<ThresholdManage> restList = new ArrayList<>();

        QueryWrapper<ThresholdManage> query;
        ThresholdManage one;
        List<ThresholdManage> list;
        for (String id : set) {
            query = Wrappers.query();
            query.eq("ASSET_ID", id);
            query.eq("CATEGORY", category);
            if (reset == 0) {
                query.eq("AUTO_FLAG", ThresholdAutoFlagConst.ORG_THRESHOLD);
            }
            list = this.list(query);
            if (CollectionUtils.isEmpty(list) || list.size() > 1) {

                this.remove(query);

                ThresholdManage m = new ThresholdManage();
                BeanUtils.copyProperties(manage, m);
                m.setId(MyIdUtil.getId());
                Asset oen = assetService.getById(id);
                m.setAssetId(id);
                m.setOrgId(oen.getOrgId());
                m.setAssetDesk(oen.getDesk());
                m.setServiceTypeId(oen.getServiceTypeId());
                newList.add(m);
                continue;
            }

            one = list.get(0);

            ThresholdDto dto = new ThresholdDto();
            BeanUtils.copyProperties(manage, dto);

            String oid = one.getId();
            String orgId = one.getOrgId();
            Integer assetDesk = one.getAssetDesk();
            String serviceTypeId = one.getServiceTypeId();
            if (reset == 1 && one.getAutoFlag() == ThresholdAutoFlagConst.SIGNLE_THRESHOLD) {
                ThresholdManage m = new ThresholdManage();
                BeanUtils.copyProperties(manage, m);
                m.setId(oid);
                m.setAssetId(id);
                m.setOrgId(orgId);
                m.setServiceTypeId(serviceTypeId);
                m.setAssetDesk(assetDesk);
                restList.add(m);
                continue;
            }

            BeanUtils.copyProperties(manage, one);
            one.setId(oid);
            one.setAssetId(id);
            one.setServiceTypeId(serviceTypeId);
            one.setOrgId(orgId);
            one.setAssetDesk(assetDesk);
            oldList.add(one);
        }
        if (!CollectionUtils.isEmpty(newList)) {
            this.saveBatch(newList);
        }
        if (!CollectionUtils.isEmpty(oldList)) {
            this.removeByIds(oldList.stream().map(ThresholdManage::getId).collect(Collectors.toList()));
            this.saveBatch(oldList);
        }
        if (!CollectionUtils.isEmpty(restList)) {
            this.removeByIds(restList.stream().map(ThresholdManage::getId).collect(Collectors.toList()));
            this.saveBatch(restList);
        }
    }

    private void checkData(ThresholdManage manage) {
        Double general = manage.getGeneral();
        Double rangeMin = manage.getRangeMin();
        Double rangeMax = manage.getRangeMax();

        Double stepHigh = manage.getStepHigh();
        Double stepHigher = manage.getStepHigher();
        Double stepHighest = manage.getStepHighest();

        if (Objects.isNull(general) && Objects.isNull(rangeMin) && Objects.isNull(rangeMax)
                && Objects.isNull(stepHigh) && Objects.isNull(stepHigher) && Objects.isNull(stepHighest)) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "未保存全空阈值，应至少填写一项");
        }

        if (!(ThresholdCategoryEnum.SWITCH_OPTICAL_RX.name().equals(manage.getCategory())
                || ThresholdCategoryEnum.SWITCH_OPTICAL_TX.name().equals(manage.getCategory()))) {
            this.check(rangeMin);
            this.check(rangeMax);
            this.check(stepHigh);
            this.check(stepHigher);
            this.check(stepHighest);

            if (!ThresholdCategoryEnum.RUNNINGTIME_DEVIATION.name().equals(manage.getCategory())) {
                this.check(general);
            }
        }

        if ((Objects.isNull(rangeMin) && Objects.nonNull(rangeMax)) || (Objects.nonNull(rangeMin) && Objects.isNull(rangeMax))) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "范围阈值应同时有上下限");
        }

        if (Objects.nonNull(rangeMin) && rangeMin >= rangeMax) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "范围阈值大小赋值错误");
        }

        if ((Objects.isNull(stepHigh) && Objects.nonNull(stepHigher) && Objects.nonNull(stepHighest))
                || (Objects.isNull(stepHigh) && Objects.isNull(stepHigher) && Objects.nonNull(stepHighest))
                || (Objects.nonNull(stepHigh) && Objects.nonNull(stepHigher) && Objects.isNull(stepHighest))
                || (Objects.nonNull(stepHigh) && Objects.isNull(stepHigher) && Objects.nonNull(stepHighest))) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "阶梯阈值应同时有一二三阶段");
        }

        if (Objects.isNull(stepHigh) && Objects.isNull(stepHigher)) {
            return;
        }

        if (stepHigh >= stepHigher || stepHigh >= stepHighest || stepHigher >= stepHighest) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "阶梯阈值大小赋值错误");
        }
    }

    private void check(Double value) {
        if (Objects.isNull(value)) {
            return;
        }
        double cpu = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (cpu > 100D || cpu < 0.01D) {
            throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "阈值应在0.01到100之间");
        }
    }

    /**
     * 获取资产ID 名称
     *
     * @param dto 查询条件
     * @return id  name
     */
    @Override
    public List<AssetBaseInfoVo> getAssetList(ThresholdAssetListDto dto) {
        Integer assetDesk = dto.getAssetDesk();
        ThresholdManage manage = new ThresholdManage();
        if (Objects.nonNull(assetDesk) && AssetModeConst.ORACLE_DB.intValue() == assetDesk) {
            List<ManageDb> list = manageDbService.list();
            if (CollectionUtils.isEmpty(list)) {
                throw new ResultException(ResultEnum.CANNOT_FIND.getCode(), "没有找到数据库资产");
            } else {
                List<String> assetIds = list.stream().map(ManageDb::getAssetId).collect(Collectors.toList());
                manage.setAssetIds(assetIds);
                manage.setOrgIds(dto.getOrgIds());
                return assetService.getAssetIdListV2(manage);
            }
        }

        BeanUtils.copyProperties(dto, manage);
        return assetService.getAssetIdListV2(manage);
    }

    /**
     * 查询所有打量阈值
     *
     * @return ThresholdManageVo
     */
    @Override
    public List<ThresholdManageVo> batchList(ThresholdAssetListDto dto) {
        String orgId = dto.getOrgId();
        if (!StringUtils.isEmpty(orgId)) {
            SysOrg org = orgService.getById(orgId);
            if (OrgTypeConst.LINE == org.getType()) {
                Set<SysOrg> children = orgService.getChildrenById(org.getId());
                List<String> orgIds = children.stream().map(SysOrg::getId).collect(Collectors.toList());
                dto.setOrgIds(orgIds);
                dto.setOrgId(null);
            }
        }

        return thresholdManageMapper.batchList(dto);
    }


}
