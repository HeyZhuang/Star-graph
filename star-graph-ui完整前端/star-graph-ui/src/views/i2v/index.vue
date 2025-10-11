<template>
  <div class="i2v">
    <div class="main-container">
      <div class="input-section">
        <h2>图生视频</h2>
        
        <div class="image-upload">
          <el-upload
            ref="uploadRef"
            :on-change="handleImageChange"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/*"
            drag
          >
            <div v-if="!previewImage" class="upload-placeholder">
              <el-icon :size="60"><Picture /></el-icon>
              <div class="upload-text">
                <p>将图片拖到此处，或<span>点击上传</span></p>
                <p class="upload-tip">支持 JPG/PNG 格式，建议尺寸 512×512 或更大</p>
              </div>
            </div>
            <div v-else class="preview-container">
              <img :src="previewImage" alt="预览图片" />
              <el-button 
                type="danger" 
                size="small" 
                circle 
                class="remove-btn"
                @click.stop="removeImage"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </el-upload>
        </div>

        <div class="prompt-input">
          <label>动作描述（可选）：</label>
          <el-input
            v-model="form.propmt"
            type="textarea"
            :rows="2"
            placeholder="描述你想要的动作效果，如：缓慢放大，向右平移，旋转镜头等"
          />
        </div>

        <div class="settings-grid">
          <div class="setting-item">
            <label>视频时长（秒）：</label>
            <el-radio-group v-model="form.duration">
              <el-radio :label="2">2秒</el-radio>
              <el-radio :label="4">4秒</el-radio>
              <el-radio :label="6">6秒</el-radio>
            </el-radio-group>
          </div>

          <div class="setting-item">
            <label>运动强度：</label>
            <el-slider 
              v-model="form.motionBucketId" 
              :min="1" 
              :max="255"
              :marks="{1: '静态', 127: '中等', 255: '强烈'}"
            />
          </div>

          <div class="setting-item">
            <label>帧率（FPS）：</label>
            <el-select v-model="form.fps">
              <el-option :label="4" :value="4" />
              <el-option :label="8" :value="8" />
              <el-option :label="12" :value="12" />
              <el-option :label="16" :value="16" />
            </el-select>
          </div>

          <div class="setting-item">
            <label>生成模型：</label>
            <el-select v-model="form.model">
              <el-option label="SVD-XT (推荐)" value="svd" />
              <el-option label="SVD Image Decoder" value="svd_image_decoder" />
              <el-option label="I2VGen-XL" value="i2vgen_xl" />
            </el-select>
          </div>

          <div class="setting-item">
            <label>循环播放：</label>
            <el-switch v-model="form.videoLoop" />
          </div>

          <div class="setting-item">
            <label>增强等级：</label>
            <el-slider v-model="form.augmentationLevel" :min="0" :max="1" :step="0.1" />
          </div>
        </div>

        <div class="action-buttons">
          <el-button 
            type="primary" 
            size="large" 
            @click="generateVideo"
            :disabled="!imageBase64"
          >
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
          <el-icon :size="60"><Film /></el-icon>
          <p>暂无生成结果</p>
        </div>
        <div v-else class="video-list">
          <div v-for="(video, index) in resultVideos" :key="index" class="video-card">
            <div class="video-player">
              <video 
                :src="video.url" 
                controls 
                loop
                :autoplay="index === 0"
                muted
              />
            </div>
            <div class="video-meta">
              <div class="meta-info">
                <span>{{ video.duration }}秒</span>
                <span>{{ video.fps }}fps</span>
                <span>{{ video.width }}×{{ video.height }}</span>
              </div>
              <div class="video-actions">
                <el-button text @click="downloadVideo(video)">
                  <el-icon><Download /></el-icon>
                  下载
                </el-button>
                <el-button text @click="shareVideo(video)">
                  <el-icon><Share /></el-icon>
                  分享
                </el-button>
              </div>
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
import { Picture, VideoPlay, Close, Film, Download, Share } from "@element-plus/icons-vue";
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Loading from "@/components/Loading.vue";
import Image2VideoAPI from "@/api/i2v";

const loading = ref<Loading>();
const uploadRef = ref();
const previewImage = ref("");
const imageBase64 = ref("");
const resultVideos = ref<any[]>([]);

const form = ref({
  propmt: "",
  duration: 4,
  fps: 8,
  motionBucketId: 127,
  augmentationLevel: 0.0,
  model: "svd",
  videoLoop: false
});

const queueIndex = ref<Number>();
const currentQueueIndex = ref<Number>();
const clientId = ref<String>();
clientId.value = new Date().getTime() + Math.floor(Math.random() * 10000);

const calculateCost = computed(() => {
  return 8 + (form.value.duration * 4);
});

function handleImageChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件！');
    return;
  }
  
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB！');
    return;
  }
  
  const reader = new FileReader();
  reader.onload = (e) => {
    previewImage.value = e.target?.result as string;
    imageBase64.value = (e.target?.result as string).split(',')[1];
  };
  reader.readAsDataURL(file);
}

function removeImage() {
  previewImage.value = "";
  imageBase64.value = "";
  uploadRef.value?.clearFiles();
}

function generateVideo() {
  if (!imageBase64.value) {
    ElMessage.warning('请先上传一张图片');
    return;
  }

  const data = {
    ...form.value,
    imageBase64: imageBase64.value,
    clientId: clientId.value
  };

  loading.value.openLoading();
  Image2VideoAPI.generate(data).then(res => {
    queueIndex.value = res.queueIndex;
    ElMessage.success('视频生成任务已提交');
  }).catch(err => {
    loading.value.closeLoading();
    ElMessage.error(err.message || '生成失败');
  });
}

function downloadVideo(video: any) {
  const link = document.createElement('a');
  link.href = video.url;
  link.download = `video_${Date.now()}.mp4`;
  link.click();
}

function shareVideo(video: any) {
  // 实现分享功能
  ElMessage.info('分享功能开发中...');
}

// WebSocket消息处理
function parseMessage(mes: string) {
  const receivedMessage = JSON.parse(mes);
  
  if(receivedMessage.type === 'videoResult'){
    const video = {
      url: receivedMessage.url,
      duration: receivedMessage.duration || form.value.duration,
      fps: receivedMessage.fps || form.value.fps,
      width: receivedMessage.width || 512,
      height: receivedMessage.height || 512,
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
.i2v {
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

      .image-upload {
        margin-bottom: 20px;

        .upload-placeholder {
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          height: 300px;
          border: 2px dashed #dcdfe6;
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            border-color: #409eff;
          }

          .upload-text {
            text-align: center;
            margin-top: 20px;

            p {
              margin: 5px 0;
              color: #606266;

              span {
                color: #409eff;
              }
            }

            .upload-tip {
              font-size: 12px;
              color: #909399;
            }
          }
        }

        .preview-container {
          position: relative;
          display: inline-block;
          width: 100%;

          img {
            width: 100%;
            max-height: 400px;
            object-fit: contain;
            border-radius: 6px;
            border: 1px solid #dcdfe6;
          }

          .remove-btn {
            position: absolute;
            top: 10px;
            right: 10px;
          }
        }
      }

      .prompt-input {
        margin-bottom: 20px;

        label {
          display: block;
          margin-bottom: 8px;
          color: #606266;
          font-size: 14px;
        }
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

      .video-list {
        .video-card {
          margin-bottom: 20px;
          border: 1px solid #e4e7ed;
          border-radius: 6px;
          overflow: hidden;

          .video-player {
            background: #000;

            video {
              width: 100%;
              height: auto;
            }
          }

          .video-meta {
            padding: 15px;
            background: white;

            .meta-info {
              display: flex;
              gap: 15px;
              margin-bottom: 10px;
              color: #606266;
              font-size: 14px;

              span {
                position: relative;

                &:not(:last-child)::after {
                  content: '·';
                  position: absolute;
                  right: -8px;
                  color: #c0c4cc;
                }
              }
            }

            .video-actions {
              display: flex;
              gap: 10px;
            }
          }
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .i2v .main-container {
    grid-template-columns: 1fr;
  }
}
</style>