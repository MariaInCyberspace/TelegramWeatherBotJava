package maria.incyberspace.myweatherbot.Storage;

public enum DatabaseConstants {

    USER_ID("user_id"),
    CHAT_ID("chat_id"),
    FIRST_NAME("first_name"),
    LATITUDE("latitude"),
    LONGITUDE("longitude"),
    DATE("date");

    private String constant;
    private DatabaseConstants(String constant) {
        this.constant = constant;
    }

    @Override
    public String toString() {
        return constant;
    }
}
