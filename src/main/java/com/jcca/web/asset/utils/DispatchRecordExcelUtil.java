package com.jcca.web.asset.utils;

import com.jcca.poi.hssf.usermodel.HSSFBorderFormatting;
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
import com.jcca.web.asset.utils.bean.ApplicationBase;
import com.jcca.web.asset.utils.bean.ApplicationVersionBase;
import com.jcca.web.asset.utils.bean.AssetRecordReq;
import com.jcca.web.asset.utils.bean.HardwareBase;
import com.jcca.web.asset.utils.enums.ManufacturersEnum;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 调度台导出档案工具
 *
 * @author Lvyp
 */
@Slf4j
public class DispatchRecordExcelUtil {

    /**
     * 导出一机一档
     *
     * @param req
     * @return
     */
    public static SXSSFWorkbook createExcel(AssetRecordReq req) {
        List<ApplicationBase> applicationList = req.getApplicationList();
        List<HardwareBase> hardwareList = req.getHardwareList();

        CellRangeAddress titleStyle = new CellRangeAddress(0, 1, 0, 7);
        CellRangeAddress cellStyle3 = new CellRangeAddress(3, 3, 5, 7);
        CellRangeAddress cellStyle4 = new CellRangeAddress(4, 4, 1, 7);
        CellRangeAddress cellStyle5 = new CellRangeAddress(5, 5, 0, 3);
        CellRangeAddress cellStyle5s = new CellRangeAddress(5, 5, 4, 7);
        CellRangeAddress cellStyle6 = new CellRangeAddress(6, 6, 1, 7);
        CellRangeAddress cellStyle7 = new CellRangeAddress(7, 7, 1, 7);
        CellRangeAddress cellStyle8 = new CellRangeAddress(8, 8, 1, 3);
        CellRangeAddress cellStyle8s = new CellRangeAddress(8, 8, 4, 7);
        CellRangeAddress cellStyle9 = new CellRangeAddress(9, 9, 1, 3);
        CellRangeAddress cellStyle9s = new CellRangeAddress(9, 9, 4, 7);
        CellRangeAddress cellStyle6To9 = new CellRangeAddress(6, 9, 0, 0);

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);

        SXSSFSheet sheet = workbook.createSheet();
        sheet.setColumnWidth(0, (int) 35.7 * 300);
        sheet.setDefaultRowHeight((short) 300);

        int countSize = 10;
        for (ApplicationBase application : applicationList) {
            int size = application.getVersionList().size();
            int firstRow = countSize + 1;
            int lastRow = countSize + size;
            if (firstRow != lastRow) {
                CellRangeAddress item = new CellRangeAddress(firstRow, lastRow, 1, 1);
                countSize += size;
                sheet.addMergedRegion(item);
            }

        }

        if (countSize != 10) {
            CellRangeAddress cellStyle10 = new CellRangeAddress(10, countSize, 0, 0);
            sheet.addMergedRegion(cellStyle10);
        }

        sheet.addMergedRegion(titleStyle);
        sheet.addMergedRegion(cellStyle3);
        sheet.addMergedRegion(cellStyle4);
        sheet.addMergedRegion(cellStyle5);
        sheet.addMergedRegion(cellStyle5s);
        sheet.addMergedRegion(cellStyle6);
        sheet.addMergedRegion(cellStyle7);
        sheet.addMergedRegion(cellStyle8);
        sheet.addMergedRegion(cellStyle8s);
        sheet.addMergedRegion(cellStyle6To9);
        sheet.addMergedRegion(cellStyle9);
        sheet.addMergedRegion(cellStyle9s);

        SXSSFRow row = sheet.createRow(0);

        SXSSFCell cell = row.createCell(0);

        cell.setCellValue("CTC/TDCS" + req.getPlace().getMsg() + "调度台终端履历动态记录表");
        CellStyle titleCellStyle = createCellStyle(workbook, true, true, true, true);
        titleCellStyle.setFont(getFont(workbook, (short) 18, true));
        titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);//水平居中
        titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直居中

        cell.setCellStyle(titleCellStyle);


        SXSSFRow row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("名称");
        row2.createCell(1).setCellValue(req.getAssetName());
        row2.createCell(2).setCellValue("IP");
        row2.createCell(3).setCellValue(req.getAssetIp());
        row2.createCell(4).setCellValue("上道日期");
        row2.createCell(5).setCellValue(req.getStartDateStr());
        row2.createCell(6).setCellValue("下道日期");
        row2.createCell(7).setCellValue(req.getEndDateStr());

        SXSSFRow row3 = sheet.createRow(3);
        row3.createCell(0).setCellValue("安装位置");
        row3.createCell(1).setCellValue(req.getInstallPath());
        row3.createCell(2).setCellValue("序列号");
        row3.createCell(3).setCellValue(req.getImei());
        row3.createCell(4).setCellValue("质保期限");
        row3.createCell(5).setCellValue(req.getValidityDateStr());

        SXSSFRow row4 = sheet.createRow(4);
        row4.createCell(0).setCellValue("设备厂家");
        row4.createCell(1).setCellValue(ManufacturersEnum.changeManufacturers(req.getManufacturers()));

        SXSSFRow row5 = sheet.createRow(5);
        row5.createCell(0).setCellValue("主机编号：" + req.getHostNumber());
        row5.createCell(4).setCellValue("操作系统：" + req.getServerName());

        SXSSFRow row6 = sheet.createRow(6);
        row6.createCell(0).setCellValue("硬件配置信息");
        row6.createCell(1).setCellValue("主机型号：" + req.getAssetImage());

        SXSSFRow row7 = sheet.createRow(7);
        row7.createCell(1).setCellValue("内存容量：" + req.getMemory());

        SXSSFRow row8 = sheet.createRow(8);
        row8.createCell(1).setCellValue("硬盘数量：" + req.getDiskTotal());
        row8.createCell(4).setCellValue("单碟容量：" + req.getDiskCapacity());

        SXSSFRow row9 = sheet.createRow(9);
        row9.createCell(1).setCellValue("连接显示器数量：" + req.getDisplayerTotal());
        row9.createCell(4).setCellValue("视频线接口类型：" + req.getDisplayerPortModel());

        SXSSFRow row10 = sheet.createRow(10);
        row10.createCell(0).setCellValue("应用软件版本动态");
        row10.createCell(1).setCellValue("名称");
        row10.createCell(2).setCellValue("版本号");
        row10.createCell(3).setCellValue("上道日期");
        row10.createCell(4).setCellValue("下道日期");
        row10.createCell(5).setCellValue("变更原因");
        row10.createCell(6).setCellValue("厂家签名");
        row10.createCell(7).setCellValue("中心签名");

        int rowNext = 11;
        for (ApplicationBase application : applicationList) {
            SXSSFRow rowItem = sheet.createRow(rowNext);
            rowItem.createCell(1).setCellValue(application.getName());
            List<ApplicationVersionBase> versionList = application.getVersionList();
            for (ApplicationVersionBase versionBase : versionList) {
                rowItem.createCell(2).setCellValue(versionBase.getVersion());
                rowItem.createCell(3).setCellValue(versionBase.getStartDateStr());
                rowItem.createCell(4).setCellValue(versionBase.getEndDateStr());
                rowItem.createCell(5).setCellValue(versionBase.getChangeRemark());
                rowItem.createCell(6).setCellValue(versionBase.getSignature());
                rowItem.createCell(7).setCellValue(versionBase.getCenterSignature());
                rowNext += 1;
                rowItem = sheet.createRow(rowNext);
            }
        }

        int hardwareSize = hardwareList.size();
        CellRangeAddress hardwareStyle = new CellRangeAddress(rowNext, rowNext + hardwareSize, 0, 0);
        CellRangeAddress hardwareStyleItem = new CellRangeAddress(rowNext, rowNext, 1, 2);
        CellRangeAddress hardwareStyleItem2 = new CellRangeAddress(rowNext, rowNext, 3, 4);

        sheet.addMergedRegion(hardwareStyle);
        sheet.addMergedRegion(hardwareStyleItem);
        sheet.addMergedRegion(hardwareStyleItem2);

        SXSSFRow rowN = sheet.createRow(rowNext);
        rowN.createCell(0).setCellValue("硬件动态");
        rowN.createCell(1).setCellValue("硬件变更");
        rowN.createCell(3).setCellValue("变更原因");
        rowN.createCell(5).setCellValue("变更日期");
        rowN.createCell(6).setCellValue("厂家签名");
        rowN.createCell(7).setCellValue("中心签名");
        for (HardwareBase cellAddress : hardwareList) {
            rowNext += 1;
            CellRangeAddress hardwareStyleItem3 = new CellRangeAddress(rowNext, rowNext, 1, 2);
            CellRangeAddress hardwareStyleItem4 = new CellRangeAddress(rowNext, rowNext, 3, 4);
            sheet.addMergedRegion(hardwareStyleItem3);
            sheet.addMergedRegion(hardwareStyleItem4);

            rowN = sheet.createRow(rowNext);
            rowN.createCell(1).setCellValue(cellAddress.getName());
            rowN.createCell(3).setCellValue(cellAddress.getChangeRemark());
            rowN.createCell(5).setCellValue(cellAddress.getChangeDateStr());
            rowN.createCell(6).setCellValue(cellAddress.getSignature());
            rowN.createCell(7).setCellValue(cellAddress.getCenterSignature());
        }

        return workbook;
    }

    /**
     * 获取边框样式
     *
     * @param workbook
     * @param left
     * @param right
     * @param top
     * @param button
     * @return
     */
    public static CellStyle createCellStyle(SXSSFWorkbook workbook, Boolean left, Boolean right, Boolean top,
                                            Boolean button) {
        CellStyle style = workbook.createCellStyle();
        if (left) {
            style.setBorderLeft(HSSFBorderFormatting.BORDER_MEDIUM);
            style.setLeftBorderColor(HSSFColor.BLACK.index);
        }
        if (right) {
            style.setBorderRight(HSSFBorderFormatting.BORDER_MEDIUM);
            style.setRightBorderColor(HSSFColor.BLACK.index);
        }
        if (top) {
            style.setBorderTop(HSSFBorderFormatting.BORDER_MEDIUM);
            style.setTopBorderColor(HSSFColor.BLACK.index);
        }
        if (button) {
            style.setBorderBottom(HSSFBorderFormatting.BORDER_MEDIUM);
            style.setBottomBorderColor(HSSFColor.BLACK.index);

        }
        return style;
    }

    public static Font getFont(SXSSFWorkbook workbook, short size, Boolean bold) {
        Font font = workbook.createFont();
        font.setFontName("宋体");
        font.setFontHeightInPoints(size);
        font.setColor(HSSFColor.BLACK.index);
        if (bold) {
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        }
        font.setItalic(false);
        font.setStrikeout(false);
        //font.setUnderline((byte) 1);
        return font;
    }

    public static void responseBody(SXSSFWorkbook workbook, HttpServletResponse response, String name) {
        // response
        try {
            String fileName = URLEncoder.encode(name + ".xlsx", "UTF-8");
            response.setContentType("application/msexcel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.close();
            workbook.dispose();
            workbook.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
