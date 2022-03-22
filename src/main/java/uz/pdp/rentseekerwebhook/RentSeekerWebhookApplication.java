package uz.pdp.rentseekerwebhook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import uz.pdp.rentseekerwebhook.payload.AuthDTO;

import static uz.pdp.rentseekerwebhook.util.Method.SET_WEBHOOK;
import static uz.pdp.rentseekerwebhook.util.Url.*;

@SpringBootApplication
@EnableFeignClients
public class RentSeekerWebhookApplication {


    public static void main(String[] args) {
        SpringApplication.run(RentSeekerWebhookApplication.class, args);
        System.out.println(restTemplate().postForObject(FULL_REQUEST + SET_WEBHOOK, new SetWebhook(GLOBAL+BASE_WEBHOOK), AuthDTO.class));
    }


    @Bean
    static public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
