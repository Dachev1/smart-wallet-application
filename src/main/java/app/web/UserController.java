package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getUsersPage() {

        List<User> users = userService.getAllUsers();

        ModelAndView mav = new ModelAndView("users");
        mav.addObject("users", users);

        return mav;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getUserProfile(@PathVariable("id") UUID userId) {

        ModelAndView mav = new ModelAndView("profile-menu");

        User user = userService.getUserById(UUID.fromString("bd403b83-b9b3-45f1-a4d5-0cee85234c0f"));

        mav.addObject("user", user);

        return mav;
    }

}
