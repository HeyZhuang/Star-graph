<template>
  <div class="t2i">
    <div class="input">
      <result-image-view ref="viewer" :images="resultImages"></result-image-view>
      <div class="user-input">
        <input class="iinput" v-model="form.propmt" type="text" placeholder="请输入生图文案"/>
        <div class="but-blue" @click="sendPropmt">发 送</div>
      </div>
    </div>
    <div class="setting">
      <input-option ref="config" ></input-option>
    </div>
    <loading ref="loading" :currentQueueIndex="currentQueueIndex" :queueIndex="queueIndex" :canelGen="canelGen" :proprityTask="proprityTask" />
  </div>
</template>

<script setup lang="ts">
import ResultImageView from "@/components/ResultImageView.vue";
import InputOption from "@/components/InputOption.vue";
import {onMounted, ref} from "vue";
import Text2ImageAPI from "@/api/t2i";
import { Client } from '@stomp/stompjs';
import Loading from "@/components/Loading.vue";
import {ElMessage, ElMessageBox} from "element-plus";
const loading = ref<Loading>();
const viewer = ref<ResultImageView>();

const form= ref({
  propmt:""
});
const config = ref<InputOption>();
const pid = ref<String>();
const queueIndex = ref<Number>();//任务序号
const currentQueueIndex = ref<Number>();//当前正在 执行的任务序号
const clientId = ref<String>();
clientId.value = new Date().getTime()+Math.floor(Math.random() * 10000);
const resultImages = ref([]);
function sendPropmt() {
  let data = config.value.getFormData();
  data.propmt = form.value.propmt;
  data.clientId = clientId.value;
  loading.value.openLoading();
  Text2ImageAPI.propmt(data).then(res => {
    pid.value = res.pid
    queueIndex.value = res.queueIndex
  }).catch(err=>{
    loading.value.closeLoading();
  })
}

function listImages() {
  let data = {
    pageNum: 1,
    pageSize: 5
  }
  Text2ImageAPI.listImages(data).then(res => {
    res = res.data
    for (let i = 0; i < res.length; i++) {
      resultImages.value.push(res[i].url)
    }
  }).catch(err=>{
  })
}

function canelGen() {
  ElMessageBox.confirm("是否确认要取消生成",{
    title: '取消生成',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(()=>{
    let data = {
      tempId: pid.value,
      index: queueIndex.value
    }
    Text2ImageAPI.canelGen(data).then(res => {
      ElMessage.success("取消成功")
      loading.value.closeLoading();
    }).catch(err=>{
    })
  })
}

function proprityTask() {
  console.log('原有名次：',queueIndex.value)
  ElMessageBox.confirm("是否确认要花费5积分插队10个名次？",{
    title: '插队生成',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(()=>{
    let data = {
      tempId: pid.value,
      index: queueIndex.value
    }
    Text2ImageAPI.proprityTask(data).then(res => {
      queueIndex.value = parseInt(res);
      console.log('插队名次：',queueIndex.value)
      ElMessage.success("插队成功")
    }).catch(err=>{
    })
  })
}

// 消息内容
function parseMessage(mes){
console.log('收到消息：',mes)
  const receivedMessage = JSON.parse(mes);
  if(receivedMessage.type == 'imageResult'){
    let temps = receivedMessage.urls
    for (let i = 0; i < temps.length; i++) {
      resultImages.value.unshift(temps[i])
    }
    viewer.value.onSelected(0);//选择中第1张
    if(resultImages.value.length > 20){
      resultImages.value.splice(20,resultImages.value.length-20)
    }
    loading.value.closeLoading();
  }else if("execution_error"==receivedMessage.type){
    ElMessage.error(receivedMessage.exception_message || "系统出错");
  }else if("progress"==receivedMessage.type){
    loading.value.updateProgress(receivedMessage.value*100/receivedMessage.max);
  }else if("index"==receivedMessage.type){
    currentQueueIndex.value=receivedMessage.value;
  }else if("start"==receivedMessage.type){
    loading.value.startTask();
  }
}
onMounted(()=>{
  const client = new Client({
    brokerURL: import.meta.env.VITE_WS_HOST_URL,
    connectHeaders: {
      clientId: clientId.value
    },
    reconnectDelay: 5000,
    onConnect: () => {
      console.log(111,"ok")
      client.subscribe('/topic/messages', message =>
          parseMessage(message.body)
      );
      client.subscribe('/user/'+clientId.value+'/topic/messages', message =>
          parseMessage(message.body)
      );
    },
  });
  client.activate();

  listImages();
})


</script>

<style scoped>
.t2i {
  margin-top: 31px;
  display: flex;
  .input {
    flex-grow: 6;
    margin-top: 15px;
    .user-input{
      width: 100%;
      display: flex;
      margin: 30px 0px;
      justify-content: center;
      .iinput {
        width: 60%;
        border-radius: 5px;
        padding: 3px 10px;
      }
      .iinput:focus{
        outline-color: #f5f5f5;
      }
    }
  }
}
</style>