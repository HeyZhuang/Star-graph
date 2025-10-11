<template>
  <div class="t2v">
    <div class="main-container">
      <div class="input-section">
        <h2>文生视频</h2>
        
        <div class="prompt-area">
          <el-input
            v-model="form.propmt"
            type="textarea"
            :rows="4"
            placeholder="描述你想生成的视频内容，例如：一只猫在花园里追蝴蝶，阳光明媚的下午"
          />
        </div>

        <div class="settings-grid">
          <div class="setting-item">
            <label>视频时长（秒）：</label>
            <el-slider v-model="form.duration" :min="2" :max="10" :marks="durationMarks" />
          </div>

          <div class="setting-item">
            <label>视频尺寸：</label>
            <el-radio-group v-model="form.size">
              <el-radio :label="1">512×512</el-radio>
              <el-radio :label="2">576×320</el-radio>
              <el-radio :label="3">768×432</el-radio>
            </el-radio-group>
          </div>

          <div class="setting-item">
            <label>生成模型：</label>
            <el-select v-model="form.model">
              <el-option label="SVD (稳定视频扩散)" value="svd" />
              <el-option label="ZeroScope (快速生成)" value="zeroscope" />
              <el-option label="AnimateDiff (动画风格)" value="animatediff" />
            </el-select>
          </div>

          <div class="setting-item">
            <label>运动强度：</label>
            <el-slider v-model="form.motionBucketId" :min="1" :max="255" />
          </div>

          <div class="setting-item">
            <label>帧率（FPS）：</label>
            <el-select v-model="form.fps">
              <el-option :label="4" :value="4" />
              <el-option :label="8" :value="8" />
              <el-option :label="12" :value="12" />
              <el-option :label="16" :value="16" />
              <el-option :label="24" :value="24" />
            </el-select>
          </div>

          <div class="setting-item">
            <label>生成步数：</label>
            <el-input-number v-model="form.steps" :min="10" :max="50" />
          </div>
        </div>

        <div class="negative-prompt">
          <label>负面提示词（可选）：</label>
          <el-input
            v-model="form.negativePrompt"
            type="textarea"
            :rows="2"
            placeholder="不想在视频中出现的内容"
          />
        </div>

        <div class="action-buttons">
          <el-button type="primary" size="large" @click="generateVideo" :disabled="!form.propmt">
            <el-icon><VideoPlay /></el-icon>
            生成视频
          </el-button>
          <div class="cost-info">
            预计消耗：{{ calculateCost }}积分
          </div>
        </div>
      </div>

      <div class="result-section">
        <h3>生成结果</h3>
        <div v-if="resultVideos.length === 0" class="empty-result">
          <el-icon :size="60"><VideoCamera /></el-icon>
          <p>暂无生成结果</p>
        </div>
        <div v-else class="video-grid">
          <div v-for="(video, index) in resultVideos" :key="index" class="video-item">
            <video 
              :src="video.url" 
              controls 
              autoplay 
              loop
              muted
              @click="previewVideo(video)"
            />
            <div class="video-info">
              <span>{{ video.duration }}秒 · {{ video.fps }}fps</span>
              <el-button text @click="downloadVideo(video)">
                <el-icon><Download /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <loading ref="loading" :currentQueueIndex="currentQueueIndex" :queueIndex="queueIndex" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { VideoPlay, VideoCamera, Download } from "@element-plus/icons-vue";
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Loading from "@/components/Loading.vue";
import Text2VideoAPI from "@/api/t2v";

const loading = ref<Loading>();
const resultVideos = ref<any[]>([]);

const form = ref({
  propmt: "",
  negativePrompt: "",
  duration: 4,
  size: 1,
  model: "svd",
  motionBucketId: 127,
  fps: 8,
  steps: 25
});

const durationMarks = {
  2: '2秒',
  4: '4秒',
  6: '6秒',
  8: '8秒',
  10: '10秒'
};

const queueIndex = ref<Number>();
const currentQueueIndex = ref<Number>();
const clientId = ref<String>();
clientId.value = new Date().getTime() + Math.floor(Math.random() * 10000);

const calculateCost = computed(() => {
  return 10 + (form.value.duration * 5);
});

function getSizeParams() {
  switch(form.value.size) {
    case 2:
      return { width: 576, height: 320 };
    case 3:
      return { width: 768, height: 432 };
    default:
      return { width: 512, height: 512 };
  }
}

function generateVideo() {
  if (!form.value.propmt) {
    ElMessage.warning('请输入视频描述');
    return;
  }

  const sizeParams = getSizeParams();
  const data = {
    ...form.value,
    ...sizeParams,
    clientId: clientId.value
  };

  loading.value.openLoading();
  Text2VideoAPI.generate(data).then(res => {
    queueIndex.value = res.queueIndex;
    ElMessage.success('视频生成任务已提交');
  }).catch(err => {
    loading.value.closeLoading();
    ElMessage.error(err.message || '生成失败');
  });
}

function previewVideo(video: any) {
  // 实现视频预览功能
  console.log('Preview video:', video);
}

function downloadVideo(video: any) {
  const link = document.createElement('a');
  link.href = video.url;
  link.download = `video_${Date.now()}.mp4`;
  link.click();
}

// WebSocket消息处理
function parseMessage(mes: string) {
  const receivedMessage = JSON.parse(mes);
  
  if(receivedMessage.type === 'videoResult'){
    const video = {
      url: receivedMessage.url,
      duration: receivedMessage.duration || form.value.duration,
      fps: receivedMessage.fps || form.value.fps,
      thumbnail: receivedMessage.thumbnail
    };
    resultVideos.value.unshift(video);
    loading.value.closeLoading();
    ElMessage.success('视频生成完成！');
  } else if(receivedMessage.type === "execution_error"){
    ElMessage.error(receivedMessage.exception_message || "生成出错");
    loading.value.closeLoading();
  } else if(receivedMessage.type === "progress"){
    loading.value.updateProgress(receivedMessage.value * 100 / receivedMessage.max);
  } else if(receivedMessage.type === "index"){
    currentQueueIndex.value = receivedMessage.value;
  } else if(receivedMessage.type === "start"){
    loading.value.startTask();
  }
}

onMounted(() => {
  const client = new Client({
    webSocketFactory: () => new SockJS(import.meta.env.VITE_WS_HOST_URL),
    connectHeaders: {
      clientId: clientId.value
    },
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe('/topic/messages', message => {
        parseMessage(message.body);
      });
      client.subscribe('/user/' + clientId.value + '/topic/messages', message => {
        parseMessage(message.body);
      });
    },
    onStompError: (frame) => {
      console.error('STOMP error:', frame);
    }
  });
  client.activate();
});
</script>

<style scoped lang="scss">
.t2v {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;

  .main-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    max-width: 1400px;
    margin: 0 auto;

    .input-section {
      background: white;
      border-radius: 8px;
      padding: 30px;

      h2 {
        margin-bottom: 20px;
        color: #303133;
      }

      .prompt-area {
        margin-bottom: 20px;
      }

      .settings-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
        margin-bottom: 20px;

        .setting-item {
          label {
            display: block;
            margin-bottom: 8px;
            color: #606266;
            font-size: 14px;
          }
        }
      }

      .negative-prompt {
        margin-bottom: 20px;

        label {
          display: block;
          margin-bottom: 8px;
          color: #606266;
          font-size: 14px;
        }
      }

      .action-buttons {
        display: flex;
        align-items: center;
        gap: 20px;

        .cost-info {
          color: #909399;
          font-size: 14px;
        }
      }
    }

    .result-section {
      background: white;
      border-radius: 8px;
      padding: 30px;

      h3 {
        margin-bottom: 20px;
        color: #303133;
      }

      .empty-result {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 60px;
        color: #909399;

        p {
          margin-top: 10px;
        }
      }

      .video-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: 20px;

        .video-item {
          background: #f5f7fa;
          border-radius: 6px;
          overflow: hidden;

          video {
            width: 100%;
            height: auto;
            cursor: pointer;
          }

          .video-info {
            padding: 10px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 14px;
            color: #606266;
          }
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .t2v .main-container {
    grid-template-columns: 1fr;
  }
}
</style>