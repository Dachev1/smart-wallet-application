package app.web;

import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public String getSubscriptionsPage() {
        return "upgrade";
    }

    @GetMapping("/history")
    public ModelAndView getHistoryPage(HttpSession session) {


        List<Subscription> allSubscriptionsByUser = subscriptionService.getAllSubscriptionsByUserId((UUID) session.getAttribute("user_id"));

        ModelAndView mav = new ModelAndView("subscription-history");

        mav.addObject("subscriptions", allSubscriptionsByUser);

        return mav;
    }
}
