package erikterwiel.phoneprotection;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "Usernames")
public class Username {

    private String mUsername;
    private String mName;
    private double mLatitude;
    private double mLongitude;

    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    @DynamoDBAttribute (attributeName = "Name")
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @DynamoDBAttribute (attributeName = "Latitude")
    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    @DynamoDBAttribute (attributeName = "Longitude")
    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
}
