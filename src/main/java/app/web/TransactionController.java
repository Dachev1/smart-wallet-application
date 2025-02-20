package app.web;

import app.security.AuthenticationDetails;
import app.transaction.model.Transaction;
import app.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public ModelAndView getTransactionsPage(@AuthenticationPrincipal AuthenticationDetails authenticationDetails) {

        List<Transaction> transactions = transactionService.getUserTransactionsById((UUID) authenticationDetails.getUserId());

        ModelAndView mav = new ModelAndView("transactions");
        mav.addObject("transactions", transactions);

        return mav;
    }

    @GetMapping("/transactions/{id}")
    public ModelAndView getTransactionById(@PathVariable("id") UUID transactionId) {
        Transaction transaction = transactionService.getById(transactionId);

        ModelAndView mav = new ModelAndView("transaction-result");
        mav.addObject("transaction", transaction);

        return mav;
    }

}
