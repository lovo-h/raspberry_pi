package hhl3eq.virginia.edu.soundlights;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;
//import java.util.concurrent.Semaphore;


public class MainActivity extends ActionBarActivity {
    private EditText txtInputIP;
    private String json;
    private String json2;
    private CheckBox chkIP;
    private CheckBox chkUVA;

    private int rdgrpChoice = 0;      // initial selection

    public static final int SAMPLE_RATE = 16000;
    private AudioRecord mRecorder;
    private TextView lblRecorderFeedback;
    private MediaPlayer oPlayer;

    private short[] mBuffer;
    private ProgressBar mProgressBar;
    private File file = new File(Environment.getExternalStorageDirectory(), "values.raw");
    private File waveFile = new File(Environment.getExternalStorageDirectory(), "values.wav");
    private File file2 = new File(Environment.getExternalStorageDirectory(), "amplitude.txt");
    private File targetFile;
    public int bufferSize = 0;
    private boolean isPlaying = false;
    private boolean isRecording = false;

    private boolean[] currentEmptyRows = new boolean[3];

    private int margin_left;
    private int margin_top;

    private View settingsMenuView;
    private View lytFiles;
    private boolean isCollapsedSettingsMenu = true;

    private DropboxAPI<AndroidAuthSession> dropbox;

    private final static String FILE_DIR = "/MySampleFolder/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "me1v14xv0jl0pf4";
    private final static String ACCESS_SECRET = "yxiafrv3micrcc9";
    private boolean isLoggedIn;
    private Button login;
    private Button uploadBtn;
    private GridView gridFiles;
    private ListView listOfFiles;
    String[] fnames = null;
    File[] wavFiles;
    //private Button btnOpenPopup;
    private EditText fn;
    private Menu menu;
    private File selectedFile;

    // TODO: remove after test
//    private final Semaphore inB = new Semaphore(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new SetupViewsOnLoadHelper().execute(); // loads things in the background

        uploadBtn = (Button) findViewById(R.id.upload);

        loggedIn(false);

        AndroidAuthSession session;
        AppKeyPair pair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(pair, AccessType.APP_FOLDER, token);
        } else {
            session = new AndroidAuthSession(pair, AccessType.APP_FOLDER);
        }

        dropbox = new DropboxAPI<AndroidAuthSession>(session);
        listLocalFiles();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = dropbox.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();

                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();

                loggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox authentication",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void listLocalFiles(){
        File f = new File(Environment.getExternalStorageDirectory().toString());
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                String lowerName = filename.toLowerCase();
                if (lowerName.endsWith(".wav"))
                    return true;
                else
                    return false;
            }
        };

        wavFiles = f.listFiles(filter);

        String[] wfNames = new String[wavFiles.length];
        int i=0;
        for (File a : wavFiles){
            wfNames[i] = a.getName().toString();
            i++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, wfNames){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.RED);
                return view;
            }
        };

        listOfFiles = (ListView) findViewById(R.id.listView);
        listOfFiles.setAdapter(adapter);
        listOfFiles.setVisibility(View.VISIBLE);
        listOfFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,int position, long id) {
                selectedFile = wavFiles[position];
            }
        });
    }

    public void loggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
        if (isLogged) {
            uploadBtn.setVisibility(View.VISIBLE);
            DisplayFiles display = new DisplayFiles(dropbox, handler);
            display.execute();
        } else {
            uploadBtn.setVisibility(View.GONE);
        }
        //login.setText(isLogged ? "Logout" : "Login");
    }

    private void loadIPsFromTXT() {
        String[] strIPs = new String[3];
        // reset empty rows
        for (int x = 0; x < 3; x++) {
            currentEmptyRows[x] = true;
        }

        //Get the text file
        File file = new File(Environment.getExternalStorageDirectory(), "IPs.txt");

        int ipCount = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null && ipCount < 3) {
                strIPs[ipCount] = line;
                currentEmptyRows[ipCount++] = false;
            }
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }

        EditText txtIP = (EditText) findViewById(R.id.txtInputIP);
        txtIP.setText(strIPs[0]);
        TextView saveTxt = (TextView) findViewById(R.id.firstIPDisplay);
        saveTxt.setText(strIPs[0]);
        saveTxt = (TextView) findViewById(R.id.secondIPDisplay);
        saveTxt.setText(strIPs[1]);
        saveTxt = (TextView) findViewById(R.id.thirdIPDisplay);
        saveTxt.setText(strIPs[2]);

        View someRow;
        if (currentEmptyRows[0]) {
            someRow = findViewById(R.id.firstRow);
            someRow.setVisibility(View.GONE);
        }
        if (currentEmptyRows[1]) {
            someRow = findViewById(R.id.secondRow);
            someRow.setVisibility(View.GONE);
        }
        if (currentEmptyRows[2]) {
            someRow = findViewById(R.id.thirdRow);
            someRow.setVisibility(View.GONE);
        }
    }

    private class SetupViewsOnLoadHelper extends AsyncTask<View, Void, String> {
        /* used to initialize the program in the background */
        @Override
        protected String doInBackground(View... views) {
            loadIPsFromTXT();

            lblRecorderFeedback = (TextView) findViewById(R.id.lblRecorderFeedback);
            txtInputIP = (EditText) findViewById(R.id.txtInputIP);
            chkIP = (CheckBox) findViewById(R.id.chkIP);
            chkUVA = (CheckBox) findViewById(R.id.chkUVA);
            settingsMenuView = findViewById(R.id.settingsMenu);     // used to expand/collapse menu
            lytFiles = findViewById(R.id.lytFiles);                 // used to expand/collapse menu

            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mBuffer = new short[bufferSize];
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

            Resources r = findViewById(R.id.thirdRow).getResources();   // used to add/remove IPs
            margin_left = Tools.convertPXtoDP(r, 45); // used to add/remove IPs
            margin_top = Tools.convertPXtoDP(r, 10);  // used to add/remove IPs

            setUpRadioGroup();
            linkAllOnClickListeners();

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            // onPostExecute displays the results of the AsyncTask.
        }

    }

    private void linkAllOnClickListeners() {
        TextView firstX = (TextView) findViewById(R.id.firstX);
        TextView secondX = (TextView) findViewById(R.id.secondX);
        TextView thirdX = (TextView) findViewById(R.id.thirdX);

        setOnClickListenerRemoveIP(firstX);
        setOnClickListenerRemoveIP(secondX);
        setOnClickListenerRemoveIP(thirdX);

        TextView firstEdit = (TextView) findViewById(R.id.firstEdit);
        TextView secondEdit = (TextView) findViewById(R.id.secondEdit);
        TextView thirdEdit = (TextView) findViewById(R.id.thirdEdit);

        setOnClickListenerEditIP(firstEdit);
        setOnClickListenerEditIP(secondEdit);
        setOnClickListenerEditIP(thirdEdit);

        TextView saveIP = (TextView) findViewById(R.id.saveIP);

        setOnClickListenerSaveIP(saveIP);
    }

    private void setOnClickListenerSaveIP(TextView view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIP(v);

            }
        });
    }

    private void setOnClickListenerEditIP(TextView view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editIP(v);

            }
        });
    }

    private void setOnClickListenerRemoveIP(TextView view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeIP(v);

            }
        });
    }

    public void startSend(View view) {
        boolean chkip = chkIP.isChecked();
        boolean chkuva = chkUVA.isChecked();
//        json = Tools.prepareJSON(rdgrpChoice);
        json = json2;

        if (chkip) {
            String ip = txtInputIP.getText().toString();
            Toast.makeText(getBaseContext(), "Sending data to IP address: " + ip, Toast.LENGTH_LONG).show();
            new HttpAsyncTask(json).execute(ip + "/rpi");
        }
        if (chkuva) {
            // http://cs4720.cs.virginia.edu/rpi/check.php?username=au753
            String ip = "cs4720.cs.virginia.edu/rpi/?username=au753";
            Toast.makeText(getBaseContext(), "View results: http://cs4720.cs.virginia.edu/rpi/check.php?username=au753", Toast.LENGTH_LONG).show();
            new HttpAsyncTask(json).execute(ip);
        }
        if (!chkip && !chkuva) {
            Toast.makeText(getBaseContext(), "Please check a location", Toast.LENGTH_LONG).show();
        }

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        /* sends json to some remote host on a separate thread */
        private String json;

        public HttpAsyncTask(String json) {
            /* constructor */
            this.json = json;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                String url = "http://" + params[0].replace("http://", "");
                HttpPost httpPost = new HttpPost(url);

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject(json);

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                httpclient.execute(httpPost);

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // onPostExecute displays the results of the AsyncTask.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                if (isCollapsedSettingsMenu) {
                    Tools.expand(settingsMenuView);
//                    if (isLoggedIn) {
//                        Tools.expand(lytFiles);
//                    }
                    isCollapsedSettingsMenu = false;
                } else {
                    Tools.collapse(settingsMenuView);
//                    Tools.collapse(lytFiles);
                    isCollapsedSettingsMenu = true;
                }


                Toast.makeText(getApplicationContext(), "Settings pressed", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_microphone:
                startRecorder(getWindow().getDecorView().findViewById(android.R.id.content));
                toggleIcon(item, isRecording, R.drawable.ic_action_mic_muted, R.drawable.ic_action_mic);
                break;
            case R.id.action_play:
                startPlayingRecord(getWindow().getDecorView().findViewById(android.R.id.content));
                //toggleIcon(item, isPlaying, R.drawable.pause1, R.drawable.play1);
                break;
            case R.id.action_dropbox:
                if (isLoggedIn) {
                    dropbox.getSession().unlink();
                    loggedIn(false);
                } else {
                    dropbox.getSession().startAuthentication(MainActivity.this);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleIcon(MenuItem item, boolean state, int id_off, int id_on) {
        if (state) {
            item.setIcon(getResources().getDrawable(id_off));
        } else {
            item.setIcon(getResources().getDrawable(id_on));
        }
    }

    public void setUpRadioGroup() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rdGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton radioButton1 = (RadioButton) findViewById(R.id.radioButton);
                RadioButton radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
                RadioButton radioButton3 = (RadioButton) findViewById(R.id.radioButton3);

                if (radioButton1.isChecked()) {
                    rdgrpChoice = 0;
                } else if (radioButton2.isChecked()) {
                    rdgrpChoice = 1;
                } else if (radioButton3.isChecked()) {
                    rdgrpChoice = 2;
                } else {
                    rdgrpChoice = -1;
                }
            }
        });
    }

    public void startPlayingRecord(View view) {
        if (!isPlaying) {
            if (selectedFile != null) {
                oPlayer = new MediaPlayer();
                try {
                    lblRecorderFeedback.setText("Playing Back (looped)...");
                    oPlayer.setDataSource(selectedFile.getAbsolutePath());
                    oPlayer.prepare();
                    oPlayer.start();
                    //
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                isPlaying = true;
                oPlayer.setLooping(true);
                //listFiles.setVisibility(View.GONE);
                menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.pause1));
            }
            else
                Toast.makeText(getBaseContext(), "Select File First!", Toast.LENGTH_LONG).show();

        } else {
            lblRecorderFeedback.setText("...");
            oPlayer.stop();
            oPlayer.release();
            oPlayer = null;
            isPlaying = false;
            menu.getItem(1).setIcon(getResources().getDrawable(R.drawable.play1));
        }
    }

    //start recording
    public void startRecorder(View view) {
        if (!isRecording) {
            lblRecorderFeedback.setText("Recording...");
            mRecorder.startRecording();
            startBufferedWrite(file);
            isRecording = true;
        } else {
            lblRecorderFeedback.setText("...");
            mRecorder.stop();
            isRecording = false;

            LayoutInflater layoutInflater
                    = (LayoutInflater)getBaseContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.file_name_popup, null);
            final PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            Button btnSave = (Button)popupView.findViewById(R.id.save);
            popupWindow.setFocusable(true);
            //popupWindow.update();
            fn = (EditText) popupView.findViewById(R.id.fineName);

            btnSave.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fName = fn.getText().toString();
                    //Toast.makeText(getBaseContext(), fName, Toast.LENGTH_LONG).show();
                    if (fName.length() > 0) {
                        fName += ".wav";
                        File newName = new File(Environment.getExternalStorageDirectory(), fName);
                        Toast.makeText(getBaseContext(), "File saved as " + fName, Toast.LENGTH_LONG).show();
                        waveFile.renameTo(newName);
                        listLocalFiles();
                    } else {
                        Toast.makeText(getBaseContext(), "File saved as values.wav", Toast.LENGTH_LONG).show();
                        listLocalFiles();
                    }
                    popupWindow.dismiss();
                }
            });

            TextView align = (TextView) findViewById(R.id.textView3);
            popupWindow.showAsDropDown(align, 0 , 0);
            // TODO: delete after test

//            displayAmplitude();

        }

        try {
            rawToWave(file, waveFile);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    double maxIntensity = 0;

    // TODO: delete after test
    private void displayAmplitude() {
        /* used to list amplitude values as feedback */
        // NOTE: long recordings overflow
        StringBuilder mytxt = new StringBuilder();
        mytxt.append("{\"lights\": [");
        int lightn = 0;
        int count = arrDbl.size();
        for (Double n : arrDbl) {
            // TODO: ==============================
            if (n == 0) {
                count--;
                continue;
            }

            mytxt.append("{\"lightId\":");
            lightn = (int) ((n / maxIntensity * 32));
            mytxt.append(lightn);
            mytxt.append(", \"red\":0,\"green\":255,\"blue\":0, \"intensity\": 0.75}");
            if (count > 1) {
                mytxt.append(",");
            }
            count--;

        }
        mytxt.append("],\"propagate\": true}");
//        lblRecorderFeedback.setText(mytxt.toString());
        json2 = mytxt.toString();
        maxIntensity = 0;
    }

    private void startReadingBar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isRecording) {
                        lblRecorderFeedback.setText(Integer.toString(mProgressBar.getProgress()));
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }

    // TODO: Delete after testing
    ArrayList<Double> arrDbl = new ArrayList<Double>();

    private void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;
                DataOutputStream output2 = null;
                File file2 = new File(Environment.getExternalStorageDirectory(), "amplitude.txt");
                String[] strDbl = new String[1];
                try {
                    output2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file2)));
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

                    while (isRecording) {
                        double sum = 0;
                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            output.writeShort(mBuffer[i]);
                            sum += mBuffer[i] * mBuffer[i];
                        }
                        if (readSize > 0) {
                            final double amplitude = sum / readSize;
                            mProgressBar.setProgress((int) Math.sqrt(amplitude) / 50);
                            if (amplitude > maxIntensity) {
                                maxIntensity = amplitude;
                            }
                            // TODO: delete after test
//                            arrDbl.add(amplitude);
                            output2.writeDouble(amplitude);

                        }
                    }

                    try {
                        Toast.makeText(getBaseContext(), "Please wait. Now converting...", Toast.LENGTH_SHORT).show();
                        //TODO: delete after test
                        rawToDbl(file2);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }

                } catch (IOException e) {
                    //Toast.makeText(RecordingLevelSampleActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    mProgressBar.setProgress(0);
                    if (output != null) {
                        try {
                            output.flush();
                            output2.flush();
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        } finally {
                            try {
                                output.close();
                                output2.close();
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void rawToDbl(File rawFile) throws IOException {
//        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        maxIntensity /= 2;

        InputStream is;
        try {
            // create file input stream
            is = new FileInputStream(rawFile.getAbsolutePath());

            // create new data input stream
            input = new DataInputStream(is);


            StringBuilder mytxt = new StringBuilder();
            mytxt.append("{\"lights\": [");
            int lightn = 0;
            double dbl;

            // read till end of the stream
            while (input.available() > 0) {
                // read character
                dbl = input.readDouble();
                // TODO: ==============================
                mytxt.append("{\"lightId\":");
                lightn = (int) ((dbl / maxIntensity * 32));

                mytxt.append(lightn);
                mytxt.append(",\"red\":0,\"green\":255,\"blue\":0,\"intensity\":0.75},");
            }
            // indicates completion
            mytxt.append("{\"lightId\":32,\"red\":255,\"green\":0,\"blue\":0,\"intensity\":0.95}");
            mytxt.append("],\"propagate\": true}");
            // TODO: send JSON to json file
            // TODO: delete amplitude.txt
            json2 = mytxt.toString();
        } catch (Exception e) {
            // if any I/O error occurs
            e.printStackTrace();
        } finally {
            input.close();
        }
    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {
        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    public void removeIP(View view) {
        int id = view.getId();
        View row = null;

        switch (id) {
            case R.id.firstX:
                row = findViewById(R.id.firstRow);
                currentEmptyRows[0] = true;
                break;
            case R.id.secondX:
                row = findViewById(R.id.secondRow);
                currentEmptyRows[1] = true;
                break;
            case R.id.thirdX:
                row = findViewById(R.id.thirdRow);
                currentEmptyRows[2] = true;
                break;
        }

        Toast.makeText(getBaseContext(), "Removed IP", Toast.LENGTH_LONG).show();
        row.getLayoutParams().height = 0;
        row.setVisibility(View.GONE);
        saveIPsToFile();
    }

    public void editIP(View view) {
        TextView inputTxt = (TextView) findViewById(R.id.txtInputIP);

        int id = view.getId();
        TextView saveTxt = null;
        TextView removeTxt = null;

        switch (id) {
            case R.id.firstEdit:
                saveTxt = (TextView) findViewById(R.id.firstIPDisplay);
                removeTxt = (TextView) findViewById(R.id.firstX);
                break;
            case R.id.secondEdit:
                saveTxt = (TextView) findViewById(R.id.secondIPDisplay);
                removeTxt = (TextView) findViewById(R.id.secondX);
                break;
            case R.id.thirdEdit:
                saveTxt = (TextView) findViewById(R.id.thirdIPDisplay);
                removeTxt = (TextView) findViewById(R.id.thirdX);
                break;
        }

        removeIP(removeTxt);
        inputTxt.setText(saveTxt.getText());
    }

    public void saveIP(View view) {

        TextView saveTxt = null;
        View row = null;

        int x = getEmptyIPSlot();
        int ptrIP = -1;

        // guard
        if (x == -1) {
            Toast.makeText(getBaseContext(), "Cannot add more IPs", Toast.LENGTH_LONG).show();
            return;
        }

        switch (x) {
            case 0:
                saveTxt = (TextView) findViewById(R.id.firstIPDisplay);
                row = findViewById(R.id.firstRow);
                ptrIP = 0;
                break;
            case 1:
                saveTxt = (TextView) findViewById(R.id.secondIPDisplay);
                row = findViewById(R.id.secondRow);
                ptrIP = 1;
                break;
            case 2:
                saveTxt = (TextView) findViewById(R.id.thirdIPDisplay);
                row = findViewById(R.id.thirdRow);
                ptrIP = 2;
                break;
        }

        LinearLayout.LayoutParams rowParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParam.setMargins(margin_left, margin_top, 0, 0);
        currentEmptyRows[x] = false;
        row.setLayoutParams(rowParam);
        row.setVisibility(View.VISIBLE);
        saveTxt.setText(txtInputIP.getText());
        saveIPsToFile();
    }

    private void saveIPsToFile() {
        try {
            StringBuilder myIPs = new StringBuilder();
            TextView tv;

            if (!currentEmptyRows[0]) {
                tv = (TextView) findViewById(R.id.firstIPDisplay);
                myIPs.append(tv.getText().toString());
                myIPs.append(System.getProperty("line.separator"));
            }
            if (!currentEmptyRows[1]) {
                tv = (TextView) findViewById(R.id.secondIPDisplay);
                myIPs.append(tv.getText().toString());
                myIPs.append(System.getProperty("line.separator"));
            }
            if (!currentEmptyRows[2]) {
                tv = (TextView) findViewById(R.id.thirdIPDisplay);
                myIPs.append(tv.getText().toString());
            }

            File file = new File(Environment.getExternalStorageDirectory(), "IPs.txt");
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(myIPs.toString());
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getEmptyIPSlot() {
        /* used to determine if there exists an empty slot for an IP address*/
        int x;
        for (x = 0; x < 3; x++) {
            if (currentEmptyRows[x]) {
                break;
            }
        }

        if (x >= 3) {
            x = -1;
        }

        return x;
    }

    public void upload(View view) {
        if (selectedFile != null) {
            UploadFile upload = new UploadFile(this, dropbox, selectedFile);
            upload.execute();
            DisplayFiles display = new DisplayFiles(dropbox, handler);
            display.execute();
        }
        else
            Toast.makeText(getBaseContext(), "Select File First!", Toast.LENGTH_LONG).show();
    }

    public void download(View view) {
        DownloadFile download = new DownloadFile(this, dropbox, waveFile);
        download.execute();
    }

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ArrayList<String> result = msg.getData().getStringArrayList("data");
            fnames = result.toArray(new String[result.size()]);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, fnames){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    text.setTextColor(Color.CYAN);
                    return view;
                }
            };

            gridFiles = (GridView) findViewById(R.id.grdFiles);
            gridFiles.setAdapter(adapter);
            gridFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    targetFile = new File(Environment.getExternalStorageDirectory(), fnames[position]);
                    DownloadFile download = new DownloadFile(getApplicationContext(), dropbox, targetFile);
                    download.execute();
                    listLocalFiles();
                }
            });
        }
    };
}