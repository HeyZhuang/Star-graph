<template>
  <div class="i2i">
    <div class="input">
      <result-image-view ref="viewer" :images="resultImages"></result-image-view>
      <div class="image-upload">
        <el-upload
          class="upload-demo"
          ref="uploadRef"
          :on-change="handleImageChange"
          :auto-upload="false"
          :show-file-list="false"
          accept="image/*"
        >
          <div v-if="!previewImage" class="upload-placeholder">
            <el-icon :size="60"><Plus /></el-icon>
            <div>点击或拖拽上传图片</div>
          </div>
          <img v-else :src="previewImage" class="preview-image" />
        </el-upload>
      </div>
      <div class="user-input">
        <input class="iinput" v-model="form.propmt" type="text" placeholder="请输入图片描述"/>
        <div class="strength-slider">
          <span>强度：</span>
          <el-slider v-model="form.strength" :min="0" :max="1" :step="0.05" :show-tooltip="true"/>
        </div>
        <div class="but-blue" @click="sendImage">生成图片</div>
      </div>
    </div>
    <div class="setting">
      <input-option ref="config"></input-option>
    </div>
    <loading ref="loading" :currentQueueIndex="currentQueueIndex" :queueIndex="queueIndex" :canelGen="canelGen" :proprityTask="proprityTask" />
  </div>
</template>

<script setup lang="ts">
import ResultImageView from "@/components/ResultImageView.vue";
import InputOption from "@/components/InputOption.vue";
import { onMounted, ref } from "vue";
import Image2ImageAPI from "@/api/i2i";
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import Loading from "@/components/Loading.vue";
import { ElMessage, ElMessageBox, ElUpload } from "element-plus";
import { Plus } from "@element-plus/icons-vue";

const loading = ref<Loading>();
const viewer = ref<ResultImageView>();
const uploadRef = ref();
const previewImage = ref("");
const imageBase64 = ref("");

const form = ref({
  propmt: "",
  strength: 0.75
});

const config = ref<InputOption>();
const pid = ref<String>();
const queueIndex = ref<Number>();
const currentQueueIndex = ref<Number>();
const clientId = ref<String>();
clientId.value = new Date().getTime() + Math.floor(Math.random() * 10000);
const resultImages = ref([]);

function handleImageChange(uploadFile: any) {
  const file = uploadFile.raw;
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件！');
    return;
  }
  
  const reader = new FileReader();
  reader.onload = (e) => {
    previewImage.value = e.target?.result as string;
    imageBase64.value = (e.target?.result as string).split(',')[1]; // 去除data:image/xxx;base64,前缀
  };
  reader.readAsDataURL(file);
}

function sendImage() {
  if (!imageBase64.value) {
    ElMessage.warning('请先上传一张图片');
    return;
  }
  
  let data = config.value.getFormData();
  data.propmt = form.value.propmt;
  data.imageBase64 = imageBase64.value;
  data.strength = form.value.strength;
  data.clientId = clientId.value;
  
  loading.value.openLoading();
  Image2ImageAPI.generate(data).then(res => {
    pid.value = res.pid;
    queueIndex.value = res.queueIndex;
  }).catch(err => {
    loading.value.closeLoading();
  });
}

function canelGen(){
  // 实现取消生成的逻辑
}

function proprityTask(){
  // 实现优先任务的逻辑
}

// WebSocket连接逻辑（与t2i相同）
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
.i2i {
  display: flex;
  flex-wrap: wrap;
  flex-direction: row;
  width: 100%;
  height: 100%;
  
  .input {
    flex: 1;
    background: #ffffff;
    padding: 20px;
    
    .image-upload {
      margin-bottom: 20px;
      
      .upload-placeholder {
        width: 300px;
        height: 300px;
        border: 2px dashed #dcdfe6;
        border-radius: 6px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          border-color: #409eff;
        }
      }
      
      .preview-image {
        max-width: 300px;
        max-height: 300px;
        border-radius: 6px;
        cursor: pointer;
      }
    }
    
    .user-input {
      display: flex;
      flex-direction: column;
      gap: 15px;
      
      .iinput {
        height: 45px;
        padding: 10px 15px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        font-size: 14px;
        
        &:focus {
          outline: none;
          border-color: #409eff;
        }
      }
      
      .strength-slider {
        display: flex;
        align-items: center;
        gap: 10px;
        
        span {
          min-width: 50px;
        }
      }
      
      .but-blue {
        height: 45px;
        background: #409eff;
        color: white;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #66b1ff;
        }
      }
    }
  }
  
  .setting {
    width: 300px;
    background: #f5f7fa;
    padding: 20px;
  }
}
</style>