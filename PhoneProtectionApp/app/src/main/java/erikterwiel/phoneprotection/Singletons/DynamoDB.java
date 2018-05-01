package erikterwiel.phoneprotection.Singletons;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import static erikterwiel.phoneprotection.Keys.DynamoDBKeys.POOL_ID_UNAUTH;
import static erikterwiel.phoneprotection.Keys.DynamoDBKeys.POOL_REGION;

public class DynamoDB {

    private static DynamoDB instance;
    private DynamoDBMapper mapper;

    private DynamoDB(Context context) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));
        AmazonDynamoDBClient DDBClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(DDBClient);
    }

    public static void init(Context context) {
        if (instance == null) {
            context = context.getApplicationContext();
            instance = new DynamoDB(context);
        }
    }

    public static DynamoDB getInstance() {
        return instance;
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }
}
