package com.jcca.web2.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jcca.admin.system.entity.CabinetTask;
import com.jcca.admin.system.entity.ImportCabinet;
import com.jcca.admin.system.service.CabinetTaskService;
import com.jcca.admin.system.service.ImportCabinetService;
import com.jcca.admin.system.service.impl.TemplateImportCabinet;
import com.jcca.admin.system.util.TemplateExportUtil;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.asset.utils.NullFieldException;
import com.jcca.web2.adapter.cabinet.CabinetAssetInfoHeader;
import com.jcca.web2.dto.CabinetBaseInfoQueryDto;
import com.jcca.web2.vo.CabinetAssetInfoVo;
import com.jcca.web2.vo.CabinetBaseInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * @description: 机柜相关接口V2版本
 * @author: Lvyp
 * @create: 2023/10/24 10:09
 */
@RestController
@RequestMapping("/api/v2/cabinet")
@Api(tags = "机柜相关接口V2")
public class CabinetControllerV2 {

    @Resource
    private CabinetService cabinetServ;
    @Resource
    private AssetService assetService;
    @Resource
    private CabinetAssetInfoHeader cabinetAssetHeader;
    @Resource
    private CabinetTaskService cabinetTaskService;
    @Resource
    private ImportCabinetService importCabinetService;
    @Resource
    private TemplateImportCabinet templateImportCabinet;


    @GetMapping("/queryBaseInfo")
    @ApiOperation("查询机柜基础信息")
    public ResultVo<Object> queryBaseInfo(CabinetBaseInfoQueryDto query) {
        CabinetBaseInfoVo cabinetBaseInfoV2 = cabinetServ.findCabinetBaseInfoV2(query);
        return ResultVoUtil.success(cabinetBaseInfoV2);
    }

    @GetMapping("/queryAssetInfo")
    @ApiOperation("查询机柜内设备信息")
    public ResultVo<Object> queryAssetInfo(String assetId) {
        CabinetAssetInfoVo vo = assetService.findCabinetAssetInfoV2(assetId);

        return ResultVoUtil.success(cabinetAssetHeader.fattenCabinetInfo(vo));
    }

    @PostMapping("/list")
    @ApiOperation("获取机柜列表")
    public ResultVo<Object> getList(@RequestBody Cabinet cabinet) {

        List<Cabinet> list = cabinetServ.getCabinetListV2(cabinet);

        return ResultVoUtil.success(list);
    }

    @PostMapping("/save")
    @RequiresPermissions("api:v2:cabinet:save")
    @ApiOperation("保存机柜")
    @ActionLog(name = "保存机柜", title = "机柜管理", key = LogTypeConstant.ADD)
    public ResultVo<String> save(@Validated @RequestBody Cabinet cabinet) {

        cabinetServ.saveCabinetV2(cabinet);

        return ResultVoUtil.SAVE_SUCCESS;
    }

    @PostMapping("/del/{id}")
    @RequiresPermissions("api:v2:cabinet:del")
    @ApiOperation("删除机柜")
    @ActionLog(name = "删除机柜", title = "机柜管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<Object> del(@PathVariable String id) {

        cabinetServ.delById(id);

        return ResultVoUtil.success();
    }


    //==================================================以下为机柜导入================================================================

    /**
     * 进度条数据
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/pmgressBar")
    public ResultVo<Object> pmgressBar() {
        String taskId = cabinetTaskService.getLastOneId();

        if (StrUtil.isEmpty(taskId)) {
            CabinetTask cabinetTask = new CabinetTask();
            cabinetTask.setBar("0");
            return ResultVoUtil.success(cabinetTask);
        }
        CabinetTask cabinetTask = cabinetTaskService.getById(taskId);
        if ((cabinetTask.getSuccess() + cabinetTask.getFail()) == 0 || cabinetTask.getCount() == 0) {
            cabinetTask.setBar("0");
            return ResultVoUtil.success(cabinetTask);
        }
        BigDecimal bar = new BigDecimal(cabinetTask.getSuccess()).add(new BigDecimal(cabinetTask.getFail())).divide(new BigDecimal(cabinetTask.getCount()), 2, BigDecimal.ROUND_HALF_UP);

        cabinetTask.setBar(bar.toString());
        return ResultVoUtil.success(cabinetTask);
    }


    /**
     * 机柜列表
     *
     * @return
     * @Author: sophia
     */
    @PostMapping("/index")
    public ResultVo<Object> index(@RequestBody ImportCabinet importCabinet, Integer size, Integer page) {
        /*获取模板列表*/
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<ImportCabinet> wrapper = new QueryWrapper<>();

        if (StringUtils.isNotEmpty(importCabinet.getRoomName())) {
            wrapper.like("ROOM_NAME", importCabinet.getRoomName());
        }
        if (StringUtils.isNotEmpty(importCabinet.getStatus())) {
            wrapper.like("STATUS", importCabinet.getStatus());
        }
        wrapper.orderByDesc("CREATE_TIME");
        iPage = importCabinetService.page(iPage, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("total", iPage.getTotal());
        map.put("records", iPage.getRecords());

        return ResultVoUtil.success(map);
    }


    /**
     * 查询任务运行状态
     *
     * @return
     * @Author: sophia
     */

    @GetMapping("/importStatus")
    public ResultVo<Object> importStatus() {

        String taskId = cabinetTaskService.getLastOneId();
        if (StrUtil.isEmpty(taskId)) {
            return ResultVoUtil.success(false);
        }
        if (cabinetTaskService.getStatusById(taskId) == 1) {
            return ResultVoUtil.success(true);
        }

        return ResultVoUtil.success(false);
    }


    /**
     * 下载文件
     */
    @GetMapping("/download")
    @ActionLog(name = "下载机柜模板文件", title = "机柜管理", key = LogTypeConstant.DOWNLOAD)
    public void exportExcel(HttpServletResponse response) {

        try {
            Map<String, String> cabinetTemplate = new LinkedHashMap<String, String>();
            cabinetTemplate.put("组织名称", "orgName");
            cabinetTemplate.put("机房名称", "roomName");
            cabinetTemplate.put("机柜名称", "name");
            cabinetTemplate.put("机柜编号", "code");
            cabinetTemplate.put("横向索引", "rowIndex");
            cabinetTemplate.put("纵向索引", "columnIndex");
            cabinetTemplate.put("备注", "remark");
            cabinetTemplate.put("识别号", "qrCodeNum");

            SXSSFWorkbook excel = TemplateExportUtil.createCabinetExcel(cabinetTemplate);
            DispatchRecordExcelUtil.responseBody(excel, response, "CabinetTemplate");

        } catch (Exception e) {

        }
    }


    /**
     * 模板开始导入
     *
     * @return
     * @Author: sophia
     */
    @PostMapping("/cabinetImport")
    @ActionLog(name = "导入机柜文件", title = "机柜管理", key = LogTypeConstant.UPLOAD)
    @RequiresPermissions("api:v2:cabinet:cabinetImport")
    public ResultVo<Object> templateImportCabinet(@RequestParam("file") MultipartFile file) {
        //清空历史错误数据  并建立一个新的机柜导入任务
        importCabinetService.deleteAll();
        CabinetTask cabinetTask = new CabinetTask();
        cabinetTask.setId(MyIdUtil.getId());
        cabinetTask.setSuccess(0);
        cabinetTask.setFail(0);
        cabinetTask.setStatus(1);//设置程序运行状态

        try {
            if (Objects.isNull(file)) {
                throw new NullFieldException("模板文件不能为空,请填写数据");
            }
            InputStream in = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(in);

            //判断模板是否含有必需字段
            List<Object> titleList = reader.readRow(1);

            Map<String, String> CabinetMap = DictUtil.value("CABINET_TEMPLATE");
            if (!titleList.containsAll(CabinetMap.keySet())) {
                throw new NullFieldException("模板不符合规范 请重新下载模板");
            }

            //判断数据为空
            List<Map<String, Object>> importCabinets = reader.read(1, 2, 2147483647);
            List<ImportCabinet> importCabinetList = reader.read(1, 2, ImportCabinet.class);
            if (importCabinets.isEmpty()) {
                throw new NullFieldException("模板不可为空,请填写数据");
            }

            cabinetTask.setCount(importCabinets.size());
            cabinetTask.setLog("成功运行");
            cabinetTaskService.saveOrUpdate(cabinetTask);

            //异步进行数据导入(删除异步)
            templateImportCabinet.forImportCabniet(importCabinets, importCabinetList, cabinetTask);
            return ResultVoUtil.success("任务成功开始运行");

        } catch (NullFieldException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CABINET_IMPORT, "机柜导入字段空异常", e);

            cabinetTask.setLog(e.getMessage());
            cabinetTask.setStatus(0);
            cabinetTaskService.saveOrUpdate(cabinetTask);
            return ResultVoUtil.error(e.getMessage());

        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CABINET_IMPORT, "机柜导入IO异常", e);

            cabinetTask.setLog("建立IO链接失败" + e.getMessage());
            cabinetTask.setStatus(0);
            cabinetTaskService.saveOrUpdate(cabinetTask);
            return ResultVoUtil.error(e.getMessage());

        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.CABINET_IMPORT, "机柜导入异常", e);

            cabinetTask.setLog("导入任务失败" + e.getMessage());
            cabinetTask.setStatus(0);
            cabinetTaskService.saveOrUpdate(cabinetTask);
            return ResultVoUtil.error(e.getMessage());
        }
    }

    /**
     * 任务日志页面
     */
    @GetMapping("/log")
    @ActionLog(name = "查看导入机柜任务日志", title = "机柜管理", key = LogTypeConstant.QUERY)
    public ResultVo<Object> index(Integer size, Integer page) {

        /*获取模板列表*/
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<CabinetTask> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("CREATE_TIME");

        iPage = cabinetTaskService.page(iPage, wrapper);

        List<CabinetTask> records = iPage.getRecords();
        return ResultVoUtil.success(records);
    }

}
