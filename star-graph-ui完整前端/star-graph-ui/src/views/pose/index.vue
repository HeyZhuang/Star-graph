<template>
  <div class="pose">
    <div class="main-container">
      <div class="pose-input">
        <h2>姿势控制生成</h2>
        <div class="upload-area">
          <el-upload
            ref="uploadRef"
            :on-change="handleImageChange"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/*"
            drag
          >
            <div v-if="!poseImage" class="upload-placeholder">
              <el-icon :size="60"><User /></el-icon>
              <div class="upload-text">上传姿势参考图</div>
              <div class="upload-tip">拖拽或点击上传人物姿势图片</div>
            </div>
            <div v-else class="pose-preview">
              <img :src="poseImage" alt="姿势参考" />
              <el-button type="danger" size="small" @click="clearImage" circle>
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </el-upload>
        </div>

        <div class="control-panel">
          <div class="control-item">
            <label>控制类型：</label>
            <el-select v-model="form.poseType">
              <el-option label="OpenPose (姿势骨架)" value="openpose" />
              <el-option label="Depth (深度图)" value="depth" />
              <el-option label="Canny (边缘检测)" value="canny" />
              <el-option label="Normal (法线图)" value="normal" />
            </el-select>
          </div>

          <div class="control-item">
            <label>控制强度：</label>
            <el-slider 
              v-model="form.controlStrength" 
              :min="0" 
              :max="1" 
              :step="0.05" 
              :show-tooltip="true"
            />
          </div>

          <div class="control-item">
            <label>描述词：</label>
            <el-input
              v-model="form.propmt"
              type="textarea"
              :rows="3"
              placeholder="描述你想生成的图像内容，如：一位穿着红色连衣裙的女孩"
            />
          </div>

          <div class="control-item">
            <label>负向词：</label>
            <el-input
              v-model="form.reverse"
              type="textarea"
              :rows="2"
              placeholder="不想出现的内容"
            />
          </div>
        </div>
      </div>

      <div class="settings-panel">
        <h3>生成设置</h3>
        <input-option ref="config"></input-option>
        <el-button 
          type="primary" 
          size="large" 
          @click="generateFromPose"
          :disabled="!imageBase64 || !form.propmt"
        >
          开始生成
        </el-button>
      </div>

      <div class="result-panel">
        <h3>生成结果</h3>
        <result-image-view ref="viewer" :images="resultImages"></result-image-view>
      </div>
    </div>
    
    <loading ref="loading" :currentQueueIndex="currentQueueIndex" :queueIndex="queueIndex" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import PoseAPI from "@/api/pose";
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Loading from "@/components/Loading.vue";
import ResultImageView from "@/components/ResultImageView.vue";
import InputOption from "@/components/InputOption.vue";
import { ElMessage } from "element-plus";
import { User, Close } from "@element-plus/icons-vue";

const loading = ref<Loading>();
const viewer = ref<ResultImageView>();
const config = ref<InputOption>();
const uploadRef = ref();
const poseImage = ref("");
const imageBase64 = ref("");
const resultImages = ref([]);

const form = ref({
  propmt: "",
  reverse: "",
  poseType: "openpose",
  controlStrength: 1.0
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
  
  const reader = new FileReader();
  reader.onload = (e) => {
    poseImage.value = e.target?.result as string;
    imageBase64.value = (e.target?.result as string).split(',')[1];
  };
  reader.readAsDataURL(file);
}

function clearImage() {
  poseImage.value = "";
  imageBase64.value = "";
  uploadRef.value.clearFiles();
}

function generateFromPose() {
  if (!imageBase64.value) {
    ElMessage.warning('请先上传姿势参考图');
    return;
  }
  
  if (!form.value.propmt) {
    ElMessage.warning('请输入图像描述');
    return;
  }
  
  let configData = config.value.getFormData();
  const data = {
    ...configData,
    imageBase64: imageBase64.value,
    propmt: form.value.propmt,
    reverse: form.value.reverse,
    poseType: form.value.poseType,
    controlStrength: form.value.controlStrength,
    clientId: clientId.value
  };
  
  loading.value.openLoading();
  PoseAPI.generate(data).then(res => {
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
    webSocketFactory: () => new SockJS(import.meta.env.VITE_WS_HOST_URL),
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
.pose {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
  
  .main-container {
    display: grid;
    grid-template-columns: 1fr 350px;
    grid-template-rows: auto 1fr;
    gap: 20px;
    max-width: 1400px;
    margin: 0 auto;
    
    .pose-input {
      background: white;
      border-radius: 8px;
      padding: 30px;
      
      h2 {
        margin-bottom: 20px;
        color: #303133;
      }
      
      .upload-area {
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
            margin-top: 10px;
            color: #303133;
            font-size: 16px;
          }
          
          .upload-tip {
            margin-top: 5px;
            color: #909399;
            font-size: 12px;
          }
        }
        
        .pose-preview {
          position: relative;
          display: inline-block;
          
          img {
            max-width: 100%;
            max-height: 300px;
            border-radius: 6px;
            border: 1px solid #dcdfe6;
          }
          
          .el-button {
            position: absolute;
            top: 10px;
            right: 10px;
          }
        }
      }
      
      .control-panel {
        .control-item {
          margin-bottom: 20px;
          
          label {
            display: block;
            margin-bottom: 8px;
            color: #606266;
            font-weight: 500;
          }
          
          .el-select {
            width: 100%;
          }
        }
      }
    }
    
    .settings-panel {
      background: white;
      border-radius: 8px;
      padding: 20px;
      height: fit-content;
      
      h3 {
        margin-bottom: 20px;
        color: #303133;
      }
      
      .el-button {
        width: 100%;
        margin-top: 20px;
      }
    }
    
    .result-panel {
      grid-column: 1 / -1;
      background: white;
      border-radius: 8px;
      padding: 30px;
      
      h3 {
        margin-bottom: 20px;
        color: #303133;
      }
    }
  }
}
</style>