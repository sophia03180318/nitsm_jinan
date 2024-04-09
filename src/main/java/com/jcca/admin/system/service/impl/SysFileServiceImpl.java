package com.jcca.admin.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysFileMapper;
import com.jcca.admin.system.entity.SysFile;
import com.jcca.admin.system.entity.SysFolder;
import com.jcca.admin.system.service.SysFileService;
import com.jcca.admin.system.service.SysFolderService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-06 12:14
 **/
@Service
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    @Resource
    private SysFileMapper fileMapper;
    @Resource
    private SysFolderService folderService;

    @Override
    public SysFile getBySha1(String fileSha1) {
        QueryWrapper<SysFile> wrapper = new QueryWrapper<>();
        wrapper.eq("sha1", fileSha1);
        List<SysFile> sysFiles = fileMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(sysFiles)) {
            return null;
        }

        return sysFiles.get(0);
    }

    /**
     * 根据文件ID获取网盘文件所在路径
     *
     * @param id     网盘文件ID
     * @param rootId 根文件夹ID
     * @return 网盘文件路径
     */
    @Override
    public String getPathById(String id, String rootId) {
        SysFile sysFile = fileMapper.selectById(id);

        String folderId = sysFile.getFolderId();
        if (folderId.equals(rootId)) {
            return "/";
        }
        SysFolder folder = folderService.getById(folderId);
        String pids = folder.getPids().replace("[", "")
                .replace("]", "");
        String[] pidArr = pids.split(",");
        StringBuilder sb = new StringBuilder();
        for (String ida : pidArr) {
            if (rootId.equals(ida)) {
                continue;
            }
            sb.append("/");
            sb.append(folderService.getById(ida).getTitle());
        }
        sb.append("/");
        sb.append(folder.getTitle());

        return sb.toString();
    }
}