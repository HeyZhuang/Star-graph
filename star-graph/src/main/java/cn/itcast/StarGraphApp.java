package cn.itcast;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 开启调度-定时器
@MapperScan("cn.itcast.star.graph.core.mapper")
public class StarGraphApp {

    public static void main(String[] args) {
        SpringApplication.run(StarGraphApp.class,args);
    }

}
