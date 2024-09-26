package com.jcca.web2.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.enums.SystemTypeEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.common.utils.TestIpUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dao.InspectRecordMapper;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.InspectDetailService;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.service.XunjianSupportService;
import com.jcca.web2.vo.InspectOrgAssetVo;
import com.jcca.web2.vo.InspectResultVo;
import com.jcca.web2.vo.InspectTargetVo;
import com.jcca.web2.vo.InspectVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectServiceImpl
 * @date 2023/11/14 17:32
 * @since 2.1.0.0
 */
@Service
public class InspectRecordServiceImpl extends ServiceImpl<InspectRecordMapper, InspectRecord> implements InspectRecordService {

    @Resource
    private InspectRecordMapper inspectRecordMapper;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private AssetService assetService;
    @Resource
    private XunjianSupportService xunjianSupportService;
    @Resource
    private InspectDetailService inspectDetailService;
    @Resource
    private AssetModeService assetModeService;

    @Value("${project.upload.file-path}")
    private String filePath;
    @Value("${project.upload.static-path}")
    private String staticPath;
    @Value("${project.upload.static-url}")
    private String staticUrl;

    // 巡检管理操作状态
    private String inspectOperState = Web2Const.INSPECT_NO;

    // 存放多中心虚拟机柜ID
    private Map<String, String> virMap = new HashMap<>();

    /**
     * @description: 分组查询组织机柜
     * @author: HanHW
     * @date: 2023/11/14 17:34
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.InspectVo>
     **/
    @Override
    public List<InspectVo> getOrgCabinet(String inspectCode) {
        String inspectType = inspectRecordMapper.findNowInspectType();
        if (StringUtils.isEmpty(inspectType)) {
            inspectType = Web2Const.INSPECT_ASSET;
        }
        List<InspectVo> resList = new ArrayList<>();
        List<InspectRecord> orgRecords = inspectRecordMapper.findLastGroupByOrgId(inspectType);
        List<InspectRecord> cabinetRecords = inspectRecordMapper.findLastGroupByCabinetId(inspectType);
        for (InspectRecord cord1 : orgRecords) {
            String orgId = cord1.getOrgId();
            List<InspectVo> cabinetList = new ArrayList<>();
            for (InspectRecord cord2 : cabinetRecords) {
                if (orgId.equals(cord2.getOrgId())) {
                    String cabinetId = cord2.getCabinetId();
                    String state = inspectRecordMapper.findCabinetState(cabinetId, inspectType).getInspectState();
                    InspectVo ca = new InspectVo();
                    ca.setId(cabinetId);
                    ca.setName(cord2.getCabinetName());
                    ca.setType("2");
                    ca.setState(Objects.isNull(state) ? Web2Const.INSPECT : state);
                    cabinetList.add(ca);
                }
            }

            if (!CollectionUtils.isEmpty(cabinetList)) {
                InspectVo vo = new InspectVo();
                vo.setId(cord1.getOrgId());
                vo.setName(cord1.getOrgName());
                vo.setType("1");
                vo.setList(cabinetList);

                resList.add(vo);
            }
        }
        return resList;
    }

    /**
     * @description: 获取资产所有采集指标信息
     * @author: HanHW
     * @date: 2023/11/15 11:40
     * @param: [assetId]
     * @return: com.jcca.web2.vo.InspectionTargetVo
     */
    @Override
    public InspectTargetVo getAssetTarget(String assetId) {

        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "当前巡检状态", inspectOperState);

        String inspectType = inspectRecordMapper.findNowInspectType();
        if (StringUtils.isEmpty(inspectType)) {
            inspectType = Web2Const.INSPECT_ASSET;
        }

        List<InspectResultVo> resList = new ArrayList<>();
        QueryWrapper<InspectRecord> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        if (Web2Const.INSPECT_ASSET.equals(inspectType)) {
            query.eq("ASSET_STATUS", StatusConst.OK);
        }
        if (Web2Const.INSPECT_TARGET.equals(inspectType)) {
            query.eq("TARGET_STATUS", StatusConst.OK);
        }
        List<InspectRecord> list = this.list(query);
        for (InspectRecord record : list) {
            InspectResultVo rz = new InspectResultVo();
            rz.setAssetId(assetId);
            rz.setTargetItem(record.getTargetItem());
            rz.setModeType(record.getModeType());
            rz.setTargetName(record.getTargetName());
            rz.setInspectState(record.getInspectState());
            resList.add(rz);
        }

        // 查资产异常指标
        List<InspectRecord> ablist = this.findAssetAbnormalTarget(assetId);
        // 查资产正常指标
        List<InspectRecord> normlist = this.findAssetNormalTarget(assetId);
        InspectTargetVo vo = new InspectTargetVo();
        vo.setTargetTotal(list.size());
        vo.setNormalCount(normlist.size());
        vo.setAbnormalCount(ablist.size());
        vo.setTargettList(resList);
        return vo;
    }

    private static Map<Integer, String> map = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 6835734280829721552L;

        {
            put(SystemTypeEnum.WINDOWS.getCode(), "WIN");
            put(SystemTypeEnum.LINUX.getCode(), "LINUX");
            put(SystemTypeEnum.AIX.getCode(), "AIX");
        }
    };


    private Set<String> unIpSet = new HashSet<>();

    /**
     * @description: 获取资产指标状态
     * @author: HanHW
     * @date: 2023/11/15 11:58
     * @param: [assetId, targetItem]
     * @return: com.jcca.web2.vo.InspectionResult
     */
    @Override
    public List<InspectResultVo> getTargetState(String assetId) {

        Asset asset = assetService.getById(assetId);
        if (Objects.isNull(asset)) {
            return new ArrayList<>();
        }
        String ip = asset.getIp();
        boolean ping = false;
        try {
            ping = TestIpUtil.ping(ip, 1);
        } catch (IOException e) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, assetId, "5:" + ResultEnum.INSPECT_NO_DATA.getMessage());
        }

        if (!ping) {
            unIpSet.add(ip);
            UpdateWrapper<InspectRecord> wrapper = Wrappers.update();
            wrapper.eq("ASSET_ID", assetId);
            wrapper.set("INSPECT_STATE", Web2Const.INSPECT_ERROR);
            wrapper.set("INSPECT_VALUE", "网络不通");
            this.update(wrapper);
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, assetId, "3:" + ResultEnum.INSPECT_NO_DATA.getMessage());
            return new ArrayList<>();
        }
        unIpSet.remove(ip);

        String inspectType = inspectRecordMapper.findNowInspectType();
        if (StringUtils.isEmpty(inspectType)) {
            inspectType = Web2Const.INSPECT_ASSET;
        }

        // 获取巡检指标状态
        QueryWrapper<InspectRecord> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        if (Web2Const.INSPECT_ASSET.equals(inspectType)) {
            query.eq("ASSET_STATUS", StatusConst.OK);
        }
        if (Web2Const.INSPECT_TARGET.equals(inspectType)) {
            query.eq("TARGET_STATUS", StatusConst.OK);
        }
        List<InspectRecord> list = this.list(query);
        String assetIp = list.get(0).getAssetIp1();
        List<InspectResultVo> resList = new ArrayList<>();
        try {
            resList = xunjianSupportService.inspect(assetId, assetIp,
                    list.stream().map(InspectRecord::getTargetItem).collect(Collectors.toList()));
        } catch (Exception e) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, assetId, "1:" + ResultEnum.INSPECT_NO_DATA.getMessage());
        }
        if (CollectionUtils.isEmpty(resList)) {
            // 更新未巡检到的结果为  未知
            UpdateWrapper<InspectRecord> wrapper = Wrappers.update();
            wrapper.eq("ASSET_ID", assetId);
            wrapper.notIn("INSPECT_STATE", Arrays.asList(Web2Const.INSPECTED, Web2Const.INSPECT_ERROR));
            wrapper.set("INSPECT_STATE", Web2Const.UNKNOWN);
            this.update(wrapper);
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, assetId, "2:" + ResultEnum.INSPECT_NO_DATA.getMessage());
            return new ArrayList<>();
        }

        Integer collectionType = asset.getCollectionType();
        if (Objects.isNull(collectionType)) {
            collectionType = SystemTypeEnum.SWITCH_ROUTER.getCode();
        }

        // 更新巡检结果
        List<InspectRecord> records = new ArrayList<>(1024);
        for (InspectResultVo rz : resList) {
            query = Wrappers.query();
            query.eq("ASSET_ID", assetId);
            query.eq("TARGET_ITEM", rz.getTargetItem());
            if (Web2Const.INSPECT_ASSET.equals(inspectType)) {
                query.eq("ASSET_STATUS", StatusConst.OK);
            }
            if (Web2Const.INSPECT_TARGET.equals(inspectType)) {
                query.eq("TARGET_STATUS", StatusConst.OK);
            }
            List<InspectRecord> rs = this.list(query);
            if (rs.size() > 1) {
                query.like("MODE_TYPE", map.get(collectionType));
                rs = this.list(query);
            }
            if (CollectionUtils.isEmpty(rs)) {
                continue;
            }
            InspectRecord one = rs.get(0);
            if (one.getTargetItem().equals(rz.getTargetItem())) {
                String inspectValue = rz.getInspectValue();
                one.setResultMsg(rz.getResultMsg());
                if (!StringUtils.isEmpty(inspectValue)) {
                    one.setInspectValue(inspectValue.length() > 2000 ? inspectValue.substring(0, 2000) : inspectValue);
                }
                one.setThresholdValue(rz.getThresholdValue());
                one.setCommand(rz.getReferCommand());
                one.setInspectState(rz.getInspectState());
                records.add(one);
            }
        }
        this.updateBatchById(records);

        // 更新未巡检到的结果为  未知
        UpdateWrapper<InspectRecord> wrapper = Wrappers.update();
        wrapper.eq("ASSET_ID", assetId);
        if (Web2Const.INSPECT_ASSET.equals(inspectType)) {
            query.eq("ASSET_STATUS", StatusConst.OK);
        }
        if (Web2Const.INSPECT_TARGET.equals(inspectType)) {
            query.eq("TARGET_STATUS", StatusConst.OK);
        }
        wrapper.notIn("INSPECT_STATE", Arrays.asList(Web2Const.INSPECTED, Web2Const.INSPECT_ERROR));
        wrapper.set("INSPECT_STATE", Web2Const.UNKNOWN);
        this.update(wrapper);

        Date date = new Date();
        wrapper = Wrappers.update();
        wrapper.set("INSPECT_TIME", date);
        this.update(wrapper);
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, assetId, "该资产巡检结束");
        return resList;
    }

    /**
     * @description: 按资产类型获取资产列表
     * @author: HanHW
     * @date: 2023/11/15 15:06
     * @param: [modeType]
     * @return: java.util.List<com.jcca.web2.vo.InspectVo>
     **/
    @Override
    public List<InspectVo> getModeAsset(String modeType) {
        List<InspectVo> resList = new ArrayList<>();
        List<InspectRecord> orgRecords = inspectRecordMapper.findByModeTypeGroupByOrgId(modeType);
        List<InspectRecord> assetRecords = inspectRecordMapper.findByModeTypeGroupByAssetId(modeType);
        for (InspectRecord record : orgRecords) {
            String orgId = record.getOrgId();
            List<InspectVo> vos = new ArrayList<>();
            for (InspectRecord c : assetRecords) {
                if (orgId.equals(c.getOrgId())) {
                    InspectVo vo = new InspectVo();
                    vo.setId(c.getAssetId());
                    vo.setName(c.getAssetName());
                    vo.setTargetStatus(c.getTargetStatus() + "");
                    vo.setAssetStatus(c.getAssetStatus() + "");
                    vos.add(vo);
                }
            }

            InspectVo vo = new InspectVo();
            vo.setId(orgId);
            vo.setName(record.getOrgName());
            vo.setList(vos);

            resList.add(vo);
        }

        return resList;
    }

    /**
     * @description: 开始巡检
     * @author: HanHW
     * @date: 2023/11/16 17:06
     * @param: []
     * @return: void
     **/
    @Override
    public void start(String inspectType) {
        if (Web2Const.INSPECT_BEGIN.equals(inspectOperState)) {
            throw new ResultException(ResultEnum.INSPECT_BEGIN);
        }

        inspectType = inspectRecordMapper.findNowInspectType();
        if (StringUtils.isEmpty(inspectType)) {
            inspectType = Web2Const.INSPECT_ASSET;
        }

        List<InspectRecord> list;
        // 按设备巡检
        if (Web2Const.INSPECT_ASSET.equals(inspectType)) {
            list = inspectRecordMapper.findCheckedAsset();
            if (CollectionUtils.isEmpty(list)) {
                throw new ResultException(ResultEnum.INSPECT_BEGIN.getCode(), "应至少选择一台设备");
            }
            inspectRecordMapper.updateCheckedAsset();
        }

        // 按指标巡检
        if (Web2Const.INSPECT_TARGET.equals(inspectType)) {
            list = inspectRecordMapper.findCheckedTarget();
            if (CollectionUtils.isEmpty(list)) {
                throw new ResultException(ResultEnum.INSPECT_BEGIN.getCode(), "应至少选择一种指标");
            }
            inspectRecordMapper.updateCheckedTarget();
        }

        inspectOperState = Web2Const.INSPECT_BEGIN;

        String newcode = MyIdUtil.getId();
        UpdateWrapper<InspectRecord> update = Wrappers.update();
        update.set("INSPECT_CODE", newcode);
        this.update(update);

        // 生成巡检文件
        QueryWrapper<InspectRecord> query = Wrappers.query();
        query.eq("ASSET_STATUS", 1);
        List<InspectRecord> rlist = this.list(query);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(() -> {
            createInspectFile(rlist, newcode);
        });
    }

    private void createInspectFile(List<InspectRecord> rlist, String inspectCode) {
        StringBuilder errorAssetOrgStr = new StringBuilder("【智能巡检原始报告】\r\n");
        StringBuilder successAssetOrgStr = new StringBuilder("【智能巡检原始报告】\r\n");

        Map<String, List<InspectRecord>> tabMap = rlist.stream().collect(Collectors.groupingBy(InspectRecord::getAssetId));
        Set<String> keySet = tabMap.keySet();
        for (String key : keySet) {
            List<InspectRecord> records = tabMap.get(key);
            InspectRecord record = records.get(0);
            StringBuilder msg = new StringBuilder();
            msg.append("【设备：");
            msg.append(record.getAssetName());
            msg.append("(");
            msg.append(record.getAssetIp1());
            msg.append(")】");
            msg.append("\r\n");

            errorAssetOrgStr.append(msg);
            successAssetOrgStr.append(msg);
            getStringBuilder(successAssetOrgStr, errorAssetOrgStr, records);
        }

        String id1 = MyIdUtil.getId();
        String path = "/xunjian/" + DateUtil.today() + "/" + id1;
        //保存正常的日志
        FileWriter writer = new FileWriter(filePath + path + "normal.txt");
        writer.write(successAssetOrgStr.toString());

        //保存异常的日志
        FileWriter writer2 = new FileWriter(filePath + path + "error.txt");
        writer2.write(errorAssetOrgStr.toString());

        //保存全量的日志
        FileWriter writer3 = new FileWriter(filePath + path + "all.txt");
        writer3.write(errorAssetOrgStr.append(successAssetOrgStr).toString());

        UpdateWrapper<InspectRecord> update = Wrappers.update();
        update.eq("INSPECT_CODE", inspectCode);
        update.eq("ASSET_STATUS", 1);
        update.set("RESULT_PATH", staticUrl + staticPath + path);
        this.update(update);
    }

    /**
     * 拼接报告
     * StringBuilder msg
     */
    private void getStringBuilder(StringBuilder successAssetOrgStr, StringBuilder errorAssetOrgStr, List<InspectRecord> records) {
        for (InspectRecord recor : records) {
            if (Web2Const.INSPECTED.equals(recor.getInspectState())) {
                gather(successAssetOrgStr, recor, "系统提示结果：【正常✓】");
            } else if (Web2Const.INSPECT_ERROR.equals(recor.getInspectState())) {
                gather(errorAssetOrgStr, recor, "系统提示结果：【【★★异常★★】】");
            } else if (Web2Const.UNKNOWN.equals(recor.getInspectState())) {
                gather(errorAssetOrgStr, recor, "系统提示结果：【【★★需要二次确认★★】】");
            }
        }
    }

    private void gather(StringBuilder msg, InspectRecord recor, String s) {
        msg.append("巡检项（");
        msg.append(recor.getTargetName());
        msg.append("）命令：");
        msg.append("\r\n");
        msg.append(Objects.isNull(recor.getCommand()) ? "无" : recor.getCommand());
        msg.append("\r\n");
        msg.append(s);
        msg.append("\r\n");
        msg.append("输出：");
        msg.append("\r\n");
        msg.append(Objects.isNull(recor.getInspectValue()) ? "无" : recor.getInspectValue());
        msg.append("\r\n");
        msg.append("----------------------------分割线----------------------------");
        msg.append("\r\n");
        msg.append("\r\n");
        msg.append("\r\n");
    }

    /**
     * @description: 查询上一次巡检记录
     * @author: HanHW
     * @date: 2023/11/17 9:05
     * @param: []
     * @return: java.util.List<com.jcca.web2.vo.InspectVo>
     **/
    @Override
    public List<InspectVo> getNextRecords(String inspectCode) {
        List<InspectVo> resList = new ArrayList<>();
        List<InspectRecord> records = inspectRecordMapper.findGroupByModeType();
        for (InspectRecord record : records) {
            String modeType = record.getModeType();
            List<InspectVo> vos = new ArrayList<>();
            List<InspectRecord> list = inspectRecordMapper.findByModeType(modeType);
            for (InspectRecord cord : list) {
                InspectVo vo = new InspectVo();
                vo.setId(cord.getTargetItem());
                vo.setName(cord.getTargetName());
                vo.setTargetStatus(cord.getTargetStatus() + "");
                vo.setInspectCode(inspectCode);
                vos.add(vo);
            }

            String modeName = record.getModeName();
            InspectVo vo = new InspectVo();
            vo.setId(modeType);
            vo.setName(modeName);
            vo.setTotal(inspectRecordMapper.findByModeTypeGroupByAssetId(modeType).size());
            vo.setCount(inspectRecordMapper.countCheckedByModeType(modeType));
            vo.setList(vos);
            resList.add(vo);
        }
        return resList;
    }

    /**
     * @description: 增减巡检指标
     * @author: HanHW
     * @date: 2023/11/17 11:22
     * @param: [modeType, targetItem, status]
     * @return: void
     */
    @Override
    public void modifyTarget(Set<String> modeSet, Set<String> targetSet, String status) {
        UpdateWrapper<InspectRecord> wrapper = Wrappers.update();
        wrapper.in("MODE_TYPE", modeSet);
        wrapper.in("TARGET_ITEM", targetSet);
        wrapper.set("TARGET_STATUS", status);
        wrapper.set("MODIFY_TIME", new Date());
        this.update(wrapper);
    }

    /**
     * @description: 增减巡检资产
     * @author: HanHW
     * @date: 2023/11/17 11:22
     * @param: [assetId, status]
     * @return: void
     */
    @Override
    public void modifyAsset(List<Map<String, String>> list) {
        List<String> ids = new ArrayList<>();
        for (Map<String, String> map : list) {
            String assetId = map.get("assetId");
            ids.add(assetId);
        }

        Map<String, String> map = list.get(0);
        String status = map.get("status");
        String type = map.get("modeType");
        UpdateWrapper<InspectRecord> wrapper = Wrappers.update();
        wrapper.in("ASSET_ID", ids);
        if (!StringUtils.isEmpty(type)) {
            wrapper.eq("MODE_TYPE", type);
        }
        wrapper.set("ASSET_STATUS", status);
        wrapper.set("MODIFY_TIME", new Date());
        this.update(wrapper);
    }

    /**
     * @description: 按机柜ID查询机柜内设备信息
     * @author: HanHW
     * @date: 2023/11/17 12:03
     * @param: [cabinetId]
     * @return: java.util.List<com.jcca.web.asset.vo.DetailCabinetVo>
     */
    @Override
    public List<DetailCabinetVo> findByCabinetId(String cabinetId) {
        String inspectType = inspectRecordMapper.findNowInspectType();
        if (StringUtils.isEmpty(inspectType)) {
            inspectType = Web2Const.INSPECT_ASSET;
        }
        List<DetailCabinetVo> resList = new ArrayList<>();
        List<InspectRecord> records = inspectRecordMapper.findCabinetInfo(cabinetId, inspectType);
        Collection<String> values = virMap.values();
        if (values.contains(cabinetId)) {
            for (InspectRecord record : records) {
                String assetId = record.getAssetId();
                List<InspectRecord> list = inspectRecordMapper.findAssetState(assetId, inspectType);
                if (list.isEmpty()) {
                    continue;
                }
                String state = list.get(0).getInspectState();
                DetailCabinetVo vo = new DetailCabinetVo();
                vo.setCabinetId(cabinetId);
                vo.setCabinetName(record.getCabinetName());
                vo.setAssetId(assetId);
                vo.setState(Objects.isNull(state) ? Web2Const.INSPECT : state);
                vo.setAssetName(record.getAssetName());
                vo.setIp(record.getAssetIp1());
                vo.setIp2(record.getAssetIp2());
                resList.add(vo);
            }
            return resList;
        }
        List<String> assetIds = records.stream().map(InspectRecord::getAssetId).collect(Collectors.toList());
        List<DetailCabinetVo> cabinetVosList = cabinetService.findDetailById(cabinetId);
        for (DetailCabinetVo cabinetVo : cabinetVosList) {
            String assetId = cabinetVo.getAssetId();
            if (assetIds.contains(assetId)) {
                List<InspectRecord> assetStateList = inspectRecordMapper.findAssetState(assetId, inspectType);
                if (assetStateList.isEmpty()) {
                    continue;
                }
                String state = assetStateList.get(0).getInspectState();
                cabinetVo.setState(Objects.isNull(state) ? Web2Const.INSPECT : state);
                cabinetVo.setCabinetId(cabinetId);
                resList.add(cabinetVo);
            }
        }
        return resList;
    }

    /**
     * @description: 获取正在巡检采集记录 INSPECT_CODE
     * @author: HanHW
     * @date: 2023/11/17 13:30
     * @param: []
     * @return: java.lang.String
     **/
    @Override
    public String getLastInspectCode() {
        return inspectRecordMapper.findLastInspectCode();
    }

    /**
     * @description: 查询当前巡检中异常指标
     * @author: HanHW
     * @date: 2023/11/17 13:49
     * @param: [assetId]
     * @return: java.util.List<com.jcca.web2.entity.InspectRecord>
     */
    @Override
    public List<InspectRecord> findAssetAbnormalTarget(String assetId) {
        String inspectType = inspectRecordMapper.findNowInspectType();
        QueryWrapper<InspectRecord> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        if (Web2Const.INSPECT_ASSET.equals(inspectType)) {
            query.eq("ASSET_STATUS", StatusConst.OK);
        }
        if (Web2Const.INSPECT_TARGET.equals(inspectType)) {
            query.eq("TARGET_STATUS", StatusConst.OK);
        }
        query.in("INSPECT_STATE", Arrays.asList(Web2Const.INSPECT_ERROR, Web2Const.UNKNOWN));
        return this.list(query);
    }

    /**
     * @description: 暂停巡检
     * @author: HanHW
     * @date: 2023/11/17 14:03
     * @param: []
     * @return: void
     **/
    @Override
    public void pause() {
        inspectOperState = Web2Const.INSPECT_PAUSE;
    }

    /**
     * @description: 结束巡检
     * @author: HanHW
     * @param: []
     * @return: void
     **/
    @Override
    public void end(String msg) {
        if (Web2Const.INSPECT_NO.equals(inspectOperState) || Web2Const.INSPECT_END.equals(inspectOperState)) {
            throw new ResultException(ResultEnum.INSPECT_NO);
        }
        inspectOperState = Web2Const.INSPECT_END;

        // 将正在巡检的状态修改为结束状态
        UpdateWrapper<InspectRecord> wrapper = Wrappers.update();
        wrapper.notIn("INSPECT_STATE", Arrays.asList(Web2Const.INSPECTED, Web2Const.INSPECT_ERROR));
        wrapper.set("INSPECT_STATE", Web2Const.UNKNOWN);
        wrapper.set("RESULT_MSG", msg);
        this.update(wrapper);


        String inspectType = inspectRecordMapper.findNowInspectType();
        if (StringUtils.isEmpty(inspectType)) {
            inspectType = Web2Const.INSPECT_ASSET;
        }

        // 将结果存入 INSPECT_DETAIL
        List<InspectDetail> dlist = new ArrayList<>(2048);
        QueryWrapper<InspectRecord> query = Wrappers.query();
        query.eq("INSPECT_TYPE", inspectType);
        if (Web2Const.INSPECT_ASSET.equals(inspectType)) {
            query.eq("ASSET_STATUS", StatusConst.OK);
        }
        if (Web2Const.INSPECT_TARGET.equals(inspectType)) {
            query.eq("TARGET_STATUS", StatusConst.OK);
        }
        List<InspectRecord> list = this.list(query);
        Date date = new Date();
        for (InspectRecord record : list) {
            InspectDetail detail = new InspectDetail();
            BeanUtils.copyProperties(record, detail);
            detail.setId(MyIdUtil.getId());
            detail.setInspectTime(date);
            dlist.add(detail);
        }
        inspectDetailService.saveBatch(dlist);
    }

    /**
     * 获取巡检状态
     */
    @Override
    public String getState() {
        return inspectOperState;
    }

    @Override
    public void deleteInspectByAssetId(String assetId) {
        QueryWrapper<InspectRecord> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        this.remove(query);
    }

    /**
     * 查询指标巡检状态
     *
     * @param assetId
     * @param targetItem
     * @return
     */
    @Override
    public InspectRecord findTargetState(String assetId, String targetItem, String modeType, String inspectCode) {
        if (StringUtils.isEmpty(inspectCode)) {
            throw new ResultException(ResultEnum.PARAM_ERROR);
        }
        InspectRecord targetState = inspectRecordMapper.findTargetState(assetId, targetItem, modeType);
        if (Web2Const.INSPECT.equals(targetState.getInspectState())) {
            this.getTargetState(assetId);
            targetState = inspectRecordMapper.findTargetState(assetId, targetItem, modeType);
        }

        return targetState;
    }

    /**
     * 查询资产状态
     *
     * @param assetId
     * @return
     */
    @Override
    public InspectRecord findAssetState(String assetId) {
        String inspectType = inspectRecordMapper.findNowInspectType();
        if (StringUtils.isEmpty(inspectType)) {
            inspectType = Web2Const.INSPECT_ASSET;
        }
        List<InspectRecord> list = inspectRecordMapper.findAssetState(assetId, inspectType);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 查询机柜状态
     *
     * @param cabinetId
     * @return
     */
    @Override
    public InspectRecord findCabinetState(String cabinetId) {
        String inspectType = inspectRecordMapper.findNowInspectType();
        if (StringUtils.isEmpty(inspectType)) {
            inspectType = Web2Const.INSPECT_ASSET;
        }
        return inspectRecordMapper.findCabinetState(cabinetId, inspectType);
    }

    /**
     * 查询待巡检资产ID
     *
     * @param
     * @return
     */
    @Override
    public List<String> findAssetIdList() {
        return inspectRecordMapper.findAssetIdList();
    }

    @Override
    public InspectRecord findOneByAssetAndTarget(String assetId, String modeType, String targetItem) {
        List<InspectRecord> list = inspectRecordMapper.findOneByAssetAndTarget(assetId, modeType, targetItem);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(list)) {
            return null;
        }
        if (list.size() > 1) {
            Map<String, Object> map = new HashMap<>();
            map.put("ASSET_ID", assetId);
            map.put("MODE_TYPE", modeType);
            map.put("TARGET_ITEM", targetItem);
            inspectRecordMapper.deleteByMap(map);
            return null;
        }
        return list.get(0);
    }

    /**
     * 准备巡检数据
     */
    @Override
    public void prepareRecord() {
        if (virMap.isEmpty()) {
            QueryWrapper<SysOrg> query = Wrappers.query();
            query.in("TYPE", Arrays.asList(OrgTypeConst.CENTER, OrgTypeConst.STATION));
            query.eq("STATUS", StatusEnum.OK.getCode());
            List<SysOrg> orgList = orgService.list(query);

            int i = 0;
            for (SysOrg org : orgList) {
                virMap.put(org.getId(), String.valueOf(i++));
            }
        }

        this.checkRecord();

        List<String> assetIdList = this.findAssetIdList();
        Executor executorService = (Executor) SpringContextUtil.getBean("xunjianAsync");
        for (String id : assetIdList) {
            executorService.execute(() -> {
                this.getTargetState(id);
            });
        }
    }

    private volatile boolean flag = false;

    @Override
    public synchronized void checkRecord() {
        if (flag) {
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "巡检", "正在初始化数据");
            return;
        }
        flag = true;
        String inspectCode = "";
        int count = this.count();
        if (count == 0) {
            List<InspectRecord> records = this.checkReadyRecords();
            this.saveBatch(records, 1000);

            inspectCode = records.get(0).getInspectCode();
        }

        if (StringUtils.isEmpty(inspectCode)) {
            List<InspectRecord> nlist = new ArrayList<>(1024);
            List<InspectRecord> readyRecords = this.checkReadyRecords();
            InspectRecord one;
            for (InspectRecord record : readyRecords) {
                one = this.findOneByAssetAndTarget(record.getAssetId(), record.getModeType(), record.getTargetItem());
                if (Objects.isNull(one)) {
                    record.setInspectState(Web2Const.UNKNOWN);
                    nlist.add(record); // 添加新的
                    continue;
                }
                String id = one.getId();
                Integer assetStatus = one.getAssetStatus();
                Integer targetStatus = one.getTargetStatus();
                one.setInspectCode(record.getInspectCode());
                BeanUtils.copyProperties(record, one);
                one.setId(id);
                one.setAssetStatus(assetStatus);
                one.setTargetStatus(targetStatus);
                nlist.add(one); // 更新旧的
            }

            if (!CollectionUtils.isEmpty(nlist)) {
                this.saveOrUpdateBatch(nlist, 2000);
            }
        }
        flag = false;
        AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_MANAGE, "巡检", "初始化数据结束");
    }

    private List<InspectRecord> checkReadyRecords() {
        List<InspectRecord> records = new ArrayList<>();
        List<InspectVo> voList = xunjianSupportService.inspectItem();
        Set<String> deskSet = new HashSet<>();
        for (InspectVo vo : voList) {
            String desks = vo.getId();
            if (!desks.contains("_")) {
                continue;
            }
            deskSet.addAll(Arrays.asList(desks.split("_")));
        }

        String inspectCode = MyIdUtil.getId();
        QueryWrapper<Asset> query = Wrappers.query();
        query.eq("IS_DEL", StatusConst.OK);
        query.eq("WATCH", StatusConst.OK);
        query.in("DESK", deskSet);
        List<Asset> assets = assetService.list(query);
        for (InspectVo vo : voList) {
            String modeType = vo.getId() + "_" + vo.getCode();
            String modeName = vo.getName();
            String code = vo.getCode();
            for (Asset asset : assets) {
                Integer collectionType = asset.getCollectionType();
                if (Objects.isNull(collectionType)) {
                    collectionType = SystemTypeEnum.SWITCH_ROUTER.getCode();
                }
                if (code.contains("WIN") && collectionType != SystemTypeEnum.WINDOWS.getCode().intValue()) {
                    continue;
                }
                if (code.contains("AIX") && collectionType != SystemTypeEnum.AIX.getCode().intValue()) {
                    continue;
                }
                if (code.contains("LINUX") && collectionType != SystemTypeEnum.LINUX.getCode().intValue()) {
                    continue;
                }

                String desk = "" + asset.getDesk();
                if (!modeType.contains(desk)) continue;
                List<InspectVo> list = vo.getList();
                for (InspectVo inspectVo : list) {
                    InspectRecord record = new InspectRecord();
                    this.setRecordData(asset, inspectVo, record);
                    if (StringUtils.isEmpty(record.getCabinetId())) {
                        continue;
                    }
                    record.setModeType(modeType);
                    record.setModeName(modeName);
                    record.setInspectCode(inspectCode);
                    record.setAssetDesk(asset.getDesk());
                    record.setDeskName(assetModeService.getByCode(asset.getDesk()).getName());
                    records.add(record);
                }
            }
        }

        return records;
    }

    private void setRecordData(Asset asset, InspectVo vo, InspectRecord record) {
        SysOrg org = orgService.getById(asset.getOrgId());
        String assetId = asset.getId();
        record.setId(MyIdUtil.getId());
        record.setAssetId(assetId);
        record.setAssetName(asset.getName());
        record.setAssetIp1(asset.getIp());
        record.setAssetIp2(asset.getIp2());
        record.setOrgId(asset.getOrgId());
        record.setTargetItem(vo.getId());
        record.setTargetName(vo.getName());
        record.setOrgName(org.getTitle());
        record.setInspectType(1);
        record.setInspectState(Web2Const.INSPECT);
        record.setTargetStatus((int) StatusConst.OK);
        record.setAssetStatus((int) StatusConst.OK);
        record.setRemark("");
        Cabinet cabinet = cabinetService.findByAssetId(assetId);
        if (Objects.nonNull(cabinet)) {
            record.setCabinetId(cabinet.getId());
            record.setCabinetName(cabinet.getName());
            return;
        }

        String cabinetId = virMap.get(org.getId());
        record.setCabinetName(Web2Const.VIR_CABINET_NAME);
        record.setCabinetId(cabinetId);
    }

    /**
     * 查询正常指标项
     *
     * @param assetId
     * @return
     */
    @Override
    public List<InspectRecord> findAssetNormalTarget(String assetId) {
        String inspectType = inspectRecordMapper.findNowInspectType();
        QueryWrapper<InspectRecord> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        query.eq("INSPECT_STATE", Web2Const.INSPECTED);
        if (Web2Const.INSPECT_ASSET.equals(inspectType)) {
            query.eq("ASSET_STATUS", StatusConst.OK);
        }
        if (Web2Const.INSPECT_TARGET.equals(inspectType)) {
            query.eq("TARGET_STATUS", StatusConst.OK);
        }
        return this.list(query);
    }

    /**
     * 按组织 资产类型 统计资产
     *
     * @return InspectOrgAssetVo
     */
    @Override
    public List<InspectOrgAssetVo> getOrgAsset() {
        List<InspectOrgAssetVo> orgList = inspectRecordMapper.findOrgList();
        List<InspectOrgAssetVo> resList = new ArrayList<>();
        Set<String> lineIdSet = new HashSet<>();

        Map<String, List<InspectOrgAssetVo>> stationmap = new HashMap<>(); // <线ID,stations>
        Iterator<InspectOrgAssetVo> iterator = orgList.iterator();
        while (iterator.hasNext()) {
            InspectOrgAssetVo orgVo = iterator.next();
            String orgId = orgVo.getOrgId();
            SysOrg org = orgService.getById(orgId);
            if (OrgTypeConst.CENTER == org.getType()) {
                orgVo.setId(MyIdUtil.getId());
                this.setAssetList(orgVo);
                resList.add(orgVo);

                iterator.remove();
                continue;
            }
            if (OrgTypeConst.STATION == org.getType()) {
                List<InspectOrgAssetVo> stations = stationmap.get(org.getPid());
                if (CollectionUtils.isEmpty(stations)) {
                    stations = new ArrayList<>();
                }
                InspectOrgAssetVo station = new InspectOrgAssetVo();
                station.setOrgId(orgId);
                station.setOrgName(org.getTitle());
                station.setId(MyIdUtil.getId());
                stations.add(station);

                stationmap.put(org.getPid(), stations);

                if (!lineIdSet.contains(org.getPid())) {
                    SysOrg porg = orgService.getById(org.getPid());
                    InspectOrgAssetVo line = new InspectOrgAssetVo();
                    line.setOrgId(porg.getId());
                    line.setOrgName(porg.getTitle());
                    line.setOrgList(stations);
                    line.setId(MyIdUtil.getId());
                    resList.add(line);

                    lineIdSet.add(org.getPid());
                }
            }
        }

        for (InspectOrgAssetVo orgVo : resList) {
            String orgId = orgVo.getOrgId();
            SysOrg org = orgService.getById(orgId);
            if (OrgTypeConst.CENTER == org.getType()) {
                continue;
            }
            Set<SysOrg> stations = orgService.getChildrenById(orgId);
            if (CollectionUtils.isEmpty(stations)) {
                continue;
            }
            Set<String> stationids = stations.stream().map(SysOrg::getId).collect(Collectors.toSet());
            List<InspectOrgAssetVo> stationvos = inspectRecordMapper.findLineStations(stationids);
            for (InspectOrgAssetVo station : stationvos) {
                station.setId(MyIdUtil.getId());
                this.setAssetList(station);
            }
            orgVo.setOrgList(stationvos);
        }

        return resList;
    }

    /**
     * 更新巡检类型
     *
     * @param inspectType 1资产巡检，2指标巡检
     */
    @Override
    public void updateInspect(String inspectType) {
        UpdateWrapper<InspectRecord> update = Wrappers.update();
        update.set("INSPECT_TYPE", inspectType);
        this.update(update);
    }

    private void setAssetList(InspectOrgAssetVo orgVo) {
        String orgId = orgVo.getOrgId();
        List<InspectOrgAssetVo> deskList = inspectRecordMapper.findDeskListByOrgId(orgId);
        List<InspectOrgAssetVo> desks = new ArrayList<>();
        for (InspectOrgAssetVo deskVo : deskList) {
            String deskOrgId = deskVo.getOrgId();
            Integer assetDesk = deskVo.getAssetDesk();
            if (orgId.equals(deskOrgId)) {
                List<InspectOrgAssetVo> assetList = inspectRecordMapper.findAssetListByOrgDesk(orgId, assetDesk);
                deskVo.setAssetList(assetList);
                deskVo.setId(MyIdUtil.getId());
                desks.add(deskVo);
            }
        }
        orgVo.setDeskList(desks);
    }

}
