package hhl3eq.virginia.edu.soundlights;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

public class DownloadFile extends AsyncTask<Void, Void, Boolean> {

    private DropboxAPI<?> dropbox;
    private Context context;
    private File file;
    private File localFile;

    public DownloadFile(Context context, DropboxAPI<?> dropbox,
                      File file) {
        this.context = context.getApplicationContext();
        this.dropbox = dropbox;
        this.file = file;
        localFile = new File(Environment.getExternalStorageDirectory(), file.getName());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(localFile);
            dropbox.getFile("/" + file.getName(), null, fileOutputStream, null);
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
            Toast.makeText(context, "File Downloaded Successfully!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to download file. Check if logged in.", Toast.LENGTH_LONG)
                    .show();
        }
    }
}
