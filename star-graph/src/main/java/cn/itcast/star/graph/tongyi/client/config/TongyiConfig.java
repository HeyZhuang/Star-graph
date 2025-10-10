package cn.itcast.star.graph.tongyi.client.config;

import cn.itcast.star.graph.tongyi.client.api.TongyiApi;
import com.squareup.okhttp3.OkHttpClient;
import com.squareup.okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * 通义万相 API 配置
 */
@Configuration
public class TongyiConfig {

    @Value("${tongyi.base-url:https://dashscope.aliyuncs.com/api/v1/}")
    private String baseUrl;

    @Bean
    public TongyiApi tongyiApi() {
        // 配置日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 构建 OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();

        // 构建 Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return retrofit.create(TongyiApi.class);
    }
}
