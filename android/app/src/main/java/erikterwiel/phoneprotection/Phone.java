package erikterwiel.phoneprotection;

public class Phone {
    private String mUnique;
    private String mName;
    private double mLatitude;
    private double mLongitude;

    public Phone(String unique, String name, double latitude, double longitude) {
        mUnique = unique;
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getUnique() {
        return mUnique;
    }

    public void setUnique(String unique) {
        mUnique = unique;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }
}
