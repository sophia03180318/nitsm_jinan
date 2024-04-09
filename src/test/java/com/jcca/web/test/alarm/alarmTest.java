package com.jcca.web.test.alarm;

import com.jcca.common.redis.queue.RedisQueueTemplate;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * 测试资产
 *
 * @author lyp
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class alarmTest {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void testCpu() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("cpuAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                cpuStr = cpuStr + line;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        RedisQueueTemplate redisQueueTemplate = new RedisQueueTemplate(stringRedisTemplate);
        String finalStr = cpuStr;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    redisQueueTemplate.rPush("_threshold_queue", finalStr);
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testInterface() throws InterruptedException {


        RedisQueueTemplate redisQueueTemplate = new RedisQueueTemplate(stringRedisTemplate);
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while(true){
                    String interfacestr="";

                    try {
                        File file = new File("C:\\Users\\lyp\\Desktop\\data\\4.txt");
                        Scanner scanner = new Scanner(file);
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            interfacestr=interfacestr+line;
                        }
                        scanner.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    redisQueueTemplate.rPush("_threshold_queue", interfacestr);
                    Thread.sleep(20000);
                }
            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }


}
