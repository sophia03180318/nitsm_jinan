package com.jcca.web.asset.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Strings;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.entity.AssetImportRecord;
import com.jcca.web.asset.entity.AssetImportTask;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetImportService;
import com.jcca.web.asset.service.AssetImportTaskService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.bean.AddAssetException;
import com.jcca.web.asset.utils.AssetImportUtils;
import com.jcca.web.asset.utils.NullFieldException;
import com.jcca.web.asset.utils.TerminationImportException;
import com.jcca.web.asset.utils.enums.AssetStatusEnum;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import com.jcca.web.asset.vo.AssetInfoVo;
import com.jcca.web.common.vo.AssetCollectTestVo;
import com.jcca.web.common.vo.AssetTestResult;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web2.entity.AssetMode;
import com.jcca.web2.entity.AssetModel;
import com.jcca.web2.service.AssetModeService;
import com.jcca.web2.service.AssetModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ Author：sophia
 * @ Date：Created in 17:53 2021/7/30
 * @ Description:
 */
@Service
@Slf4j
public class ImportAsset {

    private static final String ERR_CODE = AssetCollectTestVo.ERRO_CODE;
    private static final String SUCCESS_CODE = AssetCollectTestVo.SUCCES_CODE;

    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private AssetImportService assetImportService;
    @Resource
    private AssetImportTaskService assetImportTaskService;
    @Resource
    private RedisService redisService;
    @Resource
    private AssetService assetService;
    @Resource
    private IpInfoService ipInfoService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private AssetModelService assetModelService;
    @Resource
    private AssetModeService assetModeService;


    public void forImportAsset(List<Map<String, Object>> rowList, List<AssetImportRecord> assetImportList, AssetImportTask assetImportTask) {
        String taskId = assetImportTask.getId();
        AssetImportRecord assetImportRecord;
        ResultVo<?> resultVo2;
        int fail = 0;
        int success = 0;
        try {
            if (Strings.isNullOrEmpty(taskId)) {
                if (LogInputUtils.inputError(ServerTypeEnum.WEB_ASSET_IMPORT)) {
                    LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_ASSET_IMPORT, ErrorCodeEnum.WEB_ASSET_STOP_IMPORT_02, "", "");
                }
                throw new TerminationImportException("资产导入因异步执行时传入任务ID为空,所以强行终止任务!");
            }

            for (int i = 0; i < rowList.size(); i++) {

                Map<String, Object> rowMap = rowList.get(i);
                Map<String, Object> rowMapNotNull = AssetImportUtils.removeMapNullValue(rowMap);
                AssetInfoVo assetInfoVo = this.resovelAssetTemplate(rowMapNotNull);
                Asset asset = assetInfoVo.getAsset();
                asset.setTemplate(true);

                if (assetInfoVo.isJudge()) {
                    //监控设备
                    asset.setBeforeVerify("no");
                    if (asset.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode()) {
                        asset.setStatus(AssetStatusEnum.ASSET_STATUS_ONLINE.getCode());
                        resultVo2 = this.asset(asset);

                    } else {
                        //非监控设备
                        asset.setStatus(AssetStatusEnum.ASSET_STATUS_NO_WATCH.getCode());
                        resultVo2 = this.asset(asset);
                    }
                    AssetCollectTestVo assetCollectTestVo = (AssetCollectTestVo) resultVo2.getData();

                    if (assetCollectTestVo.getCode() == AssetCollectTestVo.ERRO_CODE) {
                        fail++;

                        assetImportRecord = assetImportList.get(i);

                        List<AssetTestResult> testResultList = assetCollectTestVo.getTestResultList();
                        String msg = assetCollectTestVo.getMsg();
                        if (ObjectUtil.isNotNull(testResultList) && testResultList.size() != 0) {

                            for (AssetTestResult assetTestResult : testResultList) {
                                msg += assetTestResult.getErrorMsg();
                            }
                        }
                        assetImportRecord.setErrorLog(msg);
                        assetImportRecord.setStatus("导入失败");

                        assetImportService.saveErrorAsset(assetImportRecord);

                    } else {
                        success++;
                    }

                } else {
                    fail++;
                    resultVo2 = ResultVoUtil.error(assetInfoVo.getInfo());

                    assetImportRecord = assetImportList.get(i);
                    assetImportRecord.setErrorLog(resultVo2.getMsg());
                    assetImportRecord.setStatus("导入失败");
                    assetImportService.saveErrorAsset(assetImportRecord);

                }

                //刷新任务信息
                assetImportTask.setSuccess(success);
                assetImportTask.setFail(fail);
                //每次第二次执行前先判定是否已经停止导入
                int status = assetImportTaskService.getStatusById(taskId);
                assetImportTask.setStatus(status);
                if (status != 1) {
                    for (int j = i + 1; j < assetImportList.size(); j++) {
                        assetImportRecord = assetImportList.get(j);
                        assetImportRecord.setStatus("未执行");
                        assetImportRecord.setErrorLog("未执行导入");
                        assetImportService.saveErrorAsset(assetImportRecord);
                    }
                    throw new TerminationImportException();
                }
                assetImportTaskService.updateById(assetImportTask);
            }
            assetImportTask.setLog("运行完成");
        } catch (TerminationImportException e) {
            assetImportTask.setLog("任务执行终止:" + e.toString());
            log.error("任务执行终止:" + e.toString());
        } catch (Exception e) {
            assetImportTask.setLog("任务执行失败:" + e.toString());
            log.error("任务执行终止:" + e.toString());
        } finally {
            //更新Task信息
            redisService.remove("IMPORT_TASK_STATUS");
            log.info("REDIS IMPORT_TASK_STATUS 解锁");
            assetImportTask.setStatus(0);
            assetImportTask.setEndTime(DateUtil.date());
            assetImportTaskService.updateById(assetImportTask);
        }

    }

    /*将row map对象转换为asset对象*/
    private AssetInfoVo resovelAssetTemplate(Map<String, Object> rowMap) {
        String Info = "";
        Asset asset = new Asset();
        AssetInfoVo assetInfoVo = new AssetInfoVo();
        assetInfoVo.setJudge(true);
        try {
            //有则提取赋值,无则报错 name:资产名称,assetImage:资产型号,orgId:组织机构,roomId:机房
            Map<String, String> assetNotempty = new HashMap<>();
            assetNotempty.put("name", "资产名称");
            assetNotempty.put("assetImage", "资产型号");
            assetNotempty.put("assetCode", "资产编号");
            assetNotempty.put("orgId", "组织机构");
            assetNotempty.put("roomId", "机房");
            for (Map.Entry<String, String> nameValue : assetNotempty.entrySet()) {
                if (rowMap.containsKey(nameValue.getKey())) {
                    AssetImportUtils.setFieldValues(asset, nameValue.getKey(), rowMap.get(nameValue.getKey()).toString().trim());
                } else {
                    throw new NullFieldException(nameValue.getValue() + "字段错误,请重新填写");
                }
            }

            //资产类型 有则获取转化赋值,,无则报错
            if (rowMap.containsKey("assetMode") && !DictUtil.getKey("ASSET_MODE", rowMap.get("assetMode").toString().trim()).equals("")) {
                asset.setAssetMode(Integer.parseInt(DictUtil.getKey("ASSET_MODE", rowMap.get("assetMode").toString().trim())));
            } else {
                throw new NullFieldException("资产类型字段错误");
            }
            //检查 资产类型和型号层级关系
            List<AssetModel> models  = assetModelService.list();

            Integer desk = 0;
            for (AssetModel model : models) {
                if (model.getModel().equals(asset.getAssetImage())) {
                    AssetMode one = assetModeService.getById(model.getAssetModeId());
                    if (Objects.isNull(one)) {
                        throw new NullFieldException("未找到对应资产类型");
                    }
                    desk = one.getCode();
                    break;
                }
            }
            if (asset.getAssetMode().intValue() != desk) {
                throw new NullFieldException("资产类型和资产型号不匹配");
            }

            //资产厂商 有则获取转化赋值,无则报错
            if (rowMap.containsKey("manufacturerId") && !DictUtil.getKey("ASSET_FACTORY", rowMap.get("manufacturerId").toString().trim()).equals("")) {
                asset.setManufacturerId(Integer.parseInt(DictUtil.getKey("ASSET_FACTORY", rowMap.get("manufacturerId").toString().trim())));
            } else {
                throw new NullFieldException("资产厂商字段错误");
            }
            //是否为终端


            if (asset.getAssetMode() == 1831) {
                //判断组织机房
                assetInfoVo = assetImportService.estimateDouble(asset.getOrgId(), asset.getRoomId());

            } else {
                //判断组织机房机柜  起始位置
                if (rowMap.containsKey("cabinetId")) {
                    asset.setCabinetId(rowMap.get("cabinetId").toString().trim());
                } else {
                    throw new NullFieldException("机柜不可为空");
                }
                assetInfoVo = assetImportService.estimateTreble(asset.getOrgId(), asset.getRoomId(), asset.getCabinetId());
                if (rowMap.containsKey("startPosition") && rowMap.containsKey("endPosition")) {
                    try {
                        asset.setStartPosition(Integer.parseInt(rowMap.get("startPosition").toString()));
                        asset.setEndPosition(Integer.parseInt(rowMap.get("endPosition").toString()));
                    } catch (NumberFormatException n) {
                        assetInfoVo.setJudge(false);
                        assetInfoVo.setInfo("起始位置请使用数字填写");
                    }
                } else {
                    assetInfoVo.setJudge(false);
                    assetInfoVo.setInfo("机柜起始位置丶结束位置不可为空");
                }
            }
            if (!assetInfoVo.isJudge()) {
                throw new NullFieldException(assetInfoVo.getInfo());
            }
            Asset asset2 = assetInfoVo.getAsset();
            asset.setOrgId(asset2.getOrgId());
            asset.setRoomId(asset2.getRoomId());
            asset.setCabinetId(asset2.getCabinetId());
            int assetMode = asset.getAssetMode();
            String assetModeStr = assetMode + "";

            //根据类型判断和采集类型 判断必需字段
            switch (assetMode) {
                case 318:
                    if (rowMap.containsKey("ip")) {
                        asset.setIp(rowMap.get("ip").toString().trim());
                    } else {
                        throw new NullFieldException("ip不可为空");
                    }

                    //监控非是则设置为否
                    if (rowMap.containsKey("watch")) {
                        String value = rowMap.get("watch").toString().trim();
                        if (value.equals("是")) {
                            asset.setWatch((byte) 1);
                        } else {
                            asset.setWatch((byte) 0);
                        }
                    } else {
                        asset.setWatch((byte) 0);
                    }

                    //监控状态下  验证用户名密码
                    if (asset.getWatch() == (byte) 1) {
                        if (rowMap.containsKey("osUser")) {
                            asset.setOsUser(rowMap.get("osUser").toString().trim());
                        } else {
                            throw new NullFieldException("小型机设置监控时,用户名不可为空");
                        }
                        if (rowMap.containsKey("osPassword")) {
                            asset.setOsPassword(rowMap.get("osPassword").toString());
                        } else {
                            throw new NullFieldException("小型机设置监控时,登录密码不可为空");
                        }
                    } else {
                        if (rowMap.containsKey("osUser")) {
                            asset.setOsUser(rowMap.get("osUser").toString().trim());
                        }
                        if (rowMap.containsKey("osPassword")) {
                            asset.setOsPassword(rowMap.get("osPassword").toString().trim());
                        }
                    }

                    //类型为 服务器 工控机 小型机 终端 自律机
                case 183:
                case 1833:
                case 1832:
                case 1831:
                case 1834:
                    if (rowMap.containsKey("ip")) {
                        asset.setIp(rowMap.get("ip").toString().trim());
                    } else {
                        throw new NullFieldException("ip不可为空");
                    }

                    //监控非是则设置为否
                    if (rowMap.containsKey("watch")) {
                        String value = rowMap.get("watch").toString().trim();
                        if (value.equals("是")) {
                            asset.setWatch((byte) 1);
                        } else {
                            asset.setWatch((byte) 0);
                        }
                    } else {
                        asset.setWatch((byte) 0);
                    }
                    if (asset.getWatch() == (byte) 1) {
                        //监控状态下  验证用户名密码
                        if (rowMap.containsKey("collectionType") && !DictUtil.getKey("COLLECTION_TYPE", rowMap.get("collectionType").toString().trim()).equals("")) {
                            asset.setCollectionType(Integer.parseInt(DictUtil.getKey("COLLECTION_TYPE", rowMap.get("collectionType").toString().trim())));
                        } else {
                            throw new NullFieldException("此类型设置为监控设备时,采集类型不可为空");
                        }

                        if (rowMap.containsKey("osUser")) {
                            asset.setOsUser(rowMap.get("osUser").toString().trim());
                        } else {
                            throw new NullFieldException("监控资产中(登录用户名/团体号)不可为空");
                        }
                        if (asset.getCollectionType() != 1) {//采集类型为 非windows
                            if (rowMap.containsKey("osPassword")) {
                                asset.setOsPassword(rowMap.get("osPassword").toString());
                            } else {
                                throw new NullFieldException("非windows采集类型时,登录密码不可为空");
                            }
                        }
                    } else {
                        if (rowMap.containsKey("osUser")) {
                            asset.setOsUser(rowMap.get("osUser").toString().trim());
                        }
                        if (rowMap.containsKey("osPassword")) {
                            asset.setOsPassword(rowMap.get("osPassword").toString().trim());
                        }
                        if (rowMap.containsKey("collectionType")) {
                            try {
                                asset.setCollectionType(Integer.parseInt(DictUtil.getKey("COLLECTION_TYPE", rowMap.get("collectionType").toString().trim())));
                            } catch (Exception e) {
                            }
                        }
                    }

                    //拓扑显示  非是设置为空
                    if (rowMap.containsKey("showTopo")) {
                        String value = rowMap.get("showTopo").toString().trim();
                        if (value.equals("是")) {
                            asset.setShowTopo((byte) 1);
                        } else {
                            asset.setShowTopo((byte) 0);
                        }
                    } else {
                        asset.setShowTopo((byte) 0);
                    }


                    if (assetModeStr.length() > 3) {//区分小类型
                        asset.setAssetMode(183);
                        asset.setDesk(Integer.parseInt(assetModeStr.substring(assetModeStr.length() - 1)));
                    }
                    break;

                case 201:
                case 42:
                    if (rowMap.containsKey("ip")) {
                        asset.setIp(rowMap.get("ip").toString().trim());
                    } else {
                        throw new NullFieldException("ip不可为空");
                    }

                    //监控非是 设置为否
                    if (rowMap.containsKey("watch")) {
                        String value = rowMap.get("watch").toString().trim();
                        if (value.equals("是")) {
                            asset.setWatch((byte) 1);
                        } else {
                            asset.setWatch((byte) 0);
                        }
                    } else {
                        asset.setWatch((byte) 0);
                    }

                    //不监控也放入团体名
                    if (rowMap.containsKey("osUser")) {
                        asset.setOsUser(rowMap.get("osUser").toString().trim());
                    } else {
                        if (asset.getWatch() == (byte) 1) {
                            throw new NullFieldException("网络设备设置监控时,团体号不可为空");
                        }
                    }

                    //交换机路由器额外判断是否是核心路由
                    if (rowMap.containsKey("CR") && rowMap.get("CR").toString().trim().equals("是")) {
                        asset.setShowCore("SHOW_TOPO_@_SHOW");
                    } else {
                        asset.setShowCore("SHOW_TOPO_@_NO_SHOW");
                    }


                    //拓扑显示  非是设置为空
                    if (rowMap.containsKey("showTopo")) {
                        String value = rowMap.get("showTopo").toString().trim();
                        if (value.equals("是")) {
                            asset.setShowTopo((byte) 1);
                        } else {
                            asset.setShowTopo((byte) 0);
                        }
                    } else {
                        asset.setShowTopo((byte) 0);
                    }

                    break;

                default:
                    asset.setWatch(AssetWatchStatusEnum.WATCH_STATUS_NO.getCode());
                    asset.setShowTopo((byte) 0);
                    break;


            }


            /*非必需
            -- 新增对于非监控设备的非必需字段 (collectionType:采集类型 osUser:登录用户名/团体名 osPassword:登录密码)
            供货商:assetSupplier(ASSET_SUPPLIER)   AB机标识:aBFlag(AB_FLAG)  机运行方式:runModel(ASSET_RUN_MODEL)
            ntpFlag:是否采集系统时间(NTP_FLAG)

            string:连接显示器数量:displayerTotal,电源模块型号:powerModel,视频线接口类型:displayerPortModel,
            主机编号:hostNumber,序列号:serialNumber,内存:memory,单硬盘容量:diskCapacity,CPU主频:cpuFrequency
            ip地址2:ip2,管理口ip:ipmiIp,管理口账号:ipmiUser,管理口密码:ipmiPwd,操作系统版本:operationSystem,CPU型号:cpuModel,

            integer:cpuNumber:CPU个数,硬盘个数:diskTotal,电源模块个数:powerTotal,登录端口:loginPort,cpuCoreNumber CPU核数
            date:上架时间:onlineTime,下架时间:downlineTime,质保期限:validityDate,

            */

            Map<String, String> ConvertValue = DictUtil.value("CONVERT_VALUE");
            for (Map.Entry<String, String> nameValue : ConvertValue.entrySet()) {
                if (rowMap.containsKey(nameValue.getKey())) {
                    String key = nameValue.getKey();
                    try {
                        String value = rowMap.get(key).toString().trim();
                        switch (key) {
                            case "assetSupplier":
                                AssetImportUtils.setFieldValues(asset, key, DictUtil.getKeyNotNull("ASSET_SUPPLIER", value));
                                break;

                            case "aBFlag":
                                if ("A机".equals(value)) {
                                    asset.setABFlag((byte) 0);
                                } else if ("B机".equals(value)) {
                                    asset.setABFlag((byte) 1);
                                }
                                break;

                            case "runModel":
                                AssetImportUtils.setFieldValues(asset, key, DictUtil.getKeyNotNull("ASSET_RUN_MODEL", value));
                                break;

                            case "ntpFlag":
                                if (value.equals("是")) {
                                    AssetImportUtils.setFieldValues(asset, key, (byte) 1);
                                }
                                break;

                            case "cpuNumber":
                            case "diskTotal":
                            case "powerTotal":
                            case "loginPort":
                            case "cpuCoreNumber":
                                AssetImportUtils.setFieldValues(asset, key, Integer.parseInt(value));
                                break;

                            case "onlineTime":
                            case "downlineTime":
                            case "validityDate":

                                Date newTime = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                                AssetImportUtils.setFieldValues(asset, key, newTime);

                                break;
                            default:
                                AssetImportUtils.setFieldValues(asset, key, value);
                                break;
                        }
                    } catch (ParseException p) {
                        throw new NullFieldException(ConvertValue.get(key) + "字段的日期格式需严格按照 2021-06-13 相同格式填写");

                    } catch (Exception e) {
                        throw new NullFieldException(ConvertValue.get(key) + "字段填写错误");

                    }
                }
            }
            if (ObjectUtil.isNull(asset.getNtpFlag())) {
                asset.setNtpFlag((byte) 0);
            }

        } catch (NoSuchMethodException e) {
            assetInfoVo.setJudge(false);
            Info = "模板错误! 请重新下载模板";
        } catch (InvocationTargetException e) {
            assetInfoVo.setJudge(false);
            Info = "模板错误! 请重新下载模板";
        } catch (IllegalAccessException e) {
            Info = "模板错误! 请重新下载模板";
            assetInfoVo.setJudge(false);
        } catch (NullFieldException n) {
            Info = n.getMessage();
            assetInfoVo.setJudge(false);
        }
        assetInfoVo.setInfo(Info);
        assetInfoVo.setAsset(asset);
        return assetInfoVo;
    }

    // 资产录入验证
    public ResultVo<?> asset(Asset asset) {
        // 1终端，2小型机，3工控机 4自律机
        List<Integer> deskTypeList = Arrays.asList(1, 2, 3, 4);
        if (deskTypeList.contains(asset.getDesk())) {
            asset.setDesk(Integer.valueOf(asset.getAssetMode() + "" + asset.getDesk()));
        } else if (Objects.nonNull(asset.getDesk())) {
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(ERR_CODE);
            testVo.setMsg("设备小类型错误" + asset.getDesk());
            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
        }


        // 默认值
        if (Objects.isNull(asset.getDesk())) {
            asset.setDesk(asset.getAssetMode());
        }

        if (StrUtil.isNotEmpty(asset.getOsPassword())) {
            asset.setOsPassword(EncryptUtil.aesEncryptHex(asset.getOsPassword()));
        }
        if (StrUtil.isNotEmpty(asset.getLoginPwd())) {
            asset.setLoginPwd(EncryptUtil.aesEncryptHex(asset.getLoginPwd()));
        }
        if (StrUtil.isNotEmpty(asset.getIpmiPwd())) {
            asset.setIpmiPwd(EncryptUtil.aesEncryptHex(asset.getIpmiPwd()));
        }

        // 检查IP是否已分配
        if (ObjectUtil.isNotNull(asset.getIp())) {
            Asset one = assetService.findOneByIp(asset.getIp());
            if (Objects.nonNull(one)) {
                String orgName = getAssetOrgName(one);
                String msg = String.format("IP[%s]已分配给[%s]的设备[%s]", asset.getIp(), orgName, one.getName());
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg(msg);
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            }

            //查询ip是否可用
            QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("IP", asset.getIp());
            List<IpInfo> ipList = ipInfoService.list(queryWrapper);
            if (ipList.size() == 0) {
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg("该ip不可用,未录入网段");
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            }
        }


        // 判断机柜中设备U位
        if (StrUtil.isNotEmpty(asset.getCabinetId())) {
            Integer startPosition = asset.getStartPosition();
            Integer endPosition = asset.getEndPosition();
            String msg = this.checkPosition(startPosition, endPosition, asset.getCabinetId(), asset.getId());

            if (StrUtil.isNotEmpty(msg)) {
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg(msg);
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            }
        }

        // 判断上下架时间
        Date downlineTime = asset.getDownlineTime();
        Date onlineTime = asset.getOnlineTime();
        if (Objects.nonNull(downlineTime) && Objects.nonNull(onlineTime)) {
            if (downlineTime.before(onlineTime)) {
                AssetCollectTestVo testVo = new AssetCollectTestVo();
                testVo.setCode(ERR_CODE);
                testVo.setMsg("上架时间不应晚于下架时间");
                return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
            }
        }

        try {
            asset.setId(MyIdUtil.getId());

            assetService.createAsset(asset);

            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(SUCCESS_CODE);
            testVo.setMsg("操作成功");
            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
        } catch (AddAssetException e) {
            log.error(e.getAddErrorMsg(), e);
            AssetCollectTestVo testVo = new AssetCollectTestVo();
            testVo.setCode(ERR_CODE);
            testVo.setMsg(e.getAddErrorMsg());

            if (AddAssetException.COLLECT_ERROR.equals(e.getAddErrorcode())) {
                testVo.setTestResultList(e.getTestResultList());
            }

            return ResultVoUtil.error(ResultEnum.SUCCESS.getCode(), "", testVo);
        }
    }


    private String checkPosition(Integer startPosition, Integer endPosition, String cabinetId, String assetId) {
        if (Objects.isNull(startPosition) || Objects.isNull(endPosition)) {
            return "请确认设备位置";
        }
        // 校验机柜U位数
        if (startPosition < 1 || startPosition > 42) {
            return "起始位置应在1~42之内";
        }
        // 校验机柜U位数
        if (endPosition < 1 || endPosition > 42) {
            return "结束位置应在1~42之内";
        }
        // 校验机柜中设备位置
        if (Integer.compare(startPosition, endPosition) == 1) {
            return "起始位置应小于等于结束位置";
        }
        // 同一机柜中U位不能重叠
        QueryWrapper<AssetAttach> query = Wrappers.query();
        query.eq("cabinet_id", cabinetId);
        List<AssetAttach> assetAttachList = assetAttachService.list(query);
        for (AssetAttach attach : assetAttachList) {
            int start = attach.getStartPosition();
            int end = attach.getEndPosition();
            if ((startPosition >= start && startPosition <= end) || (endPosition >= start && endPosition <= end) || (startPosition < start && endPosition > end)) {
                return "设备位置与机柜内已有U位重叠";
            }
        }
        return "";
    }

    private String getAssetOrgName(Asset one) {
        String orgName = "";
        SysOrg org = orgService.getById(one.getOrgId());

        String[] pidArr = org.getPids().split(",");
        for (String pid : pidArr) {
            pid = pid.replace("[", "").replace("]", "");
            if ("0".equals(pid)) {
                continue;
            }
            String title = orgService.getById(pid).getTitle();
            if (StrUtil.isNotEmpty(title)) {
                orgName = title + "-";
            }
        }
        orgName += org.getTitle();
        return orgName;
    }

}
