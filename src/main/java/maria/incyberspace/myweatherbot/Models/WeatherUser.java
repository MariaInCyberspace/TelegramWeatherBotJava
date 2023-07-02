package maria.incyberspace.myweatherbot.Models;

import java.util.Date;

public class WeatherUser {
    Long userId;
    Long chatId;
    String firstName;
    Double latitude;
    Double longitude;
    Date date;

    public WeatherUser(Long userId, Long chatId, String firstName, Double latitude, Double longitude, Date date) {
        this.userId = userId;
        this.chatId = chatId;
        this.firstName = firstName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    @Override
    public String toString() {
        return "WeatherUser{" +
                "userId=" + userId +
                ", chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", date=" + date +
                '}';
    }

    public Long getUserId() {
        return userId;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Date getDate() {
        return date;
    }
}
