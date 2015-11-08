package com.seven.actionbar;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends DrawerActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> eventsList;

    // url to get all events list
    private static String url_all_events = "http://www.ufgatorevents.com/android_connect/get_events.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_EVENTS = "events";
    private static final String TAG_EID = "E_id";
    private static final String TAG_NAME = "E_name";
    private static final String TAG_DES = "Description";

    // events JSONArray
    JSONArray events = null;

    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.container);

        LayoutInflater layoutInflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_main, null, false);
        frameLayout.addView(activityView);

        // Hashmap for ListView
        eventsList = new ArrayList<HashMap<String, String>>();

        // Loading events in Background Thread
        new LoadAllEvents().execute();

        // Get listview
        lv = (ListView)findViewById(R.id.list);

        // on seleting single event
        // launching  Detail Screen
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String eid = ((TextView) view.findViewById(R.id.eid)).getText()
                        .toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        CategoryActivity.class);//EventsDetail
                // sending eid to next activity
                in.putExtra(TAG_EID, eid);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });





    }


    @Override
    public void onStart(){
        super.onStart();
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.home_name));


    }

    // Response from HomeActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        /*if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.action_bar, menu);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            try {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            }

            catch (Exception e)
            {
                System.err.println(e.toString());
            }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);*/
        getMenuInflater().inflate(R.menu.action_bar, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        try {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        catch (Exception e)
        {
            System.err.println(e.toString());
        }
        return true;
    }


    /**
     * Background Async Task to Load all event by making HTTP Request
     * */
    class LoadAllEvents extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);//EventsActivity
            pDialog.setMessage("Loading events. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All events from url
         * */
        protected String doInBackground(String... args) {
            //String name = inputName.getText().toString();

            // Building Parameters
            //List<NameValuePair> params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("name", name));
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_events, "GET", params);

            // Check your log cat for JSON reponse
//            Log.d("All Events: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // events found
                    // Getting Array of events
                    events = json.getJSONArray(TAG_EVENTS);

                    // looping through All Events
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject c = events.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_EID);
                        String name = c.getString(TAG_NAME);
                        String decrp = c.getString(TAG_DES);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_EID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_DES, decrp);

                        // adding HashList to ArrayList
                        eventsList.add(map);
                    }
                } else {
                    // no events found, go to home page
                    Intent i = new Intent(getApplicationContext(),
                            MainActivity.class);//HomeAcitivity
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
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
            // dismiss the dialog after getting all events
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView   EventsActivity
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            MainActivity.this, eventsList,
                            R.layout.events_detail, new String[] { TAG_EID,
                            TAG_NAME, TAG_DES},
                            new int[] { R.id.eid, R.id.name, R.id.decrp});
                    // updating listview
                        lv.setAdapter(adapter);
                }
            });

        }

    }


}
