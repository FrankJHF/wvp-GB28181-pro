package com.genersoft.iot.vmp.analysis.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务状态枚举单元测试
 * @author Claude
 */
@DisplayName("任务状态枚举测试")
class TaskStatusTest {

    @Test
    @DisplayName("测试过渡状态判断")
    void testIsTransitioning() {
        // 过渡状态
        assertTrue(TaskStatus.STARTING.isTransitioning());
        assertTrue(TaskStatus.PAUSING.isTransitioning());
        assertTrue(TaskStatus.RESUMING.isTransitioning());
        assertTrue(TaskStatus.STOPPING.isTransitioning());

        // 稳定状态
        assertFalse(TaskStatus.CREATED.isTransitioning());
        assertFalse(TaskStatus.RUNNING.isTransitioning());
        assertFalse(TaskStatus.PAUSED.isTransitioning());
        assertFalse(TaskStatus.STOPPED.isTransitioning());
        assertFalse(TaskStatus.FAILED.isTransitioning());
        assertFalse(TaskStatus.ERROR.isTransitioning());
    }

    @Test
    @DisplayName("测试稳定状态判断")
    void testIsStable() {
        // 稳定状态
        assertTrue(TaskStatus.CREATED.isStable());
        assertTrue(TaskStatus.RUNNING.isStable());
        assertTrue(TaskStatus.PAUSED.isStable());
        assertTrue(TaskStatus.STOPPED.isStable());
        assertTrue(TaskStatus.FAILED.isStable());
        assertTrue(TaskStatus.ERROR.isStable());

        // 过渡状态
        assertFalse(TaskStatus.STARTING.isStable());
        assertFalse(TaskStatus.PAUSING.isStable());
        assertFalse(TaskStatus.RESUMING.isStable());
        assertFalse(TaskStatus.STOPPING.isStable());
    }

    @Test
    @DisplayName("测试终止状态判断")
    void testIsTerminated() {
        // 终止状态
        assertTrue(TaskStatus.STOPPED.isTerminated());
        assertTrue(TaskStatus.FAILED.isTerminated());
        assertTrue(TaskStatus.ERROR.isTerminated());

        // 非终止状态
        assertFalse(TaskStatus.CREATED.isTerminated());
        assertFalse(TaskStatus.STARTING.isTerminated());
        assertFalse(TaskStatus.RUNNING.isTerminated());
        assertFalse(TaskStatus.PAUSING.isTerminated());
        assertFalse(TaskStatus.PAUSED.isTerminated());
        assertFalse(TaskStatus.RESUMING.isTerminated());
        assertFalse(TaskStatus.STOPPING.isTerminated());
    }

    @Test
    @DisplayName("测试活跃状态判断")
    void testIsActive() {
        // 活跃状态
        assertTrue(TaskStatus.RUNNING.isActive());
        assertTrue(TaskStatus.STARTING.isActive());
        assertTrue(TaskStatus.PAUSING.isActive());
        assertTrue(TaskStatus.RESUMING.isActive());

        // 非活跃状态
        assertFalse(TaskStatus.CREATED.isActive());
        assertFalse(TaskStatus.PAUSED.isActive());
        assertFalse(TaskStatus.STOPPED.isActive());
        assertFalse(TaskStatus.FAILED.isActive());
        assertFalse(TaskStatus.ERROR.isActive());
        assertFalse(TaskStatus.STOPPING.isActive());
    }

    @Test
    @DisplayName("测试根据值获取枚举")
    void testFromValue() {
        assertEquals(TaskStatus.CREATED, TaskStatus.fromValue("created"));
        assertEquals(TaskStatus.STARTING, TaskStatus.fromValue("starting"));
        assertEquals(TaskStatus.RUNNING, TaskStatus.fromValue("running"));
        assertEquals(TaskStatus.PAUSING, TaskStatus.fromValue("pausing"));
        assertEquals(TaskStatus.PAUSED, TaskStatus.fromValue("paused"));
        assertEquals(TaskStatus.RESUMING, TaskStatus.fromValue("resuming"));
        assertEquals(TaskStatus.STOPPING, TaskStatus.fromValue("stopping"));
        assertEquals(TaskStatus.STOPPED, TaskStatus.fromValue("stopped"));
        assertEquals(TaskStatus.FAILED, TaskStatus.fromValue("failed"));
        assertEquals(TaskStatus.ERROR, TaskStatus.fromValue("error"));
    }

    @Test
    @DisplayName("测试未知值异常")
    void testFromValueWithUnknownValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TaskStatus.fromValue("unknown")
        );
        assertEquals("Unknown status: unknown", exception.getMessage());
    }

    @Test
    @DisplayName("测试获取值和描述")
    void testValueAndDescription() {
        assertEquals("created", TaskStatus.CREATED.getValue());
        assertEquals("已创建", TaskStatus.CREATED.getDescription());

        assertEquals("running", TaskStatus.RUNNING.getValue());
        assertEquals("运行中", TaskStatus.RUNNING.getDescription());

        assertEquals("failed", TaskStatus.FAILED.getValue());
        assertEquals("失败", TaskStatus.FAILED.getDescription());
    }

    @Test
    @DisplayName("测试所有状态的一致性")
    void testAllStatusConsistency() {
        for (TaskStatus status : TaskStatus.values()) {
            // 检查值不为空
            assertNotNull(status.getValue());
            assertFalse(status.getValue().trim().isEmpty());

            // 检查描述不为空
            assertNotNull(status.getDescription());
            assertFalse(status.getDescription().trim().isEmpty());

            // 检查fromValue一致性
            assertEquals(status, TaskStatus.fromValue(status.getValue()));

            // 检查状态属性的一致性
            boolean isTransitioning = status.isTransitioning();
            boolean isStable = status.isStable();
            assertTrue(isTransitioning != isStable, "状态必须是过渡状态或稳定状态之一");

            // 活跃状态不应该包含终止状态
            if (status.isActive()) {
                assertFalse(status.isTerminated(), "活跃状态不应该是终止状态");
            }

            // STOPPING状态特殊处理：它是过渡状态但不是活跃状态
            if (status == TaskStatus.STOPPING) {
                assertTrue(status.isTransitioning());
                assertFalse(status.isActive());
            }
        }
    }
}