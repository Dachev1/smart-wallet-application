package app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public String getLoginPage() {

        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {

        return "register";
    }
}
