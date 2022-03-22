package uz.pdp.rentseekerwebhook.util;


import uz.pdp.rentseekerwebhook.util.security.BaseData;

public interface Url {
    String TOKEN="bot"+ BaseData.TOKEN_TEST +"/";

    String BASE_WEBHOOK="api/telegram";

    String BASE_USER="api/user";
    String USER_ACTIVE="/active";
    String USER_INACTIVE="/inactive";


    String GLOBAL="https://0abc-31-40-27-36.ngrok.io/";

//    String GLOBAL="https://1050-31-40-27-36.ngrok.io/";

    String TELEGRAM_BASE="https://api.telegram.org/";

    String FULL_REQUEST=TELEGRAM_BASE+TOKEN;




}
