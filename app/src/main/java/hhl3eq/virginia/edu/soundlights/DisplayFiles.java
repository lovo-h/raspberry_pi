package hhl3eq.virginia.edu.soundlights;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class DisplayFiles extends AsyncTask<Void, Void, ArrayList<String>>  {
    private DropboxAPI<?> dropbox;
    private Handler handler;

    public DisplayFiles(DropboxAPI<?> dropbox, Handler handler) {
        this.dropbox = dropbox;
        this.handler = handler;
    }

    protected ArrayList<String> doInBackground(Void... params) {
        ArrayList<String> dir = new ArrayList<String>();
        try {
            DropboxAPI.Entry dirent = dropbox.metadata("/", 1000, null, true, null);

            for ( DropboxAPI.Entry ent: dirent.contents){
                dir.add(ent.fileName());
            }
            } catch (DropboxException e) {
                e.printStackTrace();
            }
        return dir;
        }

    protected void onPostExecute(ArrayList<String> result) {
        Message msgObj = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putStringArrayList("data", result);
        msgObj.setData(b);
        handler.sendMessage(msgObj);

    }
}