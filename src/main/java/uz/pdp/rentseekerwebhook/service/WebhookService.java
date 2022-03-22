package uz.pdp.rentseekerwebhook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.LoginUrl;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.rentseekerwebhook.feign.TelegramFeign;
import uz.pdp.rentseekerwebhook.payload.HandleSendPhoto;
import uz.pdp.rentseekerwebhook.payload.LanStateDTO;
import uz.pdp.rentseekerwebhook.util.enums.BotState;
import uz.pdp.rentseekerwebhook.util.enums.Language;
import uz.pdp.rentseekerwebhook.util.enums.Role;

import java.util.ArrayList;
import java.util.List;

import static uz.pdp.rentseekerwebhook.util.constant.Constant.*;
import static uz.pdp.rentseekerwebhook.util.security.BaseData.TOKEN;

@Service
@RequiredArgsConstructor
public class WebhookService {


    private final UserService userService;

    private final BotService botService;

    private final TelegramFeign feign;

    private final AdminPanel adminPanel;


    public void onUpdateToReceived(Update update) {
        if (!update.hasCallbackQuery() && !update.hasMessage())
            return;
        LanStateDTO res = userService.getAndCheck(update);
        BotState state = res.getState();
        Language lan = res.getLanguage();
        Role role = res.getRole();
        boolean isAdmin = res.isAdmin();

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals(START))
            isAdmin = false;
        if (isAdmin) {
            adminPanel.onUpdateReceived(update);
            return;
        }
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals(START)) {
                    if (!state.equals(BotState.CHOOSE_LANGUAGE)) {
                        state = role.equals(Role.ADMIN) ? BotState.ADMIN_MENU_SEND : BotState.MAIN_MENU_SEND;
                        execute(botService.removeKeyBoardMarkup(update));
                    }
                } else if (text.equals(TOKEN))
                    state = BotState.CHECK_USER_BY_ADMIN;
                else if (state.equals(BotState.CHECK_USER_BY_ADMIN) && botService.checkByCode(update)) {
                    state = BotState.ADMIN_MENU_SEND;
                    role = Role.ADMIN;
                } else if (state.equals(BotState.REGISTER))
                    state = BotState.SETTINGS_MENU_SEND;
                else if (state.equals(BotState.LOCATION_MENU))
                    state = BotState.WRITE_OR_SEND_LOCATION;
                else if (state.equals(BotState.GIVE_ADDRESS))
                    state = BotState.GIVE_HOME_TYPE_SEND;
                else if (state.equals(BotState.GIVE_HOME_NUMBER))
                    state = BotState.GIVE_HOME_AREA_SEND;
                else if (state.equals(BotState.GIVE_HOME_AREA_SEND) || state.equals(BotState.GIVE_HOME_AREA_EDIT))
                    state = BotState.GIVE_HOME_PHOTO_SEND;
                else if (state.equals(BotState.GIVE_HOME_PRICE_SEND) || state.equals(BotState.GIVE_HOME_PRICE_EDIT))
                    state = BotState.GIVE_HOME_DESCRIPTION;
                else if (state.equals(BotState.GIVE_HOME_DESCRIPTION))
                    state = BotState.SAVE_HOME_TO_STORE;
                else if (state.equals(BotState.CHOOSE_HOME_NUMBER_EDIT) || state.equals(BotState.CHOOSE_HOME_NUMBER_SEND))
                    state = BotState.CHOOSE_HOME_MIN_PRICE_SEND;
                else if (state.equals(BotState.CHOOSE_HOME_MIN_PRICE_SEND) || state.equals(BotState.CHOOSE_HOME_MIN_PRICE_EDIT))
                    state = BotState.CHOOSE_HOME_MAX_PRICE_SEND;
                else if (state.equals(BotState.CHOOSE_HOME_MAX_PRICE_SEND) || state.equals(BotState.CHOOSE_HOME_MAX_PRICE_EDIT))
                    state = BotState.SHOW_SORTED_OPTIONS;
                else {
                    execute(botService.setError(update));
                    return;
                }
            } else if (message.hasContact() && state.equals(BotState.REGISTER))
                state = BotState.SETTINGS_MENU_SEND;
            else if (message.hasLocation() && state.equals(BotState.LOCATION_MENU))
                state = BotState.GIVE_HOME_TYPE_SEND;
            else if (message.hasPhoto() && (state.equals(BotState.GIVE_HOME_PHOTO_SEND) || state.equals(BotState.GIVE_HOME_PHOTO_EDIT)))
                state = BotState.GIVE_HOME_PRICE_SEND;
            else {
                execute(botService.setError(update));
                return;
            }
        } else if (update.hasCallbackQuery()) {
            switch (update.getCallbackQuery().getData()) {
                case UZ -> {
                    lan = Language.UZ;
                    state = state.equals(BotState.CHOOSE_LANGUAGE) ? BotState.MAIN_MENU_EDIT : BotState.SETTINGS_MENU_EDIT;
                }
                case RU -> {
                    lan = Language.RU;
                    state = state.equals(BotState.CHOOSE_LANGUAGE) ? BotState.MAIN_MENU_EDIT : BotState.SETTINGS_MENU_EDIT;
                }
                case EN -> {
                    lan = Language.EN;
                    state = state.equals(BotState.CHOOSE_LANGUAGE) ? BotState.MAIN_MENU_EDIT : BotState.SETTINGS_MENU_EDIT;
                }
                case ADMIN -> state = BotState.ADMIN_PANEL;
                case SETTINGS, BACK_TO_SETTINGS_MENU_EDIT -> state = BotState.SETTINGS_MENU_EDIT;
                case BACK_TO_ADMIN_MENU -> state = BotState.ADMIN_MENU_EDIT;
                case BACK_TO_SETTINGS_MENU_SEND -> state = BotState.SETTINGS_MENU_SEND;
                case BACK_TO_MAIN_MENU_SEND -> state = BotState.MAIN_MENU_SEND;
                case BACK_TO_MAIN_MENU_EDIT, USER -> state = BotState.MAIN_MENU_EDIT;
                case CHANGE_LANGUAGE -> state = BotState.CHANGE_LANGUAGE;
                case REGISTRATION -> state = BotState.REGISTER;
                case ADD_ACCOMMODATION, BACK_TO_GIVE_HOME_STATUS -> state = BotState.GIVE_HOME_STATUS;
                case BACK_TO_WRITE_SEND_LOCATION, FOR_RENTING, FOR_SELLING -> state = BotState.WRITE_OR_SEND_LOCATION;
                case BACK_SEND_LOCATION, SEND_LOCATION -> state = BotState.LOCATION_MENU;
                case BACK_TO_GIVE_REGION, WRITE_ADDRESS -> state = BotState.GIVE_REGION;
                case BACK_TO_GIVE_DISTRICT -> state = BotState.GIVE_DISTRICT;
                case BACK_TO_GIVE_ADDRESS -> state = BotState.GIVE_ADDRESS;
                case BACK_TO_GIVE_HOME_TYPE -> state = BotState.GIVE_HOME_TYPE_EDIT;
                case BACK_TO_GIVE_HOME_NUMBER -> state = BotState.GIVE_HOME_NUMBER;
                case BACK_TO_GIVE_HOME_AREA -> state = BotState.GIVE_HOME_AREA_EDIT;
                case BACK_TO_GIVE_HOME_PHOTO -> state = BotState.GIVE_HOME_PHOTO_EDIT;
                case BACK_TO_GIVE_HOME_PRICE -> state = BotState.GIVE_HOME_PRICE_EDIT;
                case BACK_TO_SHOW_MENU_EDIT, SHOW_ACCOMMODATIONS -> state = BotState.SHOW_MENU_EDIT;
                case BACK_TO_SHOW_MENU_SEND -> state = BotState.SHOW_MENU_SEND;
                case SHOW_ALL, PREV, NEXT -> state = BotState.SHOW_OPTIONS;
                case BACK_TO_CHOOSE_REGION, SEARCH -> state = BotState.CHOOSE_REGION;
                case BACK_TO_CHOOSE_DISTRICT -> state = BotState.CHOOSE_DISTRICT;
                case BACK_TO_CHOOSE_HOME_STATUS -> state = BotState.CHOOSE_HOME_STATUS;
                case BACK_TO_CHOOSE_HOME_TYPE, GET_RENTING, FOR_BUY -> state = BotState.CHOOSE_HOME_TYPE;
                case BACK_TO_CHOOSE_HOME_NUMBER -> state = BotState.CHOOSE_HOME_NUMBER_EDIT;
                case BACK_TO_CHOOSE_MIN_PRICE -> state = BotState.CHOOSE_HOME_MIN_PRICE_EDIT;
                case BACK_TO_CHOOSE_MAX_PRICE_SEND -> state = BotState.CHOOSE_HOME_MAX_PRICE_SEND;
                case BACK_TO_CHOOSE_MAX_PRICE_EDIT -> state = BotState.CHOOSE_HOME_MAX_PRICE_EDIT;
                case BACK_TO_MY_NOTES_MENU_EDIT, MY_NOTES -> state = BotState.MY_NOTES_MENU_EDIT;
                case BACK_TO_MY_NOTES_MENU_SEND -> state = BotState.MY_NOTES_MENU_SEND;
                case MY_FAVOURITES -> state = BotState.MY_FAVOURITES;
                case MY_ACCOMMODATIONS -> state = BotState.MY_HOMES;
                case SKIP -> state = botService.getStateBySkip(update, state);
                default -> state = botService.getState(update, state);
            }
        }


        switch (state) {
            case CHOOSE_LANGUAGE -> execute(botService.chooseLanguage(update, lan));
            case CHECK_USER_BY_ADMIN -> execute(botService.checkUserMenu(update, lan));
            case ADMIN_MENU_SEND -> {
                execute(botService.getAdminMenuSend(update, lan));
                botService.changeIsAdminUser(update, false);
            }
            case ADMIN_MENU_EDIT -> {
                execute(botService.getAdminMenuEdit(update, lan));
                botService.changeIsAdminUser(update, false);
            }
            case ADMIN_PANEL -> {
                if (role.equals(Role.ADMIN)) {
                    adminPanel.onUpdateReceived(update);
                    isAdmin = true;
                }
            }
            case CHANGE_LANGUAGE -> execute(botService.changeLanguage(update, lan));
            case MAIN_MENU_EDIT -> execute(botService.setMenuEdit(update, lan, role));
            case MAIN_MENU_SEND -> execute(botService.setMenuSend(update, lan, role));
            case SETTINGS_MENU_EDIT -> execute(botService.getSettingMenuEdit(update, lan));
            case SETTINGS_MENU_SEND -> {
                if (botService.saveContact(update, lan)) {
                    execute(botService.removeKeyBoardMarkup(update));
                    execute(botService.getSettingMenuSend(update, lan));
                } else {
                    state = BotState.REGISTER;
                    execute(botService.getRegister(update, lan));
                }
            }
            case REGISTER -> {
                execute(botService.deleteMessage(update));
                execute(botService.getRegister(update, lan));
            }
            case GIVE_HOME_STATUS -> {
                if (botService.checkByPhone(update))
                    execute(botService.setHomeStatusMenu(update, lan));
                else {
                    execute(botService.setWarningRegister(update, lan));
                    execute(botService.setMenuSend(update, lan, role));
                }
            }
            case WRITE_OR_SEND_LOCATION -> {
                if (update.hasCallbackQuery())
                    execute(botService.setWriteOrSendLocationMenuEdit(update, lan));
                else if (update.hasMessage()) {
                    if (update.getMessage().getText().equals(LanguageService.getWord(BACK, lan))) {
                        execute(botService.removeKeyBoardMarkup(update));
                        execute(botService.setWriteOrSendLocationMenuSend(update, lan));
                    } else {
                        execute(botService.getMenuLocation(update, lan));
                        state = BotState.LOCATION_MENU;
                    }

                }
            }
            case LOCATION_MENU -> {
                execute(botService.deleteMessage(update));
                execute(botService.getMenuLocation(update, lan));
            }
            case GIVE_REGION -> execute(botService.giveRegionMenu(update, lan));
            case GIVE_DISTRICT -> execute(botService.giveDistrictMenu(update, lan, state));
            case GIVE_ADDRESS -> execute(botService.giveAddressMenu(update, lan, state));
            case GIVE_HOME_TYPE_SEND -> {
                if (botService.checkLocation(update)) {
                    execute(botService.removeKeyBoardMarkup(update));
                    execute(botService.giveHomeTypeMenuSend(update, lan, state));
                } else {
                    execute(botService.LocationNotFound(update, lan));
                    execute(botService.getMenuLocation(update, lan));
                    state = BotState.LOCATION_MENU;
                }
            }
            case GIVE_HOME_TYPE_EDIT -> execute(botService.giveHomeTypeMenuEdit(update, lan));
            case GIVE_HOME_NUMBER -> execute(botService.giveHomeNumberMenu(update, lan, state));
            case GIVE_HOME_AREA_SEND -> {
                if (botService.saveNumberOfRoom(update, state))
                    execute(botService.giveHomeAreaMenuSend(update, lan));
                else {
                    execute(botService.giveHomeNumberMenuSend(update, lan, state));
                    state = BotState.GIVE_HOME_NUMBER;
                }
            }
            case GIVE_HOME_AREA_EDIT -> execute(botService.giveHomeAreaMenuEdit(update, lan));
            case GIVE_HOME_PHOTO_SEND -> {
                if (botService.saveHomeArea(update, state))
                    execute(botService.giveHomePhotoMenuSend(update, lan));
                else {
                    execute(botService.giveHomeAreaMenuSend(update, lan));
                    state = BotState.GIVE_HOME_AREA_SEND;
                }
            }
            case GIVE_HOME_PHOTO_EDIT -> execute(botService.giveHomePhotoMenuEdit(update, lan));
            case GIVE_HOME_PRICE_SEND -> execute(botService.giveHomePriceMenuSend(update, lan, state));
            case GIVE_HOME_PRICE_EDIT -> execute(botService.giveHomePriceMenuEdit(update, lan));
            case GIVE_HOME_DESCRIPTION -> {
                if (botService.saveHomePrice(update, state))
                    execute(botService.giveHomeDescription(update, lan));
                else {
                    execute(botService.giveHomePriceMenuSend(update, lan, state));
                    state = BotState.GIVE_HOME_PRICE_SEND;
                }
            }
            case SAVE_HOME_TO_STORE -> {
                botService.saveHomeDescription(update, state);
                execute(botService.successfullySaved(update, lan));
                execute(botService.setMenuSend(update, lan, role));
            }
            case SHOW_MENU_SEND -> execute(botService.getShowMenuSend(update, lan));
            case SHOW_MENU_EDIT -> execute(botService.getShowMenuEdit(update, lan));
            case SHOW_OPTIONS -> {
                if (update.getCallbackQuery().getData().equals(SHOW_ALL))
                    execute(botService.deleteMessage(update));
                List<HandleSendPhoto> sendPhotos = botService.showAllHomes(update, lan);
                if (sendPhotos == null) {
                    execute(botService.homeNotFound(update, lan, BACK_TO_SHOW_MENU_EDIT));
                    return;
                }
                for (HandleSendPhoto sendPhoto : sendPhotos)
                    execute(sendPhoto);
            }
            case SHOW_HOME_PHONE_MENU_ALL -> {
                execute(botService.changeVisibleHomePhone(update, lan, BACK_TO_SHOW_MENU_SEND, state));
                state = BotState.SHOW_OPTIONS;
            }
            case SHOW_HOME_PHONE_MENU_FAVOURITES -> {
                execute(botService.changeVisibleHomePhone(update, lan, BACK_TO_MY_NOTES_MENU_SEND, state));
                state = BotState.MY_FAVOURITES;
            }
            case SHOW_HOME_PHONE_MENU_MY_ACCOMMODATIONS -> {
                execute(botService.changeVisibleHomePhone(update, lan, BACK_TO_MY_NOTES_MENU_SEND, state));
                state = BotState.MY_HOMES;
            }
            case SHOW_HOME_PHONE_MENU_SEARCH -> {
                execute(botService.changeVisibleHomePhone(update, lan, BACK_TO_CHOOSE_MAX_PRICE_SEND, state));
                state = BotState.SHOW_SORTED_OPTIONS;
            }
            case CHANGE_HOME_LIKE_MENU_ALL -> {
                execute(botService.changeHomeLike(update, lan, BACK_TO_SHOW_MENU_SEND, state));
                state = BotState.SHOW_OPTIONS;
            }
            case CHANGE_HOME_LIKE_MENU_FAVOURITES -> {
                execute(botService.changeHomeLike(update, lan, BACK_TO_MY_NOTES_MENU_SEND, state));
                state = BotState.MY_FAVOURITES;
            }
            case CHANGE_HOME_LIKE_MENU_MY_ACCOMMODATIONS -> {
                execute(botService.changeHomeLike(update, lan, BACK_TO_MY_NOTES_MENU_SEND, state));
                state = BotState.MY_HOMES;
            }
            case CHANGE_HOME_LIKE_MENU_SEARCH -> {
                execute(botService.changeHomeLike(update, lan, BACK_TO_CHOOSE_MAX_PRICE_SEND, state));
                state = BotState.SHOW_SORTED_OPTIONS;
            }
            case CHOOSE_REGION -> execute(botService.chooseRegionMenu(update, lan));
            case CHOOSE_DISTRICT -> execute(botService.chooseDistrict(update, lan));
            case CHOOSE_HOME_STATUS -> execute(botService.chooseHomeStatus(update, lan));
            case CHOOSE_HOME_TYPE -> execute(botService.chooseHomeType(update, lan));
            case CHOOSE_HOME_NUMBER_EDIT -> execute(botService.chooseHomeNumberEdit(update, lan));
            case CHOOSE_HOME_MIN_PRICE_SEND -> {
                if (botService.saveSearchNumber(update)) {
                    execute(botService.chooseMinPriceMenuSend(update, lan));
                } else {
                    execute(botService.chooseHomeNumberSend(update, lan));
                    state = BotState.CHOOSE_HOME_NUMBER_SEND;
                }
            }
            case CHOOSE_HOME_MIN_PRICE_EDIT -> execute(botService.chooseMinPriceMenuEdit(update, lan));
            case CHOOSE_HOME_MAX_PRICE_SEND -> {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    if (botService.saveSearchMinPrice(update)) {
                        execute(botService.chooseMaxPriceMenuSend(update, lan));
                    } else {
                        execute(botService.chooseMinPriceMenuSend(update, lan));
                        state = BotState.CHOOSE_HOME_MIN_PRICE_SEND;
                    }
                } else
                    execute(botService.chooseMaxPriceMenuSend(update, lan));
            }
            case CHOOSE_HOME_MAX_PRICE_EDIT -> execute(botService.chooseMaxPriceMenuEdit(update, lan));
            case SHOW_SORTED_OPTIONS -> {
                if (botService.saveSearchMaxPrice(update)) {
                    execute(botService.deleteMessage(update));
                    List<HandleSendPhoto> sendPhotos = botService.showSortedOptionsSend(update, lan);
                    if (sendPhotos == null) {
                        execute(botService.homeNotFound(update, lan, BACK_TO_CHOOSE_MAX_PRICE_EDIT));
                        return;
                    }
                    for (HandleSendPhoto sendPhoto : sendPhotos)
                        execute(sendPhoto);
                } else {
                    execute(botService.chooseMaxPriceMenuSend(update, lan));
                    state = BotState.CHOOSE_HOME_MAX_PRICE_SEND;
                }
            }
            case MY_NOTES_MENU_EDIT -> execute(botService.getMyNotesMenuEdit(update, lan));
            case MY_NOTES_MENU_SEND -> execute(botService.getMyNotesMenuSend(update, lan));
            case MY_FAVOURITES -> {
                execute(botService.deleteMessage(update));
                List<HandleSendPhoto> sendPhotos = botService.getMyFavouriteHomes(update, lan);
                if (sendPhotos == null) {
                    execute(botService.homeNotFound(update, lan, BACK_TO_MY_NOTES_MENU_EDIT));
                    return;
                }
                for (HandleSendPhoto sendPhoto : sendPhotos)
                    execute(sendPhoto);
            }
            case MY_HOMES -> {
                execute(botService.deleteMessage(update));
                List<HandleSendPhoto> sendPhotos = botService.getMyHomes(update, lan);
                if (sendPhotos == null) {
                    execute(botService.homeNotFound(update, lan, BACK_TO_MY_NOTES_MENU_EDIT));
                    return;
                }
                for (HandleSendPhoto sendPhoto : sendPhotos)
                    execute(sendPhoto);
            }
            case DELETE_ACCOMMODATION -> execute(botService.deleteAccommodation(update));
            default -> execute(botService.setError(update));
        }
        userService.saveStateAndLan(update, new LanStateDTO(lan, state, role, isAdmin));
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

    public void execute(EditMessageCaption editMessageCaption) {
        try {
            feign.editMessageCaption(editMessageCaption);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
