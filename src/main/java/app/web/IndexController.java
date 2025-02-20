package app.web;

import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getIndexPage(Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/home";
        }

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage() {

        ModelAndView mav = new ModelAndView("login");
        mav.addObject("loginRequest", new LoginRequest());

        return mav;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView mav = new ModelAndView("register");
        mav.addObject("registerRequest", new RegisterRequest());

        return mav;
    }

    @PostMapping("/register")
    public ModelAndView doRegister(@Valid @ModelAttribute RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/home");
    }
}
