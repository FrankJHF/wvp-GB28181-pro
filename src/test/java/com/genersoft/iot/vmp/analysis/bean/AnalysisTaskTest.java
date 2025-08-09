package com.genersoft.iot.vmp.analysis.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分析任务实体类状态转换逻辑单元测试
 * @author Claude
 */
@DisplayName("分析任务状态转换测试")
class AnalysisTaskTest {

    private AnalysisTask task;

    @BeforeEach
    void setUp() {
        task = new AnalysisTask();
        task.setId("test-task-001");
        task.setTaskName("测试任务");
    }

    @Test
    @DisplayName("测试过渡状态判断")
    void testIsTransitioning() {
        // 测试过渡状态
        task.setStatus(TaskStatus.STARTING);
        assertTrue(task.isTransitioning());

        task.setStatus(TaskStatus.PAUSING);
        assertTrue(task.isTransitioning());

        task.setStatus(TaskStatus.RESUMING);
        assertTrue(task.isTransitioning());

        task.setStatus(TaskStatus.STOPPING);
        assertTrue(task.isTransitioning());

        // 测试稳定状态
        task.setStatus(TaskStatus.CREATED);
        assertFalse(task.isTransitioning());

        task.setStatus(TaskStatus.RUNNING);
        assertFalse(task.isTransitioning());

        task.setStatus(TaskStatus.PAUSED);
        assertFalse(task.isTransitioning());

        task.setStatus(TaskStatus.STOPPED);
        assertFalse(task.isTransitioning());
    }

    @Test
    @DisplayName("测试启动条件判断")
    void testCanStart() {
        // 可以启动的状态
        task.setStatus(TaskStatus.CREATED);
        assertTrue(task.canStart());

        task.setStatus(TaskStatus.STOPPED);
        assertTrue(task.canStart());

        task.setStatus(TaskStatus.FAILED);
        assertTrue(task.canStart());

        task.setStatus(TaskStatus.ERROR);
        assertTrue(task.canStart());

        // 不可以启动的状态
        task.setStatus(TaskStatus.RUNNING);
        assertFalse(task.canStart());

        task.setStatus(TaskStatus.STARTING);
        assertFalse(task.canStart());

        task.setStatus(TaskStatus.PAUSED);
        assertFalse(task.canStart());
    }

    @Test
    @DisplayName("测试暂停条件判断")
    void testCanPause() {
        // 只有运行状态可以暂停
        task.setStatus(TaskStatus.RUNNING);
        assertTrue(task.canPause());

        // 其他状态不可暂停
        task.setStatus(TaskStatus.CREATED);
        assertFalse(task.canPause());

        task.setStatus(TaskStatus.PAUSED);
        assertFalse(task.canPause());

        task.setStatus(TaskStatus.STOPPED);
        assertFalse(task.canPause());
    }

    @Test
    @DisplayName("测试恢复条件判断")
    void testCanResume() {
        // 只有暂停状态可以恢复
        task.setStatus(TaskStatus.PAUSED);
        assertTrue(task.canResume());

        // 其他状态不可恢复
        task.setStatus(TaskStatus.RUNNING);
        assertFalse(task.canResume());

        task.setStatus(TaskStatus.STOPPED);
        assertFalse(task.canResume());

        task.setStatus(TaskStatus.CREATED);
        assertFalse(task.canResume());
    }

    @Test
    @DisplayName("测试停止条件判断")
    void testCanStop() {
        // 可以停止的状态
        task.setStatus(TaskStatus.RUNNING);
        assertTrue(task.canStop());

        task.setStatus(TaskStatus.PAUSED);
        assertTrue(task.canStop());

        task.setStatus(TaskStatus.STARTING);
        assertTrue(task.canStop());

        task.setStatus(TaskStatus.PAUSING);
        assertTrue(task.canStop());

        task.setStatus(TaskStatus.RESUMING);
        assertTrue(task.canStop());

        // 不可以停止的状态
        task.setStatus(TaskStatus.CREATED);
        assertFalse(task.canStop());

        task.setStatus(TaskStatus.STOPPED);
        assertFalse(task.canStop());
    }

    @Test
    @DisplayName("测试删除条件判断")
    void testCanDelete() {
        // 可以删除的状态
        task.setStatus(TaskStatus.CREATED);
        assertTrue(task.canDelete());

        task.setStatus(TaskStatus.STOPPED);
        assertTrue(task.canDelete());

        task.setStatus(TaskStatus.FAILED);
        assertTrue(task.canDelete());

        task.setStatus(TaskStatus.ERROR);
        assertTrue(task.canDelete());

        // 不可删除的状态
        task.setStatus(TaskStatus.RUNNING);
        assertFalse(task.canDelete());

        task.setStatus(TaskStatus.PAUSED);
        assertFalse(task.canDelete());
    }

    @Test
    @DisplayName("测试活跃状态判断")
    void testIsActive() {
        // 活跃状态
        task.setStatus(TaskStatus.RUNNING);
        assertTrue(task.isActive());

        task.setStatus(TaskStatus.STARTING);
        assertTrue(task.isActive());

        task.setStatus(TaskStatus.PAUSING);
        assertTrue(task.isActive());

        task.setStatus(TaskStatus.RESUMING);
        assertTrue(task.isActive());

        // 非活跃状态
        task.setStatus(TaskStatus.CREATED);
        assertFalse(task.isActive());

        task.setStatus(TaskStatus.PAUSED);
        assertFalse(task.isActive());

        task.setStatus(TaskStatus.STOPPED);
        assertFalse(task.isActive());
    }

    @Test
    @DisplayName("测试终止状态判断")
    void testIsTerminated() {
        // 终止状态
        task.setStatus(TaskStatus.STOPPED);
        assertTrue(task.isTerminated());

        task.setStatus(TaskStatus.FAILED);
        assertTrue(task.isTerminated());

        task.setStatus(TaskStatus.ERROR);
        assertTrue(task.isTerminated());

        // 非终止状态
        task.setStatus(TaskStatus.RUNNING);
        assertFalse(task.isTerminated());

        task.setStatus(TaskStatus.CREATED);
        assertFalse(task.isTerminated());
    }

    @Test
    @DisplayName("测试操作权限检查")
    void testCanPerformAction() {
        task.setStatus(TaskStatus.CREATED);

        // 测试启动操作
        assertTrue(task.canPerformAction(TaskAction.START));
        assertFalse(task.canPerformAction(TaskAction.PAUSE));
        assertFalse(task.canPerformAction(TaskAction.RESUME));
        assertFalse(task.canPerformAction(TaskAction.STOP));
        assertTrue(task.canPerformAction(TaskAction.DELETE));

        // 测试运行状态
        task.setStatus(TaskStatus.RUNNING);
        assertFalse(task.canPerformAction(TaskAction.START));
        assertTrue(task.canPerformAction(TaskAction.PAUSE));
        assertFalse(task.canPerformAction(TaskAction.RESUME));
        assertTrue(task.canPerformAction(TaskAction.STOP));
        assertFalse(task.canPerformAction(TaskAction.DELETE));

        // 测试null操作
        assertFalse(task.canPerformAction(null));
    }

    @Test
    @DisplayName("测试获取过渡状态")
    void testGetTransitioningStatus() {
        task.setStatus(TaskStatus.CREATED);

        assertEquals(TaskStatus.STARTING, task.getTransitioningStatus(TaskAction.START));
        assertEquals(TaskStatus.PAUSING, task.getTransitioningStatus(TaskAction.PAUSE));
        assertEquals(TaskStatus.RESUMING, task.getTransitioningStatus(TaskAction.RESUME));
        assertEquals(TaskStatus.STOPPING, task.getTransitioningStatus(TaskAction.STOP));

        // null操作返回当前状态
        assertEquals(TaskStatus.CREATED, task.getTransitioningStatus(null));
    }

    @Test
    @DisplayName("测试获取最终状态")
    void testGetFinalStatus() {
        task.setStatus(TaskStatus.STARTING);

        assertEquals(TaskStatus.RUNNING, task.getFinalStatus(TaskAction.START));
        assertEquals(TaskStatus.PAUSED, task.getFinalStatus(TaskAction.PAUSE));
        assertEquals(TaskStatus.RUNNING, task.getFinalStatus(TaskAction.RESUME));
        assertEquals(TaskStatus.STOPPED, task.getFinalStatus(TaskAction.STOP));

        // null操作返回当前状态
        assertEquals(TaskStatus.STARTING, task.getFinalStatus(null));
    }

    @Test
    @DisplayName("测试活跃时间更新")
    void testUpdateLastActiveTime() {
        LocalDateTime beforeUpdate = LocalDateTime.now();
        task.updateLastActiveTime();
        LocalDateTime afterUpdate = LocalDateTime.now();

        assertNotNull(task.getLastActiveTime());
        assertTrue(task.getLastActiveTime().isAfter(beforeUpdate.minusSeconds(1)));
        assertTrue(task.getLastActiveTime().isBefore(afterUpdate.plusSeconds(1)));
    }

    @Test
    @DisplayName("测试状态同步时间更新")
    void testUpdateLastStatusSync() {
        LocalDateTime beforeUpdate = LocalDateTime.now();
        task.updateLastStatusSync();
        LocalDateTime afterUpdate = LocalDateTime.now();

        assertNotNull(task.getLastStatusSync());
        assertTrue(task.getLastStatusSync().isAfter(beforeUpdate.minusSeconds(1)));
        assertTrue(task.getLastStatusSync().isBefore(afterUpdate.plusSeconds(1)));
    }

    @Test
    @DisplayName("测试状态同步需求判断")
    void testNeedsStatusSync() {
        // 没有同步时间的情况
        task.setLastStatusSync(null);
        assertTrue(task.needsStatusSync(5));

        // 超过阈值时间
        task.setLastStatusSync(LocalDateTime.now().minusMinutes(10));
        assertTrue(task.needsStatusSync(5));

        // 未超过阈值时间
        task.setLastStatusSync(LocalDateTime.now().minusMinutes(2));
        assertFalse(task.needsStatusSync(5));

        // 刚好在阈值上
        task.setLastStatusSync(LocalDateTime.now().minusMinutes(5));
        assertTrue(task.needsStatusSync(5));
    }

    @Test
    @DisplayName("测试状态转换的完整流程")
    void testStatusTransitionWorkflow() {
        // 初始状态：已创建
        task.setStatus(TaskStatus.CREATED);
        assertTrue(task.canStart());
        assertFalse(task.isActive());
        assertFalse(task.isTerminated());

        // 启动过程：创建 -> 启动中 -> 运行中
        TaskStatus transitioningStatus = task.getTransitioningStatus(TaskAction.START);
        assertEquals(TaskStatus.STARTING, transitioningStatus);
        task.setStatus(transitioningStatus);
        assertTrue(task.isTransitioning());
        assertTrue(task.isActive());

        TaskStatus finalStatus = task.getFinalStatus(TaskAction.START);
        assertEquals(TaskStatus.RUNNING, finalStatus);
        task.setStatus(finalStatus);
        assertFalse(task.isTransitioning());
        assertTrue(task.isActive());
        assertTrue(task.canPause());
        assertTrue(task.canStop());

        // 暂停过程：运行中 -> 暂停中 -> 已暂停
        task.setStatus(task.getTransitioningStatus(TaskAction.PAUSE));
        assertEquals(TaskStatus.PAUSING, task.getStatus());
        task.setStatus(task.getFinalStatus(TaskAction.PAUSE));
        assertEquals(TaskStatus.PAUSED, task.getStatus());
        assertTrue(task.canResume());
        assertTrue(task.canStop());

        // 停止过程：已暂停 -> 停止中 -> 已停止
        task.setStatus(task.getTransitioningStatus(TaskAction.STOP));
        assertEquals(TaskStatus.STOPPING, task.getStatus());
        task.setStatus(task.getFinalStatus(TaskAction.STOP));
        assertEquals(TaskStatus.STOPPED, task.getStatus());
        assertTrue(task.isTerminated());
        assertTrue(task.canDelete());
        assertTrue(task.canStart());
    }
}