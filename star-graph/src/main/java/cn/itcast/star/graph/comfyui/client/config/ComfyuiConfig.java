package cn.itcast.star.graph.comfyui.client.config;

import cn.itcast.star.graph.comfyui.client.api.ComfyuiApi;
import cn.itcast.star.graph.comfyui.client.handler.ComfyuiMessageHandler;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiModel;
import cn.itcast.star.graph.core.common.Constants;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;

@Configuration
public class ComfyuiConfig {

    @Bean
    public ComfyuiApi comfyuiApi(){
        // 设置请求的地址
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Retrofit的日志功能是通过底层OkHttp框架来提供的
        OkHttpClient client  = new OkHttpClient.Builder()
                .addInterceptor(logInterceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(Duration.ofSeconds(30))// 连接超时时间
                .readTimeout(Duration.ofSeconds(30))// 读超时时间
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.100.129:8188")
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())//告诉Retrofit框架使用Jackson工具包来处理数据
                .build();
        ComfyuiApi comfyuiApi = retrofit.create(ComfyuiApi.class);// 让Retrofit框架帮我们实现接口
        return comfyuiApi;
    }

    /**
     * 声明一个ws客服端并启动生效
     * @param comfyuiMessageHandler
     * @return
     */
    @Bean
    public WebSocketConnectionManager webSocketConnectionManager(ComfyuiMessageHandler comfyuiMessageHandler){
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        String url = "ws://192.168.100.129:8188/ws?clientId="+ Constants.COMFYUI_CLIENT_ID;
        WebSocketConnectionManager webSocketConnectionManager =  new WebSocketConnectionManager(webSocketClient,comfyuiMessageHandler,url);
        webSocketConnectionManager.start();

        return webSocketConnectionManager;
    }

}
