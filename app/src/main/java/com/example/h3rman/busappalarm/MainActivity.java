package com.example.h3rman.busappalarm;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private ProgressDialog pDialog;
        private static String SearchBusId;
        private static String url = "http://busalarm-h3rmanapp.rhcloud.com/";
        //"http://api.androidhive.info/contacts/";
                //"http://datamall2.mytransport.sg/ltaodataservice/BusArrival";
        private static final String ARG_SECTION_NUMBER = "section_number";

        // Services JSONArray
        JSONArray services = null;

        // JSON Node names
        private static final String TAG_SERVICES = "services";
        private static final String TAG_SERVICE_NO = "no";
        private static final String TAG_STATUS = "status";
        private static final String TAG_NEXT_BUS = "next";
        private static final String TAG_NEXT_BUS_ESTIMATED_ARRIVAL = "time";
        private static final String TAG_NEXT_BUS_DURATION = "duration_ms";
        private static final String TAG_NEXT_BUS_LOAD = "load";

        // Hashmap for ListView
        ArrayList<HashMap<String,String>> serviceList;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            //if(getArguments().getInt(ARG_SECTION_NUMBER) == 1){

            //}
            //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }

        @Override
        public void onViewCreated (View view, Bundle savedInstanceState){
            serviceList = new ArrayList<HashMap<String, String>>();
            ListView lv = getListView();

            // your text box
            final EditText textBusId = (EditText) getActivity().findViewById(R.id.busId);

            textBusId.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        SearchBusId = textBusId.getText().toString();
                        // Calling async task to get json
                        new GetServices().execute();
                        return true;
                    }
                    return false;
                }
            });

        }

        /**
         * Async task class to get json by making HTTP call
         * */
        private  class GetServices extends AsyncTask<Void,Void,Void> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Showing progress dialog
                pDialog = new ProgressDialog(getActivity());
                pDialog.setMessage("Please wait...");
                pDialog.setCancelable(false);
                pDialog.show();

            }

            @Override
            protected Void doInBackground(Void... arg0) {

//                String tag_json_obj = "json_obj_req";
//                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
//                        url,null,
//                        new Response.Listener<JSONObject>(){
//
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                Log.d("VOLLEY Response: ",response.toString());
//                                pDialog.hide();
//                            }
//                        },new Response.ErrorListener(){
//                                @Override
//                                public void onErrorResponse(VolleyError error){
//                                    VolleyLog.d("VOLLEY Error: ", error.getMessage());
//                                    pDialog.hide();
//                                }
//                        }) {
//                    /**
//                     * Passing some request headers
//                     * */
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        HashMap<String,String> headers = new HashMap<String,String>();
//                        headers.put("accept","application/json");
//                        headers.put("AccountKey", "SUoJf+QUbPhGMRjOF9/4FQ==");
//                        headers.put("UniqueUserID", "21e564ee-52c1-4bbb-9242-819ce7adc22a");
//                        return headers;
//                    }
//
//                    @Override
//                    protected Map<String,String> getParams(){
//                        Map<String,String> params = new HashMap<String,String>();
//                        params.put("BusStopId","83139");
//                        params.put("SST","True");
//                        return params;
//                    }
//                };
//
//                AppController.getInstance().addToRequestQueue(jsonObjReq,tag_json_obj);

                // Creating service handler class instance
                ServiceHandler sh = new ServiceHandler();

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("id",SearchBusId));
                params.add(new BasicNameValuePair("SST","True"));
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET,params);

                Log.d("Response: ", "> " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        services = jsonObj.getJSONArray(TAG_SERVICES);

                        // looping through All Contacts
                        for (int i = 0; i < services.length(); i++) {
                            JSONObject c = services.getJSONObject(i);

                            String serviceNo = c.getString(TAG_SERVICE_NO);
                            String status = c.getString(TAG_STATUS);
                            //String name = c.getString(TAG_NAME);
                            //String email = c.getString(TAG_EMAIL);
                            //String address = c.getString(TAG_ADDRESS);
                            //String gender = c.getString(TAG_GENDER);

                            // NextBus node is JSON Object
                            JSONObject nextBus = c.getJSONObject(TAG_NEXT_BUS);
                            String nextBusEta = nextBus.getString(TAG_NEXT_BUS_ESTIMATED_ARRIVAL);
                            String nextBusLoad = nextBus.getString(TAG_NEXT_BUS_LOAD);
                            String nextBusDuration = nextBus.getString(TAG_NEXT_BUS_DURATION);
                            try {
                                if (Integer.parseInt(nextBusDuration) < 0) {
                                    nextBusDuration = "Arriving";
                                } else {
                                    nextBusDuration = (Integer.parseInt(nextBusDuration) / 60000) + " mins";
                                }
                            }
                            catch (NumberFormatException ex) {
                                ex.printStackTrace();
                            }
                            //String office = phone.getString(TAG_PHONE_OFFICE);

                            // tmp hashmap for single contact
                            HashMap<String, String> service = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            service.put(TAG_SERVICE_NO, serviceNo);
                            service.put(TAG_STATUS, status);
                            service.put(TAG_NEXT_BUS_ESTIMATED_ARRIVAL, nextBusEta);
                            service.put(TAG_NEXT_BUS_LOAD, nextBusLoad);
                            service.put(TAG_NEXT_BUS_DURATION, nextBusDuration);

                            // adding contact to contact list
                            serviceList.add(service);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                // Dismiss the progress dialog
                if (pDialog.isShowing())
                    pDialog.dismiss();
                /**
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        getActivity(), serviceList,
                        R.layout.list_item, new String[] { TAG_SERVICE_NO, TAG_NEXT_BUS_LOAD,
                        TAG_NEXT_BUS_DURATION }, new int[] { R.id.serviceno,
                        R.id.load, R.id.eta });

                setListAdapter(adapter);
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Search";
                case 1:
                    return "Alarms";
                case 2:
                    return "Favourites";
            }
            return null;
        }
    }
}
