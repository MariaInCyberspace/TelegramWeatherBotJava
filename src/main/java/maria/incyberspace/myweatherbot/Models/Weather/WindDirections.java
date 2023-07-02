package maria.incyberspace.myweatherbot.Models.Weather;


public enum WindDirections {
    N("N"), S("S"), W("W"), E("E"),
    NE("NE"), SE("SE"), SW("SW"), NW("NW"),
    NNE("NNE"), ENE("ENE"), ESE("ESE"), SSE("SSE"),
    SSW("SSW"), WSW("WSW"), WNW("WNW"), NNW("NNW")
    ;
    private String direction;
    private WindDirections(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return direction;
    }
}