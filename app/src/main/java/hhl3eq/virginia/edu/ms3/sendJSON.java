package hhl3eq.virginia.edu.ms3;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;

public class sendJSON extends Activity {
    int rdgrpChoice = 0;      // initial selection
    EditText txtInputIP;
    String json;
    CheckBox chkIP;
    CheckBox chkUVA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_json);
        setUpRadioGroup();

        txtInputIP = (EditText) findViewById(R.id.txtInputIP);
        chkIP = (CheckBox) findViewById(R.id.chkIP);
        chkUVA = (CheckBox) findViewById(R.id.chkUVA);
    }


    public void myStart(View view) {
        boolean chkip = chkIP.isChecked();
        boolean chkuva = chkUVA.isChecked();
        new onCreateTask().execute();
        prepareJSON(rdgrpChoice);

        if (chkip) {
            String ip = txtInputIP.getText().toString();
            Toast.makeText(getBaseContext(), "Sending data to IP address: " +  ip, Toast.LENGTH_LONG).show();
            new HttpAsyncTask(json).execute(ip + "/rpi/");
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

    // separate thread
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        private String json;

        public  HttpAsyncTask(String json) {
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
//                HttpResponse httpResponse = httpclient.execute(httpPost);
                httpclient.execute(httpPost);

                // 9. receive response as inputStream
//                inputStream = httpResponse.getEntity().getContent();

                // 10. convert inputstream to string
//                if(inputStream != null)
//                    result = convertInputStreamToString(inputStream);
//                else
//                    result = "Did not work!";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }

            return null;
        }


        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_json, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void prepareJSON(int choice) {
        switch (choice) {
            case 0:
                json = "{\"lights\": [{\"lightId\": 0, \"red\":0,\"green\":0,\"blue\":255, \"intensity\": 0.5}],\"propagate\": true}"; break;
            case 1:
                json = "{ \"lights\": [  {\"lightId\": 1, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 3, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 5, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 7, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 9, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 11, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 13, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 15, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 17, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 19, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 21, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 23, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 25, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 27, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 29, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}, {\"lightId\": 31, \"red\":242,\"green\":116,\"blue\":12, \"intensity\": 0.5}],  \"propagate\": false }"; break;
            case 2:
                json = "{\"lights\": [{\"lightId\": 0, \"red\":0,\"green\":255,\"blue\":0, \"intensity\": 0.5}],\"propagate\": true}"; break;
            case -1:
                json = "{\"lights\": [{\"lightId\": 0, \"red\":255,\"green\":0,\"blue\":0, \"intensity\": 0.9}],\"propagate\": true}"; break;
        }
    }


    // separate thread
    private class onCreateTask extends AsyncTask<Integer, Void, Integer> {
        public onCreateTask() {

        }

        @Override
        protected Integer doInBackground(Integer... params) {
//            return setUpRadioGroup();
            return null;
        }

        // onPostExecute displays the results of the AsyncTask.

        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    public void setUpRadioGroup() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rdGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                final RadioButton radioButton1 = (RadioButton) findViewById(R.id.radioButton);
                final RadioButton radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
                final RadioButton radioButton3 = (RadioButton) findViewById(R.id.radioButton3);

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

//        return rdgrpChoice;
    }
}
