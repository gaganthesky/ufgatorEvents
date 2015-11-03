package com.seven.actionbar;

/**
 * Created by vineet on 10/19/15.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaExtractor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {

    Button mLogin;
    EditText mName;

    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();

    SharedPreferences sharedPreferences;

    //url to create new user
    private static String url_login = "http://www.ufgatorevents.com/android_connect/login_user.php";

    //JSON Node names
    private static final String TAG_SUCCESS = "success";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mName = (EditText)findViewById(R.id.loginName);
        mLogin = (Button)findViewById(R.id.btnLogin);

        // button click event
        mLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new user in background thread
                new GatorLogin().execute();
            }
        });

    }

    public void register(View view)
    {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Background Async Task to Create new User
     * */
    class GatorLogin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Loginning...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating user
         * */
        protected String doInBackground(String... args) {
            String name = mName.getText().toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));

            // getting JSON Object
            // Note that login url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_login,
                    "POST", params);

            // check log cat for respons
//             Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created user
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);

                    // closing this screen
                    finish();
                } else {
                    Looper.prepare();//user Handler to make the Thread running on the Main Thread
                    Toast.makeText(getApplicationContext(), "No such a user!", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    Looper.myLooper().quit();
                    //inputName.setText("");
                    // Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    // startActivity(i);
                    //finish();
                    // inputName.setText("");
                    // failed to login
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch(NullPointerException e){
                e.printStackTrace();
            }
            catch(RuntimeException e){
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }

    }

}
