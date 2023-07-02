package maria.incyberspace.myweatherbot.Storage;

import maria.incyberspace.myweatherbot.Models.WeatherUser;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DatabaseStorage implements UserStorage {
    private final String url = "jdbc:postgresql://localhost:5432/weatherbot";
    private final String user = "postgres";
    private final String password = "wonderland";

    private Map<Long, WeatherUser> users;


    private java.sql.Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("ssl","false");
        return java.sql.DriverManager.getConnection(url, props);
    }

    private Map<Long, WeatherUser> loadUsers() throws ClassNotFoundException, SQLException {
        Map<Long, WeatherUser> users = new HashMap<>();
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("select * from users");
                while (resultSet.next()) {
                    Long userId = resultSet.getObject(DatabaseConstants.USER_ID.toString(), Long.class);
                    Long chatId = resultSet.getObject(DatabaseConstants.CHAT_ID.toString(), Long.class);
                    Double lati = resultSet.getDouble(DatabaseConstants.LATITUDE.toString());
                    Double longi = resultSet.getDouble(DatabaseConstants.LONGITUDE.toString());
                    String name = resultSet.getString(DatabaseConstants.FIRST_NAME.toString());
                    LocalTime time = resultSet.getTime(DatabaseConstants.DATE.toString()).toLocalTime();
                    Date date = resultSet.getDate(DatabaseConstants.DATE.toString());
                    LocalDateTime localDateTime = LocalDateTime.of(date.toLocalDate(), time);
                    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                    java.util.Date date1 = Date.from(instant);

                    WeatherUser weatherUser = new WeatherUser(userId, chatId, name, lati, longi, date1);
                    users.put(userId, weatherUser);
                }
            }
        }
        return users;
    }


    @Override
    public void placeIntoDatabase(WeatherUser user) {
        Long userId = user.getUserId();
        Long chatId = user.getChatId();
        String name = user.getFirstName();
        Double lati = user.getLatitude();
        Double longi = user.getLongitude();
        java.util.Date date = user.getDate();


        String INSERT = "INSERT INTO public.users(" +
                "user_id, chat_id, date, first_name, latitude, longitude)" +
                "VALUES (?, ?, ?, ?, ?, ?);";
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                statement.setLong(1, userId);
                statement.setLong(2, chatId);
                statement.setTimestamp(3, new Timestamp(date.getTime()));
                statement.setString(4, name);
                statement.setDouble(5, lati);
                statement.setDouble(6, longi);
                statement.executeUpdate();
            }
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void removeFromTheDatabase(Long userId) {
        String DELETE = "DELETE FROM users " +
                "WHERE user_id = ?;";
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
                statement.setLong(1, userId);
                statement.executeUpdate();
            }
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public Map<Long, WeatherUser> getUsers() throws SQLException, ClassNotFoundException {
        users = loadUsers();
        return users;
    }
}
