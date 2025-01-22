package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
public class HomeController {
    // THIS IS PAGE SHOW ONLY WHEN USER IS LOGGED

    private final UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/home")
    public ModelAndView getHomePage() {

        // After I add session I will be removed this
        // To get the user last transaction for <p> in the html
        User user = userService.getUserById(UUID.fromString("5363c7f5-19aa-4f06-9fe6-28498790e7eb"));

        ModelAndView mav = new ModelAndView("home");
        mav.addObject("user", user);

        return mav;
    }
}
