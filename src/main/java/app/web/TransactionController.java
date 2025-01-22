package app.web;

import app.transaction.model.Transaction;
import app.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ModelAndView getTransactionsPage() {

        // After I add session I will be removed this
        List<Transaction> transactions = transactionService.getUserTransactionsById(UUID.fromString("bd403b83-b9b3-45f1-a4d5-0cee85234c0f"));

        ModelAndView mav = new ModelAndView("transactions");
        mav.addObject("transactions", transactions);

        return mav;
    }
}
