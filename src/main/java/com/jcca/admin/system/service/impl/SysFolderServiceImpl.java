package com.jcca.admin.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysFolderMapper;
import com.jcca.admin.system.entity.SysFolder;
import com.jcca.admin.system.service.SysFolderService;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件夹
 *
 * @author lyp
 */
@Service
public class SysFolderServiceImpl extends ServiceImpl<SysFolderMapper, SysFolder> implements SysFolderService {

    @Resource
    private SysFolderMapper folderMapper;
    @Resource
    private BizManageService bizService;

    @Override
    public String getPids(String pid) {
        if (StrUtil.isEmpty(pid) || StrUtil.equals(SysFolder.FOLDER_ROOT_ID, pid)) {
            return "[0]";
        }
        LinkedList<String> pidList = new LinkedList<String>();
        while (!StrUtil.equals(SysFolder.FOLDER_ROOT_ID, pid)) {
            pidList.push(pid);
            SysFolder obj = folderMapper.selectById(pid);
            if (Objects.isNull(obj)) {
                pidList.add(SysFolder.FOLDER_ROOT_ID);
                pid = SysFolder.FOLDER_ROOT_ID;
            } else {
                pid = obj.getPid();
            }
        }
        pidList.push(pid);
        StringBuilder pidStrs = new StringBuilder();
        int length = pidList.size();
        for (int i = 0; i < length; i++) {
            pidStrs.append("[").append(pidList.pop()).append("],");
        }

        return pidStrs.substring(0, pidStrs.length() - 1);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createFolder(SysFolder folder) {
        String id = MyIdUtil.getId();
        folder.setId(id);
        folderMapper.insert(folder);
        bizService.save(id, BizManageConstant.FOLDER);
    }

    /**
     * 根据文件夹类型获取所有文件夹ID
     * 不包括根ID
     *
     * @param category 文件夹类型
     * @return id列表
     */
    @Override
    public List<String> getAllFolderId(Byte category) {
        QueryWrapper<SysFolder> query = Wrappers.query();
        query.eq("category", category);
        query.eq("status", StatusEnum.OK.getCode());
        List<SysFolder> list = this.list(query);

        return list.stream().map(SysFolder::getId).collect(Collectors.toList());
    }

}
