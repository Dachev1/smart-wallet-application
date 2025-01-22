package app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WalletController {

    @GetMapping("/wallets")
    public ModelAndView getWalletsPage() {

        return new ModelAndView("wallets");
    }
}
