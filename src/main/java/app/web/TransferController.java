package app.web;

import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.TransferRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/transfers")
public class TransferController {

    private final WalletService walletService;
    private final UserService userService;

    @Autowired
    public TransferController(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getTransferForm(HttpSession session) {

        ModelAndView mav = new ModelAndView("transfer");
        mav.addObject("transferRequest", new TransferRequest());
        mav.addObject("user", userService.getById((UUID) session.getAttribute("user_id")));

        return mav;
    }

    @PostMapping
    public ModelAndView processTransfer(@Valid TransferRequest transferRequest, BindingResult bindingResult, HttpSession session) {

        User userSender = userService.getById((UUID) session.getAttribute("user_id"));

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("transfer");
            mav.addObject("transferRequest", transferRequest);
            return mav;
        }

        Transaction transaction = walletService.transferFunds(transferRequest, userSender);

        return new ModelAndView("redirect:/transactions/" + transaction.getId());
    }

}
