package com.jcca.web2.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jcca.admin.system.entity.SysDict;
import com.jcca.admin.system.service.SysDictService;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.service.impl.OutServiceImpl;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.xunjian.adapter.v2.XunjianV2Handler;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.service.XunjianSupportService;
import com.jcca.web2.vo.InspectResultVo;
import com.jcca.web2.vo.InspectVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zhaozheng
 * @description TODO
 * @className XunjianSupportServiceImpl
 * @date 2024/1/11 17:14
 * @since 2.1.0.0
 */
@Service
public class XunjianSupportServiceImpl implements XunjianSupportService {
    @Resource
    private SysDictService sysDictServ;
    @Resource
    private RedisService redisService;

    @Resource
    private AssetService assetServ;

    @Resource
    private XunjianV2Handler xunjianV2Handler;

    @Resource
    private OutService outService;
    //网络设备类型
    private static final List<Integer> NET_ASSET_MODE = Arrays.asList(201, 42);

    @Override
    public List<InspectVo> inspectItem() {
        Map<String, InspectVo> map = new HashMap();
        StatusInfoChangeTypeEnum[] values = StatusInfoChangeTypeEnum.values();
        //循环分类
        for (StatusInfoChangeTypeEnum value : values) {
            String code = value.getCode();
            if (code.startsWith("event:") && value.getAssetMode() != null && !value.getAssetMode().equals("")) {
                InspectVo inspectVo = new InspectVo();
                inspectVo.setId(value.getAssetMode());
                inspectVo.setCode(code);
                inspectVo.setName(value.getXuanjianName());
                inspectVo.setList(new ArrayList<InspectVo>());
                map.put(code, inspectVo);
            }
        }
        //循环指标项
        for (StatusInfoChangeTypeEnum value : values) {
            String code = value.getCode();
            if (code.startsWith("event:") && !value.getXuanjianName().equals("") && value.getAssetMode().equals("")) {
                String[] str = code.split(":");
                InspectVo inspectVo = map.get(str[0] + ":" + str[1]);
                if (inspectVo != null) {
                    InspectVo inspectVoSon = new InspectVo();
                    inspectVoSon.setId(code);
                    inspectVoSon.setName(value.getXuanjianName());
                    inspectVo.getList().add(inspectVoSon);
                }
            }

        }
        List<InspectVo> list = new ArrayList<>(map.values());
        List<SysDict> allLikeName = sysDictServ.getByNames("XUNJIAN_");
        for (int i = 0; i < allLikeName.size(); i++) {
            SysDict sysDict = allLikeName.get(i);
            String[] split = sysDict.getTitle().split("-");
            if (split.length > 1) {
                InspectVo inspectVo = new InspectVo();
                inspectVo.setName(split[0]);
                inspectVo.setCode(sysDict.getName());
                inspectVo.setId(split[1].replace("巡检", ""));
                inspectVo.setList(new ArrayList<InspectVo>());
                String str = sysDict.getValue();
                String[] vals = str.split(",");
                for (int j = 0; j < vals.length; j++) {
                    InspectVo inspectSon = new InspectVo();
                    inspectSon.setId(vals[j].split(":")[0]);
                    inspectSon.setName(vals[j].split(":")[1]);
                    inspectVo.getList().add(inspectSon);
                }
                list.add(inspectVo);
            }
        }
        return list;
    }

    @Override
    public List<InspectResultVo> inspect(String assetId, String assetIp, List<String> targetList) {
        ArrayList<InspectResultVo> resultList = new ArrayList<>();
        Date date = new Date();
        //巡检的状态信息
        Map<String, Object> hashMap = redisService.getHashMap(String.format("%s:%s:statusEvent", assetIp, assetId));
        //巡检值，详细信息
        Map<String, Object> hashMapValue = redisService.getHashMap(String.format("%s:%s:statusEventValue", assetIp, assetId));
        //过滤出选中的巡检状态信息
        Map<String, Object> filteredMap = hashMap.entrySet()
                .stream()
                .filter(entry -> targetList.stream().anyMatch(entry.getKey()::contains))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        //过滤出选中的巡检值信息
        Map<String, Object> filteredMapValue = hashMapValue.entrySet()
                .stream()
                .filter(entry -> targetList.stream().anyMatch(entry.getKey()::contains))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, InspectResultVo> mapResult = new HashMap<>();
        Set<String> keySet = filteredMap.keySet();
        for (String key : keySet) {
            String[] split = key.split("\\.");
            int status = (Integer) filteredMap.get(key);
            InspectResultVo inspectResultVo = new InspectResultVo();
            inspectResultVo.setAssetId(assetId);
            inspectResultVo.setCreateTime(date);
            inspectResultVo.setTargetItem(split[0]);
            String str = split.length == 1 ? "" : split[1];
            inspectResultVo.setTargetName(StatusInfoChangeTypeEnum.getXunjianName(split[0]) + ":" + str);
            inspectResultVo.setInspectState(status == EventLevelEnum.NORMAL.getCode() ? Web2Const.INSPECTED : Web2Const.INSPECT_ERROR);
            inspectResultVo.setResultMsg(String.format("巡检结果：%s", status == EventLevelEnum.NORMAL.getCode() ? "正常" : "异常"));
            mapResult.put(key, inspectResultVo);
        }
        Set<String> keySetValue = filteredMapValue.keySet();
        for (String key : keySetValue) {
            String[] split = key.split("\\.");
            String keyValue = split[split.length - 1];
            InspectResultVo inspectResultVo = mapResult.get(key.replace("." + keyValue, ""));
            if (inspectResultVo != null) {
                if (
                        keyValue.equals(StatusInfoChangeTypeEnum.NORMAL.getCode()) ||
                                keyValue.equals(StatusInfoChangeTypeEnum.SECTION_ONE.getCode()) ||
                                keyValue.equals(StatusInfoChangeTypeEnum.SECTION.getCode()) ||
                                keyValue.equals(StatusInfoChangeTypeEnum.SECTION_TWO.getCode()) ||
                                keyValue.equals(StatusInfoChangeTypeEnum.SECTION_THREE.getCode())
                ) {
                    inspectResultVo.setThresholdValue(filteredMapValue.get(key).toString());
                } else if (
                        keyValue.equals(StatusInfoChangeTypeEnum.NORMAL_VAL.getCode()) ||
                                keyValue.equals(StatusInfoChangeTypeEnum.SECTION_ONE_VAL.getCode()) ||
                                keyValue.equals(StatusInfoChangeTypeEnum.SECTION_VAL.getCode()) ||
                                keyValue.equals(StatusInfoChangeTypeEnum.SECTION_TWO_VAL.getCode()) ||
                                keyValue.equals(StatusInfoChangeTypeEnum.SECTION_THREE_VAL.getCode())

                ) {
                    inspectResultVo.setInspectValue(filteredMapValue.get(key).toString());
                    if (key.contains(StatusInfoChangeTypeEnum.event_run_time_state.getCode())) {
                        BigDecimal collectDay = new BigDecimal(filteredMapValue.get(key).toString()).divide(new BigDecimal(86400), 0, BigDecimal.ROUND_DOWN);
                        inspectResultVo.setInspectValue(collectDay.toString());
                    }
                }
            }
        }
        List list = new ArrayList<>(mapResult.values());
        try {
            List remotes = this.remoteInspect(assetId, targetList, date);
            list.addAll(remotes);
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "设备：" + assetId + ",远程采集异常", e);
        }
        return list;
    }


    @Override
    public List<InspectResultVo> remoteInspect(String assetId, List<String> targetList, Date creatDate) {
        List<InspectResultVo> list = new ArrayList<>();
        Asset asset = assetServ.getById(assetId);
        Integer assetMode = asset.getAssetMode();
        //过滤掉系统采集的指标
        List<String> filterList = targetList.stream().filter(item -> !item.startsWith("event:")).collect(Collectors.toList());

        //找到需要执行的指标，完成命令收集
        List<String> exeCommand = new ArrayList<>();
        for (String code : filterList) {
            String xunjianKey = code + ":" + assetMode;
            if (!AssetModeConst.B24.equals(asset.getAssetImage())) {
                xunjianKey = code;
            }
            String command = xunjianV2Handler.getCommand(xunjianKey);
            if (command == null || "".equals(command)) {
                continue;
            }
            exeCommand.add(command);
        }

        //是否为网络设备
        if (NET_ASSET_MODE.contains(assetMode)) {
            List<String> telnetResult = outService.getTelnetResult(asset, exeCommand);
            for (int i = 0; i < telnetResult.size(); i++) {
                InspectResultVo inspectResultVo = new InspectResultVo();

                String xunjianKey = filterList.get(i) + ":" + assetMode;
                if (!AssetModeConst.B24.equals(asset.getAssetImage())) {
                    xunjianKey = filterList.get(i);
                }

                //分析结果
                String orgMsg = OutServiceImpl.ERROR_FLAG;
                if (!telnetResult.isEmpty()) {
                    orgMsg = telnetResult.get(i);
                }
                XunjianDetailV2 result = null;
                String targetName = xunjianV2Handler.getTargetName(xunjianKey, asset);
                String command = xunjianV2Handler.getCommand(xunjianKey);
                //如果command为空或者等于"",说明没有此巡检指标，跳过
                if (command == null || "".equals(command)) {
                    continue;
                }


                inspectResultVo.setAssetId(assetId);
                inspectResultVo.setCreateTime(creatDate);
                inspectResultVo.setTargetItem(filterList.get(i));
                inspectResultVo.setTargetName(targetName);
                inspectResultVo.setReferCommand(command);
                inspectResultVo.setThresholdValue(xunjianV2Handler.getMaxValue(xunjianKey));
                try {
                    result = xunjianV2Handler.xunjian(xunjianKey, asset, assetId, orgMsg);
                    inspectResultVo.setInspectState(1 == result.getNormalFlag() ? Web2Const.INSPECT_ERROR : Web2Const.INSPECTED);
                    inspectResultVo.setResultMsg(result.getNormalFlagStr());
                    inspectResultVo.setInspectValue(result.getInputOrgStr());
                    if (OutServiceImpl.ERROR_FLAG.equals(result.getInputOrgStr())) {
                        inspectResultVo.setResultMsg(result.getNormalFlagStr());
                        if (StrUtil.isNotEmpty(result.getInputErrorStr())) {
                            inspectResultVo.setResultMsg(result.getInputErrorStr());
                        }
                        inspectResultVo.setInspectState(Web2Const.INSPECT_ERROR);
                        inspectResultVo.setInspectValue("网络不通或用户名密码错误");
                    }
                } catch (Exception e) {
                    inspectResultVo.setInspectState(Web2Const.INSPECT_ERROR);
                    inspectResultVo.setResultMsg("异常");
                }

                list.add(inspectResultVo);
            }

        }

        //其他种类设备
        for (int i = 0; i < filterList.size(); i++) {
            String target = filterList.get(i);
            String xunjianKey = target + ":" + assetMode + ":" + asset.getCollectionType();
            XunjianDetailV2 result = null;
            String targetName = xunjianV2Handler.getTargetName(xunjianKey, asset);
            String command = xunjianV2Handler.getCommand(xunjianKey);
            //如果command为空或者等于"",说明没有此巡检指标，跳过
            if (command == null || "".equals(command)) {
                continue;
            }
            InspectResultVo inspectResultVo = new InspectResultVo();
            inspectResultVo.setAssetId(assetId);
            inspectResultVo.setCreateTime(creatDate);
            inspectResultVo.setTargetItem(filterList.get(i));
            inspectResultVo.setTargetName(targetName);
            inspectResultVo.setReferCommand(command);
            try {
                result = xunjianV2Handler.xunjian(xunjianKey, asset, assetId, "");

                inspectResultVo.setInspectState(1 == result.getNormalFlag() ? Web2Const.INSPECT_ERROR : Web2Const.INSPECTED);
                inspectResultVo.setResultMsg(result.getNormalFlagStr());
                inspectResultVo.setInspectValue(result.getInputOrgStr());
                if (OutServiceImpl.ERROR_FLAG.equals(result.getInputOrgStr())) {
                    inspectResultVo.setInspectState(Web2Const.INSPECT_ERROR);
                    inspectResultVo.setResultMsg(result.getNormalFlagStr());
                    inspectResultVo.setInspectValue("");
                }
            } catch (Exception e) {
                inspectResultVo.setInspectState(Web2Const.INSPECT_ERROR);
                inspectResultVo.setResultMsg("异常");
            }
            list.add(inspectResultVo);
        }


        return list;
    }


}
