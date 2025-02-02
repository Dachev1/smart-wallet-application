package app.web;

import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
public class HomeController {

    private final UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/home")
    public ModelAndView getHomePage(HttpSession session) {

        User user = userService.getById((UUID) session.getAttribute("user_id"));

        ModelAndView mav = new ModelAndView("home");
        mav.addObject("user", user);

        return mav;
    }
}
