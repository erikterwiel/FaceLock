package erikterwiel.phoneprotection;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.ArrayList;
import java.util.List;

@DynamoDBTable(tableName = "Usernames")
public class Account {

    private String mUsername;
    private List<String> mUniques = new ArrayList<>();
    private List<String> mNames = new ArrayList<>();
    private List<Double> mLatitudes = new ArrayList<>();
    private List<Double> mLongitudes = new ArrayList<>();

    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    @DynamoDBAttribute(attributeName = "Unique")
    public List<String> getUniques() {
        return mUniques;
    }

    public void setUniques(List<String> uniques) {
        mUniques = uniques;
    }

    public void addUnique(String unique) {
        mUniques.add(unique);
    }

    @DynamoDBAttribute (attributeName = "Name")
    public List<String> getNames() {
        return mNames;
    }

    public void setNames(List<String> names) {
        mNames = names;
    }

    public void addName(String name) {
        mNames.add(name);
    }

    public void replaceName(int index, String name) {
        mNames.set(index, name);
    }

    @DynamoDBAttribute (attributeName = "Latitude")
    public List<Double> getLatitudes() {
        return mLatitudes;
    }

    public void setLatitudes(List<Double> latitudes) {
        mLatitudes = latitudes;
    }

    public void addLatitude(Double latitude) {
        mLatitudes.add(latitude);
    }

    public void replaceLatitude(int index, Double latitude) {
        mLatitudes.set(index, latitude);
    }

    @DynamoDBAttribute (attributeName = "Longitude")
    public List<Double> getLongitudes() {
        return mLongitudes;
    }

    public void setLongitudes(List<Double> longitudes) {
        mLongitudes = longitudes;
    }

    public void addLongitude(Double longitude) {
        mLongitudes.add(longitude);
    }

    public void replaceLongitude(int index, Double longitude) {
        mLongitudes.set(index, longitude);
    }
}
