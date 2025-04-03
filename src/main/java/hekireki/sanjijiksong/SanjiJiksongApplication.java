package hekireki.sanjijiksong;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SanjiJiksongApplication {

    public static void main(String[] args) {
        SpringApplication.run(SanjiJiksongApplication.class, args);
    }

}
