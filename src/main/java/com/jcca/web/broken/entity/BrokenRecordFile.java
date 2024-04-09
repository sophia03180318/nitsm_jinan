package com.jcca.web.broken.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 故障记录文件
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("BROKEN_RECORD_FILE")
public class BrokenRecordFile extends Model<BrokenRecordFile> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 故障主键
     */
    @TableId("BROKEN_RECORD_ID")
    private String brokenRecordId;
    /**
     * 文件主键
     */
    @TableField("SYS_FILE_ID")
    private String sysFileId;

}
