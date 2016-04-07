/**************************************************************************************************
 * Copyright (c) 2016-present, Produvia, LTD.
 * All rights reserved.
 * This source code is licensed under the MIT license
 **************************************************************************************************/
package produvia.com.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.produvia.sdk.WeaverSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import produvia.com.weaverandroidsdk.WeaverSdkApi;


/**
 * An activity representing a list of lighting services. This activity
 * is the main activity - it first scans for local services
 * and fetches the scanned services from the SDK
 * The activity displays the list in a fragment containing a RecyclerView
 */
public class DevicesActivity extends Activity implements DevicesFragment.Callbacks, WeaverSdk.WeaverSdkCallback{

    private static final long MINIMUM_TIME_BETWEEN_SCANS_MILLIS = (1000*60*2);//wait 2 minutes between scans
    private static final int MAX_SCAN_CYCLES = 2;
    //let's not show services we haven't seen in two weeks:
    public static boolean mErrorOccurred = false;
    public static String mErrorMessage = "";

    private static int mScanCycleCounter = 0;
    private static Calendar mLastScanStartedAt = null;
    private boolean mActivityPaused = true;

    private boolean mShowScanProgress = false;


    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private DevicesFragment listFragment = null;
    private CustomRecyclerAdapter mCategoryListAdapter;
    public static ArrayList<CustomListItem> mDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mErrorOccurred = false;
        mErrorMessage = "";
        setContentView(R.layout.activity_devices);

        listFragment = new DevicesFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment, listFragment);
        transaction.addToBackStack(null);


        // Commit the transaction
        transaction.commit();
        //Start running the discovery service in the background
        //any discovered services will be reported on the onTaskUpdate callback:
        boolean run_discovery = true;
        Calendar time_now = Calendar.getInstance();
        if(mScanCycleCounter > 0 && mLastScanStartedAt != null ) {

            if (time_now.getTimeInMillis() - mLastScanStartedAt.getTimeInMillis() < MINIMUM_TIME_BETWEEN_SCANS_MILLIS)
                run_discovery = false;
        }
        if( run_discovery ) {

            mLastScanStartedAt = time_now;
            showScanProgress(true);
            WeaverSdkApi.discoveryService(this, true);
        }
        //fetch the services that have already been discovered in previous scans
        //these services will be returned in the onTaskCompleted callback:
        WeaverSdkApi.servicesGet(this, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mActivityPaused = false;
    }

    @Override
    protected void onPause() {
        mActivityPaused = true;
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent browserIntent;
        switch (item.getItemId()) {
            case R.id.developer:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.weavingthings.com/"));
                startActivity(browserIntent);
                return true;
            case R.id.get_source:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Produvia-Weaver/"));
                startActivity(browserIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /**
     * Callback method from {@link DevicesFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(CustomListItem item, View v, int position) {

    }

    @Override
    public void onViewCreated(CustomRecyclerAdapter adapter) {
        mCategoryListAdapter = adapter;
        if (mCategoryListAdapter == null)
            return;
        mCategoryListAdapter.notifyDataSetChanged();
    }


    /******************************************************************
     * Starts up the LoginActivity - called on user sign out
     *****************************************************************/
    protected void runWelcomeActivity() {
        WeaverSdkApi.discoveryService(this, false);
        Intent intent = new Intent(DevicesActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    /*********************************************************************
     * The WeaverSdk callback indicating that a task has been completed:
     *********************************************************************/
    @Override
    public void onTaskCompleted(final int flag, final JSONObject response) {
        if (response== null || mActivityPaused)
            return;
        try {

            if (response.has("responseCode") && response.getInt("responseCode") == 401) {
                //unauthorized:
                runWelcomeActivity();
            }

            switch (flag) {
                case WeaverSdk.ACTION_USER_LOGOUT:
                    runWelcomeActivity();
                    break;

                case WeaverSdk.ACTION_SERVICES_GET:
                    if (response.getBoolean("success")) {
                        handleReceivedServices(response.getJSONObject("data"));
                    }
                    break;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*********************************************************************
     * The WeaverSdk callback indicating that a task update occurred.
     * for example, a new service was discovered in the current network:
     *********************************************************************/
    @Override
    public void onTaskUpdate(int flag, JSONObject response) {
        if(response == null || mActivityPaused)
            return;
        try {
            //this flag indicates that a new service was discovered in the scan:
            if (flag == WeaverSdk.ACTION_SERVICES_SCAN) {
                if (response.getBoolean("success")) {
                    handleReceivedServices(response.getJSONObject("data"));
                }
            }
            //when tha scan is running - it'll provide general state information from time to time:
            else if (flag == WeaverSdk.ACTION_SCAN_STATUS) {
                if (response.getBoolean("success")) {
                    if(response.getString("info").equals("Scan running")){
                        mLastScanStartedAt = Calendar.getInstance();
                        showScanProgress(true);
                    }else{
                        mScanCycleCounter += 1;
                        showScanProgress(false);
                        //if we haven't found any light services - we'll show an error message:
                        //if we finished the scan - check if we found any devices:
                        if (mScanCycleCounter > 0 && (mDevices == null || mDevices.size() <= 0)) {
                            setErrorMessage("Weaver didn't detect any services in the network\nPlease make sure you're connected to the wifi\nand restart the app");
                            //stop the scan:
                            WeaverSdkApi.discoveryService( null, false );
                            return;
                        }
                        //stop the discovery service after max scan cycles:
                        if(mScanCycleCounter >= MAX_SCAN_CYCLES)
                            WeaverSdkApi.discoveryService( null, false );


                    }
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }


    }

    /*
       When a new service is discovered - we merge the device and services data:
     */
    private void handleReceivedServices(JSONObject data) throws JSONException {

        updateServiceDeviceDatabase(data);
    }

    private int addNetworkCard(String network_name, String network_id, boolean user_inside_network){
        CustomListItem cli = new CustomListItem(network_name, network_id,
                R.drawable.icon_transparent, true, false);

        int idx = mDevices.size();
        if(user_inside_network) {
            cli.setStatus("inside network");
            idx = 0;
        }


        mDevices.add(idx, cli);
        notifyDataSetChanged();
        return idx;

    }

    private void updateServiceDeviceDatabase(JSONObject data) {
        try {
            //first add the services to the devices:
            JSONArray services = data.getJSONArray("services");
            for(int i = 0; i < services.length(); i++){
                JSONObject service = services.getJSONObject(i);
                String device_id = service.getString("device_id");
                JSONObject device = data.getJSONObject("devices_info").getJSONObject(device_id);
                if(!device.has("services"))
                    device.put("services", new JSONObject());
                device.getJSONObject("services").put(service.getString("id"), service);
            }

            JSONObject devices = data.getJSONObject("devices_info");
            //loop over the devices and merge them into the device display:
            for(Iterator<String> iter = devices.keys(); iter.hasNext();) {
                String device_id = iter.next();
                JSONObject device = devices.getJSONObject(device_id);
                //if a device card is already present - just merge the data:
                boolean found = false;
                int network_card_idx = -1;
                for(int i = 0; i < mDevices.size(); i++){
                    CustomListItem cli = mDevices.get(i);
                    if( cli instanceof DeviceCard && ((DeviceCard)cli).getId().equals(device_id)) {
                        ((DeviceCard) cli).updateInfo(device);
                        found = true;
                        break;
                    } else if(cli.getDescription().equals(device.getString("network_id"))){
                        network_card_idx = i;
                    }
                }


                if(!found) {
                    if(network_card_idx < 0){
                        JSONObject network= data.getJSONObject("networks_info").getJSONObject(device.getString("network_id"));
                        String name = "";
                        if(network.has("name") && network.getString("name") != null)
                            name = network.getString("name");
                        network_card_idx = addNetworkCard(name, device.getString("network_id"), network.getBoolean("user_inside_network"));
                    }
                    network_card_idx += 1;
                    //find the correct index for the card sorted by last seen:
                    for(; network_card_idx < mDevices.size(); network_card_idx++){
                        CustomListItem cli = mDevices.get(network_card_idx);
                        if(!(cli instanceof DeviceCard))
                            break;
                        if(((DeviceCard) cli).getLastSeen().compareTo(DeviceCard.getLastSeenFromString(device.getString("last_seen")))< 0)
                            break;
                    }


                    DeviceCard dc = new DeviceCard(device);
                    mDevices.add(network_card_idx, dc);
                }
            }
            notifyDataSetChanged();

        }catch (JSONException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    private void notifyDataSetChanged() {
        if (mCategoryListAdapter == null)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCategoryListAdapter.notifyDataSetChanged();
            }
        });

        showScanProgress(null);
    }


    protected void showScanProgress(Boolean progress) {

        if(progress == null)
            progress = mShowScanProgress;
        mShowScanProgress = progress;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View pbarLayout = findViewById(R.id.discovery_progress_layout);
                if(pbarLayout == null)
                    return;

                if(mShowScanProgress) {
                    if (pbarLayout.getVisibility() == View.GONE)
                        pbarLayout.setVisibility(View.VISIBLE);
                } else {
                    if (pbarLayout.getVisibility() != View.GONE)
                        pbarLayout.setVisibility(View.GONE);
                }
            }
        });
    }



    public void promptLogin(final JSONObject loginService, final JSONObject responseData) {

        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String type = loginService.getString("type");
                    //there was a login error. login again
                    if (type.equals(WeaverSdk.FIRST_LOGIN_TYPE_NORMAL)) {
                        //prompt for username and password and retry:
                        promptUsernamePassword(loginService, responseData, false, null);

                    } else if(type.equals(WeaverSdk.FIRST_LOGIN_TYPE_KEY)) {

                        promptUsernamePassword(loginService, responseData, true, loginService.getString("description"));


                    }
                    else if (type.equals(WeaverSdk.FIRST_LOGIN_TYPE_PRESS2LOGIN)) {
                        //prompt for username and password and retry:
                        int countdown = loginService.has("login_timeout")? loginService.getInt("login_timeout"): 15;
                        final AlertDialog alertDialog = new AlertDialog.Builder(DevicesActivity.this).create();
                        alertDialog.setTitle(loginService.getString("description"));
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setMessage(loginService.getString("description") + "\n" + "Attempting to login again in " + countdown + " seconds...");
                        alertDialog.show();   //

                        new CountDownTimer(countdown * 1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                try {
                                    alertDialog.setMessage(loginService.getString("description") + "\n" + "Attempting to login again in " + millisUntilFinished / 1000 + " seconds...");
                                } catch (JSONException e) {

                                }
                            }

                            @Override
                            public void onFinish() {
                                alertDialog.dismiss();
                                new Thread(new Runnable() {
                                    public void run() {

                                        try {
                                            JSONArray services = new JSONArray();
                                            services.put(loginService);
                                            responseData.put("services", services);
                                            WeaverSdkApi.servicesSet(DevicesActivity.this, responseData);
                                        }catch (JSONException e){

                                        }
                                    }
                                }).start();

                            }
                        }.start();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        });

    }



    public void promptUsernamePassword(final JSONObject loginService,
                                       final JSONObject responseData,
                                       final boolean isKey,
                                       String description ) throws JSONException {

        LayoutInflater li = LayoutInflater.from(DevicesActivity.this);
        View promptsView = li.inflate(R.layout.prompt_userpass, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DevicesActivity.this);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.pu_username);
        final EditText passInput = (EditText) promptsView.findViewById(R.id.pu_password);
        //if it's a key type input hide the password field:
        if(isKey) {
            passInput.setVisibility(View.GONE);
            userInput.setText(loginService.getJSONObject("properties").getString("key"));
            userInput.setHint("Enter key");
        } else{
            userInput.setText(loginService.getJSONObject("properties").getString("username"));
            passInput.setText(loginService.getJSONObject("properties").getString("password"));
        }


        final TextView prompt_user_pass = (TextView)promptsView.findViewById(R.id.user_pass_title);


        String name = responseData.getJSONObject("devices_info").getJSONObject(loginService.getString("device_id")).getString("name");

        String message;
        if(description == null) {
            message = "Enter " +
                    name + "'s username and password.";
        }else{
            message = description;
        }
        message += "\n(if it's disconnected just press cancel)";

        prompt_user_pass.setText(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setNegativeButton("Go", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String username = (userInput.getText()).toString();
                String password = (passInput.getText()).toString();
                try {
                    if (isKey) {
                        loginService.getJSONObject("properties").put("key", username);

                    } else {
                        loginService.getJSONObject("properties").put("username", username);
                        loginService.getJSONObject("properties").put("password", password);
                    }
                    //stick the service into the response data structure and set the service:
                    JSONArray services = new JSONArray();
                    services.put(loginService);
                    responseData.put("services", services);
                    WeaverSdkApi.servicesSet(DevicesActivity.this, responseData);
                } catch (JSONException e) {
                }

            }
        }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    public void setErrorMessage(String error){
        mErrorOccurred = true;
        mErrorMessage = error;
        if (listFragment != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listFragment.showError();
                }
            });
        }
    }

}
