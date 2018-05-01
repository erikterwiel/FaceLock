package erikterwiel.phoneprotection.Singletons;

import android.content.Context;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;

import static erikterwiel.phoneprotection.Keys.DynamoDBKeys.POOL_ID_UNAUTH;
import static erikterwiel.phoneprotection.Keys.DynamoDBKeys.POOL_REGION;

public class Rekognition {

    private static Rekognition instance;
    private AmazonRekognitionClient rekognitionClient;

    private Rekognition(Context context) {
        AWSCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));
        rekognitionClient = new AmazonRekognitionClient(credentialsProvider);
    }

    public static void init(Context context) {
        if (instance == null) {
            context = context.getApplicationContext();
            instance = new Rekognition(context);
        }
    }

    public static Rekognition getInstance() {
        return instance;
    }

    public AmazonRekognitionClient getRekognitionClient() {
        return rekognitionClient;
    }
}
