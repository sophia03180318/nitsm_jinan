package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.PromptInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 进程阈值
 *
 * @author syt
 */
@Mapper
public interface PromptInfoMapper extends BaseMapper<PromptInfo> {

    List<String> getPromptByDictName(String dictName, String type);

    List<String> getExistPrompt(String key);
}
