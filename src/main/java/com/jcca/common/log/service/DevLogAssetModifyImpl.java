package com.jcca.common.log.service;

import cn.hutool.core.date.DateUtil;
import com.jcca.admin.system.entity.SysActionLog;
import com.jcca.admin.system.entity.SysActionLogDetail;
import com.jcca.admin.system.service.SysActionLogDetailService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.enums.AssetManufacturerEnum;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.log.annotation.FieldLogAnno;
import com.jcca.common.log.constant.DevLogConstant;
import com.jcca.common.log.constant.LogDetailItemIdType;
import com.jcca.common.utils.EncryptUtil;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.utils.enums.AssetRunModelEnum;
import com.jcca.web.asset.utils.enums.AssetWatchStatusEnum;
import com.jcca.web.asset.utils.enums.ManufacturersEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author HanHW
 * @description 修改资产 运维日志记录
 * @className DevLogAssetModifyImpl
 * @date 2024/4/9 11:21
 * @since 2.1.0.0
 */
@Service
public class DevLogAssetModifyImpl implements DevLogService {
    @Resource
    private AssetService assetService;
    @Resource
    private SysActionLogDetailService sysActionLogDetailService;

    /**
     * 运维日志分类
     *
     * @return DevLogConstant
     */
    @Override
    public String getDevType() {
        return DevLogConstant.ASSET_MODIFY;
    }

    /**
     * 设置运维日志内容
     *
     * @param actionLog 日志
     * @param args      参数
     */
    @Override
    public void setDevLog(SysActionLog actionLog, Object[] args) {
        Object arg = args[0];
        if (!(arg instanceof Asset)) {
            return;
        }

        Asset req = (Asset) arg;
        String id = req.getId();
        if (StringUtils.isEmpty(id)) {
            actionLog.setLogName("新增资产【" + req.getName() + "】");
        } else {
            Integer desk = req.getDesk();
            req.setDesk(desk);
            actionLog.setLogName("修改资产【" + req.getName() + "】");
            Asset one = assetService.getById(id);
            try {
                this.compareContent(req, one, actionLog.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            req.setDesk(desk);
        }
    }

    private void compareContent(Asset req, Asset one, String actionLogId) throws IllegalAccessException {
        List<SysActionLogDetail> detailList = new ArrayList<>();
        Class<? extends Asset> aClass = req.getClass();
        Field[] afields = aClass.getDeclaredFields();

        Class<? extends Asset> bClass = one.getClass();
        Field[] bfields = bClass.getDeclaredFields();
        for (Field afield : afields) {
            String aname = afield.getName();
            FieldLogAnno annotation = afield.getAnnotation(FieldLogAnno.class);
            if (null == annotation) {
                continue;
            }

            for (Field bfield : bfields) {
                String bname = bfield.getName();
                if (!aname.equals(bname)) {
                    continue;
                }
                afield.setAccessible(true);
                bfield.setAccessible(true);

                Object ao = afield.get(req);
                Object bo = bfield.get(one);
                if (ao != null && bo != null && ao.toString().equals(bo.toString())) {
                    continue;
                }
                if (ao == null && bo == null) {
                    continue;
                }

                String temp = "-99";
                if (StringUtils.isEmpty(ao) || "null".equals(ao)) {
                    ao = temp;
                }
                if (StringUtils.isEmpty(bo) || "null".equals(bo)) {
                    bo = temp;
                }

                if ("osPassword".equals(bname) && !temp.equals(bo.toString()) && (bo.toString().length() % 16) == 0) {
                    bo = EncryptUtil.aesDecryptStr(bo.toString());
                    if (ao.equals(bo)) {
                        continue;
                    }
                }
                if ("loginPwd".equals(bname) && !temp.equals(bo.toString()) && (bo.toString().length() % 16) == 0) {
                    bo = EncryptUtil.aesDecryptStr(bo.toString());
                    if (ao.equals(bo)) {
                        continue;
                    }
                }
                if ("ipmiPwd".equals(bname) && !temp.equals(bo.toString()) && (bo.toString().length() % 16) == 0) {
                    bo = EncryptUtil.aesDecryptStr(bo.toString());
                    if (ao.equals(bo)) {
                        continue;
                    }
                }

                Map<Object, Object> map = this.translate(bname, ao, bo, one.getId());
                Set<Object> objects = map.keySet();
                for (Object object : objects) {
                    String description = annotation.title()
                            + "由【" + map.get(object) + "】变更为【" + object + "】";
                    SysActionLogDetail detail = sysActionLogDetailService.setDetail(actionLogId,
                            req.getId(), req.getId(), LogDetailItemIdType.ASSET, description);
                    detailList.add(detail);
                }
            }
        }
        sysActionLogDetailService.saveBatch(detailList);
    }

    @Resource
    private SysOrgService orgService;
    @Resource
    private RoomService roomService;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private AssetAttachService assetAttachService;

    private Map<Object, Object> translate(String name, Object ao, Object bo, String bid) {
        String temp = "-99";
        if (StringUtils.isEmpty(ao) || "null".equals(ao)) {
            ao = temp;
        }
        if (StringUtils.isEmpty(bo) || "null".equals(bo)) {
            bo = temp;
        }

        if ("desk".equals(name)) {
            ao = AssetModeEnum.getName(Integer.parseInt(ao.toString()));
            bo = AssetModeEnum.getName(Integer.parseInt(bo.toString()));
        }
        if ("runModel".equals(name)) {
            ao = "".equals(AssetRunModelEnum.getMsg(ao.toString())) ? "无" : AssetRunModelEnum.getMsg(ao.toString());
            bo = "".equals(AssetRunModelEnum.getMsg(bo.toString())) ? "无" : AssetRunModelEnum.getMsg(bo.toString());
        }
        if ("orgId".equals(name)) {
            ao = orgService.getById(ao.toString()).getTitle();
            bo = orgService.getById(bo.toString()).getTitle();
        }
        if ("roomId".equals(name)) {
            AssetAttach attach = assetAttachService.getByAssetId(bid);
            String roomId = attach.getRoomId();
            if (StringUtils.isEmpty(roomId)) {
                bo = temp;
            } else {
                if (ao.equals(roomId)) {
                    return new HashMap<>();
                } else {
                    ao = roomService.getById(ao.toString()).getName();
                    bo = roomService.getById(roomId).getName();
                }
            }
        }
        if ("cabinetId".equals(name)) {
            AssetAttach attach = assetAttachService.getByAssetId(bid);
            String cabinetId = attach.getCabinetId();
            if (StringUtils.isEmpty(cabinetId)) {
                bo = temp;
            } else {
                if (ao.equals(cabinetId)) {
                    return new HashMap<>();
                } else {
                    ao = cabinetService.getById(ao.toString()).getName();
                    bo = cabinetService.getById(cabinetId).getName();
                }
            }
        }
        if ("collectionType".equals(name)) {
            if ("-99".equals(ao) && "-1".equals(bo.toString())) {
                return new HashMap<>();
            }
            Integer linux = 0;
            Integer aix = 2;
            List<Integer> telnetList = Arrays.asList(3, 4);
            if (linux.equals(ao) || aix.equals(ao)) {
                ao = "SSH";
            } else if (telnetList.contains(Integer.parseInt(ao.toString()))) {
                ao = "TELNET";
            } else {
                ao = "SNMP";
            }
            if (linux.equals(bo) || aix.equals(bo)) {
                bo = "SSH";
            } else if (telnetList.contains(Integer.parseInt(bo.toString()))) {
                bo = "TELNET";
            } else {
                bo = "SNMP";
            }
        }
        if ("showCore".equals(name)) {
            ao = "SHOW_TOPO_@_SHOW".equals(ao) ? "核心设备" : "非核心设备";
            bo = "SHOW_TOPO_@_SHOW".equals(bo) ? "核心设备" : "非核心设备";
        }
        if ("aBFlag".equals(name)) {
            ao = "0".equals(ao.toString()) ? "A机" : "B机";
            bo = "0".equals(bo.toString()) ? "A机" : "B机";
        }
        if ("showTopo".equals(name)) {
            ao = "0".equals(ao.toString()) ? "不显示" : "显示";
            bo = "0".equals(bo.toString()) ? "不显示" : "显示";
        }
        if ("ntpFlag".equals(name)) {
            ao = "0".equals(ao.toString()) ? "不采集" : "采集";
            bo = "0".equals(bo.toString()) ? "不采集" : "采集";
        }
        if ("watch".equals(name)) {
            ao = AssetWatchStatusEnum.getEnum(Byte.parseByte(ao.toString())).getSta();
            bo = AssetWatchStatusEnum.getEnum(Byte.parseByte(bo.toString())).getSta();
        }
        if ("manufacturerId".equals(name)) {
            ao = AssetManufacturerEnum.getName(Integer.parseInt(ao.toString()));
            bo = AssetManufacturerEnum.getName(Integer.parseInt(bo.toString()));
        }
        if ("onlineTime".equals(name) || "downlineTime".equals(name)) {
            if (!temp.equals(ao)) {
                Date a = (Date) ao;
                ao = DateUtil.format(a, "yyyy-MM-dd");
            }
            if (!temp.equals(bo)) {
                Date a = (Date) bo;
                bo = DateUtil.format(a, "yyyy-MM-dd");
            }
        }
        if ("assetSupplier".equals(name)) {
            ManufacturersEnum anEnum = ManufacturersEnum.getEnum(ao.toString());
            ao = temp;
            if (Objects.nonNull(anEnum)) {
                ao = anEnum.getMsg();
            }
            ManufacturersEnum bnEnum = ManufacturersEnum.getEnum(bo.toString());
            bo = temp;
            if (Objects.nonNull(bnEnum)) {
                bo = bnEnum.getMsg();
            }
        }
        if ("startPosition".equals(name)) {
            AssetAttach attach = assetAttachService.getByAssetId(bid);
            Integer startPosition = attach.getStartPosition();
            if (startPosition == null) {
                bo = temp;
            } else {
                if (ao.equals(startPosition)) {
                    return new HashMap<>();
                } else {
                    bo = startPosition;
                }
            }
        }
        if ("endPosition".equals(name)) {
            AssetAttach attach = assetAttachService.getByAssetId(bid);
            Integer endPosition = attach.getEndPosition();
            if (endPosition == null) {
                bo = temp;
            } else {
                if (ao.equals(endPosition)) {
                    return new HashMap<>();
                } else {
                    bo = endPosition;
                }
            }
        }
        if (temp.equals(ao)) {
            ao = "无";
        }
        if (temp.equals(bo)) {
            bo = "无";
        }
        if ("无".equals(ao) && "无".equals(bo)) {
            return new HashMap<>();
        }

        Map<Object, Object> map = new HashMap<>();
        map.put(ao, bo);
        return map;
    }
}
