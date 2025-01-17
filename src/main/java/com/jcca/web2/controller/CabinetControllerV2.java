package com.jcca.web2.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.CabinetTask;
import com.jcca.admin.system.entity.ImportCabinet;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.CabinetTaskService;
import com.jcca.admin.system.service.ImportCabinetService;
import com.jcca.admin.system.service.SysOrgService;
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
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.utils.DispatchRecordExcelUtil;
import com.jcca.web.asset.utils.NullFieldException;
import com.jcca.web.asset.vo.RoomVo;
import com.jcca.web2.adapter.cabinet.CabinetAssetInfoHeader;
import com.jcca.web2.dto.CabinetBaseInfoQueryDto;
import com.jcca.web2.util.TwoExport;
import com.jcca.web2.vo.CabinetAssetInfoVo;
import com.jcca.web2.vo.CabinetBaseInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
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
    @Resource
    private SysOrgService orgService;
    @Resource
    private RoomService roomService;


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

        return cabinetServ.delById(id);
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
    public ResultVo<Object> index(@RequestBody ImportCabinet importCabinet) {
        Integer page = importCabinet.getPage();
        Integer size = importCabinet.getSize();
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
            cabinetTemplate.put("*组织名称(16个汉字)", "orgName");
            cabinetTemplate.put("*机房名称(16个汉字)", "roomName");
            cabinetTemplate.put("*机柜名称(16个汉字)", "name");
            cabinetTemplate.put("*机柜编号(不可重复)", "code");
            cabinetTemplate.put("*横向索引", "rowIndex");
            cabinetTemplate.put("*纵向索引", "columnIndex");
            cabinetTemplate.put("备注", "remark");
            cabinetTemplate.put("识别号", "qrCodeNum");

            SXSSFWorkbook excel = TemplateExportUtil.createCabinetExcel(cabinetTemplate);
            DispatchRecordExcelUtil.responseBody(excel, response, "CabinetTemplate");

        } catch (Exception e) {

        }
    }

    @GetMapping("/exportErrorCabinet")
    @ActionLog(name = "导出失败机柜列表", title = "组织管理", key = LogTypeConstant.DOWNLOAD)
    public void exportErrorCabinet(HttpServletResponse response) {
        try {
            Map<String, String> cabinetTemplate = new LinkedHashMap<>();
            cabinetTemplate.put("组织名称", "orgName");
            cabinetTemplate.put("机房名称", "roomName");
            cabinetTemplate.put("机柜名称", "name");
            cabinetTemplate.put("机柜编号", "code");
            cabinetTemplate.put("横向索引", "rowIndex");
            cabinetTemplate.put("纵向索引", "columnIndex");
            cabinetTemplate.put("备注", "remark");
            cabinetTemplate.put("识别号", "qrCodeNum");
            cabinetTemplate.put("错误信息", "erroLog");

            QueryWrapper<ImportCabinet> query = Wrappers.query();
            query.eq("STATUS", "1");
            List<ImportCabinet> list = importCabinetService.list(query);
            SXSSFWorkbook excel = TemplateExportUtil.createErrorCabinetExcel(cabinetTemplate, list);
            DispatchRecordExcelUtil.responseBody(excel, response, "ErrorCabinetLog");

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


    @GetMapping("/download2")
    public void Cascade(HttpServletResponse response) {
        // 创建一个excel
        ClassPathResource resource = new ClassPathResource("templates/system/export/CabinetTemplate.xlsx");
        XSSFWorkbook workbook = null;
        try {
            InputStream is = resource.getInputStream();
            workbook = new XSSFWorkbook(is);
        } catch (IOException e) {
            return;
        }
        XSSFSheet sheet = workbook.getSheet("Sheet0");
        //一级选项，放在列表里
        QueryWrapper<SysOrg> qw = new QueryWrapper<>();
        qw.select("ID", "TITLE");
        qw.in("TYPE", Arrays.asList(2, 4));
        qw.eq("STATUS", 1);
        qw.orderByAsc("TITLE");
        List<SysOrg> list = orgService.list(qw);
        Map<String, String[]> areaMap = new HashMap<>();
        String[] orgArr = new String[35];
        for (int i = 0; i < 35; i++) {
            SysOrg org = list.get(i);
            orgArr[i] = org.getTitle();
            String[] roomNames = roomService.listByOrgId(org.getId()).stream().map(RoomVo::getName).toArray(String[]::new);
            areaMap.put(org.getTitle(), roomNames);
        }
/*        String[] orgArr = {"线上","线下"};
        //依次列出各省的市、各市的县
        String[] cityJiangSu = {"线上","招标网站","门户网络媒体","移动社交媒体","移动社交媒体","搜索引擎","平面媒体","户外媒体","其他"};
        String[] cityAnHui = {"招标网站","行业峰会/论坛/沙龙","产业级市场活动","大区/公路港市场活动","本部门市场活动","存量客户上下游","政府关系网络","个人关系网络","传化生态业务","其他"};
        areaMap.put("线上", cityJiangSu);
        areaMap.put("线下",cityAnHui);*/

        //创建一个专门用来存放地区信息的隐藏sheet页
        //因此也不能在现实页之前创建，否则无法隐藏。
        XSSFSheet hideSheet = workbook.createSheet("area");
        //这一行作用是将此sheet隐藏，功能未完成时注释此行,可以查看隐藏sheet中信息是否正确
        workbook.setSheetHidden(workbook.getSheetIndex(hideSheet), true);

        int rowId = 0;
        // 设置第一行，存省的信息
        Row provinceRow = hideSheet.createRow(rowId++);
        provinceRow.createCell(0).setCellValue("组织");
        for (int i = 0; i < orgArr.length; i++) {
            Cell provinceCell = provinceRow.createCell(i + 1);
            provinceCell.setCellValue(orgArr[i]);
        }
        // 将具体的数据写入到每一行中，行开头为父级区域，后面是子区域。
        for (int i = 0; i < orgArr.length; i++) {
            String key = orgArr[i];
            String[] son = areaMap.get(key);
            Row row1 = hideSheet.createRow(rowId++);
            row1.createCell(0).setCellValue(key);
            for (int j = 0; j < son.length; j++) {
                Cell cell0 = row1.createCell(j + 1);
                cell0.setCellValue(son[j]);
            }

            // 添加名称管理器
            String range = TwoExport.getRange(1, rowId, son.length);
            Name name = workbook.createName();
            //key不可重复
            name.setNameName(key);
            String formula = "area!" + range;
            name.setRefersToFormula(formula);
        }

        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        // 省规则
        DataValidationConstraint provConstraint = dvHelper.createExplicitListConstraint(orgArr);
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList provRangeAddressList = new CellRangeAddressList(2, 202, 0, 0);
        DataValidation provinceDataValidation = dvHelper.createValidation(provConstraint, provRangeAddressList);
        //验证
        provinceDataValidation.createErrorBox("error", "请选择正确的组织");
        provinceDataValidation.setShowErrorBox(true);
        provinceDataValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(provinceDataValidation);

        //对前204行设置有效性
        for (int i = 3; i < 204; i++) {
            TwoExport.setDataValidation("A", sheet, i, 2);
        }
        FileOutputStream os = null;
        try {
            String fileName = URLEncoder.encode("机柜导入模板" + ".xlsx", "UTF-8");
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            workbook.close();
        } catch (Exception e) {
        }
    }
}
