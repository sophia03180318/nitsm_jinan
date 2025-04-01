package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.FileRelate;
import org.apache.ibatis.annotations.Select;

/**
 * @author HanHW
 * @description TODO
 * @className FileRelateMapper
 * @date 2024/3/27 16:39
 * @since 2.1.5.0
 */
public interface FileRelateMapper extends BaseMapper<FileRelate> {

    @Select("select * from file_relate where file_id = ${fileId}")
    FileRelate getByFileId(String fileId);
}
