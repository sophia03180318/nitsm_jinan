package com.jcca.web.asset.controller;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.controller.bean.ControllerVo;
import com.jcca.web.asset.controller.bean.DsVo;
import com.jcca.web.asset.controller.bean.LogInfoBean;
import com.jcca.web.asset.controller.bean.RaidDetialVo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.collect.service.CollectDsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ Author：sophia
 * @ Date：Created in 0:47 2022/6/9
 * @ Description:
 */
@RestController
@RequestMapping("/api/ds")
@Slf4j
@Api(tags = "DS相关接口")
public class ApiDsController {

    @Resource
    private CollectDsService dsService;
    @Resource
    private AssetService assetService;
    @Resource
    private AssetAttachService attachService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private RoomService roomService;


    /**
     * 资产详情
     */

    @GetMapping("/detail/{assetId}")
    @ApiOperation(value = "资产详情")
    @ActionLog(name = "查看DS详情", title = "资产管理", key = LogTypeConstant.QUERY)
    public ResultVo<RaidDetialVo> detail(@PathVariable("assetId") String assetId) {
        if (Strings.isEmpty(assetId)) {
            return ResultVoUtil.error("资产ID不可为空");
        }
        Asset asset = assetService.getById(assetId);
        if (ObjectUtil.isNull(asset)) {
            return ResultVoUtil.error("资产不存在");
        }
        RaidDetialVo raidDetialVo = new RaidDetialVo();
        try {
            raidDetialVo.setId(assetId);
            raidDetialVo.setIp(asset.getIp());
            raidDetialVo.setAssetName(asset.getName());
            raidDetialVo.setAssetImage(asset.getAssetImage());
            String manufacturerName = DictUtil.getValue("ASSET_FACTORY", asset.getManufacturerId() + "");
            raidDetialVo.setManufacturerName(manufacturerName);

            String assetModelStr = DictUtil.getValue("ASSET_MODE", asset.getAssetMode() + "");
            raidDetialVo.setAssetModelStr(assetModelStr);
            String OrgName = orgService.getById(asset.getOrgId()).getTitle();
            raidDetialVo.setOrgName(OrgName);

            AssetAttach attach = attachService.getByAssetId(assetId);

            String cabinetName = cabinetService.getById(attach.getCabinetId()).getName();
            raidDetialVo.setCabinetName(cabinetName);

            String roomName = roomService.getById(attach.getRoomId()).getName();
            raidDetialVo.setRoomName(roomName);
        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.WEB_DS_MANAGER)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_DS_MANAGER, ErrorCodeEnum.WEB_DS_MANAGER_GET_DETAIL, asset.getIp(), e.getMessage()), e);
            }
        }
        return ResultVoUtil.success(raidDetialVo);
    }


    /**
     * 容量
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/capacity/{assetId}")
    @ApiOperation(value = "容量进度条")
    public ResultVo<DsVo> capacity(@PathVariable("assetId") String assetId) {
        if (Strings.isEmpty(assetId)) {
            return ResultVoUtil.error("资产ID不可为空");
        }
        List<CollectDS> capacity = dsService.findByType(assetId, 4);
        if (Objects.nonNull(capacity) && !capacity.isEmpty()) {
            CollectDS collectDS = capacity.get(0);
            DsVo dsVo = new DsVo();
            dsVo.setCapacity(collectDS.getCapacity());
            dsVo.setFreeCapacity(collectDS.getFreeCapacity());
            dsVo.setCapacityStr(collectDS.getCapacityStr());
            long usedCapacity = collectDS.getCapacity() - collectDS.getFreeCapacity();
            dsVo.setCapacityStr(getNetFileSizeDescription(collectDS.getCapacity()));
            dsVo.setUsedCapacityStr(getNetFileSizeDescription(usedCapacity));
            dsVo.setFreeCapacityStr(getNetFileSizeDescription(collectDS.getFreeCapacity()));
            if (collectDS.getCapacity() != 0 && Objects.nonNull(collectDS.getFreeCapacity())) {
                String v = new BigDecimal(usedCapacity).multiply(new BigDecimal(100)).divide(new BigDecimal(collectDS.getCapacity()), 2, BigDecimal.ROUND_DOWN).toString();
                dsVo.setUsedRate(v);
            } else {
                dsVo.setUsedRate("0B");
            }
            dsVo.setFreeCapacityStr(getNetFileSizeDescription(collectDS.getFreeCapacity()));
            return ResultVoUtil.success(dsVo);
        }
        return ResultVoUtil.success(new DsVo());
    }

    /**
     * controller
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/controller/{assetId}")
    @ApiOperation(value = "controller")
    public ResultVo<List<ControllerVo>> controller(@PathVariable("assetId") String assetId) {
        if (Strings.isEmpty(assetId)) {
            return ResultVoUtil.error("资产ID不可为空");
        }
        List<CollectDS> drivesList = dsService.findByType(assetId, 3);
        if (ObjectUtil.isNull(drivesList) || drivesList.isEmpty()) {
            return ResultVoUtil.success("暂时未采集到相关Drive信息  请稍等!");
        }
        Set<Integer> ySet = new HashSet<>();
        ArrayList<String> x_y = new ArrayList<>();
        int max_x = drivesList.stream().map(ds -> {
            ySet.add(ds.getYindex());
            x_y.add(ds.getYindex() + "," + ds.getXindex());
            return ds;
        }).max(Comparator.comparing(CollectDS::getXindex)).get().getXindex();

        ArrayList<String> index = new ArrayList<>();
        for (Integer y : ySet) {
            for (int i = 1; i <= max_x; i++) {
                index.add(y + "," + i);
            }
        }
        index.removeAll(x_y);

        for (String s : index) {
            String[] split = s.split(",");
            CollectDS collectDS = new CollectDS();
            collectDS.setAssetId(assetId);
            collectDS.setXindex(Integer.valueOf(split[1]));
            collectDS.setYindex(Integer.valueOf(split[0]));
            collectDS.setStatus(3);
            drivesList.add(collectDS);
        }
        List<CollectDS> controllers = dsService.findByType(assetId, 0).stream().sorted(Comparator.comparing(CollectDS::getName)).collect(Collectors.toList());

        ArrayList<ControllerVo> controllerVos = new ArrayList<>();

        Map<Integer, List<CollectDS>> collect = drivesList.stream().collect(Collectors.groupingBy(CollectDS::getYindex));
        int i = 0;
        for (Map.Entry<Integer, List<CollectDS>> kv : collect.entrySet()) {
            List<CollectDS> value = kv.getValue().stream().sorted(Comparator.comparing(CollectDS::getYindex).thenComparing(CollectDS::getXindex)).collect(Collectors.toList());
            ;
            ControllerVo controllerVo = new ControllerVo();
            controllerVo.setDrivers(value);
            if (i < controllers.size()) {
                CollectDS collectDS = controllers.get(i);
                controllerVo.setName("Controller" + collectDS.getName());
                controllerVo.setStatus(collectDS.getStatusInfo());
                controllerVo.setCode(collectDS.getRaidLevel());
            } else {
                controllerVo.setName("未知");
                controllerVo.setStatus("未知");
                controllerVo.setCode("未知");
            }

            controllerVos.add(controllerVo);
            i++;
        }
        return ResultVoUtil.success(controllerVos);
    }


    /**
     * 日志文件
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/logFile/{assetId}")
    @ApiOperation(value = "downLog")
    public void logFile(@PathVariable("assetId") String assetId, HttpServletResponse response) {
        if (Strings.isEmpty(assetId)) {
            return;
        }
        try {
            String fileName = assetId + ".txt";
            InputStream inStream = new FileInputStream("/home/nitsm/file/" + fileName);// 文件的存放路径
            response.reset();
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            // 循环取出流中的数据
            byte[] b = new byte[100];
            int len;
            while ((len = inStream.read(b)) > 0)
                response.getOutputStream().write(b, 0, len);
            inStream.close();
        } catch (IOException e) {
            log.error("DS下载日志文件 IO异常", e);
        }
    }


    /**
     * 日志文件
     *
     * @return
     * @Author: sophia
     */
        /*
    @GetMapping("/logFile/{assetId}")
    @ApiOperation(value = "downLog")
    public void logFile(@PathVariable("assetId") String assetId, HttpServletResponse response) {
        if (Strings.isEmpty(assetId)) {
            return;
        }
        try {
            String fileName = assetId + ".txt";
            PullAlertLogResultReq req = new PullAlertLogResultReq();
            req.setCommand("/home/nitsm/file/" + fileName);
            PullAlertLogResultResp PullAlertLogResultResp = dsService.downLogFile(req);
            byte[] bytes = PullAlertLogResultResp.getMsg().getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            response.reset();
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            // 循环取出流中的数据
            byte[] b = new byte[100];
            int len;
            while ((len = inputStream.read(b)) > 0)
                response.getOutputStream().write(b, 0, len);
            inputStream.close();

        } catch (IOException e) {
            if (LogInputUtils.inputError(ServerTypeEnum.WEB_DS_MANAGER)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_DS_MANAGER, ErrorCodeEnum.WEB_DS_MANAGER_LOG_DOWNLOAD, "资产ID：" + assetId, e.getMessage()), e);
            }
        } catch (Exception e) {
            log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_DS_MANAGER, ErrorCodeEnum.WEB_DS_MANAGER_LOG_DOWNLOAD, "资产ID：" + assetId, e.getMessage()), e);
        }

    }

    * */

    /**
     * Drives
     *
     * @return
     * @Author: sophia
     */

    @GetMapping("/drives/{assetId}")
    @ApiOperation(value = "Drives列表")
    public ResultVo<List<CollectDS>> getDrives(@PathVariable("assetId") String assetId) {
        if (Strings.isEmpty(assetId)) {
            return ResultVoUtil.error("资产ID不可为空");
        }
        List<CollectDS> drivesList = dsService.findByType(assetId, 3);
        if (Objects.isNull(drivesList) || drivesList.isEmpty()) {
            return ResultVoUtil.error("暂未查询到相关硬盘");
        }
        Set<Integer> ySet = new HashSet<>();
        ArrayList<String> x_y = new ArrayList<>();
        int max_x = drivesList.stream().map(ds -> {
            ySet.add(ds.getYindex());
            x_y.add(ds.getYindex() + "," + ds.getXindex());
            return ds;
        }).max(Comparator.comparing(CollectDS::getXindex)).get().getXindex();

        ArrayList<String> index = new ArrayList<>();
        for (Integer y : ySet) {
            for (int i = 1; i <= max_x; i++) {
                index.add(y + "," + i);
            }
        }
        index.removeAll(x_y);

        for (String s : index) {
            String[] split = s.split(",");
            CollectDS collectDS = new CollectDS();
            collectDS.setAssetId(assetId);
            collectDS.setXindex(Integer.valueOf(split[1]));
            collectDS.setYindex(Integer.valueOf(split[0]));
            collectDS.setStatus(3);
            drivesList.add(collectDS);
        }

        return ResultVoUtil.success(drivesList);
    }


    /**
     * ARRAY
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/arrays/{assetId}")
    @ApiOperation(value = "Arrays")
    public ResultVo<List<CollectDS>> getArrays(@PathVariable("assetId") String assetId) {
        if (Strings.isEmpty(assetId)) {
            return ResultVoUtil.error("资产ID不可为空");
        }
        List<CollectDS> arrayList = dsService.findByType(assetId, 1);
        for (CollectDS collectDS : arrayList) {
            if (Objects.nonNull(collectDS.getCapacity()) && Objects.nonNull(collectDS.getFreeCapacity())) {
                BigDecimal usedCapacity = new BigDecimal(collectDS.getCapacity()).subtract(new BigDecimal(collectDS.getFreeCapacity()));
                String v = usedCapacity.multiply(new BigDecimal(100)).divide(new BigDecimal(collectDS.getCapacity()), 2, RoundingMode.DOWN).toString();
                collectDS.setUsedRate(v);
            } else {
                collectDS.setUsedRate("100");
            }
        }

        return ResultVoUtil.success(arrayList);
    }


    /**
     * logical drives
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/logicaldrives/{arrayId}")
    @ApiOperation(value = "Logicaldrives")
    public ResultVo<List<CollectDS>> getLogicaldrives(@PathVariable("arrayId") String arrayId) {
        if (Strings.isEmpty(arrayId)) {
            return ResultVoUtil.error("ARRAY ID不可为空");
        }

        List<CollectDS> logicalDrives = dsService.findByArray(arrayId);
        for (CollectDS logicalDrive : logicalDrives) {
            if (Objects.nonNull(logicalDrive.getFreeCapacityStr())) {
                logicalDrive.setHostGroup(logicalDrive.getFreeCapacityStr());
            }
        }
        return ResultVoUtil.success(logicalDrives);
    }

    /**
     * 最后采集时间
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/lastTime/{assetId}")
    @ApiOperation(value = "lastTime")
    public ResultVo<String> getLastTime(@PathVariable("assetId") String assetId) {
        if (Strings.isEmpty(assetId)) {
            return ResultVoUtil.error("资产ID不可为空");
        }
        Date lastTime = dsService.findLastTime(assetId);
        if (Objects.nonNull(lastTime)) {
            return ResultVoUtil.success(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(lastTime));
        }
        return ResultVoUtil.success("");
    }

    /**
     * 事件
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/logInfo/{assetId}")
    @ApiOperation(value = "logInfo")
    public ResultVo<List<LogInfoBean>> getLogInfo(@PathVariable("assetId") String assetId) {

        if (Strings.isEmpty(assetId)) {
            return ResultVoUtil.error("资产id不可为空");
        }
        List<String> logInfoList = dsService.findByType(assetId, 5).stream().map(CollectDS::getLogInfo).collect(Collectors.toList());
        ArrayList<LogInfoBean> logInfoBeans = new ArrayList<>();
        for (String s : logInfoList) {
            LogInfoBean logInfoBean = new LogInfoBean();
            int i = s.indexOf("<br/>");
            logInfoBean.setTitle(s.substring(0, i));
            logInfoBean.setContent(s.substring(i + 5));
            logInfoBeans.add(logInfoBean);
        }
        return ResultVoUtil.success(logInfoBeans);
    }


    public static String getNetFileSizeDescription(long size) {
        BigDecimal bigDecimal = new BigDecimal(size);
        if (size >= 1099511627776L) {
            return bigDecimal.divide(new BigDecimal(1099511627776L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "TB";
        } else if (size >= 1073741824L) {
            return bigDecimal.divide(new BigDecimal(1073741824L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "GB";
        } else if (size >= 11048576L) {
            return bigDecimal.divide(new BigDecimal(1048576L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "MB";
        } else if (size >= 1024L) {
            return bigDecimal.divide(new BigDecimal(1024L), 2, BigDecimal.ROUND_DOWN).doubleValue() + "KB";
        } else {
            return size + "B";
        }
    }


}
