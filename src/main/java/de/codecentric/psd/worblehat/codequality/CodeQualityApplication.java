package de.codecentric.psd.worblehat.codequality;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodeQualityApplication {

    public static void main(String[] args) {
        new CodeQualityApplication().run(args);
    }

    @SuppressWarnings("squid:S2095")
    public void run(String[] args) {
        SpringApplication.run(CodeQualityApplication.class, args);
    }
}
