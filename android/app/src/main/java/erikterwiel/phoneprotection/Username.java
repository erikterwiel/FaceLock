package erikterwiel.phoneprotection;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "Usernames")
public class Username {

    private String mUsername;
    private String[] mUniques;
    private String[] mNames;
    private double[] mLatitudes;
    private double[] mLongitudes;

    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    @DynamoDBAttribute(attributeName = "Unique")
    public String[] getUniques() {
        return mUniques;
    }

    public void setUniques(String[] uniques) {
        mUniques = uniques;
    }

    @DynamoDBAttribute (attributeName = "Name")
    public String[] getNames() {
        return mNames;
    }

    public void setNames(String[] names) {
        mNames = names;
    }

    @DynamoDBAttribute (attributeName = "Latitude")
    public double[] getLatitudes() {
        return mLatitudes;
    }

    public void setLatitudes(double[] latitudes) {
        mLatitudes = latitudes;
    }

    @DynamoDBAttribute (attributeName = "Longitude")
    public double[] getLongitudes() {
        return mLongitudes;
    }

    public void setLongitudes(double[] longitudes) {
        mLongitudes = longitudes;
    }
}
