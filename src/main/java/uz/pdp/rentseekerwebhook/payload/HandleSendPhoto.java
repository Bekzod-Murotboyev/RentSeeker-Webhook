package uz.pdp.rentseekerwebhook.payload;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandleSendPhoto {
    String chat_id;
    InputFile photo;

    String caption;

    InlineKeyboardMarkup reply_markup;

    public String getChatId() {
        return chat_id;
    }

    public void setChatId(String chat_id) {
        this.chat_id = chat_id;
    }

    public InlineKeyboardMarkup getReplyMarkup() {
        return reply_markup;
    }

    public void setReplyMarkup(InlineKeyboardMarkup reply_markup) {
        this.reply_markup = reply_markup;
    }
}
