package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.dao.AssetTemplateMapper;
import com.jcca.web.asset.entity.AssetTemplate;
import com.jcca.web.asset.service.AssetTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ Author：sophia
 * @ Date：Created in 11:25 2021/7/8
 * @ Description:
 */
@Service
@Slf4j
public class AssetTemplateServiceImpl extends ServiceImpl<AssetTemplateMapper, AssetTemplate> implements AssetTemplateService {
    @Resource
    private AssetTemplateMapper assetTemplateMapper;

}