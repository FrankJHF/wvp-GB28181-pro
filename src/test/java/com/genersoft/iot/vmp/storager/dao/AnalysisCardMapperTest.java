package com.genersoft.iot.vmp.storager.dao;

import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分析卡片数据访问层集成测试
 * @author Claude
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("分析卡片Mapper集成测试")
class AnalysisCardMapperTest {

    @Resource
    private AnalysisCardMapper analysisCardMapper;

    private AnalysisCard testCard;

    @BeforeEach
    void setUp() {
        testCard = createTestCard();
    }

    private AnalysisCard createTestCard() {
        AnalysisCard card = new AnalysisCard();
        card.setId("test-card-001");
        card.setTitle("火灾检测卡片");
        card.setDescription("基于深度学习的火灾检测分析");
        card.setIcon("fire");
        card.setEnabled(true);
        card.setPrompt("请分析视频中是否发生火灾或其他突发情况");
        card.setModelType("videollama3-fire-detection");
        card.setCreatedBy("admin");
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        // 设置标签
        List<String> tags = Arrays.asList("火灾检测", "安全监控", "实时分析");
        card.setTags(tags);

        // 设置分析配置
        Map<String, Object> config = new HashMap<>();
        config.put("inference_interval", 5);
        config.put("sampling_fps", 5);
        config.put("frame_buffer_size", 180);
        config.put("max_new_tokens", 180);
        card.setAnalysisConfig(config);

        return card;
    }

    @Test
    @DisplayName("测试插入分析卡片")
    void testInsert() {
        // Act
        int result = analysisCardMapper.insert(testCard);

        // Assert
        assertEquals(1, result);

        // 验证插入的数据
        AnalysisCard inserted = analysisCardMapper.selectById(testCard.getId());
        assertNotNull(inserted);
        assertEquals(testCard.getId(), inserted.getId());
        assertEquals(testCard.getTitle(), inserted.getTitle());
        assertEquals(testCard.getDescription(), inserted.getDescription());
        assertEquals(testCard.getIcon(), inserted.getIcon());
        assertEquals(testCard.getEnabled(), inserted.getEnabled());
        assertEquals(testCard.getPrompt(), inserted.getPrompt());
        assertEquals(testCard.getModelType(), inserted.getModelType());
        assertEquals(testCard.getCreatedBy(), inserted.getCreatedBy());

        // 验证JSON字段
        assertNotNull(inserted.getTags());
        assertTrue(inserted.getTags() instanceof List);
        assertEquals(3, ((List<?>) inserted.getTags()).size());

        assertNotNull(inserted.getAnalysisConfig());
        assertTrue(inserted.getAnalysisConfig() instanceof Map);
        Map<String, Object> config = (Map<String, Object>) inserted.getAnalysisConfig();
        assertEquals(5, config.get("inference_interval"));
        assertEquals(180, config.get("frame_buffer_size"));
    }

    @Test
    @DisplayName("测试根据ID查询分析卡片")
    void testSelectById() {
        // Arrange
        analysisCardMapper.insert(testCard);

        // Act
        AnalysisCard found = analysisCardMapper.selectById(testCard.getId());

        // Assert
        assertNotNull(found);
        assertEquals(testCard.getId(), found.getId());
        assertEquals(testCard.getTitle(), found.getTitle());
        assertEquals(testCard.getEnabled(), found.getEnabled());
    }

    @Test
    @DisplayName("测试查询不存在的卡片")
    void testSelectByIdNotFound() {
        // Act
        AnalysisCard found = analysisCardMapper.selectById("non-existing-id");

        // Assert
        assertNull(found);
    }

    @Test
    @DisplayName("测试更新分析卡片")
    void testUpdate() {
        // Arrange
        analysisCardMapper.insert(testCard);

        // 修改数据
        testCard.setTitle("更新后的标题");
        testCard.setDescription("更新后的描述");
        testCard.setEnabled(false);
        
        Map<String, Object> newConfig = new HashMap<>();
        newConfig.put("inference_interval", 10);
        newConfig.put("sampling_fps", 10);
        testCard.setAnalysisConfig(newConfig);

        // Act
        int result = analysisCardMapper.update(testCard);

        // Assert
        assertEquals(1, result);

        // 验证更新结果
        AnalysisCard updated = analysisCardMapper.selectById(testCard.getId());
        assertNotNull(updated);
        assertEquals("更新后的标题", updated.getTitle());
        assertEquals("更新后的描述", updated.getDescription());
        assertEquals(false, updated.getEnabled());
        
        Map<String, Object> updatedConfig = (Map<String, Object>) updated.getAnalysisConfig();
        assertEquals(10, updatedConfig.get("inference_interval"));
        assertEquals(10, updatedConfig.get("sampling_fps"));
    }

    @Test
    @DisplayName("测试删除分析卡片")
    void testDelete() {
        // Arrange
        analysisCardMapper.insert(testCard);
        assertNotNull(analysisCardMapper.selectById(testCard.getId()));

        // Act
        int result = analysisCardMapper.delete(testCard.getId());

        // Assert
        assertEquals(1, result);
        assertNull(analysisCardMapper.selectById(testCard.getId()));
    }

    @Test
    @DisplayName("测试查询所有分析卡片")
    void testSelectAll() {
        // Arrange - 插入多个测试卡片
        analysisCardMapper.insert(testCard);

        AnalysisCard card2 = createTestCard();
        card2.setId("test-card-002");
        card2.setTitle("烟雾检测卡片");
        card2.setEnabled(false);
        card2.setCreatedBy("user1");
        analysisCardMapper.insert(card2);

        AnalysisCard card3 = createTestCard();
        card3.setId("test-card-003");
        card3.setTitle("入侵检测卡片");
        card3.setEnabled(true);
        card3.setCreatedBy("admin");
        analysisCardMapper.insert(card3);

        // Act & Assert - 查询所有
        List<AnalysisCard> allCards = analysisCardMapper.selectAll(null, null, null);
        assertEquals(3, allCards.size());

        // 查询启用的卡片
        List<AnalysisCard> enabledCards = analysisCardMapper.selectAll(true, null, null);
        assertEquals(2, enabledCards.size());

        // 按创建人查询
        List<AnalysisCard> adminCards = analysisCardMapper.selectAll(null, "admin", null);
        assertEquals(2, adminCards.size());

        // 按标题模糊查询
        List<AnalysisCard> fireCards = analysisCardMapper.selectAll(null, null, "火灾");
        assertEquals(1, fireCards.size());
        assertEquals("火灾检测卡片", fireCards.get(0).getTitle());
    }

    @Test
    @DisplayName("测试查询启用的分析卡片")
    void testSelectEnabled() {
        // Arrange
        analysisCardMapper.insert(testCard);

        AnalysisCard disabledCard = createTestCard();
        disabledCard.setId("test-card-002");
        disabledCard.setEnabled(false);
        analysisCardMapper.insert(disabledCard);

        // Act
        List<AnalysisCard> enabledCards = analysisCardMapper.selectEnabled();

        // Assert
        assertEquals(1, enabledCards.size());
        assertEquals(testCard.getId(), enabledCards.get(0).getId());
        assertTrue(enabledCards.get(0).getEnabled());
    }

    @Test
    @DisplayName("测试按创建人查询分析卡片")
    void testSelectByCreatedBy() {
        // Arrange
        analysisCardMapper.insert(testCard);

        AnalysisCard otherUserCard = createTestCard();
        otherUserCard.setId("test-card-002");
        otherUserCard.setCreatedBy("user1");
        analysisCardMapper.insert(otherUserCard);

        // Act
        List<AnalysisCard> adminCards = analysisCardMapper.selectByCreatedBy("admin");
        List<AnalysisCard> userCards = analysisCardMapper.selectByCreatedBy("user1");

        // Assert
        assertEquals(1, adminCards.size());
        assertEquals(testCard.getId(), adminCards.get(0).getId());

        assertEquals(1, userCards.size());
        assertEquals(otherUserCard.getId(), userCards.get(0).getId());
    }

    @Test
    @DisplayName("测试统计分析卡片数量")
    void testCount() {
        // Arrange
        analysisCardMapper.insert(testCard);

        AnalysisCard card2 = createTestCard();
        card2.setId("test-card-002");
        card2.setEnabled(false);
        card2.setCreatedBy("user1");
        analysisCardMapper.insert(card2);

        // Act & Assert
        long totalCount = analysisCardMapper.count(null, null);
        assertEquals(2, totalCount);

        long enabledCount = analysisCardMapper.count(true, null);
        assertEquals(1, enabledCount);

        long disabledCount = analysisCardMapper.count(false, null);
        assertEquals(1, disabledCount);

        long adminCount = analysisCardMapper.count(null, "admin");
        assertEquals(1, adminCount);

        long userCount = analysisCardMapper.count(null, "user1");
        assertEquals(1, userCount);
    }

    @Test
    @DisplayName("测试批量更新启用状态")
    void testBatchUpdateEnabled() {
        // Arrange
        analysisCardMapper.insert(testCard);

        AnalysisCard card2 = createTestCard();
        card2.setId("test-card-002");
        card2.setEnabled(true);
        analysisCardMapper.insert(card2);

        AnalysisCard card3 = createTestCard();
        card3.setId("test-card-003");
        card3.setEnabled(true);
        analysisCardMapper.insert(card3);

        List<String> ids = Arrays.asList(testCard.getId(), card2.getId());

        // Act
        int result = analysisCardMapper.batchUpdateEnabled(ids, false);

        // Assert
        assertEquals(2, result);

        // 验证更新结果
        AnalysisCard updated1 = analysisCardMapper.selectById(testCard.getId());
        AnalysisCard updated2 = analysisCardMapper.selectById(card2.getId());
        AnalysisCard unchanged = analysisCardMapper.selectById(card3.getId());

        assertFalse(updated1.getEnabled());
        assertFalse(updated2.getEnabled());
        assertTrue(unchanged.getEnabled());
    }

    @Test
    @DisplayName("测试JSON字段的复杂数据类型")
    void testComplexJsonFields() {
        // Arrange - 创建复杂的JSON数据
        AnalysisCard complexCard = createTestCard();
        complexCard.setId("complex-card-001");

        // 复杂标签数据
        List<String> complexTags = Arrays.asList("标签1", "标签2", "带特殊字符的标签!@#");
        complexCard.setTags(complexTags);

        // 复杂配置数据
        Map<String, Object> complexConfig = new HashMap<>();
        complexConfig.put("string_param", "测试字符串");
        complexConfig.put("int_param", 100);
        complexConfig.put("bool_param", true);
        complexConfig.put("double_param", 3.14);
        
        Map<String, Object> nestedConfig = new HashMap<>();
        nestedConfig.put("nested_string", "嵌套值");
        nestedConfig.put("nested_array", Arrays.asList(1, 2, 3));
        complexConfig.put("nested_object", nestedConfig);
        
        complexCard.setAnalysisConfig(complexConfig);

        // Act
        analysisCardMapper.insert(complexCard);
        AnalysisCard retrieved = analysisCardMapper.selectById(complexCard.getId());

        // Assert
        assertNotNull(retrieved);
        
        // 验证标签
        List<?> retrievedTags = (List<?>) retrieved.getTags();
        assertEquals(3, retrievedTags.size());
        assertTrue(retrievedTags.contains("带特殊字符的标签!@#"));

        // 验证配置
        Map<String, Object> retrievedConfig = (Map<String, Object>) retrieved.getAnalysisConfig();
        assertEquals("测试字符串", retrievedConfig.get("string_param"));
        assertEquals(100, retrievedConfig.get("int_param"));
        assertEquals(true, retrievedConfig.get("bool_param"));
        assertEquals(3.14, retrievedConfig.get("double_param"));
        
        Map<String, Object> retrievedNested = (Map<String, Object>) retrievedConfig.get("nested_object");
        assertNotNull(retrievedNested);
        assertEquals("嵌套值", retrievedNested.get("nested_string"));
        
        List<?> nestedArray = (List<?>) retrievedNested.get("nested_array");
        assertEquals(3, nestedArray.size());
        assertTrue(nestedArray.contains(1));
        assertTrue(nestedArray.contains(2));
        assertTrue(nestedArray.contains(3));
    }

    @Test
    @DisplayName("测试NULL和空值处理")
    void testNullAndEmptyValues() {
        // Arrange
        AnalysisCard nullCard = new AnalysisCard();
        nullCard.setId("null-card-001");
        nullCard.setTitle("空值测试卡片");
        nullCard.setEnabled(true);
        nullCard.setCreatedBy("admin");
        nullCard.setCreatedAt(LocalDateTime.now());
        nullCard.setUpdatedAt(LocalDateTime.now());
        
        // 设置null和空值
        nullCard.setDescription(null);
        nullCard.setIcon(null);
        nullCard.setTags(null);
        nullCard.setPrompt("");
        nullCard.setAnalysisConfig(null);

        // Act
        int result = analysisCardMapper.insert(nullCard);

        // Assert
        assertEquals(1, result);
        
        AnalysisCard retrieved = analysisCardMapper.selectById(nullCard.getId());
        assertNotNull(retrieved);
        assertEquals("空值测试卡片", retrieved.getTitle());
        assertNull(retrieved.getDescription());
        assertNull(retrieved.getIcon());
        assertNull(retrieved.getTags());
        assertEquals("", retrieved.getPrompt());
        assertNull(retrieved.getAnalysisConfig());
    }
}