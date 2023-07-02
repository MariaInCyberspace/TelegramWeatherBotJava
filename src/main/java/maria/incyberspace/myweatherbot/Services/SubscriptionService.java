package maria.incyberspace.myweatherbot.Services;

import maria.incyberspace.myweatherbot.Models.WeatherUser;
import maria.incyberspace.myweatherbot.Storage.DatabaseStorage;
import maria.incyberspace.myweatherbot.Storage.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SubscriptionService {
    private final TelegramLongPollingBot bot;
    private final UserStorage userStorage;
    // Log info to the console for testing
    private final static Logger log = LoggerFactory.getLogger(DatabaseStorage.class);
    private Set<Long> currentThreads = new HashSet<>();

    public SubscriptionService(TelegramLongPollingBot bot, UserStorage userStorage) {
        this.bot = bot;
        this.userStorage = userStorage;
    }

    public void placeIntoDatabase(WeatherUser user) {
        log.info("Placing user in the database {}. Location: {}, {}", user.getUserId(), user.getLatitude(), user.getLongitude());
        userStorage.placeIntoDatabase(user);
        run(user.getUserId()); // Start running timer task right away
    }

    public void removeFromDatabase(Long userId) {
        userStorage.removeFromTheDatabase(userId);
        log.info("Removing user from the database {}", userId);
        currentThreads.remove(userId);
    }

    // Start when there's another user placed in the database
    public void run(Long userId) {
        try {
            sendMessage(userId);
            log.info("Thread: {}, user: {}", Thread.currentThread().getName(), userId);
        } catch (TelegramApiException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void runFromSeed() {
        try {
            if (userStorage.getUsers().isEmpty()) {
                log.info("Storage is empty. I'll return");
                return; // Nobody in storage? Then, stop this run call
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            userStorage.getUsers()
                    .keySet()
                    .forEach(user -> {
                        try {
                            currentThreads.add(user);
                            sendMessage(user);
                            log.info("Thread: {}, user: {}", Thread.currentThread().getName(), user);
                        } catch (TelegramApiException | SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void sendMessage(Long userId) throws TelegramApiException, SQLException, ClassNotFoundException {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (userStorage.getUsers().get(userId) != null) {
                        String name = userStorage.getUsers().get(userId).getFirstName();
                        Double[] location = new Double[] {
                                userStorage.getUsers().get(userId).getLatitude(),
                                userStorage.getUsers().get(userId).getLongitude() };
                        String weatherDetails = LocationParsingService.parseLocation(location, name);
                        SendMessage message = SendMessage.builder()
                                .chatId(userId.toString())
                                .text(weatherDetails)
                                .build();
                        try {
                            log.info("Thread: {}, user: {}", Thread.currentThread().getName(), userId);
                            bot.executeAsync(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            log.info("Task killed for {} ", userId);
                            Thread.currentThread().join(); // Kill this task
                            currentThreads.remove(userId);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (SQLException | ClassNotFoundException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        };

        // Daily updates
        // To customise the time of receiving updates
        // Date date = userStorage.getUsers().get(userId).getDate();
        // timer.schedule(task, date, ChronoUnit.DAYS.getDuration().toMillis());

        // Updates by the minute for testing
        // timer.schedule(task, date, ChronoUnit.MINUTES.getDuration().toMillis());

        // Updates every couple of seconds. For testing purposes
        timer.schedule(task, 3000, 7000);
        currentThreads.add(userId);
        log.info(currentThreads.toString() + " Currently running threads");
    }
}
