package com.jcca.web.asset.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppPattenUtils;
import com.jcca.common.utils.FileUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.common.utils.file.FileUpload;
import com.jcca.common.utils.file.config.properties.UploadProjectProperties;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.entity.CollectInterfaces;
import com.jcca.web.collect.service.CollectInterfacesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author hanwone
 * @description 前端文件上传下载
 * @className ApiOpticalExportController
 * @date 2022/7/20 16:09
 * @since 2.0.0.1
 */
@Controller
@RequestMapping("/api")
@Api(tags = "光功率表格文件上传下载")
@Slf4j
public class ApiOpticalExportController {

    @Resource
    private SysFileService uploadService;
    @Resource
    private CollectInterfacesService collectInterfacesService;
    @Resource
    private AssetService assetService;
    @Resource
    private SysOrgService orgService;
    @Resource
    private RedisService redisService;

    private Map<String, String> ipIdMap = new HashMap<>(64);

    /**
     * 上传表格文件
     */
    @PostMapping("/optical/upload")
    @ResponseBody
    @ApiOperation(value = "上传光功率表格文件")
    @RequiresPermissions({"api:optical"})
    @ActionLog(name = "上传光功率表格文件", title = "监控管理", key = LogTypeConstant.UPLOAD)
    public ResultVo<Object> uploadExcel(@RequestParam("file") MultipartFile multipartFile) {

        if (multipartFile.getSize() == 0) {
            return ResultVoUtil.error("不能上传空文件");
        }

        String[] types = {
                "xls",
                "xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"
        };
        if (!FileUpload.isContentType(multipartFile, types)) {
            return ResultVoUtil.error(ResultEnum.NO_FILE_TYPE.getMessage());
        }

        try {
            ExcelReader reader = ExcelUtil.getReader(multipartFile.getInputStream());
            int columnCount = reader.getColumnCount();
            if (columnCount != 18) {
                return ResultVoUtil.error("请上传正确的模板文件");
            }
        } catch (IOException e) {
            return ResultVoUtil.error("上传excel文件读取文件异常");
        }
        SysOrg org = orgService.getDefaultOrg();
        String orgId = org.getId();

        // 判断文件是否存在
        SysFile uploadSha1 = uploadService.getBySha1(FileUpload.getFileSha1(multipartFile));
        if (uploadSha1 != null) {
            uploadSha1.setId(orgId);
            uploadService.saveOrUpdate(uploadSha1);
            return ResultVoUtil.success(uploadSha1);
        }

        // 创建Upload实体对象
        SysFile upload = FileUpload.getFile(multipartFile, "/optical");
        try {
            upload.setId(orgId);
            return saveExcel(multipartFile, upload);
        } catch (Exception e) {
            log.error("光功率表格上传异常-ApiOpticalExportController", e);
            return ResultVoUtil.error("上传excel文件失败");
        }
    }

    private ResultVo<Object> saveExcel(MultipartFile multipartFile, SysFile upload) throws IOException, NoSuchAlgorithmException {

        FileUpload.transferTo(multipartFile, upload);

        // 将文件信息保存到数据库中
        uploadService.saveOrUpdate(upload);

        return ResultVoUtil.success(upload);
    }


    /**
     * 下载表格文件验证
     */
    @PostMapping("/optical/download/verify")
    @ApiOperation(value = "下载光功率表格验证")
    @ResponseBody
    public ResultVo<Object> downloadExcelVerify() {
        FileUtil.createTmpPath();
        SysOrg org = orgService.getDefaultOrg();
        String orgId = org.getId();
        SysFile sysFile = uploadService.getById(orgId);
        if (Objects.isNull(sysFile)) {
            throw new ResultException(ResultEnum.TEMPLATE_NULL);
        }
        UploadProjectProperties properties = SpringContextUtil.getBean(UploadProjectProperties.class);
        String filePath = sysFile.getFilePath();
        filePath = properties.getFilePath() + filePath.replace("/upload", "");
        File nfile = new File(filePath);
        if (!nfile.exists()) {
            throw new ResultException(ResultEnum.TEMPLATE_NULL);
        }
        return ResultVoUtil.success();
    }

    /**
     * 下载表格文件
     */
    @GetMapping("/optical/template")
    @ApiOperation(value = "下载光功率文件模板")
    @ActionLog(name = "下载光功率文件模板", title = "监控管理", key = LogTypeConstant.DOWNLOAD)
    public void downloadTemplate(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=optical_template_V2.xls");
        try {
            String filePath = "/templates/import/optical_template_V2.xls";
            InputStream fileInput = ApiAssetController.class.getResourceAsStream(filePath);
            ExcelReader reader = ExcelUtil.getReader(fileInput);
            ExcelWriter writer = reader.getWriter();
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
        } catch (IOException e) {
            log.error("资产管理-下载模板失败：{}", e.getMessage(), e);
        }
    }


    /**
     * 下载表格文件
     */
    @GetMapping("/optical/download")
    @ApiOperation(value = "下载光功率表格文件")
    @ActionLog(name = "下载光功率表格文件", title = "监控管理", key = LogTypeConstant.DOWNLOAD)
    public void downloadExcel(HttpServletResponse response) throws IOException {

        SysOrg org = orgService.getDefaultOrg();
        String orgId = org.getId();
        SysFile sysFile = uploadService.getById(orgId);
        if (Objects.isNull(sysFile)) {
            throw new ResultException(ResultEnum.TEMPLATE_NULL);
        }

        UploadProjectProperties properties = SpringContextUtil.getBean(UploadProjectProperties.class);
        String filePath = sysFile.getFilePath();
        filePath = properties.getFilePath() + filePath.replace("/upload", "");
        if (!new File(filePath).exists()) {
            throw new ResultException(ResultEnum.TEMPLATE_NULL);
        }

        String fileName = sysFile.getFileName();
        String[] splitm = fileName.split("\\.");
        String nfilePath = filePath.replace(fileName, splitm[0] + "_." + splitm[1]);
        File nfile = new File(nfilePath);

        response.setContentType("application/octet-stream;charset=utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(sysFile.getOrignName(), "utf-8"));
        ExcelReader reader = ExcelUtil.getReader(filePath);
        ExcelWriter writer = reader.getWriter();
        List<Sheet> sheets = reader.getSheets();
        // 本端IP
        String ip1 = "";
        // key为IP, 本端设备
        Map<String, CollectInterfaces> opticalMap1 = new HashMap<>();
        // key为IP, 对端设备
        Map<String, CollectInterfaces> opticalMap2 = new HashMap<>();
        // 存放相同IP临时数据，外面key为IP，里面key为portName
        Map<String, Map<String, CollectInterfaces>> tempDataMap = new HashMap<>();

        try {
            for (int s = 0; s < sheets.size(); s++) {
                Sheet sheet = sheets.get(s);
                int lastRowNum = sheet.getLastRowNum();
                // 本端端口信息
                CollectInterfaces interface1 = null;
                // 对端端口信息
                CollectInterfaces interface2 = null;
                for (int i = 2; i < lastRowNum; i++) {
                    Row row = sheet.getRow(i);
                    Cell cell0 = row.getCell(0);
                    // 每行首列不是数字序号则跳过
                    if (Objects.isNull(cell0)) {
                        continue;
                    }
                    if (i % 2 == 0) {
                        String name = cell0.getCellType().name();
                        if (!CellType.NUMERIC.name().equals(name)) {
                            if (LogInputUtils.inputError(ServerTypeEnum.WEB_OPTICAL_EXPORT)) {
                                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_OPTICAL_EXPORT, ErrorCodeEnum.WEB_OPTICAL_TYPE_NUMBER, "", "表格首行类型错误"));
                            }
                        }
                    }
                    // 本端光口名称
                    Cell cell1 = row.getCell(1);
                    // 本端设备IP
                    Cell cell3 = row.getCell(3);
                    ip1 = cell3.getStringCellValue();
                    Boolean isIp1 = AppPattenUtils.isIp(ip1);
                    if (isIp1) {
                        opticalMap1 = getOpticalMap(ip1, tempDataMap);
                    } else {
                        if (LogInputUtils.inputError(ServerTypeEnum.WEB_OPTICAL_EXPORT)) {
                            log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_OPTICAL_EXPORT, ErrorCodeEnum.WEB_OPTICAL_IP_FORMAT, ip1, "导入的ip1不是IP数据" + ip1));
                        }
                    }

                    // 对端IP
                    Cell cell12 = row.getCell(12);
                    if (Objects.isNull(cell12)) {
                        continue;
                    }
                    String ip2 = cell12.getStringCellValue();
                    Boolean isIp2 = AppPattenUtils.isIp(ip2);
                    if (isIp2) {
                        opticalMap2 = getOpticalMap(ip2, tempDataMap);
                    } else {
                        if (LogInputUtils.inputError(ServerTypeEnum.WEB_OPTICAL_EXPORT)) {
                            log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_OPTICAL_EXPORT, ErrorCodeEnum.WEB_OPTICAL_IP_FORMAT, ip2, "导入的ip2不是IP数据" + ip2));
                        }
                    }
                    // 光功率正常范围
                    Cell cell6 = row.getCell(6);
                    // 采集到的光功率
                    Cell cell7 = row.getCell(7);
                    // 光口光功率判断是否正常 Y/N
                    Cell cell8 = row.getCell(8);
                    // 对端光口名称
                    Cell cell10 = row.getCell(10);
                    // 对端光功率正常范围
                    Cell cell15 = row.getCell(15);
                    // 采集到的对端设备光口光功率
                    Cell cell16 = row.getCell(16);
                    // 对端设备光口光功率 判断是否正常 Y/N
                    Cell cell17 = row.getCell(17);

                    String[] splitVo = cell6.getStringCellValue().split("/");
                    String[] split = cell15.getStringCellValue().split("/");
                    boolean bo = false;
                    boolean b = false;
                    if (i % 2 == 0) {
                        // 本端设备光功率
                        interface1 = opticalMap1.get(cell1.getStringCellValue());
                        if (Objects.nonNull(interface1)) {
                            String txPower = interface1.getTxPower();
                            if (Objects.nonNull(txPower)) {
                                cell7.setCellValue(txPower);
                                bo = intRange((int) (Double.parseDouble(txPower) * 100), (int) (Double.parseDouble(splitVo[1]) * 100), (int) (Double.parseDouble(splitVo[0]) * 100));
                            }
                        }

                        // 对端设备光功率
                        interface2 = opticalMap2.get(cell10.getStringCellValue());
                        if (Objects.nonNull(interface2)) {
                            String txPower = interface2.getTxPower();
                            if (Objects.nonNull(txPower)) {
                                cell16.setCellValue(txPower);
                                b = intRange((int) (Double.parseDouble(txPower) * 100), (int) (Double.parseDouble(split[1]) * 100), (int) (Double.parseDouble(split[0]) * 100));
                            }
                        }
                    } else {
                        if (Objects.nonNull(interface1)) {
                            String rxPower = interface1.getRxPower();
                            if (Objects.nonNull(rxPower)) {
                                cell7.setCellValue(rxPower);
                                bo = intRange((int) (Double.parseDouble(rxPower) * 100), (int) (Double.parseDouble(splitVo[1]) * 100), (int) (Double.parseDouble(splitVo[0]) * 100));
                            }
                        }

                        if (Objects.nonNull(interface2)) {
                            String rxPower = interface2.getRxPower();
                            if (Objects.nonNull(rxPower)) {
                                cell16.setCellValue(rxPower);
                                b = intRange((int) (Double.parseDouble(rxPower) * 100), (int) (Double.parseDouble(split[1]) * 100), (int) (Double.parseDouble(split[0]) * 100));
                            }
                        }
                    }

                    if (bo) {
                        cell8.setCellValue("Y");
                    } else {
                        if (Objects.nonNull(interface1)) {
                            cell8.setCellValue("N");
                        }
                    }
                    if (b) {
                        cell17.setCellValue("Y");
                    } else {
                        if (Objects.nonNull(interface2)) {
                            cell17.setCellValue("N");
                        }
                    }
                }

                // 单独处理误码统计
                if (sheets.size() > 2 && s == sheets.size() - 1) {
                    handleErrorCode(sheet, tempDataMap);
                    break;
                }
            }

            writer.flush(nfile);
            writer.flush(response.getOutputStream(), true);

        } catch (IOException e) {
            if (LogInputUtils.inputError(ServerTypeEnum.WEB_OPTICAL_EXPORT)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_OPTICAL_EXPORT, ErrorCodeEnum.WEB_OPTICAL_EXPORT_ERROR, "", e.getMessage()), e);
            }
        } finally {
            writer.close();
            reader.close();
        }
    }

    private void handleErrorCode(Sheet sheet, Map<String, Map<String, CollectInterfaces>> tempDataMap) {
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 2; i < lastRowNum; i++) {
            Row row = sheet.getRow(i);
            // 本端设备
            Map<String, CollectInterfaces> opticalMap1 = new HashMap<>();
            Cell cell2 = row.getCell(2);
            String ip1 = cell2.getStringCellValue();
            if (StrUtil.isEmpty(ip1)) {
                continue;
            }
            if (AppPattenUtils.isIp(ip1)) {
                opticalMap1 = this.getOpticalMap(ip1, tempDataMap);
                // 本端端口
                Cell cell3 = row.getCell(3);
                CollectInterfaces interface1 = opticalMap1.get(cell3.getStringCellValue());

                // 本端误码数
                long error1 = getError(ip1, interface1);
                Cell cell4 = row.getCell(4);
                cell4.setCellValue(error1 + "");
                // 是否正常
                Cell cell5 = row.getCell(5);
                cell5.setCellValue("Y");
                if (error1 > 0) {
                    cell5.setCellValue("N");
                }
            } else {
                log.warn("资产不存在,IP为[{}]", ip1);
                continue;
            }

            // 对端设备
            Map<String, CollectInterfaces> opticalMap2 = new HashMap<>();
            Cell cell7 = row.getCell(7);
            String ip2 = cell7.getStringCellValue();
            if (StrUtil.isEmpty(ip2)) {
                continue;
            }
            if (AppPattenUtils.isIp(ip2)) {
                opticalMap2 = this.getOpticalMap(ip2, tempDataMap);
                // 对端端口
                Cell cell8 = row.getCell(8);
                CollectInterfaces interface2 = opticalMap2.get(cell8.getStringCellValue());
                // 对端误码数
                long error2 = getError(ip2, interface2);
                Cell cell9 = row.getCell(9);
                cell9.setCellValue(error2 + "");

                // 对端是否正常
                Cell cell10 = row.getCell(10);
                cell10.setCellValue("Y");
                if (error2 > 0) {
                    cell10.setCellValue("N");
                }
            } else {
                if (LogInputUtils.inputError(ServerTypeEnum.WEB_OPTICAL_EXPORT)) {
                    log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_OPTICAL_EXPORT, ErrorCodeEnum.COMMON_ASSET_LOSE, ip2, "ip对应设备不存在"));
                }
            }
        }
    }

    private long getError(String ip, CollectInterfaces interfaces) {
        long error = 0L;
        String assetId = ipIdMap.get(ip);
        if (StrUtil.isNotEmpty(assetId)) {
            Object o2 = redisService.get("_" + assetId + "_error_code");
            if (Objects.nonNull(interfaces) && Objects.nonNull(o2)) {
                error = interfaces.getErrorCodeIn() + interfaces.getErrorCodeOut();
                error = new BigDecimal(o2.toString()).subtract(new BigDecimal(error)).longValue();
            }
        }
        return error;
    }

    private Map<String, CollectInterfaces> getOpticalMap(String ip, Map<String, Map<String, CollectInterfaces>> tempDataMap) {

        Map<String, CollectInterfaces> map = tempDataMap.get(ip);
        if (CollectionUtil.isNotEmpty(map)) {
            return map;
        }

        Map<String, CollectInterfaces> opticalVoMap = new HashMap<>(64);
        Asset asset = assetService.findOneByIp(ip);
        if (Objects.isNull(asset)) {
            if (LogInputUtils.inputError(ServerTypeEnum.WEB_OPTICAL_EXPORT)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.WEB_OPTICAL_EXPORT, ErrorCodeEnum.COMMON_ASSET_LOSE, ip, "ip对应设备不存在"));
            }
        } else {
            List<CollectInterfaces> list = getList(asset.getId());
            for (CollectInterfaces item : list) {
                opticalVoMap.put(item.getPortName(), item);
            }
            tempDataMap.put(ip, opticalVoMap);
            ipIdMap.put(ip, asset.getId());
        }

        return opticalVoMap;
    }

    private List<CollectInterfaces> getList(String assetId) {
        QueryWrapper<CollectInterfaces> query = Wrappers.query();
        query.select("collect_code");
        query.eq("asset_id", assetId);
        query.groupBy("collect_code");
        query.orderByDesc("collect_code");
        List<CollectInterfaces> list = collectInterfacesService.list(query);
        if (CollectionUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        query = Wrappers.query();
        query.eq("collect_code", list.get(0).getCollectCode());
        return collectInterfacesService.list(query);
    }


    private boolean intRange(int current, int min, int max) {
        return Math.max(current, min) == Math.min(current, max);
    }

}
