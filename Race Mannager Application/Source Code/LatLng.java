
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 *This class LatLng hold's coordinate information
 */
/**
 *
 * @author idan nicolet
 */
public class LatLng {

    public final float latitude , longitude;
    public final int user , type;
    public final String name , azimuth , speed , time;

    public LatLng(float lat, float lng, int user, int type, String name, String azimuth, String speed, long time) {
        this.latitude = lat;
        this.longitude = lng;
        this.user = user;
        this.type = type;
        this.name = name;
        this.azimuth = azimuth;
        this.speed = speed;

        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.time = formatter.format(date);
    }

    public LatLng(float lat, float lng, int type, String name) {
        this.latitude = lat;
        this.longitude = lng;
        this.type = type;
        this.name = name;
        this.user = -1;
        this.azimuth = null;
        this.speed = null;
        this.time = null;
    }

    public LatLng(float lat, float lng, String name) {
        this.latitude = lat;
        this.longitude = lng;
        this.type = 0;
        this.name = name;
        this.user = -1;
        this.azimuth = null;
        this.speed = null;
        this.time = null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LatLng)) {
            return false;
        } else {
            LatLng var2 = (LatLng) o;
            return Double.doubleToLongBits(this.latitude) == Double.doubleToLongBits(var2.latitude) && Double.doubleToLongBits(this.longitude) == Double.doubleToLongBits(var2.longitude);
        }
    }
}
