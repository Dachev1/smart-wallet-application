package app.web;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionService;
import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UpgradeRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getUpgradePage(HttpSession session) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        ModelAndView mav = new ModelAndView("upgrade");
        mav.addObject("user", user);
        mav.addObject("upgradeRequest", new UpgradeRequest());

        return mav;
    }

    @PostMapping
    public ModelAndView subscribe(@RequestParam("subscription-type") SubscriptionType subscriptionType, HttpSession session, UpgradeRequest upgradeRequest) {

        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.getById(userId);

        Transaction transaction = subscriptionService.createSubscription(user, subscriptionType, upgradeRequest);

        ModelAndView mav = new ModelAndView("transaction-result");
        mav.addObject("transaction", transaction);

        return mav;
    }

    @GetMapping("/history")
    public ModelAndView getHistoryPage(HttpSession session) {


        List<Subscription> allSubscriptionsByUser = subscriptionService.getAllSubscriptionsByUserId((UUID) session.getAttribute("user_id"));

        ModelAndView mav = new ModelAndView("subscription-history");

        mav.addObject("subscriptions", allSubscriptionsByUser);

        return mav;
    }
}
