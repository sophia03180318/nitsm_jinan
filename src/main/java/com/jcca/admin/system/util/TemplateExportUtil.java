package com.jcca.admin.system.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.jcca.admin.system.entity.ImportCabinet;
import com.jcca.admin.system.entity.SysUser;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author：sophia
 * @ Date：Created in 14:08 2021/6/29
 * @ Description:
 */
public class TemplateExportUtil {
    /*资产导出模板格式*/
    public static SXSSFWorkbook createExcel(List<String> attributeList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
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

        SXSSFRow row0 = sheet.createRow(0);
        SXSSFRow row1 = sheet.createRow(1);
        row0.setHeightInPoints(31);
        row1.setZeroHeight(true);

        for (int i = 0; i < attributeList.size(); i++) {
            sheet.setColumnWidth(i, (int) 11 * 256);

            SXSSFCell headerCell = row0.createCell(i);
            headerCell.setCellValue(DictUtil.keyValue("TEMPLATE_KV", attributeList.get(i)));
            headerCell.setCellStyle(headerCellStyle);

            SXSSFCell titleCell = row1.createCell(i);
            titleCell.setCellValue(attributeList.get(i).trim());
            titleCell.setCellStyle(titleCellStyle);

        }

        return workbook;
    }

    /*机柜导入格式*/
    public static SXSSFWorkbook createCabinetExcel(Map<String, String> cabinetList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
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

        SXSSFRow row0 = sheet.createRow(0);
        SXSSFRow row1 = sheet.createRow(1);
        row0.setHeightInPoints(31);
        row1.setZeroHeight(true);
        int i = 0;
        for (Map.Entry<String, String> nameValue : cabinetList.entrySet()) {
            String key = nameValue.getKey();
            String value = cabinetList.get(key).trim();

            sheet.setColumnWidth(i, (int) 11 * 256);

            SXSSFCell headerCell = row0.createCell(i);
            headerCell.setCellValue(key);
            headerCell.setCellStyle(headerCellStyle);

            SXSSFCell titleCell = row1.createCell(i);
            titleCell.setCellValue(value);
            titleCell.setCellStyle(titleCellStyle);
            i++;
        }

        return workbook;
    }

    /*用户导出格式*/
    public static SXSSFWorkbook exportUser(List<SysUser> userList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        SXSSFSheet sheet = workbook.createSheet();

        XSSFCellStyle headerCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerCellStyle.setFont(getFont(workbook, (short) 11, false, true));
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        headerCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        headerCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(189, 215, 238)));
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
        titleCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(242, 242, 242)));
        titleCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        titleCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);

        SXSSFRow row0 = sheet.createRow(0);
        row0.setHeightInPoints(31);
        SXSSFCell Cell0 = row0.createCell(0);
        Cell0.setCellValue("用户名");
        Cell0.setCellStyle(headerCellStyle);
        SXSSFCell Cell1 = row0.createCell(1);
        Cell1.setCellValue("昵称");
        Cell1.setCellStyle(headerCellStyle);
        SXSSFCell Cell2 = row0.createCell(2);
        Cell2.setCellValue("性别");
        Cell2.setCellStyle(headerCellStyle);
        SXSSFCell Cell3 = row0.createCell(3);
        Cell3.setCellValue("手机");
        Cell3.setCellStyle(headerCellStyle);
        SXSSFCell Cell4 = row0.createCell(4);
        Cell4.setCellValue("创建时间");
        Cell4.setCellStyle(headerCellStyle);
        sheet.setColumnWidth(0, (int) 11 * 256);
        sheet.setColumnWidth(1, (int) 11 * 256);
        sheet.setColumnWidth(2, (int) 11 * 256);
        sheet.setColumnWidth(3, (int) 11 * 256);
        sheet.setColumnWidth(4, (int) 22 * 256);


        for (int i = 0; i < userList.size(); i++) {
            SXSSFRow row = sheet.createRow(i + 1);

            SXSSFCell usernameCell = row.createCell(0);
            usernameCell.setCellValue(userList.get(i).getUsername());
            usernameCell.setCellStyle(titleCellStyle);

            SXSSFCell nicknameCell = row.createCell(1);
            nicknameCell.setCellValue(userList.get(i).getNickname());
            nicknameCell.setCellStyle(titleCellStyle);

            SXSSFCell genderCell = row.createCell(2);
            Byte gender = userList.get(i).getGender();
            if (gender.toString().equals("1")) {
                genderCell.setCellValue("男");
            } else {
                genderCell.setCellValue("女");
            }
            genderCell.setCellStyle(titleCellStyle);

            SXSSFCell phoneCell = row.createCell(3);
            phoneCell.setCellValue(userList.get(i).getPhone());
            phoneCell.setCellStyle(titleCellStyle);

            SXSSFCell createTimeeCell = row.createCell(4);
            String date = DateUtil.format(userList.get(i).getCreateTime(), "yyyy-MM-dd HH:mm:ss");
            createTimeeCell.setCellValue(date);
            createTimeeCell.setCellStyle(titleCellStyle);


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


    /*获取对象所有非空属性列表*/
    public static ArrayList<String> isAllFieldNotNull(Object obj) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        Class stuCla = (Class) obj.getClass();// 得到类对象
        Field[] fs = stuCla.getDeclaredFields();//得到属性集合
        for (Field f : fs) {//遍历属性
            f.setAccessible(true); // 设置属性是可以访问的(私有的也可以)
            Object val = f.get(obj);// 得到此属性的值
            if (val != null && f.getName() != "serialVersionUID") {
                list.add(val + "");
            }
        }
        return list;
    }

    /*获取非空属性值字符串(排除list中)  皆为空返回 "" */
    public static String getValuesStr(Object obj, List<String> nameList) throws NoSuchFieldException, IllegalAccessException {
        String valueStr = "";
        Class stuCla = (Class) obj.getClass();
        Field[] fs = stuCla.getDeclaredFields();//得到属性集合
        for (Field f : fs) {
            f.setAccessible(true);
            Object val = f.get(obj);
            if (ObjectUtil.isNotNull(val) && !nameList.contains(f.getName())) {
                String key = DictUtil.keyValue("TEMPLATE_KV", val.toString());
                valueStr += key + ",";
            }
        }

        if (valueStr.isEmpty()) {
            return valueStr;
        }
        return valueStr.substring(0, valueStr.length() - 1);

    }


    /*给指定对象一系列属性进行赋值操作*/
    public static void setFieldValues(Object obj, String name, String value) {
        Class stuCla = (Class) obj.getClass();
        String methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method setMethod = null;
        try {
            setMethod = stuCla.getDeclaredMethod("set" + methodName, String.class);
            setMethod.setAccessible(true);
            //执行该set方法
            setMethod.invoke(obj, value);
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (NoSuchMethodException e) {
        }

    }

    public static SXSSFWorkbook createErrorCabinetExcel(Map<String, String> cabinetTemplate, List<ImportCabinet> list) {
        SXSSFWorkbook workbook = createCabinetExcel(cabinetTemplate);
        SXSSFSheet sheet = workbook.getSheetAt(0);

        for (int i = 0; i < list.size(); i++) {
            SXSSFRow row = sheet.createRow(i + 2);

            SXSSFCell cell1 = row.createCell(0);
            cell1.setCellValue(list.get(i).getOrgName());
            SXSSFCell cell2 = row.createCell(1);
            cell2.setCellValue(list.get(i).getRoomName());
            SXSSFCell cell3 = row.createCell(2);
            cell3.setCellValue(list.get(i).getName());
            SXSSFCell cell4 = row.createCell(3);
            cell4.setCellValue(list.get(i).getCode());
            SXSSFCell cell5 = row.createCell(4);
            cell5.setCellValue(list.get(i).getRowIndex());
            SXSSFCell cell6 = row.createCell(5);
            cell6.setCellValue(list.get(i).getColumnIndex());
            SXSSFCell cell7 = row.createCell(6);
            cell7.setCellValue(list.get(i).getRemark());
            SXSSFCell cell8 = row.createCell(7);
            cell8.setCellValue(list.get(i).getQrCodeNum());
            SXSSFCell cell9 = row.createCell(8);
            cell9.setCellValue(list.get(i).getErrorLog());

        }

        return workbook;
    }
}
