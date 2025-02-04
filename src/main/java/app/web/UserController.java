package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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

    // Get all users
    @GetMapping
    public ModelAndView getUsersPage() {
        List<User> users = userService.getAllUsers();
        ModelAndView mav = new ModelAndView("users");
        mav.addObject("users", users);
        return mav;
    }

    // Get a single user's profile
    @GetMapping("/{id}/profile")
    public ModelAndView getUserProfile(@PathVariable("id") UUID userId) {
        User user = userService.getById(userId);
        UserEditRequest userEditRequest = DtoMapper.mapToUserEditRequest(user);

        ModelAndView mav = new ModelAndView("profile-menu");
        mav.addObject("user", user);
        mav.addObject("userEditRequest", userEditRequest);

        return mav;
    }

    // Update a user's profile
    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable("id") UUID userId, @Valid UserEditRequest userEditRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("profile-menu");
            mav.addObject("user", userService.getById(userId));
            mav.addObject("userEditRequest", userEditRequest);
            return mav;
        }

        userService.editUserDetails(userId, userEditRequest);
        return new ModelAndView("redirect:/home");
    }

    @PutMapping("/{id}/status")
    public ModelAndView updateUserProfile(@PathVariable("id") UUID userId) {

        userService.toggleUserActiveStatus(userId);

        return new ModelAndView("redirect:/users");
    }
}
