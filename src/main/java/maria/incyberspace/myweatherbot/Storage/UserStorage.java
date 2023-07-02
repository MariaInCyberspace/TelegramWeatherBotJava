package maria.incyberspace.myweatherbot.Storage;

import maria.incyberspace.myweatherbot.Models.WeatherUser;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public interface UserStorage {
    public void placeIntoDatabase(WeatherUser user);
    public void removeFromTheDatabase(Long userId);
    public Map<Long, WeatherUser> getUsers() throws SQLException, ClassNotFoundException;
}
