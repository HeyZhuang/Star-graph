<template>
  <div class="upscale">
    <div class="main-content">
      <div class="upload-section">
        <h2>画质提升</h2>
        <el-upload
          class="upload-area"
          ref="uploadRef"
          :on-change="handleImageChange"
          :auto-upload="false"
          :show-file-list="false"
          accept="image/*"
          drag
        >
          <div v-if="!originalImage" class="upload-placeholder">
            <el-icon :size="80"><UploadFilled /></el-icon>
            <div class="upload-text">
              <p>将图片拖到此处，或<span>点击上传</span></p>
              <p class="upload-tip">支持 JPG/PNG/GIF 格式</p>
            </div>
          </div>
          <div v-else class="image-comparison">
            <div class="image-box">
              <h3>原图</h3>
              <img :src="originalImage" alt="原图" />
            </div>
          </div>
        </el-upload>
      </div>

      <div class="control-section">
        <div class="control-item">
          <label>放大倍数：</label>
          <el-radio-group v-model="form.upscaleFactor">
            <el-radio :label="2">2倍</el-radio>
            <el-radio :label="4">4倍</el-radio>
          </el-radio-group>
        </div>
        
        <div class="control-item">
          <label>增强模型：</label>
          <el-select v-model="form.upscaleModel">
            <el-option label="RealESRGAN x4plus (通用)" value="RealESRGAN_x4plus" />
            <el-option label="RealESRGAN x4plus Anime (动漫)" value="RealESRGAN_x4plus_anime" />
            <el-option label="ESRGAN x4 (细节增强)" value="ESRGAN_x4" />
          </el-select>
        </div>

        <el-button type="primary" size="large" @click="enhanceImage" :disabled="!imageBase64">
          开始增强
        </el-button>
      </div>

      <div v-if="resultImages.length > 0" class="result-section">
        <h2>增强结果</h2>
        <result-image-view ref="viewer" :images="resultImages"></result-image-view>
      </div>
    </div>
    
    <loading ref="loading" :currentQueueIndex="currentQueueIndex" :queueIndex="queueIndex" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import UpscaleAPI from "@/api/upscale";
import { Client } from '@stomp/stompjs';
import Loading from "@/components/Loading.vue";
import ResultImageView from "@/components/ResultImageView.vue";
import { ElMessage } from "element-plus";
import { UploadFilled } from "@element-plus/icons-vue";

const loading = ref<Loading>();
const viewer = ref<ResultImageView>();
const uploadRef = ref();
const originalImage = ref("");
const imageBase64 = ref("");
const resultImages = ref([]);

const form = ref({
  upscaleFactor: 2,
  upscaleModel: "RealESRGAN_x4plus"
});

const queueIndex = ref<Number>();
const currentQueueIndex = ref<Number>();
const clientId = ref<String>();
clientId.value = new Date().getTime() + Math.floor(Math.random() * 10000);

function handleImageChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件！');
    return;
  }
  
  // 检查文件大小
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB！');
    return;
  }
  
  const reader = new FileReader();
  reader.onload = (e) => {
    originalImage.value = e.target?.result as string;
    imageBase64.value = (e.target?.result as string).split(',')[1];
  };
  reader.readAsDataURL(file);
}

function enhanceImage() {
  if (!imageBase64.value) {
    ElMessage.warning('请先上传一张图片');
    return;
  }
  
  const data = {
    imageBase64: imageBase64.value,
    upscaleFactor: form.value.upscaleFactor,
    upscaleModel: form.value.upscaleModel,
    clientId: clientId.value
  };
  
  loading.value.openLoading();
  UpscaleAPI.enhance(data).then(res => {
    queueIndex.value = res.queueIndex;
  }).catch(err => {
    loading.value.closeLoading();
  });
}

// WebSocket消息处理
function parseMessage(mes){
  const receivedMessage = JSON.parse(mes);
  if(receivedMessage.type == 'imageResult'){
    let temps = receivedMessage.urls;
    for (let i = 0; i < temps.length; i++) {
      resultImages.value.unshift(temps[i]);
    }
    loading.value.closeLoading();
  } else if("execution_error"==receivedMessage.type){
    ElMessage.error(receivedMessage.exception_message || "系统出错");
    loading.value.closeLoading();
  } else if("progress"==receivedMessage.type){
    loading.value.updateProgress(receivedMessage.value*100/receivedMessage.max);
  } else if("index"==receivedMessage.type){
    currentQueueIndex.value=receivedMessage.value;
  } else if("start"==receivedMessage.type){
    loading.value.startTask();
  }
}

onMounted(() => {
  const client = new Client({
    brokerURL: import.meta.env.VITE_WS_HOST_URL,
    connectHeaders: {
      clientId: clientId.value
    },
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe('/topic/messages', message => {
        parseMessage(message.body);
      });
      client.subscribe('/user/'+clientId.value+'/topic/messages', message => {
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
.upscale {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
  
  .main-content {
    max-width: 1200px;
    margin: 0 auto;
    
    .upload-section {
      background: white;
      border-radius: 8px;
      padding: 30px;
      margin-bottom: 20px;
      
      h2 {
        margin-bottom: 20px;
        color: #303133;
      }
      
      .upload-area {
        .upload-placeholder {
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          height: 400px;
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
        
        .image-comparison {
          display: flex;
          gap: 20px;
          
          .image-box {
            flex: 1;
            
            h3 {
              margin-bottom: 10px;
              color: #606266;
            }
            
            img {
              width: 100%;
              max-height: 400px;
              object-fit: contain;
              border-radius: 4px;
              border: 1px solid #dcdfe6;
            }
          }
        }
      }
    }
    
    .control-section {
      background: white;
      border-radius: 8px;
      padding: 30px;
      margin-bottom: 20px;
      
      .control-item {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
        
        label {
          width: 100px;
          color: #606266;
        }
      }
      
      .el-button {
        width: 200px;
        margin-top: 10px;
      }
    }
    
    .result-section {
      background: white;
      border-radius: 8px;
      padding: 30px;
      
      h2 {
        margin-bottom: 20px;
        color: #303133;
      }
    }
  }
}
</style>