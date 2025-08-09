/**
 * WebSocket服务 - 智能分析告警推送
 * 用于接收实时告警消息
 */
class WebSocketService {
  constructor() {
    this.socket = null
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.listeners = []
    this.connected = false
  }

  /**
   * 连接WebSocket
   * @param {string} url WebSocket URL
   */
  connect(url) {
    try {
      // 构建完整的WebSocket URL
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = window.location.host
      const wsUrl = url || `${protocol}//${host}/channel/analysis/alarm`
      
      this.socket = new WebSocket(wsUrl)
      
      this.socket.onopen = (event) => {
        console.log('[WebSocket] 告警推送连接已建立')
        this.connected = true
        this.reconnectAttempts = 0
        this.onOpen(event)
      }
      
      this.socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.onMessage(data)
        } catch (error) {
          console.error('[WebSocket] 消息解析失败:', error)
        }
      }
      
      this.socket.onclose = (event) => {
        console.log('[WebSocket] 告警推送连接已关闭:', event.reason)
        this.connected = false
        this.onClose(event)
        
        // 尝试重连
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect()
        }
      }
      
      this.socket.onerror = (error) => {
        console.error('[WebSocket] 连接错误:', error)
        this.onError(error)
      }
      
    } catch (error) {
      console.error('[WebSocket] 连接失败:', error)
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
    
    this.connected = false
    this.listeners = []
  }

  /**
   * 添加消息监听器
   * @param {Function} callback 回调函数
   */
  addListener(callback) {
    if (typeof callback === 'function') {
      this.listeners.push(callback)
    }
  }

  /**
   * 移除消息监听器
   * @param {Function} callback 回调函数
   */
  removeListener(callback) {
    const index = this.listeners.indexOf(callback)
    if (index > -1) {
      this.listeners.splice(index, 1)
    }
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.connected && this.socket && this.socket.readyState === WebSocket.OPEN
  }

  /**
   * 调度重连
   */
  scheduleReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }
    
    this.reconnectAttempts++
    const delay = this.reconnectInterval * this.reconnectAttempts
    
    console.log(`[WebSocket] ${delay}ms后尝试第${this.reconnectAttempts}次重连`)
    
    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, delay)
  }

  /**
   * 连接打开事件
   */
  onOpen(event) {
    // 子类可以重写此方法
  }

  /**
   * 接收消息事件
   * @param {Object} data 消息数据
   */
  onMessage(data) {
    // 通知所有监听器
    this.listeners.forEach(callback => {
      try {
        callback(data)
      } catch (error) {
        console.error('[WebSocket] 监听器回调错误:', error)
      }
    })
  }

  /**
   * 连接关闭事件
   */
  onClose(event) {
    // 子类可以重写此方法
  }

  /**
   * 连接错误事件
   */
  onError(error) {
    // 子类可以重写此方法
  }
}

// 创建全局实例
const alarmWebSocket = new WebSocketService()

export default alarmWebSocket