package maria.incyberspace.myweatherbot.Bot;

import maria.incyberspace.myweatherbot.Services.MessageConductorService;
import maria.incyberspace.myweatherbot.Services.SubscriptionService;
import maria.incyberspace.myweatherbot.Storage.DatabaseStorage;
import maria.incyberspace.myweatherbot.Storage.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class WeatherBot extends TelegramLongPollingBot {
    public Map<Long, Double[]> userIDsAndLocations = new HashMap<>();
    private final MessageConductorService conductor;
    private final UserStorage userStorage;
    private final SubscriptionService subscriptionService;

    private static final Logger log = LoggerFactory.getLogger(WeatherBot.class);

    public WeatherBot() {
        userStorage = new DatabaseStorage();
        subscriptionService = new SubscriptionService(this, userStorage);
        conductor = new MessageConductorService(this, subscriptionService);

        log.info("Subscription service running");

        subscriptionService.runFromSeed(); // Make sure those in the database receive their updates upon the start of the program
    }

    @Override
    public String getBotUsername() {




        return "beckyLewis_bot";
    }

    @Override
    public String getBotToken() {





        return "2098654035:AAER_bNQvoQCKrq9fTULaAGE0HHYGmMbBME";
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("update received");

        if (update.getMessage().hasLocation()) { // Save location right away if the user sends it remove when user's subscribed
            userIDsAndLocations.put(update.getMessage().getFrom().getId(),
                    new Double[] {
                            update.getMessage().getLocation().getLatitude(),
                            update.getMessage().getLocation().getLongitude()
            });
        }

        new Thread(() -> { // updates handled in a separate thread // once the update is handled, the thread terminates
            try {
                conductor.handleUpdate(update);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }).start();

    }
}

