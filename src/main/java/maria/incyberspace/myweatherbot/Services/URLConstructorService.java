package maria.incyberspace.myweatherbot.Services;

public abstract class URLConstructorService {
    public static String createURL(Double latitude, Double longitude) {
        StringBuilder urlBuilder = new StringBuilder();
        String firstPart = "https://api.openweathermap.org/data/2.5/weather?lat=";
        String lati = latitude.toString() + "&lon=";
        String longi = longitude.toString() + "&appid=3a43f561256bb731ebe5cf274fb25845&units=metric";
        urlBuilder.append(firstPart).append(lati).append(longi);
        return urlBuilder.toString();
    }
}
