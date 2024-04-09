package com.jcca.admin.system.controller;

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
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.asset.utils.NullFieldException;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @ Author：sophia
 * @ Date：Created in 9:47 2021/8/12
 * @ Description:
 */
@Slf4j
@Controller
@RequestMapping("/system/importCabinet")
public class ImportCabinetController {

    @Resource
    private CabinetTaskService cabinetTaskService;
    @Resource
    private ImportCabinetService importCabinetService;
    @Resource
    private TemplateImportCabinet templateImportCabinet;
//    @Resource
//    private CodeService codeServ;

    /**
     * 进度条数据
     *
     * @return
     * @Author: sophia
     */
    @RequestMapping("/pmgressBar")
    @ResponseBody
    public CabinetTask pmgressBar(HttpServletResponse response) {
        String taskId = cabinetTaskService.getLastOneId();

        if (StrUtil.isEmpty(taskId)) {
            CabinetTask cabinetTask = new CabinetTask();
            cabinetTask.setBar("0%");
            return cabinetTask;
        }
        CabinetTask cabinetTask = cabinetTaskService.getById(taskId);
        if ((cabinetTask.getSuccess() + cabinetTask.getFail()) == 0 || cabinetTask.getCount() == 0) {
            cabinetTask.setBar("0%");
            return cabinetTask;
        }

        NumberFormat num = NumberFormat.getPercentInstance();
        String bar = num.format((double) (cabinetTask.getSuccess() + cabinetTask.getFail()) / cabinetTask.getCount());
        cabinetTask.setBar(bar);
        return cabinetTask;
    }


    /**
     * 机柜列表
     *
     * @return
     * @Author: sophia
     */
    @GetMapping("/index")
    @RequiresPermissions("system:importCabinet:index")
    public String index(Model model, ImportCabinet importCabinet, Integer size, Integer page) {
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

        List<ImportCabinet> records = iPage.getRecords();
        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);
        return "/system/importCabinet/index";
    }


    /**
     * 查询任务运行状态
     *
     * @return
     * @Author: sophia
     */

    @GetMapping("/importStatus")
    @ApiOperation(value = "查询导入状态 true=runing false=stop")
    public ResultVo importStatus() {

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
    @ResponseBody
    @ActionLog(name = "导入机柜文件", title = "机柜管理", key = LogTypeConstant.UPLOAD)
    public ResultVo templateImportCabinet(@RequestParam("file") MultipartFile file) {
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
            if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_IMPORT_CABINET)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_IMPORT_CABINET, ErrorCodeEnum.SYSTEM_IMPORT_CABINET, "", "机柜导入失败:" + e.getMessage()));
            }
            cabinetTask.setLog(e.getMessage());
            cabinetTask.setStatus(0);
            cabinetTaskService.saveOrUpdate(cabinetTask);
            return ResultVoUtil.error(e.getMessage());

        } catch (IOException e) {
            if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_IMPORT_CABINET)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_IMPORT_CABINET, ErrorCodeEnum.SYSTEM_IMPORT_CABINET, "", "机柜导入失败:" + e.getMessage()));
            }
            cabinetTask.setLog("建立IO链接失败" + e.getMessage());
            cabinetTask.setStatus(0);
            cabinetTaskService.saveOrUpdate(cabinetTask);
            return ResultVoUtil.error(e.getMessage());

        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.SYSTEM_IMPORT_CABINET)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.SYSTEM_IMPORT_CABINET, ErrorCodeEnum.SYSTEM_IMPORT_CABINET, "", "机柜导入失败:" + e.getMessage()));
            }
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
    public String index(Model model, CabinetTask cabinetTask, Integer size, Integer page) {

        /*获取模板列表*/
        IPage iPage = PagePlugin.startPage(page, size);

        QueryWrapper<CabinetTask> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("CREATE_TIME");

        iPage = cabinetTaskService.page(iPage, wrapper);

        List<CabinetTask> records = iPage.getRecords();
        // 封装数据
        model.addAttribute("list", records);
        model.addAttribute("page", iPage);
        return "/system/importCabinet/log";
    }
}
