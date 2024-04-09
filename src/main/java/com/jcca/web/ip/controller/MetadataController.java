package com.jcca.web.ip.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.biz.dao.MetadataMapper;
import com.jcca.admin.biz.entity.MetadataTable;
import com.jcca.admin.biz.vo.MetaDataRep;
import com.jcca.admin.system.entity.SpecDictionary;
import com.jcca.admin.system.entity.SysDict;
import com.jcca.admin.system.entity.SysMenu;
import com.jcca.admin.system.service.SpecDictionaryService;
import com.jcca.admin.system.service.SysDictService;
import com.jcca.admin.system.service.SysMenuService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ResultVoUtil;
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
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.asset.utils.AssetImportUtils;
import com.jcca.web.asset.utils.NullFieldException;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ Author：sophia
 * @ Date：Created in 11:39 2022/9/29
 * @ Description:数据校验
 */
@Slf4j
@Controller
@RequestMapping("/system/metadata")
@Api(tags = "数据")
public class MetadataController {
    public static List<MetaDataRep> exportList = new ArrayList<>();
    public static List<MetaDataRep> exportList2 = new ArrayList<>();
    @Value("${spring.datasource.druid.url}")
    private String oracleUrl;
    @Resource
    MetadataMapper metadataMapper;
    @Resource
    RedisService redisService;
    @Resource
    SpecDictionaryService specService;
    @Resource
    SysDictService dictService;
    @Resource
    SysMenuService menuService;
    @Resource
    AlarmRepositoryService repositoryService;


    @GetMapping("/index")
    public String index(Model model) {
        return "/biz/metadata/index";
    }

    @GetMapping("/index2")
    public String index2(Model model) {
        return "/biz/metadata/index2";
    }

    /**
     * 生成数据表格
     */
    @GetMapping("/getDataExecl")
    @ResponseBody
    public void getDataExecl(HttpServletResponse response) {
        LinkedHashMap<String, List<String>> tableNameAndColumns = new LinkedHashMap<>();
        ArrayList<String> sqlList = new ArrayList<>();
        int size = 3;
        //SYS_USER
        List<String> columns0 = Arrays.asList("SYS_USER", "USERNAME", "root");
        tableNameAndColumns.put("SYS_USER", columns0);
        sqlList.add("Insert into SYS_USER（ID,USERNAME,NICKNAME,PASSWORD,PWD_SALT,GENDER,PHONE,PICTURE,REMARK,STATUS,CREATE_TIME,CREATOR,MODIFY_TIME,MODIFIER) values ('1','root','根','ee38b87d2d9b91ee3d7bdfafa59abd6bbebc34cef6a2403392928fee647a5189','2hcHTK',2,'10086','/upload/picture/20200520/36e5ee26f39b4b3c98f9afa130c8e0b3.jpg','管理级超级管理员',1,to_date('2020-04-04 04:04:04','YYYY-MM-DD HH24:MI:SS'),'1',to_date('2020-08-13 12:00:41','YYYY-MM-DD HH24:MI:SS'),'root');");

        //ASSET_HARDWARE_TYPE
        List<String> columns1 = task("ASSET_HARDWARE_TYPE", "NAME");
        tableNameAndColumns.put("ASSET_HARDWARE_TYPE", columns1);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO ASSET_HARDWARE_TYPE (ID,TYPE,NAME,FRU,MANUFACTURER_ID,REMARK)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || TYPE || '''' || ','\n" +
                "|| '''' || NAME || '''' || ','\n" +
                "|| '''' || FRU || '''' || ','\n" +
                "|| '''' || MANUFACTURER_ID || '''' || ','\n" +
                "|| '''' || REMARK || '''' || ');'\n" +
                " From ASSET_HARDWARE_TYPE order by ID\n"));
        if (columns1.size() > size) {
            size = columns1.size();
        }

        //ASSET_MANUFACTURER
        List<String> columns2 = task("ASSET_MANUFACTURER", "NAME");
        tableNameAndColumns.put("ASSET_MANUFACTURER", columns2);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO ASSET_MANUFACTURER (ID,NAME,ADDRESS,PHONE,EMAIL,CONTACT_PERSON)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || NAME || '''' || ','\n" +
                "|| '''' || ADDRESS || '''' || ','\n" +
                "|| '''' || PHONE || '''' || ','\n" +
                "|| '''' || EMAIL || '''' || ','\n" +
                "|| '''' || CONTACT_PERSON || '''' || ');'\n" +
                " From ASSET_MANUFACTURER order by ID"));
        if (columns2.size() > size) {
            size = columns2.size();
        }

        //ASSET_MODE
        List<String> columns3 = task("ASSET_MODE", "CODE");
        tableNameAndColumns.put("ASSET_MODE", columns3);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO ASSET_MODE (ID,CODE,NAME)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || CODE || '''' || ','\n" +
                "|| '''' || NAME || '''' || ');'\n" +
                " From ASSET_MODE order by ID"));
        if (columns3.size() > size) {
            size = columns3.size();
        }

        //ASSET_MODEL
        List<String> columns4 = task("ASSET_MODEL", "MODEL");
        tableNameAndColumns.put("ASSET_MODEL", columns4);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO ASSET_MODEL (ID,MODEL,ASSET_MODE_ID)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || MODEL || '''' || ','\n" +
                "|| '''' || ASSET_MODE_ID || '''' || ');'\n" +
                " From ASSET_MODEL order by ID"));
        if (columns4.size() > size) {
            size = columns4.size();
        }

        //PERFORMANCE_TARGET
        List<String> columns5 = task("PERFORMANCE_TARGET", "TARGET_HANDLE");
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO PERFORMANCE_TARGET (ID,ASSET_MODE,SPEC_ID,IS_AVAILABLE,TARGET_HANDLE,TARGET_DESCRIPTION,CRON_EXPRESS,COMMAND,COMMAND_NAME,SLEEP_TIME,PING_TUNNEL)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || ASSET_MODE || '''' || ','\n" +
                "|| '''' || SPEC_ID || '''' || ','\n" +
                "|| '''' || IS_AVAILABLE || '''' || ','\n" +
                "|| '''' || TARGET_HANDLE || '''' || ','\n" +
                "|| '''' || TARGET_DESCRIPTION || '''' || ','\n" +
                "|| '''' || CRON_EXPRESS || '''' || ','\n" +
                "|| '''' || COMMAND || '''' || ','\n" +
                "|| '''' || COMMAND_NAME || '''' || ','\n" +
                "|| '''' || SLEEP_TIME || '''' || ','\n" +
                "|| '''' || PING_TUNNEL || '''' || ');'\n" +
                " From PERFORMANCE_TARGET order by ID"));
        tableNameAndColumns.put("PERFORMANCE_TARGET", columns5);
        if (columns5.size() > size) {
            size = columns5.size();
        }

        //SYS_MODULE_CONFIG
        List<String> columns6 = task("SYS_MODULE_CONFIG", "NAME");
        tableNameAndColumns.put("SYS_MODULE_CONFIG", columns6);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO SYS_MODULE_CONFIG (ID,NAME,VALUE,DESCRIPTION,SERVICE_TYPE,ORG_ID)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || NAME || '''' || ','\n" +
                "|| '''' || VALUE || '''' || ','\n" +
                "|| '''' || DESCRIPTION || '''' || ','\n" +
                "|| '''' || SERVICE_TYPE || '''' || ','\n" +
                "|| '''' || ORG_ID || '''' || ');'\n" +
                " From SYS_MODULE_CONFIG order by ID"));
        if (columns6.size() > size) {
            size = columns6.size();
        }

        //IMPORT_TEMPLATE
        List<String> columns7 = Arrays.asList("IMPORT_TEMPLATE", "ID", "1131", "1132");
        tableNameAndColumns.put("IMPORT_TEMPLATE", columns7);
        sqlList.add("INSERT INTO IMPORT_TEMPLATE(ID, TEMPLATE_NAME) VALUES ('1131', '导入模板(精简版)')");
        sqlList.add("INSERT INTO IMPORT_TEMPLATE(ID, TEMPLATE_NAME) VALUES ('1132', '导入模板(完整版)')");
        if (columns7.size() > size) {
            size = columns7.size();
        }

        //SYS_MENU
        List<String> column8 = new ArrayList<>();
        column8.add("SYS_MENU");
        column8.add("URL");
        column8.addAll(metadataMapper.taskMenu());
        tableNameAndColumns.put("SYS_MENU", column8);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO SYS_MENU (ID,TITLE,PID,PIDS,URL,PERMS,ICON,TYPE,SORT,STATUS,REMARK)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || TITLE || '''' || ','\n" +
                "|| '''' || PID || '''' || ','\n" +
                "|| '''' || PIDS || '''' || ','\n" +
                "|| '''' || URL || '''' || ','\n" +
                "|| '''' || PERMS || '''' || ','\n" +
                "|| '''' || ICON || '''' || ','\n" +
                "|| '''' || TYPE || '''' || ','\n" +
                "|| '''' || SORT || '''' || ','\n" +
                "|| '''' || STATUS || '''' || ','\n" +
                "|| '''' || REMARK || '''' || ');'\n" +
                " From SYS_MENU where status = 1 order by ID"));
        if (column8.size() > size) {
            size = column8.size();
        }


      /*  //ALARM_REPOSITORY
        List<String> column10 = new ArrayList<>();
        column10.add("ALARM_REPOSITORY");
        column10.add("ALARM_CODE,,FLAG_TYPE");
        column10.addAll(metadataMapper.taskAlarm());
        tableNameAndColumns.put("ALARM_REPOSITORY", column10);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO ALARM_REPOSITORY (ID,NAME,ALARM_LEVEL,ALARM_CODE,DESC_STR,PLAN_STR,CREATOR,MODIFIER,STATUS_FLAG,FLAG_TYPE,EVENT_TYPE_ID)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || NAME || '''' || ','\n" +
                "|| '''' || ALARM_LEVEL || '''' || ','\n" +
                "|| '''' || ALARM_CODE || '''' || ','\n" +
                "|| '''' || DESC_STR || '''' || ','\n" +
                "|| '''' || PLAN_STR || '''' || ','\n" +
                "|| '''' || CREATOR || '''' || ','\n" +
                "|| '''' || MODIFIER || '''' || ','\n" +
                "|| '''' || STATUS_FLAG || '''' || ','\n" +
                "|| '''' || FLAG_TYPE || '''' || ','\n" +
                "|| '''' || EVENT_TYPE_ID || '''' || ');'\n" +
                " From ALARM_REPOSITORY order by ID"));
        if (column10.size() > size) {
            size = column10.size();
        }*/

        //SPEC_DICT
        List<String> column9 = new ArrayList<>();
        column9.add("SPEC_DICT");
        column9.add("ASSET_MODE,,ASSET_IMAGE,,MANUFACTURER_ID,,SYSTEM_TYPE,,SPEC_ID");
        QueryWrapper<SpecDictionary> qw = new QueryWrapper<>();
        qw.orderByAsc("id");
        column9.addAll(specService.list(qw).stream().map(o -> o.toData()).collect(Collectors.toList()));
        tableNameAndColumns.put("SPEC_DICT", column9);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO SPEC_DICT (ID,ASSET_MODE,ASSET_IMAGE,MANUFACTURER_ID,SYSTEM_TYPE,SPEC_ID,REMARK)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || ASSET_MODE || '''' || ','\n" +
                "|| '''' || ASSET_IMAGE || '''' || ','\n" +
                "|| '''' || MANUFACTURER_ID || '''' || ','\n" +
                "|| '''' || SYSTEM_TYPE || '''' || ','\n" +
                "|| '''' || SPEC_ID || '''' || ','\n" +
                "|| '''' || REMARK || '''' || ');'\n" +
                " From SPEC_DICT order by ID"));
        if (column9.size() > size) {
            size = column9.size();
        }

        //SYS_DICT
        List<String> column0 = new ArrayList<>();
        column0.add("SYS_DICT");
        column0.add("NAME,,VALUE");
        QueryWrapper<SysDict> qw2 = new QueryWrapper<>();
        qw2.orderByAsc("id");
        qw2.eq("status", 1);
        column0.addAll(dictService.list(qw2).stream().map(o -> o.toData()).collect(Collectors.toList()));
        tableNameAndColumns.put("SYS_DICT", column0);
        sqlList.addAll(metadataMapper.taskSql("select 'INSERT INTO SYS_DICT (ID,TITLE,NAME,TYPE,VALUE,STATUS,REMARK)\n" +
                "VALUES(' || '''' || ID ||'''' || ','\n" +
                "|| '''' || TITLE || '''' || ','\n" +
                "|| '''' || NAME || '''' || ','\n" +
                "|| '''' || TYPE || '''' || ','\n" +
                "|| '''' || VALUE || '''' || ','\n" +
                "|| '''' || STATUS || '''' || ','\n" +
                "|| '''' || REMARK || '''' || ');'\n" +
                " From SYS_DICT where status =1 order by ID"));
        if (column0.size() > size) {
            size = column0.size();
        }

        SXSSFWorkbook metadataExecl = createMetadataExecl(tableNameAndColumns, sqlList, size);
        String url = oracleUrl.replace("jdbc:oracle:thin:@", "");
        AssetImportUtils.responseBody(metadataExecl, response, url + "表数据");

    }


    /**
     * 校验表数据内容
     */
    @PostMapping("/contrastData")
    @ResponseBody
    public ResultVo contrastData(@RequestParam("file") MultipartFile file) {
        try {
            if (Objects.isNull(file)) {
                throw new NullFieldException("模板文件不能为空,请填写数据");
            }
            InputStream in = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(in, 0);
            Sheet sheet = reader.getSheet();
            Row head = sheet.getRow(0);

            short lastCellNum = head.getLastCellNum();
            int lastRowNum = sheet.getLastRowNum();

            LinkedHashMap<String, List<String>> tableNameAndColumns = new LinkedHashMap<>();
            for (int x = 0; x < lastCellNum; x++) {
                ArrayList<String> columns = new ArrayList<>();
                String tableName = head.getCell(x).getStringCellValue();
                for (int y = 1; y < lastRowNum; y++) {
                    Row row = sheet.getRow(y);
                    Cell cell = row.getCell(x);
                    if (Objects.nonNull(cell)) {
                        String value = cell.getStringCellValue();
                        if (Objects.nonNull(value) && !value.isEmpty()) {
                            columns.add(value);
                        }
                    } else {
                        break;
                    }
                }
                tableNameAndColumns.put(tableName, columns);
            }

            List<MetaDataRep> metaDataReps = new ArrayList<>();
            int index = 0;
            for (Map.Entry<String, List<String>> kv : tableNameAndColumns.entrySet()) {
                List<String> columnList = kv.getValue();
                String tableName = kv.getKey();
                String[] columnArr = columnList.get(0).split(",,");
                List<String> columns = new ArrayList<>();
                if ("SYS_DICT".equals(tableName)) {
                    columnList.remove(0);

                    // Map<String, String> dicMap = columnList.stream().collect(Collectors.toMap(o -> o.split(",,")[0], o -> o.split(",,")[1], (key1, key2) -> key2));

                    for (String column : columnList) {
                        String name = column.split(",,")[0];
                        String value = column.split(",,")[1];
                        DictUtil.clearCache(name);
                        Map<String, String> dic = DictUtil.value(name);
                        if (Objects.isNull(dic) || dic.isEmpty()) {
                            MetaDataRep metaDataRep = new MetaDataRep();
                            metaDataRep.setTtname(tableName);
                            metaDataRep.setMetaType(3);
                            metaDataRep.setCcname(name);
                            metaDataRep.setIndex(index);
                            metaDataRep.setMetaMsg("字典表中缺少标识为 [" + name + "] 的字典项");
                            metaDataReps.add(metaDataRep);
                        } else {
                            String[] valueArr = value.split(",");
                            for (String s : valueArr) {
                                String code = s.split(":")[0];
                                if (!dic.containsKey(code)) {
                                    MetaDataRep metaDataRep = new MetaDataRep();
                                    metaDataRep.setTtname(tableName);
                                    metaDataRep.setMetaType(4);
                                    metaDataRep.setCcname(name);
                                    metaDataRep.setEntity(s);
                                    metaDataRep.setCommand(DictUtil.getValue(name) + "," + s);
                                    metaDataRep.setIndex(index);
                                    metaDataRep.setMetaMsg("z字典表中标识为 [" + name + "] 的字典项 缺少 [" + s + "] 键值对");
                                    metaDataReps.add(metaDataRep);
                                }
                            }
                        }
                        index++;
                    }
                } else {
                    if (columnArr.length == 1) {
                        String columnName = columnArr[0];
                        columns = metadataMapper.task(tableName, columnName);

                    } else if ("SPEC_DICT".equals(tableName)) {
                        QueryWrapper<SpecDictionary> qw = new QueryWrapper<>();
                        qw.select(columnList.get(0).replace(",,", ","));
                        columns = specService.list(qw).stream().map(o -> {
                            return o.toData();
                        }).collect(Collectors.toList());

                    } /*else if ("ALARM_REPOSITORY".equals(tableName)) {
                        QueryWrapper<AlarmRepository> qw = new QueryWrapper<>();
                        qw.select(columnList.get(0).replace(",,", ","));
                        columns = repositoryService.list(qw).stream().map(o -> {
                            return o.toData();
                        }).collect(Collectors.toList());

                    }*/ else if ("SYS_MENU".equals(tableName)) {
                        QueryWrapper<SysMenu> qw = new QueryWrapper<>();
                        qw.select(columnList.get(0).replace(",,", ","));
                        qw.eq("STATUS", 1);
                        columns = menuService.list(qw).stream().map(o -> {
                            return o.toData();
                        }).collect(Collectors.toList());
                    }

                    for (int i = 1; i < columnList.size(); i++) {
                        if (!columns.contains(columnList.get(i))) {
                            MetaDataRep metaDataRep = new MetaDataRep();
                            metaDataRep.setTtname(tableName);
                            metaDataRep.setMetaType(3);
                            metaDataRep.setCcname(columnList.get(i));
                            metaDataRep.setIndex(index);
                            metaDataRep.setMetaMsg(tableName + "表中缺少 " + Arrays.toString(columnArr) + " 为 [" + columnList.get(i) + "] 的数据");
                            metaDataReps.add(metaDataRep);
                        }
                        index++;
                    }


                }
            }

            InputStream in2 = file.getInputStream();
            ExcelReader reader2 = ExcelUtil.getReader(in2, "SQL");
            Sheet sqlSheet = reader2.getSheet();
            for (MetaDataRep metaDataRep : metaDataReps) {
                try {
                    String sql = sqlSheet.getRow(metaDataRep.getIndex()).getCell(0).getStringCellValue();
                    if (metaDataRep.getMetaType() == 4) {
                        String sqlStr = "UPDATE SYS_DICT  SET VALUE = '" + metaDataRep.getCommand() + "' WHERE NAME ='" + metaDataRep.getCcname() + "';";
                        metaDataRep.setCommand(sqlStr);
                        metaDataRep.setCcname(metaDataRep.getCcname() + " [" + metaDataRep.getEntity() + "] ");
                    } else {
                        metaDataRep.setCommand(sql);
                    }
                } catch (IndexOutOfBoundsException e) {
                    metaDataRep.setCommand("未查询到相应SQL");
                }

            }
            exportList2 = metaDataReps;
            return ResultVoUtil.success(metaDataReps);
        } catch (Exception e) {
            return ResultVoUtil.error(e.getMessage());
        }

    }


    /**
     * 校验表名称和字段
     */
    @PostMapping("/contrastMetadata")
    @ResponseBody
    public ResultVo contrastMetadata(@RequestParam("file") MultipartFile file) {
        //   String pathname = "E:\\files\\title.xlsx";
        //   File file = new File(pathname); InputStream in = new FileInputStream(file);
        try {
            if (Objects.isNull(file)) {
                throw new NullFieldException("模板文件不能为空,请填写数据");
            }
            InputStream in = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(in, 0);
            InputStream in2 = file.getInputStream();
            ExcelReader readerCommand = ExcelUtil.getReader(in2, 1);
            List<List<Object>> commandMap = readerCommand.read();
            Map<String, String> commands = new HashMap<>();
            for (List<Object> list : commandMap) {
                for (Object o : list) {
                    if (Objects.nonNull(o)) {
                        String[] split = o.toString().split("&&");
                        if (split.length == 2) {
                            commands.put(split[0], split[1]);
                        }
                    }
                }
            }

            List<String> tableTemp = reader.readRow(0).stream().filter(o -> o != "").map(Object::toString).collect(Collectors.toList());

            List<String> tables = metadataMapper.getTables();

            List<MetaDataRep> metaDataReps = new ArrayList<>();

            tableTemp.removeAll(tables);
            if (!tableTemp.isEmpty()) {
                for (String tableName : tableTemp) {
                    //缺表格
                    if (!(tableName.startsWith("BROKER_") || tableName.startsWith("CASCO_"))) {
                        MetaDataRep metaDataRep = new MetaDataRep();
                        metaDataRep.setTtname(tableName);
                        metaDataRep.setCcname("缺整张表");
                        metaDataRep.setMetaMsg("缺少[" + tableName + "]数据表");
                        metaDataRep.setMetaType(1);
                        metaDataRep.setCommand(commands.get(tableName));
                        metaDataReps.add(metaDataRep);
                    }
                }
            }

            List<MetadataTable> allComments = metadataMapper.getAllComments();

            Map<String, List<String>> tableAndComments = allComments.stream().collect(Collectors.groupingBy(MetadataTable::getTname, Collectors.mapping(MetadataTable::getCname, Collectors.toList())));

            List<Map<String, Object>> commentsList = reader.readAll();

            for (Map<String, Object> map : commentsList) {
                Map<String, Object> rowMap = AssetImportUtils.removeMapNullValue(map);
                for (Map.Entry<String, Object> kv : rowMap.entrySet()) {
                    String tableName = kv.getKey();
                    if (tableTemp.isEmpty() || !tableTemp.contains(tableName)) {
                        String comment = kv.getValue().toString();
                        if (!tableAndComments.get(tableName).contains(comment)) {
                            if (!(tableName.startsWith("BROKER_") || tableName.startsWith("CASCO_"))) {
                                //缺字段
                                MetaDataRep metaDataRep = new MetaDataRep();
                                metaDataRep.setMetaMsg("缺少[" + tableName + "]中的[" + comment + "]字段");
                                metaDataRep.setMetaType(2);
                                metaDataRep.setTtname(tableName);
                                metaDataRep.setCcname(comment);
                                metaDataRep.setCommand(commands.get(tableName + "&" + comment));
                                metaDataReps.add(metaDataRep);
                            }
                        }
                    }
                }
            }
            if (metaDataReps.isEmpty()) {
                return ResultVoUtil.success("恭喜您 成功通过校验~");
            }
            List<MetaDataRep> collect = metaDataReps.stream().sorted(Comparator.comparing(MetaDataRep::getMetaType).reversed()).collect(Collectors.toList());
            exportList = collect;
            return ResultVoUtil.success(collect);

        } catch (Exception e) {
            return ResultVoUtil.error("出错出错");
        }

    }


    /**
     * 校验表名称和字段  包括Cisco
     */
    @PostMapping("/contrastAllMetadata")
    @ResponseBody
    public ResultVo contrastAllMetadata(@RequestParam("file") MultipartFile file) {
        //   String pathname = "E:\\files\\title.xlsx";
        //   File file = new File(pathname); InputStream in = new FileInputStream(file);
        try {
            if (Objects.isNull(file)) {
                throw new NullFieldException("模板文件不能为空,请填写数据");
            }
            InputStream in = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(in, 0);
            InputStream in2 = file.getInputStream();
            ExcelReader readerCommand = ExcelUtil.getReader(in2, 1);
            List<List<Object>> commandMap = readerCommand.read();
            Map<String, String> commands = new HashMap<>();
            for (List<Object> list : commandMap) {
                for (Object o : list) {
                    if (Objects.nonNull(o)) {
                        String[] split = o.toString().split("&&");
                        if (split.length == 2) {
                            commands.put(split[0], split[1]);
                        }
                    }
                }
            }

            List<String> tableTemp = reader.readRow(0).stream().filter(o -> o != "").map(Object::toString).collect(Collectors.toList());

            List<String> tables = metadataMapper.getTables();

            List<MetaDataRep> metaDataReps = new ArrayList<>();

            tableTemp.removeAll(tables);
            if (!tableTemp.isEmpty()) {
                for (String tableName : tableTemp) {
                    //缺表格
                    MetaDataRep metaDataRep = new MetaDataRep();
                    metaDataRep.setTtname(tableName);
                    metaDataRep.setCcname(" * ");
                    metaDataRep.setMetaMsg("缺少[" + tableName + "]数据表");
                    metaDataRep.setMetaType(1);
                    metaDataRep.setCommand(commands.get(tableName));
                    metaDataReps.add(metaDataRep);
                }
            }

            List<MetadataTable> allComments = metadataMapper.getAllComments();

            Map<String, List<String>> tableAndComments = allComments.stream().collect(Collectors.groupingBy(MetadataTable::getTname, Collectors.mapping(MetadataTable::getCname, Collectors.toList())));

            List<Map<String, Object>> commentsList = reader.readAll();

            for (Map<String, Object> map : commentsList) {
                Map<String, Object> rowMap = AssetImportUtils.removeMapNullValue(map);
                for (Map.Entry<String, Object> kv : rowMap.entrySet()) {
                    String tableName = kv.getKey();
                    if (tableTemp.isEmpty() || !tableTemp.contains(tableName)) {
                        String comment = kv.getValue().toString();
                        if (!tableAndComments.get(tableName).contains(comment)) {
                            //缺字段
                            MetaDataRep metaDataRep = new MetaDataRep();
                            metaDataRep.setMetaMsg("缺少[" + tableName + "]中的[" + comment + "]字段");
                            metaDataRep.setMetaType(2);
                            metaDataRep.setTtname(tableName);
                            metaDataRep.setCcname(comment);
                            metaDataRep.setCommand(commands.get(tableName + "&" + comment));
                            metaDataReps.add(metaDataRep);
                        }
                    }
                }
            }
            if (metaDataReps.isEmpty()) {
                return ResultVoUtil.success("恭喜您 成功通过校验~");
            }
            List<MetaDataRep> collect = metaDataReps.stream().sorted(Comparator.comparing(MetaDataRep::getMetaType).thenComparing(MetaDataRep::getTtname)).collect(Collectors.toList());
            exportList = collect;
            return ResultVoUtil.success(collect);

        } catch (Exception e) {
            return ResultVoUtil.error("出错出错");
        }

    }


    /**
     * 获取表名称和字段
     */
    @GetMapping("/getMetadataExecl")
    @ResponseBody
    public void getMetadataExecl(HttpServletResponse response) {

        if (redisService.exists("IMPORT_METADATA_TABLE")) {
//            log.info("正在形成文件,不要重复点击");
        } else {
            try {
                redisService.set("IMPORT_METADATA_TABLE", "running", (long) 300);
                List<String> tableList = metadataMapper.getTables();
                List<MetadataTable> allComments = metadataMapper.getAllComments();
                Map<String, List<String>> tableAndComments = allComments.stream().collect(Collectors.groupingBy(MetadataTable::getTname, Collectors.mapping(MetadataTable::getCname, Collectors.toList())));

                Map<String, String> commandMap = new HashMap<>();

                for (String tableName : tableList) {
                    log.info("元数据校验-正在提取表：{}", tableName);
                    List<MetadataTable> metadataTables = metadataMapper.getCommentType(tableName);
                    /**
                     * 智能巡检 增加配置导致反查建表语句报错
                     * */
                    String creatTable = getCreateTableComman(tableName + "&&" + metadataMapper.getCreatTable(tableName));
                    //String creatTable = tableName + "&&" + tableName;
                    commandMap.put(tableName, creatTable);
                    for (MetadataTable metadataTable : metadataTables) {
                        if ("DATE".equals(metadataTable.getCtype())) {
                            commandMap.put(tableName + "&" + metadataTable.getCname(), tableName + "&" + metadataTable.getCname() + "&&alter table " + tableName + " add (" + metadataTable.getCname() + " " + metadataTable.getCtype() + ");");
                        } else {
                            commandMap.put(tableName + "&" + metadataTable.getCname(), tableName + "&" + metadataTable.getCname() + "&&alter table " + tableName + " add " + metadataTable.getCname() + " " + metadataTable.getCtype() + "(" + metadataTable.getClenght() + ");");
                        }
                    }
                }
                log.info("元数据校验-数据库提取完结");
                SXSSFWorkbook excel = exportMetadataExecl(tableList, tableAndComments, commandMap);
                log.info("元数据校验-表格生成完结");
                String url = oracleUrl.replace("jdbc:oracle:thin:@", "");
                AssetImportUtils.responseBody(excel, response, url + "表结构");
                redisService.remove("IMPORT_METADATA_TABLE");
            } catch (Exception e) {
                log.error("元数据校验-获取表名和字段失败：{}", e.getMessage(), e);
            } finally {
                redisService.remove("IMPORT_METADATA_TABLE");
            }

        }


    }


    /**
     * 展示校验表元数据  (未使用)
     */
    @PostMapping("/showMetadataExecl")
    @ResponseBody
    public ResultVo showMetadataExecl(@RequestParam("file") MultipartFile file) {
        try {
            if (Objects.isNull(file)) {
                throw new NullFieldException("模板文件不能为空,请填写数据");
            }
            InputStream in = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(in, 0);
            List<Map<String, Object>> commentsList = reader.readAll();
            HashMap<String, List<String>> metaMap = new HashMap<>();
            for (Map<String, Object> map : commentsList) {
                Map<String, Object> rowMap = AssetImportUtils.removeMapNullValue(map);
                for (Map.Entry<String, Object> kv : rowMap.entrySet()) {
                    String tableName = kv.getKey();
                    if (metaMap.containsKey(tableName)) {
                        List<String> list = metaMap.get(tableName);
                        list.add(kv.getValue().toString());
                        metaMap.put(tableName, list);
                    } else {
                        ArrayList<String> value = new ArrayList<>();
                        value.add(kv.getValue().toString());
                        metaMap.put(tableName, value);
                    }
                }
            }
            return ResultVoUtil.success(metaMap);
        } catch (Exception e) {
            return ResultVoUtil.error(ResultEnum.ERROR);
        }

    }

    /**
     * 下载提示的sql文件
     */
    @GetMapping("/download")
    public void exportCommanExecl(HttpServletResponse response) {
        try {
            List<MetaDataRep> exportList = this.exportList;
            if (exportList.size() > 1) {
                SXSSFWorkbook excel = exportSqlExecl(exportList);
                AssetImportUtils.responseBody(excel, response, "缺失元数据");
            } else {
                SXSSFWorkbook workbook = new SXSSFWorkbook(10);
                AssetImportUtils.responseBody(workbook, response, "缺失元数据");
            }
        } catch (Exception e) {
            log.error("元数据校验-导出SQL文件失败 : {}", e.getMessage(), e);
        }
    }

    /**
     * 下载提示的sql文件
     */
    @GetMapping("/download2")
    public void exportCommanExecl2(HttpServletResponse response) {
        try {
            List<MetaDataRep> exportList = this.exportList2;
            if (exportList.size() > 1) {
                SXSSFWorkbook excel = exportSqlExecl(exportList);
                AssetImportUtils.responseBody(excel, response, "补充初始化数据");
            } else {
                SXSSFWorkbook workbook = new SXSSFWorkbook(10);
                AssetImportUtils.responseBody(workbook, response, "补充初始化数据");
            }
        } catch (Exception e) {
            log.error("元数据校验-导出失败 : {}", e.getMessage(), e);
        }
    }

    private String getCreateTableComman(String str) {
        String c1 = str.replace("\"ITSM\".", "");
        int tablespace = c1.indexOf("TABLESPACE");
        String c2 = c1;
        if(tablespace>0||tablespace==0){
            c2 = c1.substring(0, tablespace) + ");";
        }

        return c2;
    }


    private List<String> task(String tableName, String columnName) {
        List<String> task = new ArrayList<>();
        task.add(tableName);
        task.add(columnName);
        List<String> task1 = metadataMapper.task(tableName, columnName);
        if (!task1.isEmpty()) {
            task.addAll(task1);
        }
        return task;
    }


    private SXSSFWorkbook createMetadataExecl(HashMap<String, List<String>> tableNameAndColumns, List<String> sqlList, Integer size) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(size + 10);
        SXSSFSheet sheet = workbook.createSheet("TABLE");
        SXSSFSheet sheet2 = workbook.createSheet("SQL");
        XSSFCellStyle headerCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerCellStyle.setFont(getFont(workbook, (short) 11, false, true));
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        headerCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        headerCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(198, 224, 180)));
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);


        XSSFCellStyle lineStyle1 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle1.setFont(getFont(workbook, (short) 11, false, true));
        lineStyle1.setWrapText(true);//设置自动换行
        lineStyle1.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle1.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        lineStyle1.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle1.setFillForegroundColor(new XSSFColor(new java.awt.Color(242, 242, 242)));
        lineStyle1.setBottomBorderColor(HSSFColor.BLACK.index);
        lineStyle1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderTop(XSSFCellStyle.BORDER_THIN);

        XSSFCellStyle lineStyle2 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle2.setFont(getFont(workbook, (short) 11, false, true));
        lineStyle2.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        lineStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle2.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 242, 204)));
        lineStyle2.setBottomBorderColor(HSSFColor.BLACK.index);
        lineStyle2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle2.setBorderTop(XSSFCellStyle.BORDER_THIN);

        XSSFCellStyle lineStyle11 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle11.setFont(getFont(workbook, (short) 11, false, true));
        lineStyle11.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle11.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        //lineStyle11.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle11.setFillForegroundColor(new XSSFColor(new java.awt.Color(221, 235, 247)));
        lineStyle11.setBottomBorderColor(HSSFColor.BLACK.index);
        lineStyle11.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle11.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle11.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle11.setBorderTop(XSSFCellStyle.BORDER_THIN);

        XSSFCellStyle lineStyle22 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle22.setFont(getFont(workbook, (short) 11, false, true));
        lineStyle22.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle22.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        // lineStyle22.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle22.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 242, 204)));
        lineStyle22.setBottomBorderColor(HSSFColor.BLACK.index);
        lineStyle22.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle22.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle22.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle22.setBorderTop(XSSFCellStyle.BORDER_THIN);


        XSSFCellStyle lineStyle33 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle33.setFont(getFont(workbook, (short) 11, false, true));
        lineStyle33.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle33.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        //lineStyle33.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle33.setFillForegroundColor(new XSSFColor(new java.awt.Color(226, 239, 218)));
        lineStyle33.setBottomBorderColor(HSSFColor.BLACK.index);
        lineStyle33.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle33.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle33.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle33.setBorderTop(XSSFCellStyle.BORDER_THIN);

        sheet2.setColumnWidth(0, (int) 254 * 256);
        for (int i = 0; i < sqlList.size(); i++) {
            SXSSFRow row = sheet2.createRow(i);
            row.setHeightInPoints(31);
            SXSSFCell cell = row.createCell(0);
            cell.setCellValue(sqlList.get(i));
            if (i % 3 == 0) {
                cell.setCellStyle(lineStyle11);
            } else if (i % 3 == 1) {
                cell.setCellStyle(lineStyle22);
            } else {
                cell.setCellStyle(lineStyle33);
            }


        }


        HashMap<Integer, SXSSFRow> rowMap = new HashMap<>();
        for (int index = 0; index <= size; index++) {
            SXSSFRow row = sheet.createRow(index);
            row.setHeightInPoints(31);
            rowMap.put(index, row);
        }


        int x = 0;
        for (Map.Entry<String, List<String>> kv : tableNameAndColumns.entrySet()) {
            List<String> value = kv.getValue();
            sheet.setColumnWidth(x, (int) 21 * 256);
            for (int y = 0; y < value.size(); y++) {
                SXSSFCell cell = rowMap.get(y).createCell(x);
                if (y == 0) {
                    cell.setCellStyle(headerCellStyle);
                } else if (y == 1) {
                    cell.setCellStyle(lineStyle2);
                } else {
                    cell.setCellStyle(lineStyle1);
                }
                cell.setCellValue(value.get(y));

            }
            x++;
        }


        return workbook;
    }

    private SXSSFWorkbook exportMetadataExecl
            (List<String> headList, Map<String, List<String>> commentList, Map<String, String> commandMap) {

        SXSSFWorkbook workbook = new SXSSFWorkbook(commandMap.size() + 10);
        SXSSFSheet sheet = workbook.createSheet("表结构");
        SXSSFSheet sheet2 = workbook.createSheet("命令语句");

        XSSFCellStyle headerCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerCellStyle.setFont(getFont(workbook, (short) 11, false, true));
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        headerCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        headerCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(198, 224, 180)));
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);


        XSSFCellStyle lineStyle1 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle1.setFont(getFont(workbook, (short) 11, false, true));
        lineStyle1.setWrapText(true);//设置自动换行
        lineStyle1.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle1.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        lineStyle1.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle1.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
        lineStyle1.setBottomBorderColor(HSSFColor.BLACK.index);
        lineStyle1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderTop(XSSFCellStyle.BORDER_THIN);


        HashMap<Integer, SXSSFRow> rowMap = new HashMap<>();
        HashMap<Integer, SXSSFRow> rowwMap = new HashMap<>();
        for (int index = 0; index < 80; index++) {

            SXSSFRow row = sheet.createRow(index);
            row.setHeightInPoints(31);
            rowMap.put(index, row);

            SXSSFRow roww = sheet2.createRow(index);
            roww.setHeightInPoints(31);
            rowwMap.put(index, roww);
        }


        for (int i = 0; i < headList.size(); i++) {
            sheet.setColumnWidth(i, (int) 18 * 256);
            SXSSFCell headerCell = rowMap.get(0).createCell(i);
            headerCell.setCellValue(headList.get(i));
            headerCell.setCellStyle(headerCellStyle);

            sheet2.setColumnWidth(i, (int) 18 * 256);
            SXSSFCell commandCell = rowwMap.get(0).createCell(i);
            commandCell.setCellValue(commandMap.get(headList.get(i)));
            commandCell.setCellStyle(headerCellStyle);

            List<String> comments = commentList.get(headList.get(i));

            for (int j = 0; j < comments.size(); j++) {
                SXSSFRow row = rowMap.get(j + 1);
                SXSSFCell celli = row.createCell(i);
                celli.setCellValue(comments.get(j));
                celli.setCellStyle(lineStyle1);

                SXSSFRow roww = rowwMap.get(j + 1);
                SXSSFCell commandcelli = roww.createCell(i);
                commandcelli.setCellValue(commandMap.get(headList.get(i) + "&" + comments.get(j)));
                commandcelli.setCellStyle(lineStyle1);
            }

        }

        return workbook;
    }

    private SXSSFWorkbook exportSqlExecl(List<MetaDataRep> list) {

        SXSSFWorkbook workbook = new SXSSFWorkbook(list.size() + 10);
        SXSSFSheet sheet = workbook.createSheet("表结构");

        XSSFCellStyle headerCellStyle = (XSSFCellStyle) workbook.createCellStyle();
        headerCellStyle.setFont(getFont(workbook, (short) 11, false, true));
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        headerCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        headerCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(198, 224, 180)));
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerCellStyle.setBottomBorderColor(HSSFColor.BLACK.index);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);


        XSSFCellStyle lineStyle1 = (XSSFCellStyle) workbook.createCellStyle();
        lineStyle1.setFont(getFont(workbook, (short) 11, false, true));
        //lineStyle1.setWrapText(true);//设置自动换行
        lineStyle1.setFillPattern(CellStyle.SOLID_FOREGROUND);
        lineStyle1.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
        //lineStyle1.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
        lineStyle1.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 242, 204)));
        lineStyle1.setBottomBorderColor(HSSFColor.BLACK.index);
        lineStyle1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderRight(XSSFCellStyle.BORDER_THIN);
        lineStyle1.setBorderTop(XSSFCellStyle.BORDER_THIN);

        SXSSFRow row0 = sheet.createRow(0);
        row0.setHeightInPoints(25);
        SXSSFCell cell0 = row0.createCell(0);
        cell0.setCellValue("表名称");
        cell0.setCellStyle(headerCellStyle);
        SXSSFCell cell1 = row0.createCell(1);
        cell1.setCellValue("字段名称");
        cell1.setCellStyle(headerCellStyle);
        SXSSFCell cell2 = row0.createCell(2);
        cell2.setCellValue("建议SQL");
        cell2.setCellStyle(headerCellStyle);
        sheet.setColumnWidth(0, (int) 28 * 256);
        sheet.setColumnWidth(1, (int) 18 * 256);
        sheet.setColumnWidth(2, (int) 250 * 256);


        for (int i = 0; i < list.size(); i++) {
            MetaDataRep metaDataRep = list.get(i);
            SXSSFRow row = sheet.createRow(i + 1);
            row.setHeightInPoints(20);
            SXSSFCell cell00 = row.createCell(0);
            cell00.setCellValue(metaDataRep.getTtname());
            cell00.setCellStyle(lineStyle1);
            SXSSFCell cell11 = row.createCell(1);
            cell11.setCellValue(metaDataRep.getCcname());
            cell11.setCellStyle(lineStyle1);
            SXSSFCell cell22 = row.createCell(2);
            cell22.setCellValue(metaDataRep.getCommand());
            cell22.setCellStyle(lineStyle1);
        }

        return workbook;
    }

    /*设定字体格式*/
    private Font getFont(SXSSFWorkbook workbook, short size, Boolean bold, boolean color) {
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
