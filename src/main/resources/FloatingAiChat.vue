<template>
  <div>
    <!-- 浮动图片 -->
    <div 
      ref="floatingImage" 
      class="floating-image"
      @mousedown="startDrag"
      @click="handleClick"
      :style="imagePosition"
    >
      <img :src="aiCharacterImage" alt="AI助手">
    </div>

    <!-- 聊天窗口 -->
    <div 
      class="chat-container" 
      v-show="isChatOpen"
      :style="chatPosition"
      :class="{'slide-in': isChatOpen}"
    >
      <div class="chat-header">
        <h3>AI聊天助手</h3>
        <button class="close-btn" @click="closeChat">×</button>
      </div>
      <div class="chat-messages" ref="chatMessages">
        <div 
          v-for="(message, index) in messages" 
          :key="index" 
          class="message" 
          :class="message.sender === 'user' ? 'user-message' : 'ai-message'"
          v-html="formatMessage(message.text)"
        ></div>
      </div>
      <div class="chat-input-container">
        <textarea 
          :value="userInput"
          @input="e => userInput = (e.target as HTMLTextAreaElement).value"
          @keypress.enter.prevent="sendMessage"
          placeholder="请输入您的问题..."
          ref="userInputRef"
          id="user-input"
        ></textarea>
        <button class="send-btn" @click="sendMessage">发送</button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';

export default defineComponent({
  name: 'FloatingAiChat',
  setup() {
    // 状态变量
    const isChatOpen = ref(false);
    const isDragging = ref(false);
    const dragStartTime = ref(0);
    const dragStartPosition = ref({ x: 0, y: 0 });
    const position = ref({ x: 20, y: 20 });
    const dragOffset = ref({ x: 0, y: 0 });
    const userInput = ref('');
    const messages = ref<Array<{ text: string, sender: 'user' | 'ai' }>>([]);
    const chatId = ref(generateChatId());
    const controller = ref<AbortController | null>(null);
    const chatMessages = ref<HTMLElement | null>(null);
    const floatingImage = ref<HTMLElement | null>(null);
    const userInputRef = ref<HTMLTextAreaElement | null>(null);
    const wasChatOpenBeforeDrag = ref(false);
    const isClick = ref(true); // 用于区分点击和拖动
    
    // 使用导入的图片
    const aiCharacterImage = ref('./ai-character.png');
    
    // 计算属性
    const imagePosition = computed(() => {
      return {
        top: `${position.value.y}px`,
        left: `${position.value.x}px`
      };
    });
    
    // 计算聊天窗口位置
    const chatPosition = computed(() => {
      // 根据图片位置决定聊天窗口展开方向
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      const imageWidth = 60; // 图片宽度（缩小处理）
      const imageHeight = 60; // 图片高度（缩小处理）
      const chatWidth = 350; // 聊天窗口宽度
      const chatHeight = 500; // 聊天窗口高度
      
      // 检查是否在屏幕底部，需要向上展开
      const isNearBottom = position.value.y > windowHeight - chatHeight - 50;
      
      // 如果图片在屏幕右半部分，聊天窗口向左展开
      if (position.value.x > windowWidth / 2) {
        return {
          // 如果靠近底部，向上展开
          top: isNearBottom ? `${position.value.y - chatHeight + imageHeight}px` : `${position.value.y}px`,
          right: `${windowWidth - position.value.x}px`,
          left: 'auto'
        };
      } 
      // 否则向右展开
      else {
        return {
          // 如果靠近底部，向上展开
          top: isNearBottom ? `${position.value.y - chatHeight + imageHeight}px` : `${position.value.y}px`,
          left: `${position.value.x + imageWidth}px`,
          right: 'auto'
        };
      }
    });
    
    // 处理点击事件 - 简化逻辑
    function handleClick(e: MouseEvent) {
      console.log('点击事件触发，当前isDragging:', isDragging.value, '当前isClick:', isClick.value);
      
      // 如果正在拖动中，则不处理点击
      if (isDragging.value) {
        console.log('正在拖动中，忽略点击');
        return;
      }
      
      // 如果是拖动操作，则不处理点击
      if (!isClick.value) {
        console.log('检测到拖动操作，忽略点击');
        // 重置点击状态，但不执行点击操作
        setTimeout(() => {
          isClick.value = true;
        }, 10);
        return;
      }
      
      console.log('切换聊天窗口状态，当前状态:', isChatOpen.value);
      // 切换聊天窗口状态
      if (isChatOpen.value) {
        closeChat();
      } else {
        openChat();
      }
      
      // 防止事件冒泡
      e.stopPropagation();
    }
    
    // 开始拖动 - 仅处理拖动逻辑
    function startDrag(e: MouseEvent) {
      // 只有鼠标左键才能拖动
      if (e.button !== 0) return;
      
      console.log('开始拖动');
      
      // 阻止事件冒泡，防止触发点击事件
      e.stopPropagation();
      
      // 初始化为点击状态
      isClick.value = true;
      
      // 记录开始拖动的时间和位置
      dragStartTime.value = Date.now();
      dragStartPosition.value = { x: e.clientX, y: e.clientY };
      
      // 设置拖动状态
      isDragging.value = true;
      
      // 计算鼠标与图片的相对位置
      if (floatingImage.value) {
        const rect = floatingImage.value.getBoundingClientRect();
        dragOffset.value = {
          x: e.clientX - rect.left,
          y: e.clientY - rect.top
        };
      }
      
      // 记录聊天窗口状态，但不关闭窗口
      wasChatOpenBeforeDrag.value = isChatOpen.value;
      
      // 添加全局鼠标事件监听，解决鼠标移动过快的问题
      document.addEventListener('mousemove', onDragGlobal);
      document.addEventListener('mouseup', stopDragGlobal);
    }
    
    // 全局拖动处理函数，解决鼠标移动过快的问题
    const onDragGlobal = (e: MouseEvent) => {
      if (!isDragging.value) return;
      
      // 计算移动距离
      const moveDistance = Math.sqrt(
        Math.pow(e.clientX - dragStartPosition.value.x, 2) +
        Math.pow(e.clientY - dragStartPosition.value.y, 2)
      );
      
      // 如果移动距离超过阈值，标记为拖动而非点击
      if (moveDistance > 2) { // 降低阈值，提高灵敏度
        isClick.value = false;
        console.log('检测到移动，设置为拖动模式，移动距离:', moveDistance.toFixed(2));
      }
      
      // 更新位置
      position.value = {
        x: e.clientX - dragOffset.value.x,
        y: e.clientY - dragOffset.value.y
      };
      
      // 防止图片被拖出窗口
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      const imageWidth = 60; // 图片宽度（缩小处理）
      const imageHeight = 60; // 图片高度（缩小处理）
      
      if (position.value.x < 0) position.value.x = 0;
      if (position.value.y < 0) position.value.y = 0;
      if (position.value.x > windowWidth - imageWidth) position.value.x = windowWidth - imageWidth;
      if (position.value.y > windowHeight - imageHeight) position.value.y = windowHeight - imageHeight;
      
      // 阻止事件冒泡，防止触发点击事件
      e.preventDefault();
      e.stopPropagation();
    };
    
    // 全局停止拖动函数
    const stopDragGlobal = (e: MouseEvent) => {
      if (!isDragging.value) return;
      
      console.log('停止拖动，isClick状态:', isClick.value);
      
      // 移除全局事件监听
      document.removeEventListener('mousemove', onDragGlobal);
      document.removeEventListener('mouseup', stopDragGlobal);
      
      // 重置拖动状态
      isDragging.value = false;
      
      // 阻止事件冒泡和默认行为
      e.preventDefault();
      e.stopPropagation();
      
      // 如果是拖动操作，延迟重置点击状态
      if (!isClick.value) {
        setTimeout(() => {
          isClick.value = true;
          console.log('重置点击状态完成');
        }, 100);
      }
    };
    
    function openChat() {
      // 如果已经打开，则不做任何操作
      if (isChatOpen.value) return;
      
      isChatOpen.value = true;
      
      // 清空输入框内容
      userInput.value = '';
      
      // 使用setTimeout确保DOM已更新
      setTimeout(() => {
        const inputElement = document.getElementById('user-input');
        if (inputElement) {
          inputElement.focus();
        }
      }, 300);
    }
    
    function closeChat() {
      // 只改变对话框显示状态，不中断连接
      isChatOpen.value = false;
      
      // 不再调用 controller.value.abort()
    }
    
    function sendMessage() {
      const message = userInput.value.trim();
      if (!message) return;
      
      // 添加用户消息
      addMessage(message, 'user');
      userInput.value = '';
      
      // 取消之前的请求（如果有）
      if (controller.value) {
        controller.value.abort();
      }
      
      // 创建新的AbortController
      controller.value = new AbortController();
      
      // 发送请求到后端
      fetchStreamResponse(message);
    }
    
    function addMessage(text: string, sender: 'user' | 'ai') {
      messages.value.push({ text, sender });
      nextTick(() => {
        scrollToBottom();
      });
    }
    
    function scrollToBottom() {
      if (chatMessages.value) {
        chatMessages.value.scrollTop = chatMessages.value.scrollHeight;
      }
    }
    
    function formatMessage(text: string) {
      // 简单的文本格式化，将换行符转换为<br>
      return text.replace(/\n/g, '<br>');
    }
    
    function generateChatId() {
      return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    }
    
    function fetchStreamResponse(prompt: string) {
      // 显示AI正在输入的提示
      const loadingMsgIndex = messages.value.length;
      addMessage('正在思考...', 'ai');
      
      const url = `http://localhost:8080/ai/chat?prompt=${encodeURIComponent(prompt)}&chatId=${encodeURIComponent(chatId.value)}`;
      
      fetch(url, {
        method: 'GET',
        signal: controller.value?.signal
      })
      .then(response => {
        const reader = response.body?.getReader();
        if (!reader) {
          throw new Error('无法获取响应流');
        }
        
        const decoder = new TextDecoder('utf-8');
        let buffer = '';
        
        // 移除加载消息
        messages.value.splice(loadingMsgIndex, 1);
        
        // 创建新的AI消息
        addMessage('', 'ai');
        const aiMessageIndex = messages.value.length - 1;
        
        const processStream = ({ done, value }: ReadableStreamReadResult<Uint8Array>): Promise<void> => {
          if (done) {
            return Promise.resolve();
          }
          
          // 解码接收到的数据
          buffer += decoder.decode(value, { stream: true });
          
          // 更新AI消息内容
          messages.value[aiMessageIndex].text = buffer;
          
          // 滚动到底部
          nextTick(() => {
            scrollToBottom();
          });
          
          // 继续读取流
          return reader.read().then(processStream);
        };
        
        return reader.read().then(processStream);
      })
      .catch(error => {
        if (error.name !== 'AbortError') {
          console.error('Error fetching stream:', error);
          addMessage('抱歉，发生了错误，请稍后再试。', 'ai');
        }
      });
    }
    
    // 生命周期钩子
    onMounted(() => {
      // 设置初始位置为屏幕右上角
      const windowWidth = window.innerWidth;
      position.value = {
        x: windowWidth - 80,
        y: 80
      };
      
      // 监听窗口大小变化
      window.addEventListener('resize', handleResize);
    });
    
    onBeforeUnmount(() => {
      // 清理资源
      if (controller.value) {
        controller.value.abort();
      }
      window.removeEventListener('resize', handleResize);
      
      // 移除可能存在的全局事件监听
      document.removeEventListener('mousemove', onDragGlobal);
      document.removeEventListener('mouseup', stopDragGlobal);
    });
    
    function handleResize() {
      // 确保图片不会超出窗口边界
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      const imageWidth = 60; // 图片宽度（缩小处理）
      const imageHeight = 60; // 图片高度（缩小处理）
      
      if (position.value.x > windowWidth - imageWidth) {
        position.value.x = windowWidth - imageWidth;
      }
      
      if (position.value.y > windowHeight - imageHeight) {
        position.value.y = windowHeight - imageHeight;
      }
    }
    
    return {
      isChatOpen,
      position,
      userInput,
      messages,
      imagePosition,
      chatPosition,
      chatMessages,
      floatingImage,
      userInputRef,
      aiCharacterImage,
      openChat,
      closeChat,
      startDrag,
      handleClick,
      sendMessage,
      formatMessage
    };
  }
});
</script>

<style scoped>
.floating-image {
  position: fixed;
  z-index: 9999; /* 确保在最上层 */
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  width: 60px; /* 图片宽度（缩小处理） */
  height: 60px; /* 图片高度（缩小处理） */
  user-select: none; /* 防止拖动时选中文本 */
  touch-action: none; /* 防止触摸设备上的默认行为 */
}

.floating-image:active {
  transform: scale(1.05);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
}

.floating-image img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  object-fit: cover;
  pointer-events: none; /* 防止图片干扰拖动 */
}

.chat-container {
  position: fixed;
  width: 350px;
  height: 500px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  z-index: 9998; /* 确保聊天窗口也在高层级，但低于图标 */
  overflow: hidden;
  transform-origin: left center;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.chat-container.slide-in {
  animation: slideIn 0.3s forwards;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f5f5;
  color: #333;
  border-bottom: 1px solid #eaeaea;
}

.chat-header h3 {
  margin: 0;
  font-weight: 500;
}

.close-btn {
  background: none;
  border: none;
  color: #666;
  font-size: 20px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #333;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background-color: #fafafa;
}

.message {
  margin-bottom: 10px;
  padding: 8px 12px;
  border-radius: 12px;
  max-width: 80%;
  word-wrap: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.user-message {
  background-color: #f0f2f5;
  margin-left: auto;
  border-bottom-right-radius: 5px;
}

.ai-message {
  background-color: #ffffff;
  margin-right: auto;
  border-bottom-left-radius: 5px;
  border: 1px solid #eaeaea;
}

.chat-input-container {
  display: flex;
  padding: 10px;
  border-top: 1px solid #eaeaea;
  background-color: #f5f5f5;
}

textarea {
  flex: 1;
  border: 1px solid #e0e0e0;
  border-radius: 18px;
  padding: 8px 12px;
  resize: none;
  height: 40px;
  outline: none;
  background-color: white;
  color: #333;
  pointer-events: auto;
  user-select: text;
  -webkit-user-select: text;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

textarea:focus {
  border-color: #ccc;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.send-btn {
  background-color: #e0e0e0;
  color: #555;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  margin-left: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.send-btn:hover {
  background-color: #d0d0d0;
}

/* 适配暗色模式 */
:deep(.dark) .chat-container {
  background-color: #2a2a2a;
  color: white;
}

:deep(.dark) .chat-header {
  background-color: #333;
  color: #eee;
  border-bottom: 1px solid #444;
}

:deep(.dark) .close-btn {
  color: #aaa;
}

:deep(.dark) .close-btn:hover {
  color: #eee;
}

:deep(.dark) .chat-messages {
  background-color: #222;
}

:deep(.dark) .message.user-message {
  background-color: #3a3a3a;
}

:deep(.dark) .message.ai-message {
  background-color: #2d2d2d;
  border: 1px solid #444;
}

:deep(.dark) .chat-input-container {
  background-color: #333;
  border-top: 1px solid #444;
}

:deep(.dark) textarea {
  background-color: #3a3a3a;
  color: white;
  border-color: #555;
}

:deep(.dark) textarea:focus {
  border-color: #666;
}

:deep(.dark) .send-btn {
  background-color: #555;
  color: #eee;
}

:deep(.dark) .send-btn:hover {
  background-color: #666;
}
</style>