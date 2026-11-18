package com.matchlog.be;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BeApplication {

    // matchDate 등 LocalDateTime은 시간대 없는 한국 시간으로 저장되므로, 서버 OS 시간대와 무관하게 now()를 KST로 고정한다.
    // main이 아닌 static 블록에 두어 @SpringBootTest처럼 클래스만 로딩되는 경우에도 적용되게 한다.
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(BeApplication.class, args);
    }
}
