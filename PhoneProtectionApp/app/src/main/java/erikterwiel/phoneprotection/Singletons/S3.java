package erikterwiel.phoneprotection.Singletons;

import android.content.Context;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import static erikterwiel.phoneprotection.Keys.DynamoDBKeys.POOL_ID_UNAUTH;
import static erikterwiel.phoneprotection.Keys.DynamoDBKeys.POOL_REGION;

public class S3 {

    private static S3 instance;
    private AmazonS3Client s3Client;
    private TransferUtility transferUtility;

    private S3(Context context) {
        transferUtility = getTransferUtility(context);
    }

    public static void init(Context context) {
        if (instance == null) {
            context = context.getApplicationContext();
            instance = new S3(context);
        }
    }

    public TransferUtility getTransferUtility(Context context) {
        TransferUtility sTransferUtility = new TransferUtility(
                getS3Client(context), context);
        return sTransferUtility;
    }

    public AmazonS3Client getS3Client(Context context) {
        s3Client = new AmazonS3Client(getCredProvider(context));
        return s3Client;
    }

    private AWSCredentialsProvider getCredProvider(Context context) {
        AWSCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                POOL_ID_UNAUTH,
                Regions.fromName(POOL_REGION));
        return credentialsProvider;
    }

    public static S3 getInstance() {
        return instance;
    }

    public TransferUtility getTransferUtility() {
        return transferUtility;
    }

    public AmazonS3Client getS3Client() {
        return s3Client;
    }
}
