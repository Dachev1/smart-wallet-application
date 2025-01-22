package app.web;

import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
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
    public ModelAndView getHistoryPage() {

        // Will be fixed soon with session
        List<Subscription> allSubscriptionsByUser = subscriptionService.getAllSubscriptionsByUserId(UUID.fromString("5363c7f5-19aa-4f06-9fe6-28498790e7eb"));

        ModelAndView mav = new ModelAndView("subscription-history");

        mav.addObject("subscriptions", allSubscriptionsByUser);

        return mav;
    }
}
