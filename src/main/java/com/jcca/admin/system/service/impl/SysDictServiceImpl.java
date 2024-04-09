package com.jcca.admin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.dao.SysDictMapper;
import com.jcca.admin.system.entity.SysDict;
import com.jcca.admin.system.service.SysDictService;
import com.jcca.common.enums.StatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-06 12:14
 **/
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Resource
    private SysDictMapper sysDictMapper;

    @Override
    public boolean repeatByName(SysDict dict) {

        return this.getByNameOk(dict.getName()) != null;
    }

    @Override
    public SysDict getByNameOk(String name) {
        QueryWrapper<SysDict> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.eq("status", StatusEnum.OK.getCode());
        return sysDictMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(StatusEnum statusEnum, List<String> ids) {
        ids.forEach(id -> {
            SysDict dict = new SysDict();
            dict.setStatus(statusEnum.getCode());
            dict.setId(id);
            sysDictMapper.updateById(dict);
        });
        return true;
    }

    @Override
    public List<SysDict> getAllLikeName(String name) {
        QueryWrapper<SysDict> dictQuery = new QueryWrapper<>();
        dictQuery.like("title",name);
        List<SysDict> sysDict = sysDictMapper.selectList(dictQuery);
        for (SysDict dict : sysDict) {
            String[] split = dict.getTitle().split("-");
            dict.setTitle(split[0]);
        }
        return sysDict;
    }

    @Override
    public SysDict getByTitle(String name) {
        QueryWrapper<SysDict> dictQuery = new QueryWrapper<>();
        dictQuery.like("name", name);
        List<SysDict> sysDict = sysDictMapper.selectList(dictQuery);
        if (sysDict.isEmpty()) {
            return null;
        }
        return sysDict.get(0);
    }

    @Override
    public List<SysDict> getByNames(String name) {
        QueryWrapper<SysDict> dictQuery = new QueryWrapper<>();
        dictQuery.like("name", name);
        List<SysDict> sysDict = sysDictMapper.selectList(dictQuery);
        return sysDict;
    }

}