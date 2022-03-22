package uz.pdp.rentseekerwebhook.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.rentseekerwebhook.service.WebhookService;

import static uz.pdp.rentseekerwebhook.util.Url.BASE_WEBHOOK;

@RestController
@RequestMapping(BASE_WEBHOOK)
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    public void getRequests(@RequestBody Update update) {
        webhookService.onUpdateToReceived(update);
    }
}
