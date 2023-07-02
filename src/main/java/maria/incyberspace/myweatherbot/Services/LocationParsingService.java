package maria.incyberspace.myweatherbot.Services;

import org.telegram.telegrambots.meta.api.objects.Location;

public abstract class LocationParsingService {
    public static String parseLocation(Location location, String name) {
        double longi = location.getLongitude();
        double lati = location.getLatitude();
        return WeatherParsingService.parseWeather(lati, longi, name);
    }

    public static String parseLocation(Double[] location, String name) {
        Double latitude = location[0];
        Double longitude = location[1];
        return WeatherParsingService.parseWeather(latitude, longitude, name);
    }
}
