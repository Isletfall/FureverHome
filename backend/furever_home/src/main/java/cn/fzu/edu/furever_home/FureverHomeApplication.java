package cn.fzu.edu.furever_home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan({ "cn.fzu.edu.furever_home.auth.mapper", "cn.fzu.edu.furever_home.animal.mapper",
        "cn.fzu.edu.furever_home.post.mapper", "cn.fzu.edu.furever_home.adopt.mapper",
        "cn.fzu.edu.furever_home.review.mapper", "cn.fzu.edu.furever_home.chat.mapper",
        "cn.fzu.edu.furever_home.rating.mapper", "cn.fzu.edu.furever_home.notify.mapper" })
public class FureverHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FureverHomeApplication.class, args);
    }

}
