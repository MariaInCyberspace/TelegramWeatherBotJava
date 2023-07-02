package maria.incyberspace.myweatherbot.Services;

import com.google.gson.Gson;
import maria.incyberspace.myweatherbot.Models.Weather.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public abstract class WeatherParsingService {
    // Returns weather based on latitude and longitude
    public static String parseWeather(Double latitude, Double longitude, String name) {
        // Create url based on the passed parameters
        String url = URLConstructorService.createURL(latitude, longitude);
        StringBuilder weatherInfo = new StringBuilder();
        try (InputStreamReader inputStreamReader = new InputStreamReader(new URL(url).openStream())) {

            WeatherGSONObject gsonObject = new Gson().fromJson(inputStreamReader, WeatherGSONObject.class);

            String cityName = gsonObject.getName(); // Get city name
            Main main = gsonObject.getMain(); // Main block of json file
            Wind wind = gsonObject.getWind(); // Wind block of json file
            Sys sys = gsonObject.getSys(); // Sys block of json file
            List<Weather> weather = gsonObject.getWeather(); // Weather details
            String cloudsDescription = weather.get(0).getDescription(); // Weather description
            String country = sys.getCountry(); // Country
            Double currentTemp = main.getTemp(); // Current temperature
            Double feelsLike = main.getFeelsLike(); // Feels like temperature
            Double maxTemp = main.getTempMax();
            Double minTemp = main.getTempMin();
            Integer pressure = main.getPressure(); // Get pressure
            Integer humidity = main.getHumidity(); // Get humidity
            Double windSpeed = wind.getSpeed(); // Wind speed
            Double windDir = wind.getDeg(); // Wind direction
            // Parse direction
            String windDirection = WindDirectionParsingService.getDirection(windDir);

            weatherInfo.append("Hey, ").append(name).append("!\n")
                       .append("You're currently in: ").append(cityName).append(", ").append(country)
                       .append("\nCurrent temperature: ").append(currentTemp)
                       .append("\nFeels like: ").append(feelsLike)
                       .append("\nMaximum temperature for today: ").append(maxTemp)
                       .append("\nMinimum temperature for today: ").append(minTemp)
                       .append("\nWind speed: ").append(windSpeed)
                       .append("\nWind direction: ").append(windDirection)
                       .append("\nOutside conditions: ").append(cloudsDescription)
                       .append("\nPressure: ").append(pressure)
                       .append("\nHumidity: ").append(humidity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weatherInfo.toString();
    }
}
