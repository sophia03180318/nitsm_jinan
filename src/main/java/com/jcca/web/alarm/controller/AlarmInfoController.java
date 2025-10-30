package com.jcca.web.alarm.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.PageBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.bean.constant.AlarmToRecordConst;
import com.jcca.common.config.mybatisplus.PagePlugin;
import com.jcca.common.enums.*;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.annotation.DevLog;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.dataProcessing.manager.cache.CacheEvent;
import com.jcca.dataProcessing.manager.cache.CacheOptEnum;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.poi.hssf.usermodel.HSSFCellStyle;
import com.jcca.poi.ss.usermodel.CellStyle;
import com.jcca.poi.ss.util.CellRangeAddress;
import com.jcca.poi.xssf.streaming.SXSSFCell;
import com.jcca.poi.xssf.streaming.SXSSFRow;
import com.jcca.poi.xssf.streaming.SXSSFSheet;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.web.alarm.controller.bean.*;
import com.jcca.web.alarm.dao.bean.QueryExportByTypeReq;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.alarm.service.data.ExportAlarmInfo;
import com.jcca.web.alarm.service.data.ExportAlarmReportBean;
import com.jcca.web.alarm.vo.*;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.utils.NetworkRecordExcelUtil;
import com.jcca.web.broken.controller.bean.BrokenRecordWord;
import com.jcca.web.broken.service.BrokenRecordService;
import com.jcca.web.broken.service.BrokenRecordWordService;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.db.entity.ManageDb;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web2.dto.DialogsAlarmListDto;
import com.jcca.web2.vo.DialogsAlarmListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警信息
 *
 * @author Lvyp
 */
@Api(tags = "告警信息接口")
@Slf4j
@RestController
@RequestMapping("/api/alarm/info")
public class AlarmInfoController extends ListenerManager {

    private static final String exportTypeFlag = "type";
    private static final String exportTabOneTypeFlag = "one";

    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private BrokenRecordService brokenRecordService;
    @Resource
    private ManageDbService manageDbService;
    @Resource
    private AlarmRepositoryService alarmRepoServ;
    @Resource
    private SysOrgService orgService;
    @Resource
    private BrokenRecordWordService brokenRecordWordService;
    @Resource
    private RedisService redisServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private SysModuleConfigService configService;

    @ApiOperation(value = "删除告警")
    @RequiresPermissions({"api:alarm:info:removeAlarm"})
    @PostMapping("/removeAlarm")
    @DevLog(title = "告警管理", name = "删除告警", dev = DevLogConstant.ALARM_DEL_SINGLE, key = LogTypeConstant.DEV)
    ResultVo<?> removeAlarm(@RequestBody String reqStr) {
        JSONObject reqJson = JSONUtil.parseObj(reqStr);
        String id = reqJson.getStr("id");
        if (StrUtil.isEmpty(id)) {
            return ResultVoUtil.error("前端问题，缺少ID参数");
        }

        alarmInfoService.delAlarm(id);

        return ResultVoUtil.success("处理成功");
    }

    @ApiOperation(value = "导出告警报告")
//    @RequiresPermissions({"api:alarm:export:report"})
    @PostMapping("/export/report")
    @ActionLog(name = "导出告警报告", title = "告警管理", key = LogTypeConstant.DOWNLOAD)
    ResultVo<?> exportReport(@RequestBody ExportAlarmReportReq req) {
        if (Objects.isNull(req.getStartDate()) || Objects.isNull(req.getEndDate())) {
            return ResultVoUtil.error("请指定生成范围");
        }
        QueryExportByTypeReq queryByTypeReq = new QueryExportByTypeReq();
        queryByTypeReq.setStartDate(DateUtil.format(req.getStartDate(), "yyyy-MM-dd HH:mm:ss"));
        queryByTypeReq.setEndDate(DateUtil.format(req.getEndDate(), "yyyy-MM-dd HH:mm:ss"));
        queryByTypeReq.setAssetMode(req.getAssetMode());

        String exportType = req.getExportType();
        List<List<String>> timeConfig = req.getTimeConfig();

        Map<String, List<ExportAlarmReportBean>> excelDataResult = new HashMap<String, List<ExportAlarmReportBean>>();

        if (exportTypeFlag.equals(exportType)) {
            // 按照类型筛选
            List<Byte> typeDetail = req.getTypeDetail();
            for (Byte type : typeDetail) {
                queryByTypeReq.setType(type);
                List<ExportAlarmReportBean> reportListByOrgType = alarmInfoService
                        .getReportListByOrgType(queryByTypeReq);
                String title = OrgTypeEnum.getTitleByCode(type);
                // 一个Key就是一个shell页
                excelDataResult.put(title, reportListByOrgType);
            }
        } else {
            // 按照组织
            if (Objects.isNull(req.getOrgDetail()) || req.getOrgDetail().isEmpty()) {
                return ResultVoUtil.error("请指定生成组织范围");
            }

            queryByTypeReq.setOrgIds(req.getOrgDetail());
            List<ExportAlarmReportBean> reportData = alarmInfoService.getReportListByOrgIds(queryByTypeReq);
            // 根据组织分组
            excelDataResult = reportData.stream().collect(Collectors.groupingBy(ExportAlarmReportBean::getOrgName));
        }


        Collection<List<ExportAlarmReportBean>> values = excelDataResult.values();
        List<ExportAlarmReportBean> result = new ArrayList<ExportAlarmReportBean>();
        for (List<ExportAlarmReportBean> list : values) {
            result.addAll(list);
        }
        if (result.isEmpty()) {
            return ResultVoUtil.error("未查询到符合条件的告警记录");
        }

        ExportAlarmReportCacheBean cacheBean = new ExportAlarmReportCacheBean();
        cacheBean.setExcelDataResult(excelDataResult);
        cacheBean.setTimeConfig(timeConfig);
        cacheBean.setTabType(req.getTabType());

        String key = "EXPORT_KEY:" + IdUtil.fastUUID();
        redisServ.set(key, cacheBean, 300L);

        return ResultVoUtil.success("数据已生成，请在五分钟内凭Key下载", key);
    }


    @ApiOperation(value = "下载告警报告校验")
    @GetMapping("/export/download/verify/{key}")
    ResultVo downloadVerify(@PathVariable("key") String key) {
        FileUtil.createTmpPath();
        ExportAlarmReportCacheBean cacheBean = (ExportAlarmReportCacheBean) redisServ.get(key);
        if (Objects.isNull(cacheBean)) {
            return ResultVoUtil.error("下载凭证已失效请重新下载");
        }
        String tabType = cacheBean.getTabType();
        Map<String, List<ExportAlarmReportBean>> excelDataResult = cacheBean.getExcelDataResult();

        if (exportTabOneTypeFlag.equals(tabType)) {
            Collection<List<ExportAlarmReportBean>> values = excelDataResult.values();

            List<ExportAlarmReportBean> result = new ArrayList<ExportAlarmReportBean>();
            for (List<ExportAlarmReportBean> list : values) {
                result.addAll(list);
            }

            if (result.isEmpty()) {
                return ResultVoUtil.error("未查询到符合条件的告警记录");
            } else {
                return ResultVoUtil.success();
            }

        }
        return ResultVoUtil.success();

    }

    @ApiOperation(value = "下载告警报告")
//    @RequiresPermissions({"api:alarm:export:report"})
    @GetMapping("/export/download/{key}")
    @ActionLog(name = "下载告警报告", title = "告警管理", key = LogTypeConstant.DOWNLOAD)
    void download(HttpServletResponse response, @PathVariable("key") String key) {
        ExportAlarmReportCacheBean cacheBean = (ExportAlarmReportCacheBean) redisServ.get(key);
        if (Objects.isNull(cacheBean)) {
            if (LogInputUtils.inputError(ServerTypeEnum.ALARM_INFO)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.ALARM_INFO, ErrorCodeEnum.COMMON_KEY_TIMEOUT, "",
                        "下载告警报告传入的KEY在redis中已经失效，key:" + key));
            }
            return;
        }
        String tabType = cacheBean.getTabType();
        List<List<String>> timeConfig = cacheBean.getTimeConfig();
        Map<String, List<ExportAlarmReportBean>> excelDataResult = cacheBean.getExcelDataResult();

        if (exportTabOneTypeFlag.equals(tabType)) {
            Collection<List<ExportAlarmReportBean>> values = excelDataResult.values();

            List<ExportAlarmReportBean> result = new ArrayList<ExportAlarmReportBean>();
            for (List<ExportAlarmReportBean> list : values) {
                result.addAll(list);
            }
            excelDataResult = new HashMap<String, List<ExportAlarmReportBean>>();
            excelDataResult.put("告警分析数据", result);
        }

        // 生成excel
        Set<String> keySet = excelDataResult.keySet();

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        CellRangeAddress titleRange = new CellRangeAddress(0, 1, 0, 9);
        CellRangeAddress countRange = new CellRangeAddress(2, 5, 0, 9);

        CellStyle titleCellStyle = AppExcelUtil.createCellStyle(workbook, true, true, true, true);
        titleCellStyle.setFont(NetworkRecordExcelUtil.getFont(workbook, (short) 18, true));
        titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
        titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
        titleCellStyle.setWrapText(true);

        CellStyle countCellStyle = AppExcelUtil.createCellStyle(workbook, true, true, true, true);
        countCellStyle.setFont(NetworkRecordExcelUtil.getFont(workbook, (short) 13, true));
        countCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
        countCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
        countCellStyle.setWrapText(true);

        CellStyle docCellStyle = AppExcelUtil.createCellStyle(workbook, true, true, true, true);
        docCellStyle.setFont(NetworkRecordExcelUtil.getFont(workbook, (short) 13, true));
        docCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
        docCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中
        //自动换行
        docCellStyle.setWrapText(true);


        for (String shell : keySet) {
            SXSSFSheet sheet = workbook.createSheet(shell);
            sheet.setColumnWidth(0, (int) 10 * 300);
            sheet.setColumnWidth(1, (int) 25.7 * 300);
            sheet.setColumnWidth(2, (int) 25.7 * 300);
            sheet.setColumnWidth(3, (int) 25.7 * 300);
            sheet.setColumnWidth(4, (int) 25.7 * 300);
            sheet.setColumnWidth(5, (int) 25.7 * 300);
            sheet.setColumnWidth(6, (int) 25.7 * 300);
            sheet.setColumnWidth(7, (int) 45.7 * 300);
            sheet.setColumnWidth(8, (int) 45.7 * 300);
            sheet.setColumnWidth(9, (int) 45.7 * 300);

            sheet.setDefaultRowHeight((short) 500);

            sheet.addMergedRegion(titleRange);
            sheet.addMergedRegion(countRange);

            SXSSFRow row = sheet.createRow(0);
            SXSSFCell cell = row.createCell(0);

            cell.setCellValue("综合维护平台告警分析报告");

            SXSSFRow row2 = sheet.createRow(2);
            SXSSFCell row2cell = row2.createCell(0);
            row2cell.setCellValue("查看综合维护平台系统发现从___月___号到___月___号_____告警实际上送了____条,分析如下：");

            SXSSFRow row3 = sheet.createRow(6);

            AppExcelUtil.setCellMsg(row3, 0, "序号");
            AppExcelUtil.setCellMsg(row3, 1, "组织名称");
            AppExcelUtil.setCellMsg(row3, 2, "资产名称");
            AppExcelUtil.setCellMsg(row3, 3, "资产IP");
            AppExcelUtil.setCellMsg(row3, 4, "告警类型");
            AppExcelUtil.setCellMsg(row3, 5, "告警数量");
            AppExcelUtil.setCellMsg(row3, 6, "告警级别");
            AppExcelUtil.setCellMsg(row3, 7, "告警内容");
            AppExcelUtil.setCellMsg(row3, 8, "告警原因");
            AppExcelUtil.setCellMsg(row3, 9, "备注");

            int index = 1;
            //记录当前最大行
            int nextRowIndexTmp = 7;
            //资产起始行
            int nextRowIndex = 7;

            List<ExportAlarmReportBean> respData = excelDataResult.get(shell);
            for (ExportAlarmReportBean reportBeanItem : respData) {
                SXSSFRow rowN = sheet.createRow(nextRowIndex);
                boolean onces = true;

                AppExcelUtil.setCellMsg(rowN, 0, index + "");
                AppExcelUtil.setCellMsg(rowN, 1, reportBeanItem.getOrgName());
                AppExcelUtil.setCellMsg(rowN, 2, reportBeanItem.getAssetName());
                AppExcelUtil.setCellMsg(rowN, 3, reportBeanItem.getAssetIp());

                List<ExportAlarmInfo> alarmInfos = reportBeanItem.getAlarmInfos();
                //根据类型在分组
                Map<String, List<ExportAlarmInfo>> resultGroupMap = alarmInfos.stream().collect(Collectors.groupingBy(ExportAlarmInfo::getAlarmTitle));
                Set<String> resultGroupMapKey = resultGroupMap.keySet();

                for (String alarmTitle : resultGroupMapKey) {
                    int nextRowIndexN = nextRowIndexTmp;

                    List<ExportAlarmInfo> list = resultGroupMap.get(alarmTitle);

                    List<ExportAlarmInfo> needWriterList = new ArrayList<ExportAlarmInfo>();
                    for (ExportAlarmInfo alarmInfo : list) {
                        //根据时间过滤
                        boolean timeFilter = timeFilter(timeConfig, alarmInfo.getOccurTime());
                        if (!timeFilter) {
                            continue;
                        }
                        needWriterList.add(alarmInfo);
                    }

                    for (ExportAlarmInfo alarmInfo : needWriterList) {
                        if (onces) {
                            onces = false;
                            AppExcelUtil.setCellMsg(rowN, 6, alarmInfo.getAlarmLevel());
                            AppExcelUtil.setCellMsg(rowN, 7, alarmInfo.getAlarmContent());
                            AppExcelUtil.setCellMsg(rowN, 8, alarmInfo.getRemark());
                            AppExcelUtil.setCellMsg(rowN, 9, "");

                            AppExcelUtil.setCellMsg(rowN, 4, alarmTitle);
                            AppExcelUtil.setCellMsg(rowN, 5, needWriterList.size() + "");
                        } else {
                            SXSSFRow rowNext = sheet.createRow(nextRowIndexN);
                            AppExcelUtil.setCellMsg(rowNext, 6, alarmInfo.getAlarmLevel());
                            AppExcelUtil.setCellMsg(rowNext, 7, alarmInfo.getAlarmContent());
                            AppExcelUtil.setCellMsg(rowNext, 8, alarmInfo.getRemark());
                            AppExcelUtil.setCellMsg(rowNext, 9, "");

                            AppExcelUtil.setCellMsg(rowNext, 4, alarmTitle);
                            AppExcelUtil.setCellMsg(rowNext, 5, needWriterList.size() + "");
                        }

                        nextRowIndexN++;
                    }


                    int i = nextRowIndexTmp;
                    if (i < nextRowIndexN - 1) {
                        CellRangeAddress dynamicRange4 = new CellRangeAddress(i, nextRowIndexN - 1, 4, 4);
                        CellRangeAddress dynamicRange5 = new CellRangeAddress(i, nextRowIndexN - 1, 5, 5);
                        sheet.addMergedRegion(dynamicRange4);
                        sheet.addMergedRegion(dynamicRange5);
                    }

                    nextRowIndexTmp = nextRowIndexN;
                }

                //合并规则 处理前4项
                if (nextRowIndex < nextRowIndexTmp - 1) {
                    CellRangeAddress dynamicRange0 = new CellRangeAddress(nextRowIndex, nextRowIndexTmp - 1, 0, 0);
                    CellRangeAddress dynamicRange1 = new CellRangeAddress(nextRowIndex, nextRowIndexTmp - 1, 1, 1);
                    CellRangeAddress dynamicRange2 = new CellRangeAddress(nextRowIndex, nextRowIndexTmp - 1, 2, 2);
                    CellRangeAddress dynamicRange3 = new CellRangeAddress(nextRowIndex, nextRowIndexTmp - 1, 3, 3);
                    sheet.addMergedRegion(dynamicRange0);
                    sheet.addMergedRegion(dynamicRange1);
                    sheet.addMergedRegion(dynamicRange2);
                    sheet.addMergedRegion(dynamicRange3);
                }

                nextRowIndex = nextRowIndexTmp;

                index++;
            }

            for (int i = 0; i < 2; i++) {
                SXSSFRow rowTmp = sheet.getRow(i);
                AppExcelUtil.setCellStyle(rowTmp, 0, 10, titleCellStyle);
            }

            for (int i = 2; i < 6; i++) {
                SXSSFRow rowTmp = sheet.getRow(i);
                AppExcelUtil.setCellStyle(rowTmp, 0, 10, countCellStyle);
            }

            for (int i = 6; i < nextRowIndex; i++) {
                SXSSFRow rowTmp = sheet.getRow(i);
                AppExcelUtil.setCellStyle(rowTmp, 0, 10, docCellStyle);
            }

        }

        NetworkRecordExcelUtil.responseBody(workbook, response, "告警分析报表");

    }


    /**
     * 校验时间有效性
     *
     * @param timeConfig
     * @param time
     * @return
     */
    private boolean timeFilter(List<List<String>> timeConfig, String time) {
        if (Objects.isNull(timeConfig) || timeConfig.isEmpty()) {
            return true;
        }
        long timeInt = Long.parseLong(time);
        for (List<String> list : timeConfig) {
            if (Objects.isNull(list) || list.isEmpty()) {
                continue;
            }
            String beginTime = list.get(0);
            String endTime = list.get(1);

            if (Long.parseLong(beginTime) < timeInt && timeInt < Long.parseLong(endTime)) {
                //符合筛选时间  不可用
                return false;
            }
        }

        return true;
    }


    /**
     * 告警标题
     *
     * @return
     */
    @PostMapping("/queryAllName")
    @ApiOperation(value = "查询所有告警标题")
    ResultVo<?> queryAllName() {
        List<String> listTitle = alarmInfoService.listTitle();
        return ResultVoUtil.success(listTitle);
    }

    /**
     * 告警历史备注分页查询
     *
     * @param req
     * @return
     */
    @PostMapping("/queryRemark")
    @ApiOperation(value = "告警历史备注分页查询")
    ResultVo<PageBean<PageQueryRemarkResp>> pageQueryRemark(@RequestBody PageQueryRemarkReq req) {
        IPage<AlarmInfo> iPage = PagePlugin.startPageT(req.getPage(), req.getSize(), AlarmInfo.class);
        QueryWrapper<AlarmInfo> queryWrapper = new QueryWrapper<AlarmInfo>();
        if (StrUtil.isNotEmpty(req.getAlarmCode())) {
            queryWrapper.eq("ALARM_CODE", req.getAlarmCode());
            queryWrapper.isNotNull("REMARK");
        }

        queryWrapper.orderByDesc("OCCUR_TIME");
        IPage<AlarmInfo> page = alarmInfoService.page(iPage, queryWrapper);
        List<AlarmInfo> records = page.getRecords();

        PageBean<PageQueryRemarkResp> pageBean = new PageBean<PageQueryRemarkResp>();
        pageBean.setContent(EntityBeanUtil.copyList(records, PageQueryRemarkResp.class));
        pageBean.setTotal(page.getTotal());

        return ResultVoUtil.success(pageBean);
    }

    @GetMapping("/queryHistoryAlarm/{id}")
    @ApiOperation(value = "查询历史相关告警")
    @ActionLog(name = "查询历史告警", title = "告警管理", key = LogTypeConstant.QUERY)
    ResultVo<List<PageQueryHistoryResp>> queryHistoryAlarm(@PathVariable("id") String id) {

        AlarmInfo alarm = alarmInfoService.getById(id);
        if (Objects.isNull(alarm)) {
            return ResultVoUtil.error("未查询到相应告警");
        }
        QueryWrapper<AlarmInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("ID", alarm.getId());

        if (Objects.nonNull(alarm.getAssetId()) && !alarm.getAssetId().isEmpty()) {
            queryWrapper.eq("ASSET_ID", alarm.getAssetId());
        }
        if (Objects.nonNull(alarm.getTitle()) && !alarm.getTitle().isEmpty()) {
            queryWrapper.eq("TITLE", alarm.getTitle());
        }
        if (Objects.nonNull(alarm.getAlarmCode()) && !alarm.getAlarmCode().isEmpty()) {
            queryWrapper.eq("ALARM_CODE", alarm.getAlarmCode());
        }
        if (Objects.nonNull(alarm.getOccurTime())) {
            queryWrapper.le("OCCUR_TIME", alarm.getOccurTime());
        }
        queryWrapper.orderByDesc("OCCUR_TIME");

        List<AlarmInfo> list = alarmInfoService.list(queryWrapper);
        List<PageQueryHistoryResp> resps = EntityBeanUtil.copyList(list, PageQueryHistoryResp.class);
        return ResultVoUtil.success(resps);
    }


    /**
     * 分页查询
     *
     * @param req
     * @return
     */
    @PostMapping("/query")
    @ApiOperation(value = "告警信息分页查询")
    @RequiresPermissions({"api:alarm:info:query"})
    @ActionLog(name = "查询告警信息列表", title = "告警管理", key = LogTypeConstant.QUERY)
    ResultVo<PageBean<AlarmInfoVo>> pageQuery(@RequestBody AlarmInfoPageQuery req) {
        IPage<AlarmInfo> iPage = PagePlugin.startPageT(req.getPage(), req.getSize(), AlarmInfo.class);
        QueryWrapper<AlarmInfo> queryWrapper = this.getQueryWrapper(req);
        if (Objects.nonNull(req.getSort()) && !req.getSort().isEmpty()) {
            if (Objects.nonNull(req.getOrder()) && req.getOrder().equals("ascending")) {
                queryWrapper.orderByAsc(req.getSort(), "OCCUR_TIME");
            } else {
                queryWrapper.orderByDesc(req.getSort(), "OCCUR_TIME");
            }
        } else {//descending
            queryWrapper.orderByDesc("OCCUR_TIME");
        }

        IPage<AlarmInfo> page = alarmInfoService.page(iPage, queryWrapper);
        List<AlarmInfo> records = page.getRecords();

        List<AlarmInfoVo> voList = new ArrayList<>();
        for (AlarmInfo alarmInfo : records) {
            AlarmInfoVo vo = new AlarmInfoVo();
            BeanUtil.copyProperties(alarmInfo, vo);
            vo.setLevelStr(AlarmLevelEnum.getMsg(alarmInfo.getAlarmLevel().intValue()));
            vo.setStatusStr(AlarmStatusEnum.getMsg(alarmInfo.getStatus()));
            vo.setTypeStr(AlarmTypeEnum.getMsg(alarmInfo.getType()));
            vo.setOccurTimeStr(DateUtil.format(alarmInfo.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));
            vo.setIp(alarmInfo.getAssetIp());
            vo.setAlarmStateStr(AlarmStateEnum.getMsg(alarmInfo.getAlarmState()));
            vo.setIsShowRecover(alarmInfo.getIsShowRecover());

            Date lastTime = alarmInfo.getLastTime();
            if (Objects.isNull(lastTime)) {
                lastTime = new Date();
            }
            String timeFormat = DateUtil.format(lastTime, "yyyy-MM-dd HH:mm:ss");

            vo.setContent(alarmInfoService.getContent(alarmInfo.getContent(), alarmInfo.getAlarmState(),
                    alarmInfo.getIsShowRecover(), timeFormat));

            vo.setCorrelationId(alarmInfo.getCorrelationId());

            Asset asset = assetServ.getById(alarmInfo.getAssetId());
            if (Objects.nonNull(asset)) {
                vo.setAssetName(asset.getName());
            } else {
                vo.setAssetName("设备已删除");
            }


            voList.add(vo);
        }

        PageBean<AlarmInfoVo> pageResult = new PageBean<>();
        pageResult.setContent(voList);
        pageResult.setTotal(page.getTotal());

        return ResultVoUtil.success(pageResult);
    }

    private QueryWrapper<AlarmInfo> getQueryWrapper(AlarmInfoPageQuery req) {
        QueryWrapper<AlarmInfo> queryWrapper = new QueryWrapper<AlarmInfo>();
        if (StrUtil.isNotEmpty(req.getOrgId())) {
            String id = req.getOrgId();
            List<SysOrg> orgList = ShiroUtil.getSubjectOrgs();
            SysOrg org = orgService.getById(id);
            if (org.getType() == 3) {
                List<String> collect = orgList.stream().filter(o -> o.getPid().equals(id)).map(SysOrg::getId)
                        .collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    queryWrapper.in("ORG_ID", collect);
                } else {
                    queryWrapper.eq("ORG_ID", id);
                }
            } else {
                queryWrapper.eq("ORG_ID", id);
            }
        } else {
            List<String> orgIds = ShiroUtil.getSubjectOrgIds();
            orgIds.add("x");
            queryWrapper.in("ORG_ID", orgIds);
        }
        if (StrUtil.isNotEmpty(req.getAssetName())) {
            queryWrapper.like("ASSET_NAME", req.getAssetName());
        }
        if (StrUtil.isNotEmpty(req.getTitle())) {
            queryWrapper.eq("TITLE", req.getTitle());
        }
        if (StrUtil.isNotEmpty(req.getIp())) {
            Asset one = assetServ.getOneByAllIp(req.getIp());
            if (Objects.nonNull(one)) {
                queryWrapper.eq("ASSET_ID", one.getId());
            } else {
                queryWrapper.eq("ASSET_IP", req.getIp());
            }
        }
        if (Objects.nonNull(req.getStartDate()) && Objects.nonNull(req.getEndDate())) {
            queryWrapper.between("OCCUR_TIME", req.getStartDate(), req.getEndDate());
        }
        if (Objects.nonNull(req.getStatus())) {
            queryWrapper.eq("STATUS", req.getStatus());
        }
        if (Objects.nonNull(req.getType())) {
            queryWrapper.eq("TYPE", req.getType());
        }
        if (Objects.nonNull(req.getAlarmLevel())) {
            queryWrapper.eq("ALARM_LEVEL", req.getAlarmLevel());
        }
        if (Objects.nonNull(req.getAlarmState())) {
            queryWrapper.eq("ALARM_STATE", req.getAlarmState());
        }
        if (Objects.nonNull(req.getBlank())) {
            queryWrapper.eq("BLANK", req.getBlank());
        }
        if (Objects.nonNull(req.getAssetMode())) {
            queryWrapper.eq("ASSET_MODE", req.getAssetMode());
        }
        if (!StrUtil.equals("yes", req.getShowJcca())) {
            //显示
            List<String> jccaAssetIds = assetServ.listJccaId();
            jccaAssetIds.add("-1");
            queryWrapper.notIn("ASSET_ID", jccaAssetIds);
        }

        //  queryWrapper.orderByDesc("OCCUR_TIME");
        return queryWrapper;
    }

    /**
     * 获取最新10条未确认告警列表
     *
     * @return
     */
    @GetMapping("/unconfirmed")
    @ApiOperation(value = "获取未确认告警列表")
    public ResultVo<?> list() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        if (CollUtil.isEmpty(orgIds)) {
            return ResultVoUtil.error("请先分配组织并添加资产");
        }

        IPage<AlarmInfo> page = PagePlugin.startPageT(1, 10, AlarmInfo.class);
        QueryWrapper<AlarmInfo> query = Wrappers.query();
        query.in("org_id", orgIds);
        query.eq("status", AlarmStatusEnum.UNCONFIRM.getCode());
        query.ne("ALARM_LEVEL", AlarmLevelEnum.LEVEL_MSG.getCode());
        query.orderByDesc("occur_time");
        IPage<AlarmInfo> iPage = alarmInfoService.page(page, query);

        List<AlarmUnconfirmVo> resultList = new ArrayList<>();
        List<AlarmInfo> records = iPage.getRecords();

        for (AlarmInfo record : records) {
            AlarmUnconfirmVo vo = new AlarmUnconfirmVo();
            BeanUtil.copyProperties(record, vo);
            vo.setOccurTime(DateUtil.format(record.getOccurTime(), DatePattern.NORM_DATETIME_PATTERN));
            resultList.add(vo);
        }

        PageBean<AlarmUnconfirmVo> pageResult = new PageBean<>();
        List<AlarmUnconfirmVo> showRecover = AlarmUtils.isShowRecover(resultList);
        pageResult.setContent(showRecover);
        pageResult.setTotal(page.getTotal());

        return ResultVoUtil.success(pageResult);
    }

    /**
     * 获取某一资产告警列表
     *
     * @param req 资产ID
     * @return
     * @date 2020年7月16日 由原来的获取资产未确认告警，修改为获取资产 未确认或者已确认未恢复 告警
     */
    @PostMapping("/assetAlarm")
    @ApiOperation(value = "获取资产告警列表")
    @ActionLog(name = "获取资产告警信息列表", title = "告警管理", key = LogTypeConstant.QUERY)
    public ResultVo<?> assetAlarm(@RequestBody AssetAlarmReq req) {
        if (ReceiveAlarmTypeEnum.ORACLE_DB.getCode().equals(req.getCategory())) {
            ManageDb db = manageDbService.getById(req.getAssetId());
            req.setAssetId(db.getAssetId());
        }

        List<String> assetIds = ShiroUtil.getSubjectAssetIds();
        if (assetIds.indexOf(req.getAssetId()) < 0) {
            if (LogInputUtils.inputInfo(ServerTypeEnum.ALARM_INFO)) {
                log.error(LogInputUtils.formattingInfoLog(ServerTypeEnum.ALARM_INFO, "设备ID：" + req.getAssetId(), "获取资产告警列表-无操作权限用户访问详情告警"));
            }
            return ResultVoUtil.success(new ArrayList<AlarmUnconfirmVo>());
        }

        List<AlarmUnconfirmVo> resultList = alarmInfoService.findAssetAlarm(req);
        List<AlarmUnconfirmVo> showRecover = AlarmUtils.isShowRecover(resultList);

        return ResultVoUtil.success(showRecover);
    }

    /**
     * 确认告警
     *
     * @return
     */
    @PostMapping("/confirm")
    @ApiOperation(value = "确认告警")
    @RequiresPermissions({"api:alarm:info:confirm"})
    @DevLog(title = "告警管理", name = "单个确认告警", dev = DevLogConstant.ALARM_CONFIRM_SINGLE, key = LogTypeConstant.DEV)
    public ResultVo<?> confirm(@RequestBody AlarmHandleVo vo) {
        AlarmInfo alarmInfo = alarmInfoService.getById(vo.getId());
        if (Objects.isNull(alarmInfo)) {
            return ResultVoUtil.error("不存在该条告警");
        }
        if (!AlarmStatusEnum.UNCONFIRM.getCode().equals(alarmInfo.getStatus())) {
            return ResultVoUtil.error("该条告警已被其他用户确认，请刷新页面获取最新信息");
        }

        if (AlarmInfo.SHOW_RECOVER_NO_FLAG.equals(alarmInfo.getIsShowRecover())) {
            //这些是不会恢复的通知类型的 确认后强制恢复
            alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());
        }
        //v2 判定逻辑
        QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();
        queryWrapper.eq("ALARM_CODE", alarmInfo.getAlarmCode());
        queryWrapper.eq("FLAG_TYPE", EventLevelEnum.NORMAL.getCode());
        List<AlarmRepository> repoList = alarmRepoServ.list(queryWrapper);

        if (repoList.isEmpty()) {
            Asset asset = assetServ.getById(alarmInfo.getAssetId());
            //这些是不会恢复的类型的 确认后强制恢复
            alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());

            CacheEvent cacheEvent = new CacheEvent();
            cacheEvent.setAssetId(asset.getId());
            cacheEvent.setAssetIp(asset.getIp());
            cacheEvent.setOpt(CacheOptEnum.REMOVE);
            cacheEvent.setEventKey(alarmInfo.getAlarmCode());
            cacheEvent.setMapKey(alarmInfo.getAlarmFlag());
            this.dispatureEvent(cacheEvent);
        }

        alarmInfo.setRemark(vo.getRemark());
        alarmInfo.setStatus(AlarmStatusEnum.CONFIRMED.getCode());
        alarmInfo.setConfirmor(ShiroUtil.getSubject().getUsername());
        alarmInfo.setConfirmTime(new Date());

        alarmInfoService.updateById(alarmInfo);

        return ResultVoUtil.success("确认成功");
    }

    /**
     * 告警转故障记录
     *
     * @return
     */
    @PostMapping("/toRecord")
    @ApiOperation(value = "告警转故障记录")
    @RequiresPermissions({"api:alarm:info:toRecord"})
    @ActionLog(name = "告警转故障记录", title = "告警管理", key = LogTypeConstant.MODIFY)
    public ResultVo<?> toRecord(@Validated @RequestBody AddOpinionReq req) {
        AlarmInfo alarmInfo = alarmInfoService.getById(req.getId());
        if (Objects.isNull(alarmInfo)) {
            return ResultVoUtil.error("不存在该条告警");
        }
        if (AlarmToRecordConst.TRANSFORMED == alarmInfo.getAlarmToRecord()) {
            return ResultVoUtil.error("不能重复转为故障记录");
        }

        alarmInfo.setRemark(req.getRemark());
        // 转为故障记录
        brokenRecordService.transforRecord(alarmInfo);

        // 更新告警信息
        alarmInfo.setAlarmToRecord(AlarmToRecordConst.TRANSFORMED);
        alarmInfo.setStatus(AlarmStatusEnum.CONFIRMED.getCode());
        alarmInfo.setConfirmor(ShiroUtil.getSubject().getUsername());
        alarmInfo.setConfirmTime(new Date());
        alarmInfoService.updateById(alarmInfo);

        return ResultVoUtil.success("处理成功");
    }

    /**
     * 批量确认告警
     *
     * @return
     */
    @PostMapping("/batchDispose")
    @ApiOperation(value = "批量确认告警")
    @RequiresPermissions({"api:alarm:info:batchDispose"})
    @DevLog(title = "告警管理", name = "批量确认告警", dev = DevLogConstant.ALARM_CONFIRM_BATCH, key = LogTypeConstant.DEV)
    public ResultVo<?> batchDispose(@Validated @RequestBody DisposeAlarmReq req) {
        List<String> idList = req.getIdList();
        if (CollectionUtil.isEmpty(idList)) {
            return ResultVoUtil.error("请选择未确认告警");
        }

        QueryWrapper<AlarmInfo> query = Wrappers.query();
        query.in("id", idList);
        query.eq("status", AlarmStatusEnum.UNCONFIRM.getCode());
        List<AlarmInfo> alarmList = alarmInfoService.list(query);
        for (AlarmInfo alarm : alarmList) {
            alarm.setConfirmTime(new Date());
            alarm.setConfirmor(ShiroUtil.getSubject().getUsername());
            alarm.setStatus(AlarmStatusEnum.CONFIRMED.getCode());
            alarm.setRemark("");
            if (StrUtil.isNotEmpty(req.getRemark())) {
                alarm.setRemark(req.getRemark());
            }
            if (AlarmInfo.SHOW_RECOVER_NO_FLAG.equals(alarm.getIsShowRecover())) {
                //这些是不会恢复的通知类型的 确认后强制恢复
                alarm.setAlarmState(AlarmStateEnum.RECOVER.getCode());
            }

            QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();
            queryWrapper.eq("ALARM_CODE", alarm.getAlarmCode());
            queryWrapper.eq("FLAG_TYPE", EventLevelEnum.NORMAL.getCode());
            List<AlarmRepository> repoList = alarmRepoServ.list(queryWrapper);

            if (repoList.isEmpty()) {
                //这些是不会恢复的类型的 确认后强制恢复
                alarm.setAlarmState(AlarmStateEnum.RECOVER.getCode());

                Asset asset = assetServ.getById(alarm.getAssetId());
                CacheEvent cacheEvent = new CacheEvent();
                cacheEvent.setAssetId(asset.getId());
                cacheEvent.setAssetIp(asset.getIp());
                cacheEvent.setOpt(CacheOptEnum.REMOVE);
                cacheEvent.setEventKey(alarm.getAlarmCode());
                cacheEvent.setMapKey(alarm.getAlarmFlag());
                this.dispatureEvent(cacheEvent);
            }

        }
        if (CollectionUtil.isEmpty(alarmList)) {
            return ResultVoUtil.error("不能重复确认告警");
        }

        alarmInfoService.updateBatchById(alarmList);

        return ResultVoUtil.success("操作成功");
    }


    /**
     * 按照搜索条件批量确认
     */
    @PostMapping("/affirm")
    @ApiOperation(value = "批量确认2号")
    @RequiresPermissions({"api:alarm:info:affirm"})
    @DevLog(title = "告警管理", name = "按条件确认告警", dev = DevLogConstant.ALARM_CONFIRM_CONDITION, key = LogTypeConstant.DEV)
    ResultVo affirm(@RequestBody AlarmInfoPageQuery req) {
        QueryWrapper<AlarmInfo> query = this.getQueryWrapper(req);
        query.eq("status", AlarmStatusEnum.UNCONFIRM.getCode());
        List<AlarmInfo> alarmList = alarmInfoService.list(query);
        if (alarmList.isEmpty()) {
            return ResultVoUtil.warning("您没有筛选任何告警哦~");
        }
        for (AlarmInfo alarm : alarmList) {
            alarm.setConfirmTime(new Date());
            alarm.setConfirmor(ShiroUtil.getSubject().getUsername());
            alarm.setStatus(AlarmStatusEnum.CONFIRMED.getCode());
            alarm.setRemark("");
            if (StrUtil.isNotEmpty(req.getRemark())) {
                alarm.setRemark(req.getRemark());
            }
            if (AlarmInfo.SHOW_RECOVER_NO_FLAG.equals(alarm.getIsShowRecover())) {
                //这些是不会恢复的通知类型的 确认后强制恢复
                alarm.setAlarmState(AlarmStateEnum.RECOVER.getCode());
            }
            QueryWrapper<AlarmRepository> queryWrapper = new QueryWrapper<AlarmRepository>();
            queryWrapper.eq("ALARM_CODE", alarm.getAlarmCode());
            queryWrapper.eq("FLAG_TYPE", EventLevelEnum.NORMAL.getCode());
            List<AlarmRepository> repoList = alarmRepoServ.list(queryWrapper);

            if (repoList.isEmpty()) {
                //这些是不会恢复的类型的 确认后强制恢复
                alarm.setAlarmState(AlarmStateEnum.RECOVER.getCode());

                Asset asset = assetServ.getById(alarm.getAssetId());
                CacheEvent cacheEvent = new CacheEvent();
                cacheEvent.setAssetId(asset.getId());
                cacheEvent.setAssetIp(asset.getIp());
                cacheEvent.setOpt(CacheOptEnum.REMOVE);
                cacheEvent.setEventKey(alarm.getAlarmCode());
                cacheEvent.setMapKey(alarm.getAlarmFlag());
                this.dispatureEvent(cacheEvent);
            }
        }
        alarmInfoService.updateBatchById(alarmList);
        return ResultVoUtil.success("确认成功~");
    }

    /**
     * 告警详情
     *
     * @return
     */
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "告警详情")
    @ActionLog(name = "查看资产告警详情", title = "告警管理", key = LogTypeConstant.QUERY)
    public ResultVo<?> detail(@PathVariable("id") String id) {
        AlarmDetailVo detail = alarmInfoService.findDetailById(id);
        AlarmInfo alarm = alarmInfoService.getById(id);

        Date lastTime = alarm.getLastTime();
        if (Objects.isNull(lastTime)) {
            lastTime = new Date();
        }
        if (Objects.nonNull(alarm.getType())) {
            detail.setTypeStr(AlarmTypeEnum.getMsg(alarm.getType()));
        }

        detail.setContent(alarmInfoService.getContent(alarm.getContent(), alarm.getAlarmState(),
                alarm.getIsShowRecover(), DateUtil.format(lastTime, "yyyy-MM-dd HH:mm:ss")));

        List<String> descriptionList = alarmInfoService.queryDescriptionList(id);
        detail.setDescriptionList(descriptionList);
        detail.setIsShowRecover(alarm.getIsShowRecover());

        return ResultVoUtil.success(detail);
    }

    /**
     * 更新已确认告警备注
     *
     * @return
     */
    @PostMapping("/setRemark")
    @ApiOperation(value = "更新已确认告警备注")
    @ActionLog(name = "更新已确认告警备注", title = "告警管理", key = LogTypeConstant.MODIFY)
    public ResultVo<?> setRemark(@RequestBody AlarmReq req) {
        if (ObjectUtil.isNull(req.getAlarmId())) {
            return ResultVoUtil.error("告警id不可为空");
        }
        AlarmInfo alarmInfo = alarmInfoService.getById(req.getAlarmId());
        if (ObjectUtil.isNull(alarmInfo)) {
            return ResultVoUtil.error("告警不存在");
        }
        alarmInfo.setRemark(req.getRemark());
        alarmInfoService.saveOrUpdate(alarmInfo);
        return ResultVoUtil.success("保存成功!");
    }

    /**
     * 告警知识库
     *
     * @param correlationId
     * @return
     */
    @GetMapping("/queryRepo/{correlationId}")
    @ApiOperation(value = "告警知识库查询")
    @RequiresPermissions({"api:alarm:info:queryRepo"})
    @ActionLog(name = "查看告警关联知识库", title = "告警管理", key = LogTypeConstant.QUERY)
    public ResultVo<?> queryRepo(@PathVariable("correlationId") String correlationId) {
        if (StrUtil.isEmpty(correlationId)) {
            return ResultVoUtil.error("请传入知识库ID");
        }
        AlarmRepository alarmRepo = alarmRepoServ.getById(correlationId);
        if (Objects.isNull(alarmRepo)) {
            return ResultVoUtil.error("系统没有匹配到告警知识库内容，请在知识库内添加知识库规则");
        }

        return ResultVoUtil.success(alarmRepo);
    }

    /**
     * 清除告警 把所有告警修改为已确认已恢复
     *
     * @return
     */
    @PostMapping("/clear")
    @ApiOperation(value = "清除告警")
    @RequiresPermissions({"api:alarm:info:clear"})
    @DevLog(title = "告警管理", name = "清除告警", dev = DevLogConstant.ALARM_CLEAR_CONDITION, key = LogTypeConstant.DEV)
    public ResultVo<?> clear(@RequestBody AlarmInfoPageQuery req) {
        // 按照筛选条件一键清除告警  20221102 godwone
        List<List<String>> listList = new ArrayList<>();
//        QueryWrapper<AlarmInfo> queryWrapper = this.getQueryWrapper(req);
//        List<AlarmInfo> list = alarmInfoService.list(queryWrapper);
//        if (!CollectionUtils.isEmpty(list)) {
//            List<String> ids = list.stream().map(AlarmInfo::getId).collect(Collectors.toList());
//            listList = CollectionUtil.split(ids, 1000);
//        }
        if (!CollectionUtils.isEmpty(req.getIdList())) {
            listList.add(req.getIdList());
        }

        for (List<String> idss : listList) {
            UpdateWrapper<AlarmInfo> updateWrapper = Wrappers.update();

            updateWrapper.in("id", idss);
            updateWrapper.notLike("CONTENT", "一键清除");

            updateWrapper.set("status", AlarmStatusEnum.CONFIRMED.getCode());
            updateWrapper.set("alarm_state", AlarmStateEnum.RECOVER.getCode());
            updateWrapper.set("CONFIRMOR", ShiroUtil.getSubject().getUsername());
            updateWrapper.set("CONFIRM_TIME", new Date());
            updateWrapper.setSql("CONTENT=CONTENT||'(一键清除)'");
            alarmInfoService.update(updateWrapper);
        }
        return ResultVoUtil.success();
    }

    /**
     * 导出选中告警信息
     */
    @GetMapping("/export/batch/{ids}")
    @ApiOperation(value = "导出选中告警信息")
//    @RequiresPermissions({"api:alarm:info:export:batch"})
    @ActionLog(name = "导出选中告警", title = "告警管理", key = LogTypeConstant.DOWNLOAD)
    public void export(@PathVariable("ids") String ids, HttpServletResponse response) {
        String[] idArr = ids.split(",");
        if (idArr.length < 1) {
            return;
        }
        List<AlarmExportVo> exportVoList = alarmInfoService.findExportAlarm(Arrays.asList(idArr));

        this.exportData(exportVoList, response);
    }


    @PostMapping("/exportAll/verify")
    @ApiOperation(value = "导出全部告警信息校验")
    public ResultVo exportAllVerify(@RequestBody AlarmInfoPageQuery query) {
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        FileUtil.createTmpPath();
        if (StrUtil.isEmpty(query.getOrgId())) {
            query.setOrgIds(ShiroUtil.getSubjectOrgIds());
        }

        List<AlarmExportVo> exportVoList = alarmInfoService.findExportAllAlarm(query);
        if (Objects.isNull(exportVoList) || exportVoList.isEmpty()) {
            return ResultVoUtil.error("未查询到告警记录");
        }
        return ResultVoUtil.success();
    }

    /**
     * 导出全部告警信息
     */
    @GetMapping("/exportAll")
    @ApiOperation(value = "导出全部告警信息")
    @RequiresPermissions({"api:alarm:info:export"})
    @ActionLog(name = "导出全部告警", title = "告警管理", key = LogTypeConstant.DOWNLOAD)
    public void exportAllGet(AlarmInfoPageQuery query, HttpServletResponse response) {
        if (StrUtil.isEmpty(query.getOrgId())) {
            query.setOrgIds(ShiroUtil.getSubjectOrgIds());
        }
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        List<AlarmExportVo> exportVoList = alarmInfoService.findExportAllAlarm(query);

        this.exportData(exportVoList, response);
    }

    /**
     * 导出全部告警信息
     */
    @PostMapping("/exportAll")
    @ApiOperation(value = "导出全部告警信息")
    @RequiresPermissions({"api:alarm:info:export"})
    @ActionLog(name = "导出全部告警", title = "告警管理", key = LogTypeConstant.DOWNLOAD)
    public void exportAll(@RequestBody AlarmInfoPageQuery query, HttpServletResponse response) {
        if (StrUtil.isEmpty(query.getOrgId())) {
            query.setOrgIds(ShiroUtil.getSubjectOrgIds());
        }
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        List<AlarmExportVo> exportVoList = alarmInfoService.findExportAllAlarm(query);

        this.exportData(exportVoList, response);
    }

    private void exportData(List<AlarmExportVo> exportVoList, HttpServletResponse response) {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.renameSheet("告警信息");

        writer.addHeaderAlias("title", "告警标题");
        writer.addHeaderAlias("assetName", "资产名称");
        writer.addHeaderAlias("assetIp", "资产IP");
        writer.addHeaderAlias("alarmLevel", "告警级别");
        writer.addHeaderAlias("alarmType", "告警来源");

        writer.addHeaderAlias("content", "告警内容");
        writer.addHeaderAlias("description", "原始告警");

        writer.addHeaderAlias("occurTime", "告警时间");
        writer.addHeaderAlias("status", "确认状态");
        writer.addHeaderAlias("alarmStatus", "告警状态");
        writer.addHeaderAlias("confirmor", "确认人");
        writer.addHeaderAlias("confirmTime", "确认时间");
        writer.addHeaderAlias("remark", "备注");

        writer.getStyleSet().setAlign(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);

        writer.write(exportVoList, true);

        String fileName = "TDCS-CTC综合维护平台告警-" + DateUtil.formatDate(new Date());
        try {
            String utf8FileName = URLEncoder.encode(fileName, "utf8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename= " + utf8FileName + ".xlsx");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
        } catch (IOException e) {
            if (LogInputUtils.inputError(ServerTypeEnum.ALARM_INFO)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.ALARM_INFO, ErrorCodeEnum.COMMON_EXPORT_ERROR, "", "导出告警信息异常--" + e.getMessage()));
            }
        }
    }


    @PostMapping("/alarmAnalysisWordExport/verify")
    @ApiOperation(value = "告警分析报告导出校验")
    public ResultVo alarmAnalysisWordExport(@RequestBody AlarmInfoPageQuery query) {
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        if (StrUtil.isEmpty(query.getOrgId())) {
            query.setOrgIds(ShiroUtil.getSubjectOrgIds());
        }
        FileUtil.createTmpPath();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        if (StrUtil.isEmpty(query.getOrgId())) {
            query.setOrgIds(ShiroUtil.getSubjectOrgIds());
        }
        List<BrokenRecordWord> list = brokenRecordWordService.getBrokenRecordWords(query);

        if (Objects.isNull(list) || list.isEmpty()) {
            return ResultVoUtil.error("未查询到告警记录");
        }
        return ResultVoUtil.success();

    }

    @GetMapping("/alarmAnalysisWordExportGet")
    @ApiOperation(value = "告警分析报告导出")
    @ActionLog(name = "导出告警分析报告", title = "告警管理", key = LogTypeConstant.DOWNLOAD)
    public void alarmAnalysisWordExportGet(AlarmInfoPageQuery query, HttpServletResponse response) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        if (StrUtil.isEmpty(query.getOrgId())) {
            query.setOrgIds(ShiroUtil.getSubjectOrgIds());
        }
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        List<BrokenRecordWord> list = brokenRecordWordService.getBrokenRecordWords(query);
        Map<String, Object> dataMap = brokenRecordWordService.getExportWordMapV2(query, list);
        // 文件唯一名称
        String fileOnlyName = "告警分析报告_" + sdf.format(new Date()) + ".doc";

        /** 生成word */

        WordUtil.createWord(dataMap, "AlarmWord.ftl", fileOnlyName, response);

    }

    @PostMapping("/alarmAnalysisWordExport")
    @ApiOperation(value = "告警分析报告导出")
    @ActionLog(name = "导出告警分析报告", title = "告警管理", key = LogTypeConstant.DOWNLOAD)
    public void alarmAnalysisWordExport(@RequestBody AlarmInfoPageQuery query, HttpServletResponse response) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        if (StrUtil.isEmpty(query.getOrgId())) {
            query.setOrgIds(ShiroUtil.getSubjectOrgIds());
        }
        if (Objects.isNull(query.getAlarmLevelList()) || query.getAlarmLevelList().isEmpty()) {
            query.setAlarmLevelList(null);
        }
        if (Objects.isNull(query.getAlarmCodeList()) || query.getAlarmCodeList().isEmpty()) {
            query.setAlarmCodeList(null);
        }
        List<BrokenRecordWord> list = brokenRecordWordService.getBrokenRecordWords(query);
        Map<String, Object> dataMap = brokenRecordWordService.getExportWordMapV2(query, list);
        // 文件唯一名称
        String fileOnlyName = "告警分析报告_" + sdf.format(new Date()) + ".doc";

        /** 生成word */
        WordUtil.createWord(dataMap, "AlarmWord.ftl", fileOnlyName, response);

    }

    @GetMapping("/getAlarmKey")
    @ApiOperation(value = "是否开启小铃铛告警")
    public ResultVo<Boolean> getAlarmKey() {
        SysConfig sysConfig = configService.getSysConfig();

        if ("no".equals(sysConfig.getFirstLevel()) && "no".equals(sysConfig.getSecondLevel()) && "no".equals(sysConfig.getThirdLevel())) {
            return ResultVoUtil.success(false);
        }
        if ("no".equals(sysConfig.getAffirmStatus()) && "no".equals(sysConfig.getRecoveredStatus())) {
            return ResultVoUtil.success(false);
        }
        return ResultVoUtil.success(true);

    }

    @GetMapping("/countAlarm")
    @ApiOperation(value = "小铃铛的显示列表")
    public ResultVo queryWarnning() {
        SysConfig sysConfig = configService.getSysConfig();
        if (!sysConfig.canPoup()) {
            return ResultVoUtil.success();
        }
        DialogsAlarmListDto query = new DialogsAlarmListDto();
        if (SysConfig.YES.equals(sysConfig.getAffirmStatus())) {
            query.setStatus(AlarmStatusEnum.UNCONFIRM.getCode().intValue());
        }
        if (SysConfig.YES.equals(sysConfig.getRecoveredStatus())) {
            query.setAlarmState(AlarmStateEnum.ALARM.getCode().intValue());
        }
        query.setShowJcca(SysConfig.YES.equals(sysConfig.getShowJcca()) ? 1 : 2);
        query.setStatusLogical(1);
        query.setAlarmLevelList(sysConfig.getConfigAlarmLevelList());
        query.setBlank(AlarmBlankConst.NORMARL);
        List<DialogsAlarmListVo> dialogsAlarmListVos = alarmInfoService.queryDialogsVoListV2(query);

        List<AlarmInfo> copyList = new ArrayList<AlarmInfo>();
        for (DialogsAlarmListVo vo : dialogsAlarmListVos) {
            AlarmInfo copy = EntityBeanUtil.copy(vo, AlarmInfo.class);
            copy.setId(vo.getAlarmId());
            copy.setOccurTime(vo.getOccurTime());
            copy.setCreateTime(vo.getOccurTime());
            copyList.add(copy);
        }

        return ResultVoUtil.success(copyList);
    }
}
