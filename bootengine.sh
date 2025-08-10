#!/bin/bash

######################################################
# Copyright 2019 Pham Ngoc Hoai
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Repo: https://github.com/tyrion9/spring-boot-startup-script
#
######### PARAM ######################################

JAVA_OPT="-Xmx1024m -Dspring.profiles.active=dev -Dspring.config.location=file:${PWD}/target/"
# 重新获取JARFILE的逻辑放到start函数中，确保在build之后能找到最新的包
JARFILE=""
PID_FILE=pid.file
RUNNING=N
PWD=`pwd`

######### DO NOT MODIFY ########

# 检查进程是否在运行
check_pid() {
    if [ -f $PID_FILE ]; then
        PID=`cat $PID_FILE`
        if [ ! -z "$PID" ] && kill -0 $PID 2>/dev/null; then
            RUNNING=Y
        else
            RUNNING=N
        fi
    else
        RUNNING=N
    fi
}

start()
{
        check_pid
        if [ $RUNNING == "Y" ]; then
                echo "Application already started (PID: $PID)"
        else
                # 在启动时查找最新的jar包
                JARFILE=`ls -1r target/*.jar 2>/dev/null | head -n 1`
                if [ -z "$JARFILE" ]
                then
                        echo "ERROR: jar file not found in target/"
                else
                        nohup java  $JAVA_OPT -Djava.security.egd=file:/dev/./urandom -jar $JARFILE > nohup.out 2>&1  &
                        echo $! > $PID_FILE
                        # 使用$JARFILE的basename，而不是完整路径
                        echo "Application `basename $JARFILE` starting..."
                        sleep 1
                        tail -f nohup.out
                fi
        fi
}

stop()
{
        check_pid
        if [ $RUNNING == "Y" ]; then
                kill -9 $PID
                rm -f $PID_FILE
                echo "Application stopped (PID: $PID)"
        else
                echo "Application not running"
        fi
}

restart()
{
        stop
        sleep 1
        start
}

build()
{
    set -e
    echo "Starting project build..."
    echo "-----------------------------------"

    if [ -d "web" ]; then
        echo "Step 1: Building frontend in 'web' directory..."
        cd web
        npm run build:prod
        cd ..
        echo "Frontend build completed."
        echo "-----------------------------------"
    else
        echo "Warning: 'web' directory not found, skipping frontend build."
        echo "-----------------------------------"
    fi

    echo "Step 2: Building backend with Maven..."
    mvn clean package -Dmaven.test.skip=true
    echo "Backend build completed."
    echo "-----------------------------------"
    echo "Project build finished successfully."
    set +e
}

# --- 新增 redeploy 函数 ---
redeploy()
{
    echo "=== Starting Redeploy Process ==="

    echo ""
    echo "Step 1: Stopping application..."
    stop

    # 增加一个短暂的暂停确保端口完全释放
    sleep 2

    echo ""
    echo "Step 2: Building application..."
    # 调用build函数，如果构建失败，脚本会因为set -e而退出
    build

    # 检查构建产物是否存在
    LATEST_JAR=`ls -1r target/*.jar 2>/dev/null | head -n 1`
    if [ -z "$LATEST_JAR" ]; then
        echo ""
        echo "ERROR: Build failed, no JAR file found. Aborting start."
        exit 1
    fi

    echo ""
    echo "Step 3: Starting application..."
    start

    echo ""
    echo "=== Redeploy Process Finished ==="
}
# --- 新增结束 ---


case "$1" in

        'start')
                start
                ;;

        'stop')
                stop
                ;;

        'restart')
                restart
                ;;

        'build')
                build
                ;;

        # --- 新增 redeploy 选项 ---
        'redeploy')
                redeploy
                ;;
        # --- 新增结束 ---

        *)
                # --- 修改用法提示 ---
                echo "Usage: $0 { start | stop | restart | build | redeploy }"
                exit 1
                ;;
esac
exit 0