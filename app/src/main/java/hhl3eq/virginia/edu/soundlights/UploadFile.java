package hhl3eq.virginia.edu.soundlights;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

public class UploadFile extends AsyncTask<Void, Void, Boolean> {

    private DropboxAPI<?> dropbox;
    private Context context;
    private File file;

    public UploadFile(Context context, DropboxAPI<?> dropbox, File file) {
        this.context = context.getApplicationContext();
        this.dropbox = dropbox;
        this.file = file;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            dropbox.putFile("/values.wav", fileInputStream, file.length(), null, null);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "File Uploaded Successfully!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to upload file", Toast.LENGTH_LONG)
                    .show();
        }
    }
}
