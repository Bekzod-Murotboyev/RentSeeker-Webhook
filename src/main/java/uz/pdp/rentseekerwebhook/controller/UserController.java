package uz.pdp.rentseekerwebhook.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uz.pdp.rentseekerwebhook.service.UserService;

import static uz.pdp.rentseekerwebhook.util.Url.*;

@Controller
@RequestMapping(BASE_USER)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(USER_ACTIVE)
    public String getActiveUsers(Model model){
        model.addAttribute("response",userService.getUsers(true));
        return "user";
    }


    @GetMapping(USER_INACTIVE)
    public String getInactiveUsers(Model model){
        model.addAttribute("response",userService.getUsers(false));
        return "user";
    }
}
