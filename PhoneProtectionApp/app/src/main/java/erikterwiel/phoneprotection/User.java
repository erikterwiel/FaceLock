package erikterwiel.phoneprotection;

import android.graphics.drawable.Drawable;

public class User {
    private String mName;
    private String mFileName;
    private Drawable mImage;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public Drawable getImage() {
        return mImage;
    }

    public void setImage(Drawable image) {
        mImage = image;
    }
}
