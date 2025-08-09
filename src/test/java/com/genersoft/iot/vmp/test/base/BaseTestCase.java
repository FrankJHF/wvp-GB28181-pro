package com.genersoft.iot.vmp.test.base;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

/**
 * 测试基础类
 * 提供通用的测试环境设置和配置
 * 
 * @author Claude
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public abstract class BaseTestCase {
    
    /**
     * 测试用常量
     */
    protected static final String TEST_USER = "test-user";
    protected static final String TEST_ADMIN = "test-admin";
    protected static final String TEST_DEVICE_ID = "34020000001320000001";
    protected static final String TEST_CHANNEL_ID = "34020000001310000001";
    protected static final String TEST_RTSP_URL = "rtsp://test:password@192.168.1.100:554/stream1";
    
    /**
     * 在测试开始前执行的通用设置
     * 子类可以重写此方法添加特定的设置
     */
    protected void setUp() {
        // 通用的测试前设置
    }
    
    /**
     * 在测试结束后执行的通用清理
     * 子类可以重写此方法添加特定的清理逻辑
     */
    protected void tearDown() {
        // 通用的测试后清理
    }
}