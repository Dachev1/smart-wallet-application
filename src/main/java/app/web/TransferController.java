package app.web;

import app.security.AuthenticationDetails;
import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.TransferRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
    public ModelAndView getTransferForm(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        ModelAndView mav = new ModelAndView("transfer");
        mav.addObject("transferRequest", new TransferRequest());
        mav.addObject("user", userService.getById(authenticationDetails.getUserId()));

        return mav;
    }

    @PostMapping
    public ModelAndView processTransfer(@Valid TransferRequest transferRequest, @AuthenticationPrincipal AuthenticationDetails authenticationDetails, BindingResult bindingResult) {

        User userSender = userService.getById(authenticationDetails.getUserId());

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("transfer");
            mav.addObject("transferRequest", transferRequest);
            mav.addObject("user", userSender);
            return mav;
        }

        Transaction transaction = walletService.transferFunds(transferRequest, userSender);

        return new ModelAndView("redirect:/transactions/" + transaction.getId());
    }
}
