package de.codecentric.psd.worblehat.codequality;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeQualityApplication {

    public static void main(String[] args) {
        new CodeQualityApplication().run(args);
    }

    @SuppressWarnings("squid:S2095")
    public void run(String[] args) {
        SpringApplication.run(CodeQualityApplication.class, args);
    }
}
