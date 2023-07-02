package maria.incyberspace.myweatherbot.Services;

import maria.incyberspace.myweatherbot.Bot.WeatherBot;
import maria.incyberspace.myweatherbot.Models.CustomCommands;

import maria.incyberspace.myweatherbot.Models.WeatherUser;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageConductorService {
    Update update; // needed in other methods of this class as well
    private final static Logger log = LoggerFactory.getLogger(MessageConductorService.class);
    private final TelegramLongPollingBot bot;
    private final SubscriptionService subscriptionService;
    private final Map<Long, Double[]> userIDsAndLocations;
    private Map<Long, Date> userDates = new HashMap<>();
    private Map<Long, String> previousCommands = new HashMap<>(); // Basically only needed to gather time info

    public MessageConductorService(WeatherBot bot, SubscriptionService subscriptionService) {
        this.bot = bot;
        this.subscriptionService = subscriptionService;
        this.userIDsAndLocations = bot.userIDsAndLocations;
    }

    public void handleUpdate(Update update) throws TelegramApiException {
        this.update = update;
        SendMessage message = new SendMessage();

        // Get location and send current weather based on that location
        if (update.hasMessage() && update.getMessage().hasLocation()) {
            previousCommands.put(update.getMessage().getFrom().getId(), update.getMessage().getText());

            String weatherDetails = LocationParsingService.parseLocation(update.getMessage().getLocation(), update.getMessage().getFrom().getFirstName());
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText(weatherDetails);
            bot.executeAsync(message);
        }

        // Start command handling
        if (Objects.equals(update.getMessage().getText(), CustomCommands.START)) {
            previousCommands.put(update.getMessage().getFrom().getId(), update.getMessage().getText());

            message.setChatId(update.getMessage().getChatId().toString());
            message.setText("To get started, please send your location, " + update.getMessage().getFrom().getFirstName());
            bot.executeAsync(message);
        }

        // Unsubscribe command handling
        if (Objects.equals(update.getMessage().getText(), CustomCommands.UNSUBSCRIBE)) {
            log.info("unsubscribe called");

            previousCommands.put(update.getMessage().getFrom().getId(), update.getMessage().getText());
            // remove this user from the database
            Long userId = update.getMessage().getFrom().getId();
            subscriptionService.removeFromDatabase(userId);
            userDates.remove(update.getMessage().getFrom().getId());
            userIDsAndLocations.remove(update.getMessage().getFrom().getId());
            previousCommands.remove(update.getMessage().getFrom().getId());
            message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .text("You've unsubscribed from getting daily weather reports, " + update.getMessage().getFrom().getFirstName())
                    .build();
            bot.executeAsync(message);
        }

        // Subscribe command handling
        if (Objects.equals(update.getMessage().getText(), CustomCommands.SUBSCRIBE)) {
            previousCommands.put(update.getMessage().getFrom().getId(), update.getMessage().getText());

            assert userIDsAndLocations != null;
            if (!userIDsAndLocations.containsKey(update.getMessage().getFrom().getId())) {
                message = SendMessage.builder()
                        .chatId(update.getMessage().getChatId().toString())
                        .text("To get started, please send your location, " + update.getMessage().getFrom().getFirstName())
                        .build();
                bot.executeAsync(message);
            }

            assert userDates != null;

            // If date was successfully retrieved, create a new user and save them
            if (userDates.containsKey(update.getMessage().getFrom().getId())) {

                Long userId = update.getMessage().getFrom().getId();
                Long chatId = update.getMessage().getChatId();
                String name = update.getMessage().getFrom().getFirstName();
                Double latit = userIDsAndLocations.get(update.getMessage().getFrom().getId())[0];
                Double longit = userIDsAndLocations.get(update.getMessage().getFrom().getId())[1];
                java.util.Date date = userDates.get(userId);
                WeatherUser user = new WeatherUser(userId, chatId, name, latit, longit, date);

                subscriptionService.placeIntoDatabase(user);
                message = SendMessage.builder()
                        .chatId(update.getMessage().getChatId().toString())
                        .text("You've subscribed to get daily weather reports")
                        .build();
                bot.executeAsync(message);
            } else {
                message = SendMessage.builder()
                        .chatId(update.getMessage().getChatId().toString())
                        .text("Please provide the time you wish to receive your daily updates at in this format: \n'00 : 00' / hours : minutes")
                        .build();
                bot.executeAsync(message);
            }

        }

        // Save the time at which the user wishes to receive weather updates
        if (Objects.equals(previousCommands.get(update.getMessage().getFrom().getId()), CustomCommands.SUBSCRIBE)
                && update.getMessage().getText().contains(":")) {
            String[] time = update.getMessage().getText().split(":");

            Date temporaryDateHolder = setDateForUpdates(time);
            assert temporaryDateHolder != null;
            userDates.put(update.getMessage().getFrom().getId(), temporaryDateHolder);
            log.info("Date: " + temporaryDateHolder);
            previousCommands.put(update.getMessage().getFrom().getId(), update.getMessage().getText());

            User user = new User(); // To set the id and firstName for our custom message
            user.setId(update.getMessage().getFrom().getId());
            user.setFirstName(update.getMessage().getFrom().getFirstName());
            Message customMessage = new Message(); // Customise the message
            customMessage.setMessageId(update.getMessage().getMessageId());
            customMessage.setChat(update.getMessage().getChat());
            customMessage.setText(CustomCommands.SUBSCRIBE);
            customMessage.setFrom(user);
            update.setMessage(customMessage); // Alter the message for the update object
            log.info("Setting update");
            // The update was customised to 'land' on the '/subscribe' command handling logic now that we have the time for this user stored
            handleUpdate(update); // Recursive call
        }

    }

    // Set the time for receiving messages
    private static Date setMessageDate(int setHour, int setMinutes) {
         LocalDateTime constructedTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(setHour, setMinutes));

        // Testing: old date
        // LocalDateTime constructedTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(setHour, setMinutes));
        Instant instant = constructedTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    private Date setDateForUpdates(String[] timeElements) throws TelegramApiException {
        int hours = Integer.parseInt(timeElements[0]);
        int minutes = Integer.parseInt(timeElements[1]);
        Date temp = null;
        if ((hours >= 0 && hours <= 23) && (minutes >= 0 && minutes <= 59)) {
            temp = setMessageDate(hours, minutes);
            log.info("Time was acquired: " + hours + " " + minutes);
        } else {
            SendMessage message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .text("Please provide the time you wish to receive your daily updates at in this format: \n'00 : 00' / hours : minutes")
                    .build();
            bot.executeAsync(message);
        }
        return temp;
    }
}
