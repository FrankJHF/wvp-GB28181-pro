#!/bin/bash

# WVP 数据库初始化脚本
# 使用方法: ./init_database.sh

set -e  # 遇到错误立即退出

# 配置变量
DB_NAME="wvp"
DB_USER="wvpuser"
DB_PASSWORD="123456789"
MYSQL_ROOT_PASSWORD="sysu888"
BASE_DIR="/home/firelab/JHF/wvp_vlm/wvp-GB28181-pro"
SQL_DIR="${BASE_DIR}/数据库/2.7.4"

echo "=== WVP 数据库初始化开始 ==="

# 1. 检查MySQL服务状态
echo "1. 检查MySQL服务状态..."
systemctl status mysql --no-pager -l || {
    echo "MySQL服务未运行，正在启动..."
    sudo systemctl start mysql
}

# 2. 删除现有数据库（如果存在）
echo "2. 删除现有数据库（如果存在）..."
mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "DROP DATABASE IF EXISTS ${DB_NAME};" 2>/dev/null || true

# 3. 创建数据库
echo "3. 创建数据库 ${DB_NAME}..."
mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "CREATE DATABASE ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 4. 导入初始化SQL脚本
echo "4. 导入初始化SQL脚本..."
mysql -u root -p${MYSQL_ROOT_PASSWORD} ${DB_NAME} < "${SQL_DIR}/初始化-mysql-2.7.4.sql"

# 5. 创建用户并授权
echo "5. 创建用户 ${DB_USER} 并授权..."
mysql -u root -p${MYSQL_ROOT_PASSWORD} <<EOF
-- 删除现有用户（如果存在）
DROP USER IF EXISTS '${DB_USER}'@'localhost';

-- 创建新用户
CREATE USER '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';

-- 授予权限
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
EOF

# 6. 导入智能分析模块SQL（可选）
echo "6. 导入智能分析模块SQL..."
if [ -f "${SQL_DIR}/智能分析-mysql-2.7.4.sql" ]; then
    mysql -u root -p${MYSQL_ROOT_PASSWORD} ${DB_NAME} < "${SQL_DIR}/智能分析-mysql-2.7.4.sql"
    echo "   智能分析模块SQL导入完成"
else
    echo "   智能分析模块SQL文件不存在，跳过"
fi

# 7. 验证数据库初始化
echo "7. 验证数据库初始化..."
mysql -u ${DB_USER} -p${DB_PASSWORD} -e "USE ${DB_NAME}; SHOW TABLES;" > /dev/null && {
    echo "   数据库连接测试成功"
} || {
    echo "   数据库连接测试失败"
    exit 1
}

# 8. 显示数据库信息
echo ""
echo "=== 数据库初始化完成 ==="
echo "数据库名: ${DB_NAME}"
echo "用户名: ${DB_USER}"
echo "密码: ${DB_PASSWORD}"
echo "主机: localhost"
echo ""

# 9. 显示表统计信息
echo "数据库表统计:"
mysql -u ${DB_USER} -p${DB_PASSWORD} -e "
USE ${DB_NAME};
SELECT
    COUNT(*) as table_count,
    CONCAT(ROUND(SUM(data_length + index_length) / 1024 / 1024, 2), ' MB') as database_size
FROM information_schema.tables
WHERE table_schema = '${DB_NAME}';"

echo ""
echo "主要数据表:"
mysql -u ${DB_USER} -p${DB_PASSWORD} -e "
USE ${DB_NAME};
SHOW TABLES;" | grep -E "(device|channel|platform|user|analysis)" || echo "   未找到核心业务表"

echo ""
echo "=== 初始化脚本执行完成 ==="