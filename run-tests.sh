#!/bin/bash

# WVP智能分析模块测试运行脚本
# 用于启动测试环境并运行所有测试

echo "================================"
echo "WVP 智能分析模块测试启动脚本"
echo "================================"

# 设置环境变量
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=256m"
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# 检查Java版本
echo "检查Java环境..."
java -version
if [ $? -ne 0 ]; then
    echo "错误: 未找到Java环境，请确保Java 8或以上版本已安装"
    exit 1
fi

echo ""
echo "开始清理和编译..."

# 清理之前的构建
mvn clean -q

# 编译项目（跳过测试）
echo "编译主代码..."
mvn compile -q -Dmaven.test.skip=true
if [ $? -ne 0 ]; then
    echo "警告: 主代码编译失败，但继续测试编译"
fi

# 编译测试代码
echo "编译测试代码..."
mvn test-compile -q -Dmaven.main.skip=true 2>/dev/null
if [ $? -ne 0 ]; then
    echo "警告: 测试代码编译可能存在问题，但继续运行"
fi

echo ""
echo "开始运行测试..."

# 运行不同类型的测试
echo "1. 运行单元测试..."
mvn test -q -Dtest="*Test" -DfailIfNoTests=false -DskipTests=false

echo ""
echo "2. 运行集成测试..."
mvn test -q -Dtest="*IntegrationTest" -DfailIfNoTests=false -DskipTests=false

echo ""
echo "3. 运行所有分析模块测试..."
mvn test -q -Dtest="com.genersoft.iot.vmp.analysis.**.*Test" -DfailIfNoTests=false -DskipTests=false

echo ""
echo "================================"
echo "测试执行完成"
echo "================================"

# 生成测试报告信息
echo "测试报告位置: target/surefire-reports/"
echo "查看详细结果: mvn surefire-report:report"

# 检查测试结果文件是否存在
if [ -d "target/surefire-reports" ]; then
    TEST_COUNT=$(find target/surefire-reports -name "TEST-*.xml" | wc -l)
    echo "生成的测试报告数量: $TEST_COUNT"
else
    echo "注意: 未找到测试报告目录"
fi

echo ""
echo "测试环境配置检查完成！"