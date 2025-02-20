package app.web;

import app.subscription.service.SubscriptionService;
import app.transaction.service.TransactionService;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ReportController {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final SubscriptionService subscriptionService;

    public ReportController(UserService userService, WalletService walletService,
                            TransactionService transactionService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getReportsPage() {
        ModelAndView mav = new ModelAndView("reports");

        addUserReportData(mav);
        addWalletReportData(mav);
        addTransactionReportData(mav);
        addSubscriptionReportData(mav);

        return mav;
    }

    private void addUserReportData(ModelAndView mav) {
        mav.addObject("totalUsers", userService.getTotalUsers());
        mav.addObject("activeUsers", userService.getActiveUsers());
        mav.addObject("inactiveUsers", userService.getInactiveUsers());
        mav.addObject("admins", userService.getAdmins());
        mav.addObject("nonAdmins", userService.getNonAdmins());
    }

    private void addWalletReportData(ModelAndView mav) {
        mav.addObject("totalWallets", walletService.getTotalWallets());
        mav.addObject("totalWalletAmount", walletService.getTotalWalletAmount());
        mav.addObject("walletDistribution", walletService.getWalletDistribution());
    }

    private void addTransactionReportData(ModelAndView mav) {
        mav.addObject("totalTransactions", transactionService.getTotalTransactions());
        mav.addObject("totalTransactionAmount", transactionService.getTotalTransactionAmount());
        mav.addObject("totalWithdrawals", transactionService.getTotalWithdrawals());
        mav.addObject("totalDeposits", transactionService.getTotalDeposits());
        mav.addObject("successfulTransactions", transactionService.getSuccessfulTransactions());
        mav.addObject("failedTransactions", transactionService.getFailedTransactions());
    }

    private void addSubscriptionReportData(ModelAndView mav) {
        mav.addObject("subscriptionSummary", subscriptionService.getSubscriptionSummary());
        mav.addObject("totalSubscriptions", subscriptionService.getTotalSubscriptions());
    }
}
