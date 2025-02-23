package app.web;

import app.security.AuthenticationDetails;
import app.transaction.model.Transaction;
import app.user.model.User;
import app.wallet.service.WalletService;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    @Autowired
    public WalletController(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getWalletsPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        ModelAndView mav = new ModelAndView("wallets");
        mav.addObject("wallets", walletService.getWalletsByUser(user));
        mav.addObject("recentTransactions", walletService.getRecentTransactionsForUserWallets(user));
        mav.addObject("canAddWallet", walletService.canAddWallet(user));
        return mav;
    }

    @PostMapping
    public String createNewWallet(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        walletService.createNewWallet(user);
        return "redirect:/wallets";
    }

    @PutMapping("/{walletId}/balance/top-up")
    public ModelAndView topUpWallet(@PathVariable("walletId") UUID walletId,
                                    @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        User user = userService.getById(authenticationDetails.getUserId());
        Transaction transaction = walletService.topUp(walletId, BigDecimal.valueOf(20), user);
        ModelAndView mav = new ModelAndView("transaction-result");
        mav.addObject("transaction", transaction);
        return mav;
    }

    @PutMapping("/{walletId}/status")
    public String switchWallet(@PathVariable("walletId") UUID walletId,
                               @AuthenticationPrincipal AuthenticationDetails authenticationDetails) {
        User user = userService.getById(authenticationDetails.getUserId());
        walletService.switchActiveWallet(user, walletId);
        return "redirect:/wallets";
    }
}
