package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * @description: 模板配置
 * @author: Lvyp
 * @create: 2023/11/10 17:14
 */
@TableName("NIGHT_BOARD_PAYLOAD")
@Data
public class NightBoardPayload extends Model<NightBoardPayload> {

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("BOARD_ID")
    private String boardId;

    @TableField("PAYLOAD")
    private String payload;

}
