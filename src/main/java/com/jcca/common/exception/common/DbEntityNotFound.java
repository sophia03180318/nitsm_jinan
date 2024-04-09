package com.jcca.common.exception.common;

/**
 * @description: 在数据库未找到相应记录
 * @author: Lvyp
 * @create: 2023/11/13 09:23
 */
public class DbEntityNotFound extends Exception {

    public DbEntityNotFound(String message) {
        super(message);
    }

}
