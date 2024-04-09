package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO
 * @className ProcessEntity
 * @date 2023/11/29 14:58
 * @since 2.1.0.0
 */
@Data
public class ProcessEntity extends CommonEntity implements Serializable {

    private List<CollectProcessEntity> list;
}
