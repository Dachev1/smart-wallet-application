package app.web;

import app.security.RequireAdminRole;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
    @RequireAdminRole
    public ModelAndView getAllUsers(){
        List<User> users = userService.getAllUsers();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("users");
        modelAndView.addObject("users", users);

        return modelAndView;
    }
    @GetMapping("/{id}/profile")
    public ModelAndView getProfileMenu(@PathVariable UUID id){


        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("profile-menu");

        modelAndView.addObject("user", user);

        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));

        return modelAndView;
    }
    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid UserEditRequest userEditRequest, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            User user = userService.getById(id);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile-menu");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);
            return modelAndView;
        }

        userService.editUserDetails(id, userEditRequest);
        return new ModelAndView("redirect:/home");
    }
    @PutMapping("/{id}/status") //end point: PUT /users/{id}/id
    public String switchUserStatus(@PathVariable UUID id){
        userService.switchStatus(id);

        return "redirect:/users";
    }
    @PutMapping("/{id}/role") //end point: PUT /users/{id}/id
    public String switchUserRole(@PathVariable UUID id){

        userService.switchRole(id);

        return "redirect:/users";
    }
}
