package cn.itcast;

import cn.itcast.star.graph.ollama.client.api.OllamaApi;
import cn.itcast.star.graph.ollama.client.pojo.OllamaChatRequest;
import cn.itcast.star.graph.ollama.client.pojo.OllamaChatRespone;
import cn.itcast.star.graph.ollama.client.pojo.OllamaMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class OllamaTest {
    @Autowired
    OllamaApi ollamaApi;

    @Test
    public void test() throws IOException {
        OllamaMessage message = new OllamaMessage();
        message.setRole("user");
        message.setContent("帮我把以下内容翻译成英文：今天是星期几？");
        OllamaChatRequest body = new OllamaChatRequest();
        body.setModel("qwen2:0.5b");
        body.setMessages(List.of(message));
        Call<OllamaChatRespone> chat = ollamaApi.chat(body);
        Response<OllamaChatRespone> result = chat.execute();
        OllamaChatRespone ollamaChatRespone = result.body();
        System.out.println(ollamaChatRespone.getMessage().getContent());
    }
}
