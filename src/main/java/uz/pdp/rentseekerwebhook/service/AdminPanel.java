package uz.pdp.rentseekerwebhook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.rentseekerwebhook.feign.TelegramFeign;
import uz.pdp.rentseekerwebhook.payload.HandleSendPhoto;
import uz.pdp.rentseekerwebhook.payload.LanStateDTO;
import uz.pdp.rentseekerwebhook.util.enums.BotState;
import uz.pdp.rentseekerwebhook.util.enums.Language;
import uz.pdp.rentseekerwebhook.util.enums.Role;
import uz.pdp.rentseekerwebhook.util.security.BaseData;

import java.util.List;

import static uz.pdp.rentseekerwebhook.util.constant.Constant.*;
import static uz.pdp.rentseekerwebhook.util.enums.BotState.*;
import static uz.pdp.rentseekerwebhook.util.enums.BotState.ERROR;


@Service
@RequiredArgsConstructor
public class AdminPanel {

    private final BotService botService;

    private final UserService userService;

    private final AdminBotService adminbotService;

    private final TelegramFeign feign;

    public void onUpdateReceived(Update update) {
        LanStateDTO data = userService.getAndCheck(update);
        BotState state = data.getState();
        Language lan = data.getLanguage();
        Role role = data.getRole();
        boolean isAdmin = data.isAdmin();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals(BaseData.TOKEN)) {
                    state = ADMIN_MAIN_MENU_SEND;
                } else if (state == ADMIN_USER_ENTER_PHONE_NUMBER) {
                    if (userService.phoneNumberValidation(text)) {
                        if (userService.checkByPhoneNumber(text)) {
                            state = ADMIN_SEARCH_USER_INFO_SEND;
                        } else {
                            state = ERROR;
                        }
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {

            switch (update.getCallbackQuery().getData()) {
                case ADMIN, BACK_TO_ADMIN_MAIN_MENU -> state = ADMIN_MAIN_MENU_EDIT;
                case ADMIN_SHOW_USERS, BACK_TO_ADMIN_USERS_SHOW -> state = ADMIN_USERS_SHOW;
                case BACK_TO_ADMIN_CHOOSE_USER_TYPE -> state = ADMIN_CHOOSE_USER_TYPE_SEND;
                case ADMIN_EXCEL_FILE -> state = ADMIN_CHOOSE_USER_TYPE_EDIT;
                case ADMIN_ACTIVE_USERS -> state = ADMIN_GET_ACTIVE_EXCEL_FILE;//
                case ADMIN_DEACTIVATED_USERS -> state = ADMIN_GET_DEACTIVATED_EXCEL_FILE;//
                case SEARCH -> state = ADMIN_USER_ENTER_PHONE_NUMBER;
                case BACK_TO_ADMIN_HOMES_FILTER_SEND -> state = ADMIN_HOMES_FILTER_SEND;
                case BACK_TO_ADMIN_HOMES_FILTER_EDIT,ADMIN_SHOW_HOMES -> state = ADMIN_HOMES_FILTER_EDIT;
                case ADMIN_HOMES_IN_WEEK -> state = ADMIN_WEEK_HOMES_INFO;
                case ADMIN_HOMES_IN_DAY -> state = ADMIN_DAY_HOMES_INFO;
                case BACK_TO_ADMIN_MENU -> state = ADMIN_MENU_EDIT;
            }
            if (!update.getCallbackQuery().getData().equals(BACK_TO_ADMIN_USERS_SHOW) &&
                    state == ADMIN_SEARCH_USER_INFO_SEND) {
                state = ADMIN_SEARCH_USER_INFO_EDIT;
            }
        }
        switch (state) {
            case ADMIN_MENU_EDIT -> {
                execute(botService.getAdminMenuEdit(update, lan));
                isAdmin = false;
            }
            case ADMIN_MAIN_MENU_SEND -> execute(adminbotService.setAdminMenuSend(update, lan));
            case ADMIN_MAIN_MENU_EDIT -> execute(adminbotService.setAdminMenuEdit(update, lan));
            case ADMIN_USERS_SHOW -> execute(adminbotService.setAdminShowUsersEdit(update, lan));
            case ADMIN_CHOOSE_USER_TYPE_SEND -> execute(adminbotService.setAdminChooseUsersSend(update, lan));
            case ADMIN_CHOOSE_USER_TYPE_EDIT -> execute(adminbotService.setAdminChooseUsersEdit(update, lan));
            case ADMIN_USER_ENTER_PHONE_NUMBER -> execute(adminbotService.setAdminUserEnterPhoneEdit(update, lan));
            case ADMIN_HOMES_FILTER_SEND -> execute(adminbotService.setAdminHomeFilterSend(update, lan));
            case ADMIN_HOMES_FILTER_EDIT -> execute(adminbotService.setAdminHomeFilterEdit(update, lan));
            case ADMIN_SEARCH_USER_INFO_SEND -> execute(adminbotService.sendAdminUserInfo(update, lan));
            case ADMIN_SEARCH_USER_INFO_EDIT -> execute(adminbotService.editAdminUserInfo(update, lan));
            case ADMIN_DAY_HOMES_INFO -> {
                showHomes(update, lan, ADMIN_HOMES_IN_DAY);
                state = ADMIN_HOMES_INFO;
            }
            case ADMIN_WEEK_HOMES_INFO -> {
                showHomes(update, lan, ADMIN_HOMES_IN_WEEK);
                state = ADMIN_HOMES_INFO;
            }
            case ADMIN_HOMES_INFO -> {
                execute(adminbotService.sendAdminDeleteHome(update, lan));

            }
            default -> execute(botService.setError(update));
        }
        userService.saveStateAndLan(update, new LanStateDTO(lan, state, role, isAdmin));
    }

    private void showHomes(Update update, Language lan, String searchType){
        List<HandleSendPhoto> sendPhotos = adminbotService.showHomes(update, lan, searchType);
        if (sendPhotos == null) {
            execute(botService.deleteMessage(update));
            execute(botService.homeNotFound(update, lan, BACK_TO_ADMIN_HOMES_FILTER_EDIT));
            return;
        }
        for (HandleSendPhoto sendPhoto : sendPhotos)
            execute(sendPhoto);

    }


    public void execute(SendMessage sendMessage) {
        try {
            feign.sendMessage(sendMessage);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void execute(EditMessageText editMessageText) {
        try {
            feign.editMessageText(editMessageText);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void execute(HandleSendPhoto sendPhoto) {
        try {
            feign.sendPhoto(sendPhoto);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void execute(DeleteMessage deleteMessage) {
        try {
            feign.deleteMessage(deleteMessage);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
