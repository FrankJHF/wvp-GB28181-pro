package com.genersoft.iot.vmp.analysis.bean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务操作枚举单元测试
 * @author Claude
 */
@DisplayName("任务操作枚举测试")
class TaskActionTest {

    @Test
    @DisplayName("测试根据值获取枚举")
    void testFromValue() {
        assertEquals(TaskAction.START, TaskAction.fromValue("start"));
        assertEquals(TaskAction.PAUSE, TaskAction.fromValue("pause"));
        assertEquals(TaskAction.RESUME, TaskAction.fromValue("resume"));
        assertEquals(TaskAction.STOP, TaskAction.fromValue("stop"));
        assertEquals(TaskAction.DELETE, TaskAction.fromValue("delete"));
    }

    @Test
    @DisplayName("测试未知值异常")
    void testFromValueWithUnknownValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TaskAction.fromValue("unknown")
        );
        assertEquals("Unknown task action: unknown", exception.getMessage());
    }

    @Test
    @DisplayName("测试获取值和描述")
    void testValueAndDescription() {
        assertEquals("start", TaskAction.START.getValue());
        assertEquals("启动", TaskAction.START.getDescription());

        assertEquals("pause", TaskAction.PAUSE.getValue());
        assertEquals("暂停", TaskAction.PAUSE.getDescription());

        assertEquals("resume", TaskAction.RESUME.getValue());
        assertEquals("恢复", TaskAction.RESUME.getDescription());

        assertEquals("stop", TaskAction.STOP.getValue());
        assertEquals("停止", TaskAction.STOP.getDescription());

        assertEquals("delete", TaskAction.DELETE.getValue());
        assertEquals("删除", TaskAction.DELETE.getDescription());
    }

    @Test
    @DisplayName("测试所有操作的一致性")
    void testAllActionsConsistency() {
        for (TaskAction action : TaskAction.values()) {
            // 检查值不为空
            assertNotNull(action.getValue());
            assertFalse(action.getValue().trim().isEmpty());

            // 检查描述不为空
            assertNotNull(action.getDescription());
            assertFalse(action.getDescription().trim().isEmpty());

            // 检查fromValue一致性
            assertEquals(action, TaskAction.fromValue(action.getValue()));
        }
    }

    @Test
    @DisplayName("测试所有操作值的唯一性")
    void testAllActionsUniqueness() {
        TaskAction[] actions = TaskAction.values();
        for (int i = 0; i < actions.length; i++) {
            for (int j = i + 1; j < actions.length; j++) {
                assertNotEquals(actions[i].getValue(), actions[j].getValue(),
                        "操作值必须唯一: " + actions[i].getValue());
                assertNotEquals(actions[i].getDescription(), actions[j].getDescription(),
                        "操作描述必须唯一: " + actions[i].getDescription());
            }
        }
    }
}