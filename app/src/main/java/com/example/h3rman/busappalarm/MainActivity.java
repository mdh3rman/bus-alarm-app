package com.example.h3rman.busappalarm;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.example.h3rman.busappalarm.functions.DataMassager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.bus_stop_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String jsonStr = this.loadJSONFromAsset();
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);

                // Getting JSON Array node
                JSONArray busStops = jsonObj.getJSONArray("busStops");

                // looping through All Bus Services
                for (int i = 0; i < busStops.length(); i++) {
                    JSONObject c = busStops.getJSONObject(i);

                    String code = c.getString("code");
                    String road = c.getString("road");
                    String desc = c.getString("code");

                    editor.putString(code, road + ";" + desc);
                }
                editor.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the json file");
        }
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

    public static class SearchFragment extends ListFragment {
        private ProgressDialog pDialog;
        private static String SearchBusId;
        private static String url = "http://busalarm-h3rmanapp.rhcloud.com/";
        //"http://api.androidhive.info/contacts/";
        //"http://datamall2.mytransport.sg/ltaodataservice/BusArrival";


        // Services JSONArray
        JSONArray services = null;

        // JSON Node names
        private static String TAG_SERVICES = "services";
        private static String TAG_SERVICE_NO = "no";
        private static String TAG_STATUS = "status";
        private static String TAG_NEXT_BUS = "next";
        //private static String TAG_NEXT_BUS_ESTIMATED_ARRIVAL = "time";
        private static String TAG_NEXT_BUS_DURATION = "duration_ms";
        private static String TAG_NEXT_BUS_LOAD = "load";

        private static String TAG_SUBSEQUENT_BUS = "subsequent";
        //private static String TAG_SUBSEQUENT_BUS_ESTIMATED_ARRIVAL = "time2";
        private static String TAG_SUBSEQUENT_BUS_DURATION = "duration_ms2";
        private static String TAG_SUBSEQUENT_BUS_LOAD = "load2";

        private static String TAG_SUBSEQUENT_BUS_3 = "subsequent3";
        //private static String TAG_SUBSEQUENT_BUS_3_ESTIMATED_ARRIVAL = "time3";
        private static String TAG_SUBSEQUENT_BUS_3_DURATION = "duration_ms3";
        private static String TAG_SUBSEQUENT_BUS_3_LOAD = "load3";

        private static String TAG_LAST_UPDATED_TIME = "last_updated_on";

        // Hashmap for ListView
        ArrayList<HashMap<String,String>> serviceList;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.search_fragment, container, false);
            return rootView;
        }
        @Override
        public void onViewCreated (View view, Bundle savedInstanceState){
            serviceList = new ArrayList<HashMap<String, String>>();
            ListView lv = getListView();

            // your text box
            final EditText textBusId = (EditText) getActivity().findViewById(R.id.txtBusId);

            textBusId.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        SearchBusId = textBusId.getText().toString();
                        // Calling async task to get json
                        new GetServices().execute();
                        TextView txtBusStopCode = (TextView) getActivity().findViewById(R.id.bus_stop_id);
                        TextView txtBusStopDesc = (TextView) getActivity().findViewById(R.id.bus_stop_desc);
                        TextView txtBusStopRoad = (TextView) getActivity().findViewById(R.id.bus_stop_road);

                        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.bus_stop_file_key),Context.MODE_PRIVATE);
                        txtBusStopCode.setText();

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
                serviceList.clear();

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

                ContentValues params = new ContentValues();
                params.put("id",SearchBusId);
                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET,params);

                Log.d("Response: ", "> " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        services = jsonObj.getJSONArray(TAG_SERVICES);

                        // looping through All Bus Services
                        for (int i = 0; i < services.length(); i++) {
                            JSONObject c = services.getJSONObject(i);

                            String serviceNo = c.getString(TAG_SERVICE_NO);
                            String status = c.getString(TAG_STATUS);

                            // NextBus node is JSON Object
                            JSONObject nextBus = c.getJSONObject(TAG_NEXT_BUS);
                            //String nextBusEta = nextBus.getString(TAG_NEXT_BUS_ESTIMATED_ARRIVAL);
                            String nextBusLoad = nextBus.getString(TAG_NEXT_BUS_LOAD);
                            String nextBusDuration = nextBus.getString(TAG_NEXT_BUS_DURATION);

                            JSONObject subsequentBus = c.getJSONObject(TAG_SUBSEQUENT_BUS);
                            //String subsequentBusEta = subsequentBus.getString(TAG_SUBSEQUENT_BUS_ESTIMATED_ARRIVAL);
                            String subsequentBusLoad = subsequentBus.getString(TAG_SUBSEQUENT_BUS_LOAD);
                            String subsequentBusDuration = subsequentBus.getString(TAG_SUBSEQUENT_BUS_DURATION);

                            JSONObject subsequentBus3 = c.getJSONObject(TAG_SUBSEQUENT_BUS_3);
                            //String subsequentBus3Eta = subsequentBus3.getString(TAG_SUBSEQUENT_BUS_3_ESTIMATED_ARRIVAL);
                            String subsequentBus3Load = subsequentBus3.getString(TAG_SUBSEQUENT_BUS_3_LOAD);
                            String subsequentBus3Duration = subsequentBus3.getString(TAG_SUBSEQUENT_BUS_3_DURATION);

                            nextBusDuration = DataMassager.getBusDuration(nextBusDuration);
                            subsequentBusDuration = DataMassager.getBusDuration(subsequentBusDuration);
                            subsequentBus3Duration = DataMassager.getBusDuration(subsequentBus3Duration);

                            nextBusLoad = DataMassager.getBusLoad(nextBusLoad);
                            subsequentBusLoad = DataMassager.getBusLoad(subsequentBusLoad);
                            subsequentBus3Load = DataMassager.getBusLoad(subsequentBus3Load);

                           // tmp hashmap for single contact
                            HashMap<String, String> service = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            service.put(TAG_SERVICE_NO, serviceNo);
                            service.put(TAG_STATUS, status);
                            service.put(TAG_NEXT_BUS_LOAD, nextBusLoad);
                            service.put(TAG_NEXT_BUS_DURATION, nextBusDuration);
                            service.put(TAG_SUBSEQUENT_BUS_LOAD,subsequentBusLoad);
                            service.put(TAG_SUBSEQUENT_BUS_DURATION,subsequentBusDuration);
                            service.put(TAG_SUBSEQUENT_BUS_3_LOAD,subsequentBus3Load);
                            service.put(TAG_SUBSEQUENT_BUS_3_DURATION,subsequentBus3Duration);

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

                Collections.sort(serviceList, new Comparator<HashMap<String, String>>() {
                    @Override
                    public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                        Integer lhsBus = Integer.parseInt(lhs.get(TAG_SERVICE_NO));
                        Integer rhsBus = Integer.parseInt(rhs.get(TAG_SERVICE_NO));
                        return lhsBus.compareTo(rhsBus);
                    }
                });
                ListAdapter adapter = new SimpleAdapter(
                        getActivity(), serviceList,
                        R.layout.list_item, new String[] {
                        TAG_SERVICE_NO,
                        TAG_NEXT_BUS_LOAD,
                        TAG_NEXT_BUS_DURATION,
                        TAG_SUBSEQUENT_BUS_LOAD,
                        TAG_SUBSEQUENT_BUS_DURATION,
                        TAG_SUBSEQUENT_BUS_3_LOAD,
                        TAG_SUBSEQUENT_BUS_3_DURATION
                }, new int[] {
                        R.id.service_no,
                        R.id.next_load,
                        R.id.next_eta,
                        R.id.sub_load,
                        R.id.sub_eta,
                        R.id.sub3_load,
                        R.id.sub3_eta
                });

                setListAdapter(adapter);
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
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

            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
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
            switch (position) {
                case 0:
                    return new SearchFragment();
                case 1:
                    return PlaceholderFragment.newInstance(position + 1);
                case 2:
                    return PlaceholderFragment.newInstance(position + 1);
            }
            return null;
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

    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("jsonResult.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
