package com.jcca.admin.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.admin.system.entity.CabinetTask;
import com.jcca.admin.system.entity.ImportCabinet;
import com.jcca.admin.system.service.CabinetTaskService;
import com.jcca.admin.system.service.ImportCabinetService;
import com.jcca.common.config.thymeleaf.utility.DictUtil;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.utils.AssetImportUtils;
import com.jcca.web.asset.utils.NullFieldException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @ Author：sophia
 * @ Date：Created in 17:53 2021/8/13
 * @ Description:
 */
@Service
@Slf4j
public class TemplateImportCabinet {
    @Resource
    ImportCabinetService importCabinetService;
    @Resource
    private CabinetTaskService cabinetTaskService;

    @Resource
    private CabinetService cabinetService;

    @Async
    public void forImportCabniet(List<Map<String, Object>> rowMapList, List<ImportCabinet> rowMap, CabinetTask cabinetTask) {
        int fail = 0;
        int success = 0;

        for (int i = 0; i < rowMapList.size(); i++) {
            Cabinet cabinet = new Cabinet();
            try {
                Map<String, Object> importCabinets = AssetImportUtils.removeMapNullValue(rowMapList.get(i));
                Map<String, String> importCabinetDict = DictUtil.value("CABINET_TEMPLATE");

                for (Map.Entry<String, String> nameValue : importCabinetDict.entrySet()) {
                    String key = nameValue.getKey();
                    try {
                        String value = importCabinets.get(key).toString().trim();
                        switch (key) {
                            case "rowIndex":
                            case "columnIndex":
                            case "code":
                                AssetImportUtils.setFieldValues(cabinet, key, Integer.parseInt(value));
                                break;

                            case "name":
                            case "remark":
                                AssetImportUtils.setFieldValues(cabinet, key, value);
                                break;
                        }
                    } catch (NullPointerException e) {
                        throw new NullFieldException(importCabinetDict.get(key) + "字段不可为空");
                    } catch (NumberFormatException e) {
                        throw new NullFieldException(importCabinetDict.get(key) + "字段请填写数字");
                    } catch (Exception e) {
                        throw new NullFieldException(importCabinetDict.get(key) + "字段错误,请重新填写");
                    }
                }
                //根据组织名称和机房名称获取机房id
                // cabinet.setCreator("import");
                String roomId = importCabinetService.getRoomID(importCabinets.get("orgName").toString().trim(), importCabinets.get("roomName").toString().trim());
                cabinet.setRoomId(roomId);

                //存储机柜
                QueryWrapper<Cabinet> cabinetQuery = Wrappers.query();
                cabinetQuery.eq("ROOM_ID", cabinet.getRoomId());
                cabinetQuery.eq("ROW_INDEX", cabinet.getRowIndex());
                cabinetQuery.eq("COLUMN_INDEX", cabinet.getColumnIndex());
                if (Objects.nonNull(cabinet.getId())) {
                    cabinetQuery.ne("ID", cabinet.getId());
                }
                Cabinet one = cabinetService.getOne(cabinetQuery);
                if (Objects.nonNull(one)) {
                    throw new NullFieldException("该机房内机柜坐标(" + cabinet.getRowIndex() + "," + cabinet.getColumnIndex() + ")已存在");
                }

                if (importCabinets.containsKey("qrCodeNum")) {
                    String q = importCabinets.get("qrCodeNum").toString().trim();
                    if (!q.trim().isEmpty()) {
                        QueryWrapper<Cabinet> qw = Wrappers.query();
                        qw.eq("QR_CODE_NUM", q);
                        Cabinet c = cabinetService.getOne(qw);
                        if (Objects.nonNull(c)) {
                            throw new NullFieldException("该识别号已存在,请您更换");
                        }
                        cabinet.setQrCodeNum(q);
                    }
                }

                //记录正确机柜
                ImportCabinet importCabinet = rowMap.get(i);
                importCabinet.setStatus("0");
                importCabinet.setErrorLog("成功");
                importCabinetService.save(importCabinet);

                cabinetService.saveOrUpdate(cabinet);

                success++;
                cabinetTask.setSuccess(success);
            } catch (NullFieldException e) {
                //记录错误机柜
                ImportCabinet importCabinet = rowMap.get(i);
                importCabinet.setStatus("1");
                importCabinet.setErrorLog(e.getMessage());
                int length = importCabinet.getName().length();
                if (length > 20) {
                    importCabinet.setName("名称不能超过20个字符");
                    importCabinet.setErrorLog("名称不能超过20个字符");
                    importCabinetService.save(importCabinet);
                    continue;
                }
                importCabinetService.save(importCabinet);
                //更新taski信息
                fail++;
                cabinetTask.setFail(fail);

            } catch (Exception e) {
                //记录错误机柜
                ImportCabinet importCabinet = rowMap.get(i);
                importCabinet.setStatus("1");
                importCabinet.setErrorLog(e.getMessage());
                if (importCabinet.getName().length() > 20) {
                    importCabinet.setName("名称不能超过20个字符");
                    importCabinet.setErrorLog("名称不能超过20个字符");
                    importCabinetService.save(importCabinet);
                    continue;
                }
                importCabinetService.save(importCabinet);
                //更新taski信息
                fail++;
                cabinetTask.setFail(fail);

            } finally {
                cabinetTaskService.saveOrUpdate(cabinetTask);
            }


        }


        cabinetTaskService.setLastStatus(0);
    }


}
