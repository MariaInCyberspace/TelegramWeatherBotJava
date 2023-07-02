package maria.incyberspace.myweatherbot.Services;

import maria.incyberspace.myweatherbot.Models.Weather.WindDirections;

public abstract class WindDirectionParsingService {
    public static String getDirection(Double degrees) {
        String direction = null;
        if (degrees <= 360 && degrees > 281) {
            if (degrees >= 348) direction = WindDirections.N.toString();
            if (degrees < 348 && degrees >= 326) direction = WindDirections.NNW.toString();
            if (degrees < 326 && degrees >= 303) direction = WindDirections.NW.toString();
            if (degrees < 303) direction = WindDirections.WNW.toString();
        } else if (degrees <= 281 && degrees > 191) {
            if (degrees > 258) direction = WindDirections.W.toString();
            if (degrees < 258 && degrees >= 236) direction = WindDirections.WSW.toString();
            if (degrees < 236 && degrees >= 213) direction = WindDirections.SW.toString();
            if (degrees < 213) direction = WindDirections.SSW.toString();
        } else if (degrees <= 191 && degrees > 101) {
            if (degrees > 168) direction = WindDirections.S.toString();
            if (degrees < 168 && degrees >= 146) direction = WindDirections.SSE.toString();
            if (degrees < 146 && degrees >= 123) direction = WindDirections.SE.toString();
            if (degrees < 123) direction = WindDirections.ESE.toString();
        } else {
            if (degrees <= 101 && degrees >= 78) direction = WindDirections.E.toString();
            if (degrees < 78 && degrees >= 56) direction = WindDirections.ENE.toString();
            if (degrees < 56 && degrees >= 33) direction = WindDirections.NE.toString();
            if (degrees < 33 && degrees >= 11) direction = WindDirections.NNE.toString();
            if (degrees < 11) direction = WindDirections.N.toString();
        }
        return direction;
    }

}
