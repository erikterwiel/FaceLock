package erikterwiel.phoneprotection;

import android.app.Application;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import static erikterwiel.phoneprotection.DynamoDBKeys.POOL_ID_UNAUTH;
import static erikterwiel.phoneprotection.DynamoDBKeys.POOL_REGION;


class DynamoDB {

    private static final DynamoDB instance = new DynamoDB();
    private DynamoDBMapper mapper;

    private DynamoDB() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                //dagger2 context,
                POOL_ID_UNAUTH,
                POOL_REGION);
        AmazonDynamoDBClient DDBClient = new AmazonDynamoDBClient(credentialsProvider);
        mapper = new DynamoDBMapper(DDBClient);
    }


    static DynamoDB getInstance() {
        return instance;
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }
}
