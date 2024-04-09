package com.jcca.web.xunjian.controller.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.poi.hssf.usermodel.HSSFCellStyle;
import com.jcca.poi.hssf.usermodel.HSSFFont;
import com.jcca.poi.hssf.util.HSSFColor;
import com.jcca.poi.ss.usermodel.CellStyle;
import com.jcca.poi.ss.usermodel.Font;
import com.jcca.poi.ss.util.CellRangeAddress;
import com.jcca.poi.xssf.streaming.SXSSFCell;
import com.jcca.poi.xssf.streaming.SXSSFRow;
import com.jcca.poi.xssf.streaming.SXSSFSheet;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.poi.xssf.usermodel.XSSFCellStyle;
import com.jcca.poi.xssf.usermodel.XSSFColor;
import com.jcca.web.xunjian.controller.bean.XunjianRepoBody;
import com.jcca.web.xunjian.controller.util.bean.XunjianReportTemp;
import com.jcca.web.xunjian.entity.XunjianAlarmMsg;
import com.jcca.web.xunjian.entity.XunjianDetail;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import com.jcca.web.xunjian.entity.bean.XunjianServerDetailBean;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 巡检报告生成工具
 *
 * @author Lvyp
 */
public class XunjianReportUtil {


    /**
     * V2版本的报告导出
     *
     * @param result
     * @return
     */
    public static SXSSFWorkbook createExcelV2(XunjianServerDetailBean result) {
        CellRangeAddress titleStyle = new CellRangeAddress(0, 1, 0, 3);
        CellRangeAddress titleStyle2 = new CellRangeAddress(7, 8, 0, 3);


        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        SXSSFSheet sheet = workbook.createSheet();
        sheet.setColumnWidth(0, (int) 43 * 256);
        sheet.setColumnWidth(1, (int) 43 * 256);
        sheet.setColumnWidth(2, (int) 43 * 256);
        sheet.setColumnWidth(3, (int) 43 * 256);

        XSSFCellStyle msgStyle = getMsgStyle(workbook);
        XSSFCellStyle titleCellStyle = getTitleStyleV2(workbook, new java.awt.Color(230, 230, 230));
        XSSFCellStyle subTitleStyle2 = getSubTitleStyleV2(workbook, HSSFCellStyle.ALIGN_CENTER, new java.awt.Color(230, 230, 230));
        XSSFCellStyle subTitleStyle = getSubTitleStyleV2(workbook, HSSFCellStyle.ALIGN_LEFT, new java.awt.Color(230, 230, 230));

        SXSSFRow row = sheet.createRow(0);
        SXSSFCell cell = row.createCell(0);
        row.setHeightInPoints(25);
        cell.setCellStyle(titleCellStyle);
        cell.setCellValue("设备信息");
        sheet.addMergedRegion(titleStyle);

        SXSSFRow row2 = sheet.createRow(2);
        row2.setHeightInPoints(20);

        setCellValue(row2, 0, "应用名称", subTitleStyle);
        setCellValue(row2, 1, result.getAppName(), msgStyle);
        setCellValue(row2, 2, "IP地址", subTitleStyle);
        setCellValue(row2, 3, result.getIp(), msgStyle);

        SXSSFRow row3 = sheet.createRow(3);
        row3.setHeightInPoints(20);
        setCellValue(row3, 0, "设备型号", subTitleStyle);
        setCellValue(row3, 1, result.getAssetImage(), msgStyle);
        setCellValue(row3, 2, "序列号", subTitleStyle);
        setCellValue(row3, 3, result.getSerialNumber(), msgStyle);

        SXSSFRow row4 = sheet.createRow(4);
        row4.setHeightInPoints(20);
        setCellValue(row4, 0, "系统版本", subTitleStyle);
        setCellValue(row4, 1, result.getSysVersion(), msgStyle);
        setCellValue(row4, 2, "设备版本号", subTitleStyle);
        setCellValue(row4, 3, result.getAssetVersion(), msgStyle);

        SXSSFRow row5 = sheet.createRow(5);
        row5.setHeightInPoints(20);
        setCellValue(row5, 0, "安装位置", subTitleStyle);
        setCellValue(row5, 1, result.getAssetPosition(), msgStyle);
        setCellValue(row5, 2, "上架日期", subTitleStyle);
        setCellValue(row5, 3, Objects.nonNull(result.getOnlineTime()) ? DateUtil.format(result.getOnlineTime(), "yyyy-MM-dd HH:mm:ss") : "", msgStyle);

        SXSSFRow row6 = sheet.createRow(6);
        row6.setHeightInPoints(20);
        setCellValue(row6, 0, "设备供应商", subTitleStyle);
        setCellValue(row6, 1, result.getAssetSupplier(), msgStyle);
        setCellValue(row6, 2, "备注", subTitleStyle);
        setCellValue(row6, 3, result.getRemark(), msgStyle);

        SXSSFRow row7_8 = sheet.createRow(7);
        row7_8.setHeightInPoints(20);
        SXSSFCell cell7_0 = row7_8.createCell(0);
        cell7_0.setCellStyle(titleCellStyle);
        cell7_0.setCellValue("报告内容");
        sheet.addMergedRegion(titleStyle2);

        SXSSFRow row9 = sheet.createRow(9);
        row9.setHeightInPoints(20);
        setCellValue(row9, 0, "检查项", subTitleStyle2);
        setCellValue(row9, 1, "参考命令", subTitleStyle2);
        setCellValue(row9, 2, "结果", subTitleStyle2);
        setCellValue(row9, 3, "备注信息", subTitleStyle2);

        List<XunjianDetailV2> detailList = result.getDetailList();
        int startCell = 10;
        for (XunjianDetailV2 xunjianDetailV2 : detailList) {
            SXSSFRow rowItem = sheet.createRow(startCell);
            rowItem.setHeightInPoints(20);
            setCellValue(rowItem, 0, xunjianDetailV2.getXunjianTargetItem(), msgStyle);
            setCellValue(rowItem, 1, xunjianDetailV2.getCommand(), msgStyle);
            setCellValue(rowItem, 2, xunjianDetailV2.getNormalFlagStr(), msgStyle);
            setCellValue(rowItem, 3, xunjianDetailV2.getInputErrorStr(), msgStyle);
            startCell++;
        }

        //合并
        return workbook;
    }

    private static XSSFCellStyle getMsgStyle(SXSSFWorkbook workbook) {
        XSSFCellStyle changeLineStyle = (XSSFCellStyle) workbook.createCellStyle();
        changeLineStyle.setFont(getFont(workbook, (short) 11, false, true));
        changeLineStyle.setWrapText(true);
        ;//设置自动换行
        changeLineStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        changeLineStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        changeLineStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(230, 230, 230)));
        changeLineStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        changeLineStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        changeLineStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        changeLineStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        changeLineStyle.setBottomBorderColor(HSSFColor.BLACK.index);
        return changeLineStyle;
    }

    private static void setCellValue(SXSSFRow row, int cellIndex, String value, XSSFCellStyle cellStyle) {
        SXSSFCell cell3_0 = row.createCell(cellIndex);
        cell3_0.setCellStyle(cellStyle);
        cell3_0.setCellValue(value);
    }

    private static XSSFCellStyle getTitleStyle(SXSSFWorkbook workbook) {
        XSSFCellStyle titleCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        titleCellStyle.setFont(getFont(workbook, (short) 18, true, false));
        titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        titleCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(230, 230, 230)));
        titleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        titleCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);

        return titleCellStyle;
    }


    private static XSSFCellStyle getTitleStyleV2(SXSSFWorkbook workbook, java.awt.Color bgColor) {
        XSSFCellStyle titleCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        titleCellStyle.setFont(getFont(workbook, (short) 18, true, true));
        titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        titleCellStyle.setFillForegroundColor(new XSSFColor(bgColor));
        titleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        titleCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);

        return titleCellStyle;
    }

    /**
     * @param workbook
     * @param xType    HSSFCellStyle
     * @return
     */
    private static XSSFCellStyle getSubTitleStyleV2(SXSSFWorkbook workbook, short xType, java.awt.Color bgColor) {
        XSSFCellStyle subtitleCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        subtitleCellStyle.setFont(getFont(workbook, (short) 14, true, true));
        subtitleCellStyle.setAlignment(xType);// 水平居中
        subtitleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        subtitleCellStyle.setFillForegroundColor(new XSSFColor(bgColor));
        subtitleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        subtitleCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);

        return subtitleCellStyle;
    }


    /**
     * @param workbook
     * @param xType    HSSFCellStyle
     * @return
     */
    private static XSSFCellStyle getSubTitleStyle(SXSSFWorkbook workbook, short xType) {
        XSSFCellStyle subtitleCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        subtitleCellStyle.setFont(getFont(workbook, (short) 14, true, false));
        subtitleCellStyle.setAlignment(xType);// 水平居中
        subtitleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        subtitleCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(31, 78, 120)));
        subtitleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        subtitleCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);

        return subtitleCellStyle;
    }

    public static SXSSFWorkbook createExcel(XunjianReportTemp req) {
        Date xunjianTime = req.getXunjianTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(xunjianTime);
        calendar.add(Calendar.MINUTE, 5);
        Date xunjianEndTime = calendar.getTime();

        if (Objects.isNull(req.getExceptionNum())) {
            req.setExceptionNum(0);
        }
        if (Objects.isNull(req.getNormalNum())) {
            req.setNormalNum(0);
        }

        CellRangeAddress titleStyle = new CellRangeAddress(0, 1, 0, 10);
        CellRangeAddress row2Cell0Stell = new CellRangeAddress(2, 2, 0, 2);
        CellRangeAddress row2Cell1Stell = new CellRangeAddress(2, 2, 3, 5);
        CellRangeAddress row2Cell1Stel2 = new CellRangeAddress(2, 2, 6, 10);
        CellRangeAddress row3Cell0Stell = new CellRangeAddress(3, 3, 0, 2);
        CellRangeAddress row3Cell1Stell = new CellRangeAddress(3, 3, 3, 5);
        CellRangeAddress row3Cell1Stel2 = new CellRangeAddress(3, 3, 6, 10);
        CellRangeAddress row4Cell1Stel0 = new CellRangeAddress(4, 4, 0, 10);
        CellRangeAddress row5Cell1Stel0 = new CellRangeAddress(5, 5, 0, 10);
        CellRangeAddress row6Cell1Stel0 = new CellRangeAddress(6, 6, 0, 10);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        SXSSFSheet sheet = workbook.createSheet();
        sheet.setColumnWidth(0, (int) 23 * 256);
        sheet.setColumnWidth(1, (int) 23 * 256);
        sheet.setColumnWidth(2, (int) 23 * 256);
        sheet.setColumnWidth(3, (int) 23 * 256);
        sheet.setColumnWidth(4, (int) 23 * 256);
        sheet.setColumnWidth(5, (int) 23 * 256);
        sheet.setColumnWidth(6, (int) 23 * 256);
        sheet.setColumnWidth(7, (int) 23 * 256);
        sheet.setColumnWidth(8, (int) 23 * 256);
        sheet.setColumnWidth(9, (int) 23 * 256);
        sheet.setColumnWidth(10, (int) 23 * 256);


        XSSFCellStyle titleCellStyle = getTitleStyle(workbook);

        XSSFCellStyle subtitleCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        subtitleCellStyle.setFont(getFont(workbook, (short) 14, true, false));
        subtitleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        subtitleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        subtitleCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(31, 78, 120)));
        subtitleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        subtitleCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        subtitleCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);

        XSSFCellStyle changeLineStyle = (XSSFCellStyle) workbook.createCellStyle();
        changeLineStyle.setFont(getFont(workbook, (short) 11, false, true));
        changeLineStyle.setWrapText(true);
        ;//设置自动换行
        changeLineStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        changeLineStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        changeLineStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(242, 242, 242)));
        changeLineStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        changeLineStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        changeLineStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        changeLineStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        changeLineStyle.setBottomBorderColor(HSSFColor.BLACK.index);

        //异常样式
        XSSFCellStyle changeLineStyle2 = (XSSFCellStyle) workbook.createCellStyle();
        changeLineStyle2.setFont(getFont2(workbook, (short) 11, false, true));
        changeLineStyle2.setWrapText(true);
        ;//设置自动换行
        changeLineStyle2.setFillPattern(CellStyle.SOLID_FOREGROUND);
        changeLineStyle2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        changeLineStyle2.setFillForegroundColor(new XSSFColor(new java.awt.Color(242, 242, 242)));
        changeLineStyle2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        changeLineStyle2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        changeLineStyle2.setBorderRight(XSSFCellStyle.BORDER_THIN);
        changeLineStyle2.setBorderTop(XSSFCellStyle.BORDER_THIN);
        changeLineStyle2.setBottomBorderColor(HSSFColor.BLACK.index);

        XSSFCellStyle subheadStyle = (XSSFCellStyle) workbook.createCellStyle();
        subheadStyle.setFont(getFont(workbook, (short) 11, true, true));
        subheadStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        subheadStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        subheadStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        subheadStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(242, 242, 242)));
        subheadStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        subheadStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        subheadStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        subheadStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        subheadStyle.setBottomBorderColor(HSSFColor.BLACK.index);

        SXSSFRow row = sheet.createRow(0);
        SXSSFCell cell = row.createCell(0);
        row.setHeightInPoints(25);
        cell.setCellStyle(titleCellStyle);
        cell.setCellValue("智能巡检报告单");


        SXSSFRow row2 = sheet.createRow(2);
        row2.setHeightInPoints(20);
        SXSSFCell row2Cell0 = row2.createCell(0);
        SXSSFCell row2Cell1 = row2.createCell(1);
        SXSSFCell row2Cell2 = row2.createCell(2);
        row2Cell0.setCellValue("巡检人：" + req.getOperator());

        SXSSFCell row2Cell3 = row2.createCell(3);
        SXSSFCell row2Cell4 = row2.createCell(4);
        SXSSFCell row2Cell5 = row2.createCell(5);
        if (StrUtil.isEmpty(req.getXunjianShift())) {
            req.setXunjianShift("");
        }
        row2Cell3.setCellValue("班次：" + req.getXunjianShift());

        SXSSFCell row2Cell6 = row2.createCell(6);
        SXSSFCell row2Cell7 = row2.createCell(7);
        SXSSFCell row2Cell8 = row2.createCell(8);
        SXSSFCell row2Cell9 = row2.createCell(9);
        SXSSFCell row2Cell10 = row2.createCell(10);
        row2Cell6.setCellValue("巡检时间：" + DateUtil.format(xunjianTime, DatePattern.NORM_DATETIME_FORMAT));

        row2Cell0.setCellStyle(changeLineStyle);
        row2Cell1.setCellStyle(changeLineStyle);
        row2Cell2.setCellStyle(changeLineStyle);
        row2Cell3.setCellStyle(changeLineStyle);
        row2Cell4.setCellStyle(changeLineStyle);
        row2Cell5.setCellStyle(changeLineStyle);
        row2Cell6.setCellStyle(changeLineStyle);
        row2Cell7.setCellStyle(changeLineStyle);
        row2Cell8.setCellStyle(changeLineStyle);
        row2Cell9.setCellStyle(changeLineStyle);
        row2Cell10.setCellStyle(changeLineStyle);


        SXSSFRow row3 = sheet.createRow(3);
        row3.setHeightInPoints(32);
        SXSSFCell row3Cell0 = row3.createCell(0);
        SXSSFCell row3Cell1 = row3.createCell(1);
        SXSSFCell row3Cell2 = row3.createCell(2);
        row3Cell0.setCellValue("异常资产数量：" + req.getExceptionNum());

        SXSSFCell row3Cell3 = row3.createCell(3);
        SXSSFCell row3Cell4 = row3.createCell(4);
        SXSSFCell row3Cell5 = row3.createCell(5);
        row3Cell3.setCellValue("正常资产数量：" + req.getNormalNum());

        SXSSFCell row3Cell6 = row3.createCell(6);
        SXSSFCell row3Cell7 = row3.createCell(7);
        SXSSFCell row3Cell8 = row3.createCell(8);
        SXSSFCell row3Cell9 = row3.createCell(9);
        SXSSFCell row3Cell10 = row3.createCell(10);
        row3Cell6.setCellValue("巡检内容：" + req.getXunjianTarget());

        row3Cell0.setCellStyle(changeLineStyle);
        row3Cell1.setCellStyle(changeLineStyle);
        row3Cell2.setCellStyle(changeLineStyle);
        row3Cell3.setCellStyle(changeLineStyle);
        row3Cell4.setCellStyle(changeLineStyle);
        row3Cell5.setCellStyle(changeLineStyle);
        row3Cell6.setCellStyle(changeLineStyle);
        row3Cell7.setCellStyle(changeLineStyle);
        row3Cell8.setCellStyle(changeLineStyle);
        row3Cell9.setCellStyle(changeLineStyle);
        row3Cell10.setCellStyle(changeLineStyle);


        SXSSFRow row4 = sheet.createRow(4);
        row4.setHeightInPoints(20);
        SXSSFCell row4Cell0 = row4.createCell(0);
        SXSSFCell row4Cell1 = row4.createCell(1);
        SXSSFCell row4Cell2 = row4.createCell(2);
        SXSSFCell row4Cell3 = row4.createCell(3);
        SXSSFCell row4Cell4 = row4.createCell(4);
        SXSSFCell row4Cell5 = row4.createCell(5);
        SXSSFCell row4Cell6 = row4.createCell(6);
        SXSSFCell row4Cell7 = row4.createCell(7);
        SXSSFCell row4Cell8 = row4.createCell(8);
        SXSSFCell row4Cell9 = row4.createCell(9);
        SXSSFCell row4Cell10 = row4.createCell(10);
        row4Cell0.setCellStyle(changeLineStyle);
        row4Cell1.setCellStyle(changeLineStyle);
        row4Cell2.setCellStyle(changeLineStyle);
        row4Cell3.setCellStyle(changeLineStyle);
        row4Cell4.setCellStyle(changeLineStyle);
        row4Cell5.setCellStyle(changeLineStyle);
        row4Cell6.setCellStyle(changeLineStyle);
        row4Cell7.setCellStyle(changeLineStyle);
        row4Cell8.setCellStyle(changeLineStyle);
        row4Cell9.setCellStyle(changeLineStyle);
        row4Cell10.setCellStyle(changeLineStyle);
        row4Cell0.setCellValue("巡视情况:");

        SXSSFRow row5 = sheet.createRow(5);
        row5.setHeightInPoints(30);
        SXSSFCell row5Cell0 = row5.createCell(0);
        SXSSFCell row5Cell8 = row5.createCell(8);
        SXSSFCell row5Cell9 = row5.createCell(9);
        row5Cell0.setCellStyle(changeLineStyle);
        row5Cell8.setCellStyle(changeLineStyle);
        row5Cell9.setCellStyle(changeLineStyle);
        String format = String.format("%s，%s至%s使用自动巡视功能/人工巡视功能共对%s台设备进行巡视，其中状态正常：%s台、状态异常：%s，巡视项目包括：%s。",
                DateUtil.format(xunjianTime, "yyyy年MM月dd日"), DateUtil.format(xunjianTime, "HH时mm分"),
                DateUtil.format(xunjianEndTime, "HH时mm分"), req.getExceptionNum() + req.getNormalNum(),
                req.getNormalNum(), req.getExceptionNum(), req.getXunjianTarget());
        row5Cell0.setCellValue(format);

        SXSSFRow row6 = sheet.createRow(6);
        row6.setHeightInPoints(20);
        SXSSFCell row6Cell0 = row6.createCell(0);
        row6Cell0.setCellStyle(changeLineStyle);
        row6Cell0.setCellValue("巡检资产详情列表");
        row6Cell0.setCellStyle(subtitleCellStyle);


        SXSSFRow row7 = sheet.createRow(7);
        row7.setHeightInPoints(20);
        SXSSFCell row7Cell0 = row7.createCell(0);
        row7Cell0.setCellStyle(subheadStyle);
        row7Cell0.setCellValue("资产名称");

        SXSSFCell row7Cell1 = row7.createCell(1);
        row7Cell1.setCellStyle(subheadStyle);
        row7Cell1.setCellValue("CPU使用率");

        SXSSFCell row7Cell2 = row7.createCell(2);
        row7Cell2.setCellStyle(subheadStyle);
        row7Cell2.setCellValue("内存使用率");

        SXSSFCell row7Cell3 = row7.createCell(3);
        row7Cell3.setCellStyle(subheadStyle);
        row7Cell3.setCellValue("磁盘使用率");

        SXSSFCell row7Cell4 = row7.createCell(4);
        row7Cell4.setCellStyle(subheadStyle);
        row7Cell4.setCellValue("网卡状态");

        SXSSFCell row7Cell5 = row7.createCell(5);
        row7Cell5.setCellStyle(subheadStyle);
        row7Cell5.setCellValue("端口流入率");

        SXSSFCell row7Cell6 = row7.createCell(6);
        row7Cell6.setCellStyle(subheadStyle);
        row7Cell6.setCellValue("端口流出率");

        SXSSFCell row7Cell7 = row7.createCell(7);
        row7Cell7.setCellStyle(subheadStyle);
        row7Cell7.setCellValue("监控进程状态");

        SXSSFCell row7Cell8 = row7.createCell(8);
        row7Cell8.setCellStyle(subheadStyle);
        row7Cell8.setCellValue("告警相关信息");

        SXSSFCell row7Cell9 = row7.createCell(9);
        row7Cell9.setCellStyle(subheadStyle);
        row7Cell9.setCellValue("运行时长");

        SXSSFCell row7Cell10 = row7.createCell(10);
        row7Cell10.setCellStyle(subheadStyle);
        row7Cell10.setCellValue("oracle状态");

        int cellNum = 8;
        List<XunjianRepoBody> reportList = req.getReportList();
        for (XunjianRepoBody body : reportList) {
            cellNum = cellNum++;
            SXSSFRow row8 = sheet.createRow(cellNum++);

            // 资产名称
            SXSSFCell createCell = row8.createCell(0);
            createCell.setCellValue(body.getAssetName());
            createCell.setCellStyle(changeLineStyle);
            // CPU使用率
            SXSSFCell createCell2 = row8.createCell(1);
            createCell2.setCellValue(body.getCpuResultMsg().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            if (XunjianDetail.NORMAL_FLAG.equals(body.getCpuNormalFlag())) {
                createCell2.setCellStyle(changeLineStyle);
            } else {
                createCell2.setCellStyle(changeLineStyle2);
            }
            // 内存使用率
            SXSSFCell createCell3 = row8.createCell(2);
            createCell3.setCellValue(body.getMemoryResultMsg().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            createCell3.setCellStyle(changeLineStyle);
            if (XunjianDetail.NORMAL_FLAG.equals(body.getMemoryNormalFlag())) {
                createCell3.setCellStyle(changeLineStyle);
            } else {
                createCell3.setCellStyle(changeLineStyle2);
            }
            // 磁盘使用率
            SXSSFCell createCell4 = row8.createCell(3);
            createCell4.setCellValue(body.getDiskResultMsg().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            createCell4.setCellStyle(changeLineStyle);
            if (XunjianDetail.NORMAL_FLAG.equals(body.getDiskNormalFlag())) {
                createCell4.setCellStyle(changeLineStyle);
            } else {
                createCell4.setCellStyle(changeLineStyle2);
            }
            // 网卡状态
            SXSSFCell createCell5 = row8.createCell(4);
            createCell5.setCellValue(body.getNetCardResultMsg().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            createCell5.setCellStyle(changeLineStyle);
            if (XunjianDetail.NORMAL_FLAG.equals(body.getNetCardNormalFlag())) {
                createCell5.setCellStyle(changeLineStyle);
            } else {
                createCell5.setCellStyle(changeLineStyle2);
            }

            // 端口流入率
            SXSSFCell createCell6 = row8.createCell(5);
            createCell6.setCellValue(body.getPortInResultMsg().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            createCell6.setCellStyle(changeLineStyle);
            if (XunjianDetail.NORMAL_FLAG.equals(body.getPortInNormalFlag())) {
                createCell6.setCellStyle(changeLineStyle);
            } else {
                createCell6.setCellStyle(changeLineStyle2);
            }
            // 端口流出率
            SXSSFCell createCell7 = row8.createCell(6);
            createCell7.setCellValue(body.getPortOutResultMsg().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            createCell7.setCellStyle(changeLineStyle);
            if (XunjianDetail.NORMAL_FLAG.equals(body.getPortOutNormalFlag())) {
                createCell7.setCellStyle(changeLineStyle);
            } else {
                createCell7.setCellStyle(changeLineStyle2);
            }

            // 软件运行状态
            SXSSFCell createCell8 = row8.createCell(7);
            createCell8.setCellValue(body.getSoftwareResultMsg().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            createCell8.setCellStyle(changeLineStyle);
            if (XunjianDetail.NORMAL_FLAG.equals(body.getSoftwareNormalFlag())) {
                createCell8.setCellStyle(changeLineStyle);
            } else {
                createCell8.setCellStyle(changeLineStyle2);
            }

            // 告警相关信息
            SXSSFCell createCell9 = row8.createCell(8);
            createCell9.setCellValue(body.getAlarmResultMsg().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            createCell9.setCellStyle(changeLineStyle);
            if (XunjianDetail.NORMAL_FLAG.equals(body.getAlarmNormalFlag())) {
                createCell9.setCellStyle(changeLineStyle);
            } else {
                createCell9.setCellStyle(changeLineStyle2);
            }

            // 运行时长
            SXSSFCell createCell10 = row8.createCell(9);
            createCell10.setCellValue(body.getRunTimeMag());
            createCell10.setCellStyle(changeLineStyle);


            // 软件运行状态
            SXSSFCell createCell11 = row8.createCell(10);
            createCell11.setCellValue(body.getOracleMag().replace("<br/>", "").replace(",【", "\r【").replace("。【", "\r【"));
            createCell11.setCellStyle(changeLineStyle);
            if (XunjianDetail.NORMAL_FLAG.equals(body.getOracleFlag())) {
                createCell11.setCellStyle(changeLineStyle);
            } else {
                createCell11.setCellStyle(changeLineStyle2);
            }

        }
        int rowNumberUp = cellNum;
        CellRangeAddress rowNCell0Stell = new CellRangeAddress(rowNumberUp, rowNumberUp, 0, 10);
        int rowNumberUp1 = rowNumberUp + 1;
        CellRangeAddress rowN1Cell0Stell = new CellRangeAddress(rowNumberUp1, rowNumberUp1, 0, 3);
        CellRangeAddress rowN1Cell1Stell = new CellRangeAddress(rowNumberUp1, rowNumberUp1, 4, 10);


        SXSSFRow rowN = sheet.createRow(rowNumberUp);
        SXSSFCell rowNCell0 = rowN.createCell(0);
        rowN.setHeightInPoints(18);
        rowNCell0.setCellValue("告警明细");
        rowNCell0.setCellStyle(subtitleCellStyle);


        SXSSFRow rowN1 = sheet.createRow(rowNumberUp1);
        rowN1.setHeightInPoints(20);
        SXSSFCell rowN1Cell0 = rowN1.createCell(0);
        SXSSFCell rowN1Cell1 = rowN1.createCell(1);
        SXSSFCell rowN1Cell2 = rowN1.createCell(2);
        SXSSFCell rowN1Cell3 = rowN1.createCell(3);
        SXSSFCell rowN1Cell4 = rowN1.createCell(4);
        SXSSFCell rowN1Cell5 = rowN1.createCell(5);
        SXSSFCell rowN1Cell6 = rowN1.createCell(6);
        SXSSFCell rowN1Cell7 = rowN1.createCell(7);
        SXSSFCell rowN1Cell8 = rowN1.createCell(8);
        SXSSFCell rowN1Cell9 = rowN1.createCell(9);
        SXSSFCell rowN1Cell10 = rowN1.createCell(10);

        rowN1Cell0.setCellStyle(subheadStyle);
        rowN1Cell1.setCellStyle(subheadStyle);
        rowN1Cell2.setCellStyle(subheadStyle);
        rowN1Cell3.setCellStyle(subheadStyle);
        rowN1Cell4.setCellStyle(subheadStyle);
        rowN1Cell5.setCellStyle(subheadStyle);
        rowN1Cell6.setCellStyle(subheadStyle);
        rowN1Cell7.setCellStyle(subheadStyle);
        rowN1Cell8.setCellStyle(subheadStyle);
        rowN1Cell9.setCellStyle(subheadStyle);
        rowN1Cell10.setCellStyle(subheadStyle);
        rowN1Cell0.setCellValue("告警信息");
        rowN1Cell4.setCellValue("处理意见");

        sheet.addMergedRegion(titleStyle);
        sheet.addMergedRegion(row2Cell0Stell);
        sheet.addMergedRegion(row2Cell1Stell);
        sheet.addMergedRegion(row2Cell1Stel2);
        sheet.addMergedRegion(row3Cell0Stell);
        sheet.addMergedRegion(row3Cell1Stell);
        sheet.addMergedRegion(row3Cell1Stel2);
        sheet.addMergedRegion(row4Cell1Stel0);
        sheet.addMergedRegion(row5Cell1Stel0);
        sheet.addMergedRegion(row6Cell1Stel0);
        sheet.addMergedRegion(rowNCell0Stell);
        sheet.addMergedRegion(rowN1Cell0Stell);
        sheet.addMergedRegion(rowN1Cell1Stell);

        List<XunjianAlarmMsg> alarmList = req.getAlarmList();
        for (XunjianAlarmMsg alarmInfo : alarmList) {
            int i = rowNumberUp1 + 1;
            SXSSFRow rowN2 = sheet.createRow(i);
            rowN2.setHeightInPoints(50);
            SXSSFCell createCell0 = rowN2.createCell(0);
            SXSSFCell createCell1 = rowN2.createCell(1);
            SXSSFCell createCell2 = rowN2.createCell(2);
            SXSSFCell createCell3 = rowN2.createCell(3);

            createCell0.setCellValue(alarmInfo.getContent());
            SXSSFCell createCell4 = rowN2.createCell(4);
            SXSSFCell createCell5 = rowN2.createCell(5);
            SXSSFCell createCell6 = rowN2.createCell(6);
            SXSSFCell createCell7 = rowN2.createCell(7);
            SXSSFCell createCell8 = rowN2.createCell(8);
            SXSSFCell createCell9 = rowN2.createCell(9);
            SXSSFCell createCell10 = rowN2.createCell(10);

            createCell4.setCellValue(alarmInfo.getOpinion());
            createCell0.setCellStyle(changeLineStyle);
            createCell1.setCellStyle(changeLineStyle);
            createCell2.setCellStyle(changeLineStyle);
            createCell3.setCellStyle(changeLineStyle);
            createCell4.setCellStyle(changeLineStyle);
            createCell5.setCellStyle(changeLineStyle);
            createCell6.setCellStyle(changeLineStyle);
            createCell7.setCellStyle(changeLineStyle);
            createCell8.setCellStyle(changeLineStyle);
            createCell9.setCellStyle(changeLineStyle);
            createCell10.setCellStyle(changeLineStyle);

            CellRangeAddress item0 = new CellRangeAddress(i, i, 0, 3);
            CellRangeAddress item1 = new CellRangeAddress(i, i, 4, 10);
            sheet.addMergedRegion(item0);
            sheet.addMergedRegion(item1);
            rowNumberUp1++;
        }


        return workbook;
    }


    /**
     * 根据行内容重新计算行高
     *
     * @param
     */
    private static Font getFont(SXSSFWorkbook workbook, short size, Boolean bold, boolean color) {
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints(size);
        if (color) {
            font.setColor(HSSFColor.BLACK.index);
        } else {
            font.setColor(HSSFColor.WHITE.index);
        }

        if (bold) {
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        }
        font.setItalic(false);
        font.setStrikeout(false);
        // font.setUnderline((byte) 1);
        return font;
    }

    /**
     * 根据行内容重新计算行高
     *
     * @param
     */
    private static Font getFont2(SXSSFWorkbook workbook, short size, Boolean bold, boolean color) {
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints(size);
        if (color) {
            font.setColor(HSSFColor.RED.index);
        } else {
            font.setColor(HSSFColor.WHITE.index);
        }

        if (bold) {
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        }
        font.setItalic(false);
        font.setStrikeout(false);
        // font.setUnderline((byte) 1);
        return font;
    }

    public static String formatDateTime(long mss) {
        String DateTimes = null;
        long days = mss / (60 * 60 * 24);
        long hours = (mss % (60 * 60 * 24)) / (60 * 60);
        long minutes = (mss % (60 * 60)) / 60;
        long seconds = mss % 60;
        if (days > 0) {
            DateTimes = days + "天" + hours + "小时" + minutes + "分钟"
                    + seconds + "秒";
        } else if (hours > 0) {
            DateTimes = hours + "小时" + minutes + "分钟"
                    + seconds + "秒";
        } else if (minutes > 0) {
            DateTimes = minutes + "分钟"
                    + seconds + "秒";
        } else {
            DateTimes = seconds + "秒";
        }

        return DateTimes;
    }


}
