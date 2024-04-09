package com.jcca.common.utils;

import com.jcca.poi.hssf.usermodel.HSSFBorderFormatting;
import com.jcca.poi.hssf.usermodel.HSSFFont;
import com.jcca.poi.hssf.util.HSSFColor;
import com.jcca.poi.ss.usermodel.CellStyle;
import com.jcca.poi.ss.usermodel.Font;
import com.jcca.poi.xssf.streaming.SXSSFCell;
import com.jcca.poi.xssf.streaming.SXSSFRow;
import com.jcca.poi.xssf.streaming.SXSSFWorkbook;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * excel 工具
 *
 * @author lyp
 */
@Slf4j
public class AppExcelUtil {

    /**
     * 设置单元格内容并且加上样式
     *
     * @param row
     * @param cellNum 第几个
     * @param style   样式
     * @return
     */
    public static SXSSFCell setCellMsg(SXSSFRow row, int cellNum, String cellMsg) {
        SXSSFCell rowCell = row.createCell(cellNum);
        rowCell.setCellValue(cellMsg);
        return rowCell;
    }

    /**
     * 批量设置样式
     *
     * @param row
     * @param start
     * @param end
     * @param style
     */
    public static void setCellStyle(SXSSFRow row, int start, int end, CellStyle style) {
        if (Objects.isNull(row) || Objects.isNull(start) || Objects.isNull(end) || Objects.isNull(start)) {
            return;
        }
        for (int i = start; i < end; i++) {
            SXSSFCell cell = row.getCell(i);
            if (Objects.isNull(cell)) {
                cell = row.createCell(i);
            }
            cell.setCellStyle(style);
        }
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
        // font.setUnderline((byte) 1);
        return font;
    }

    public static void responseBody(SXSSFWorkbook workbook, HttpServletResponse response) {
        // response
        try {
            String fileName = URLEncoder.encode("网络设备履历动态记录表.xlsx", "UTF-8");
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

    public static void responseBody(SXSSFWorkbook workbook, HttpServletResponse response, String title) {
        // response
        try {
            String fileName = URLEncoder.encode(title + ".xlsx", "UTF-8");
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
