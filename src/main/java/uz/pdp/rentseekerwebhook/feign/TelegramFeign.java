package uz.pdp.rentseekerwebhook.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import uz.pdp.rentseekerwebhook.payload.HandleSendPhoto;
import uz.pdp.rentseekerwebhook.payload.Result;
import uz.pdp.rentseekerwebhook.payload.ResultDelete;

import static uz.pdp.rentseekerwebhook.util.Method.*;

import static uz.pdp.rentseekerwebhook.util.Url.*;

@FeignClient(url = FULL_REQUEST, name = "TelegramFeign")
public interface TelegramFeign {

    @PostMapping(SEND_MESSAGE)
    Result sendMessage(@RequestBody SendMessage sendMessage);

    @PostMapping(SEND_PHOTO)
    Result sendPhoto(@RequestBody HandleSendPhoto sendPhoto);

    @PostMapping(EDIT_MESSAGE_TEXT)
    Result editMessageText(@RequestBody EditMessageText editMessageText);

    @PostMapping(DELETE_MESSAGE)
    ResultDelete deleteMessage(@RequestBody DeleteMessage deleteMessage);

    @PostMapping(EDIT_MESSAGE_CAPTION)
    Result editMessageCaption(@RequestBody EditMessageCaption editMessageCaption);
}
