package com.jcca.web.asset.utils;

import com.jcca.poi.hssf.usermodel.HSSFCellStyle;
import com.jcca.poi.hssf.usermodel.HSSFFont;
import com.jcca.poi.hssf.util.HSSFColor;
import com.jcca.poi.ss.usermodel.CellStyle;
import com.jcca.poi.ss.usermodel.Font;
import com.jcca.poi.xssf.streaming.SXSSFCell;
import com.jcca.poi.xssf.streaming.SXSSFRow;
import com.jcca.poi.xssf.streaming.SXSSFSheet;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import com.jcca.poi.xssf.usermodel.XSSFCellStyle;
import com.jcca.poi.xssf.usermodel.XSSFColor;
import com.jcca.web.asset.entity.AssetImportRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 14:55 2021/7/13
 * @ Description:资产导入失败记录导出工具
 */
@Slf4j
public class AssetReportUtil {
    /*资产导出格式*/
    public static SXSSFWorkbook exportAssetExecl(List<String> headList, List<String> titleList, List<AssetImportRecord> assetList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(assetList.size() + 10);
        SXSSFSheet sheet = workbook.createSheet();

        XSSFCellStyle headerCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerCellStyle.setFont(getFont(workbook, (short) 11, false, true));
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        headerCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        headerCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(146, 208, 80)));
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);


        XSSFCellStyle titleCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        titleCellStyle.setFont(getFont(workbook, (short) 10, false, true));
        titleCellStyle.setWrapText(true);//设置自动换行
        titleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        titleCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 230, 153)));
        titleCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);


        XSSFCellStyle lineStyle1 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle1.setFont(getFont(workbook, (short) 10, false, true));
        lineStyle1.setWrapText(true);//设置自动换行
        lineStyle1.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle1.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        lineStyle1.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle1.setFillForegroundColor(new XSSFColor(new java.awt.Color(226, 239, 218)));
        lineStyle1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderTop(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBottomBorderColor(HSSFColor.BLACK.index);

        XSSFCellStyle lineStyle2 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle2.setFont(getFont(workbook, (short) 10, false, true));
        lineStyle2.setWrapText(true);//设置自动换行
        lineStyle2.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        lineStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle2.setFillForegroundColor(new XSSFColor(new java.awt.Color(198, 224, 180)));
        lineStyle2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderTop(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBottomBorderColor(HSSFColor.BLACK.index);


        SXSSFRow row0 = sheet.createRow(0);
        row0.setHeightInPoints(31);
        for (int i = 0; i < headList.size(); i++) {
            sheet.setColumnWidth(i, (int) 11 * 256);
            SXSSFCell headerCell = row0.createCell(i);
            headerCell.setCellValue(headList.get(i));
            headerCell.setCellStyle(headerCellStyle);
        }

        SXSSFRow row1 = sheet.createRow(1);
        row1.setZeroHeight(true);
        for (int i = 0; i < titleList.size(); i++) {
            SXSSFCell titleCell = row1.createCell(i);
            titleCell.setCellValue(titleList.get(i).trim());
            titleCell.setCellStyle(titleCellStyle);
        }

        SXSSFRow temRow;
        SXSSFCell temCell;

        for (int i = 0; i < assetList.size(); i++) {
            temRow = sheet.createRow(2 + i);
            temRow.setHeightInPoints(25);
            for (int j = 0; j < titleList.size(); j++) {
                temCell = temRow.createCell(j);
                String value = "";
                try {
                    if (titleList.get(j).equals("CR")) {
                        value = assetList.get(i).getShowCore();
                    } else {
                        value = AssetImportUtils.getFieldValues(assetList.get(i), titleList.get(j));
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    log.error("对象属性值" + titleList.get(j) + "get出错");
                } finally {
                    temCell.setCellValue(value);
                    if (i % 2 == 0) {
                        temCell.setCellStyle(lineStyle1);
                    } else {
                        temCell.setCellStyle(lineStyle2);
                    }

                }

            }

        }

        return workbook;
    }

    /*设定错误资产导出格式*/
    public static SXSSFWorkbook createExcel(List<String> headList, List<String> titleList, List<AssetImportRecord> assetList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(assetList.size() + 10);
        SXSSFSheet sheet = workbook.createSheet();

        XSSFCellStyle headerCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerCellStyle.setFont(getFont(workbook, (short) 11, false, true));
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        headerCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        headerCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(146, 208, 80)));
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);


        XSSFCellStyle titleCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        titleCellStyle.setFont(getFont(workbook, (short) 10, false, true));
        titleCellStyle.setWrapText(true);//设置自动换行
        titleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        titleCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 230, 153)));
        titleCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);


        XSSFCellStyle lineStyle1 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle1.setFont(getFont(workbook, (short) 10, false, true));
        lineStyle1.setWrapText(true);//设置自动换行
        lineStyle1.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle1.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        lineStyle1.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle1.setFillForegroundColor(new XSSFColor(new java.awt.Color(226, 239, 218)));
        lineStyle1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderTop(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBottomBorderColor(HSSFColor.BLACK.index);

        XSSFCellStyle lineStyle2 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle2.setFont(getFont(workbook, (short) 10, false, true));
        lineStyle2.setWrapText(true);//设置自动换行
        lineStyle2.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        lineStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle2.setFillForegroundColor(new XSSFColor(new java.awt.Color(198, 224, 180)));
        lineStyle2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderTop(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBottomBorderColor(HSSFColor.BLACK.index);

        SXSSFRow row0 = sheet.createRow(0);
        row0.setHeightInPoints(31);
        for (int i = 0; i < headList.size(); i++) {
            sheet.setColumnWidth(i, (int) 11 * 256);
            SXSSFCell headerCell = row0.createCell(i);
            headerCell.setCellValue(headList.get(i));
            headerCell.setCellStyle(headerCellStyle);
        }

        SXSSFRow row1 = sheet.createRow(1);
        row1.setZeroHeight(true);
        for (int i = 0; i < titleList.size(); i++) {
            SXSSFCell titleCell = row1.createCell(i);
            titleCell.setCellValue(titleList.get(i).trim());
            titleCell.setCellStyle(titleCellStyle);
        }

        SXSSFRow temRow;
        SXSSFCell temCell;

        for (int i = 0; i < assetList.size(); i++) {
            temRow = sheet.createRow(2 + i);
            temRow.setHeightInPoints(25);
            for (int j = 0; j < titleList.size(); j++) {
                temCell = temRow.createCell(j);
                String value = "";
                try {
                    value = AssetImportUtils.getFieldValues(assetList.get(i), titleList.get(j));
                } catch (NoSuchFieldException e) {
                    log.error("对象属性值" + titleList.get(j) + "get出错");
                } catch (IllegalAccessException e) {
                    log.error("对象属性值" + titleList.get(j) + "get出错");
                } finally {
                    temCell.setCellValue(value);
                    if (i % 2 == 0) {
                        temCell.setCellStyle(lineStyle1);
                    } else {
                        temCell.setCellStyle(lineStyle2);
                    }

                }

            }

        }


        return workbook;
    }

    /*设定字体格式*/
    private static Font getFont(SXSSFWorkbook workbook, short size, Boolean bold, boolean color) {
        Font font = workbook.createFont();
        font.setFontName("等线");
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
}
