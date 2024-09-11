package com.jcca.web.test.event;

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
public class eventTest {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


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
                        File file = new File("");
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
    public void testMemory() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("memoryAdapter.txt");
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
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }


    @Test
    public void testDisk() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("diskAdapter.txt");
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
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testIpmi() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("ipmiAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }


    @Test
    public void testPing() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("pingAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testPing1() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("pingAdapter1.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testPing2() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("pingAdapter2.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testPing3() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("pingAdapter3.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testPing4() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("pingAdapter4.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testSensor() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("sensorAdpater.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testDBAlarm() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("dbAlarmAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testdsInfoAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("dsInfoAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }


    @Test
    public void testRaidInfoAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("raidInfoAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }


    @Test
    public void testopticalAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("opticalAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testaixSystemMsgAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("aixSystemMsgAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testdbAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("dbAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testPCBAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("PCBAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }


    @Test
    public void testxinDHYAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("xinDHYAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testTiekeWorkstateAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("tiekeWorkstateAdapter1.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testTiekeSecureLinkAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("tiekeSecureLinkAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testTiekeChannelLinkAdapter1() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("tiekeChannelLinkAdapter1.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testTiekeVersionAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("tiekeVersionAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testBeiyangLinkAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("beiyangLinkAdapter1.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testBeiyangVersionAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("beiyangVersionAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testBeiYangWorkStateAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("beiYangWorkStateAdapter1.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testCascoLinkAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("cascoLinkAdapter1.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }

    @Test
    public void testCascoMasterAdapter() throws InterruptedException {
        String cpuStr = "";

        try {
            File file = new File("cascoMasterAdapter.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
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
                    redisQueueTemplate.rPush("_broker_alarm_queue", finalStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        while (true) {
            Thread.sleep(5000);
        }
    }
}
