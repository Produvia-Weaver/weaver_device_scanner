/**************************************************************************************************
 * Copyright (c) 2016-present, Produvia, LTD.
 * All rights reserved.
 * This source code is licensed under the MIT license
 **************************************************************************************************/
package produvia.com.scanner;

import com.produvia.sdk.DateTimeFormatterEx;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;



public class DeviceCard extends CustomListItem {

    private JSONObject mDevice;
    private Calendar mLastSeen;
    private ArrayList<String> mDetailsTableEntries;

    private static String extractName(JSONObject device) throws JSONException {
        String name = extractStringFromJson("name", device);
        if(!name.isEmpty())
            return name;
        name = extractStringFromJson("model", device);
        if(!name.isEmpty())
            return name;
        name = extractStringFromJson("iot_device", device);
        if(!name.isEmpty())
            return name;
        name = extractStringFromJson("type", device);
        if(!name.isEmpty())
            return name;
        name = extractStringFromJson("manufacturer", device);
        if(!name.isEmpty())
            return name;
        name = extractStringFromJson("vendor", device);
        if(!name.isEmpty())
            return name;
        name = extractStringFromJson("ip", device);
        if(!name.isEmpty())
            return name;
        return name;
    }

    private static String extractStringFromJson(String field, JSONObject json) throws JSONException{
        if(json.has(field) && json.get(field) != null && !json.getString(field).equals("null")){
            return json.getString(field);
        }
        return "";
    }

    private static String buildStringFromFields(String[] fields, JSONObject json) throws JSONException {
        String str = "";
        for(int i = 0; i < fields.length; i++) {
            String value = extractStringFromJson(fields[i], json);
            if (!value.isEmpty()) {
                if (!str.isEmpty())
                    str += ", ";
                str += value;
            }
        }
        return str;
    }

    private static String extractDescription(JSONObject device) throws JSONException{
        String description = buildStringFromFields( new String[]{"type", "model", "iot_device", "manufacturer", "vendor"}, device);
        return description;
    }

    public DeviceCard(JSONObject device ) throws JSONException {

        super(extractName(device),
                extractDescription(device),
                getIcon(extractStringFromJson("type", device)),
                false,
                false);
        mDevice = device;
        updateLastSeen();
        updateTableInfo();



    }

    private void updateTableInfo() throws JSONException{
        mDetailsTableEntries = new ArrayList<>();

        String field = extractStringFromJson("ip", mDevice);
        if(!field.isEmpty()){
            if(isBluetoothDevice())
                mDetailsTableEntries.add("ADDRESS");
            else
                mDetailsTableEntries.add("IP");
            mDetailsTableEntries.add(field);
        }
        field = extractStringFromJson("mac", mDevice);
        if(!field.isEmpty()){
            mDetailsTableEntries.add("MAC");
            mDetailsTableEntries.add(field);
        }

        //now add the services:
        if(!mDevice.has("services"))
            return;
        JSONObject services = mDevice.getJSONObject("services");
        for(Iterator<String> iter = services.keys(); iter.hasNext();) {
            String service_id = iter.next();
            JSONObject service = services.getJSONObject(service_id);
            //if a device card is already present - just merge the data:
            field = extractStringFromJson("service", service);
            if(!field.isEmpty()){
                mDetailsTableEntries.add(field);
                mDetailsTableEntries.add(buildStringFromFields(new String[]{"description"}, service));
            }
        }

    }

    public ArrayList<String>getTableEntries(){
        return mDetailsTableEntries;

    }


    public static Calendar getLastSeenFromString(String last_seen_str){
        if(last_seen_str == null || last_seen_str.isEmpty())
            return Calendar.getInstance();
        else
            return DateTimeFormatterEx.getCalendarFromISO(last_seen_str);
    }

    private void updateLastSeen() throws JSONException{
        this.mLastSeen = getLastSeenFromString(mDevice.getString("last_seen"));
    }


    public String getId() throws JSONException {
        return extractStringFromJson("id", mDevice);
    }


    /*********************************************************************************/

    private static int getIcon(String type)
    {

        String tmp_type = type.toLowerCase(Locale.ENGLISH);
        if(tmp_type.indexOf("iphone") >= 0 ) {
            return R.drawable.ic_devices_iphone;
        }
        if(tmp_type.indexOf("ipad") >= 0 || tmp_type.indexOf("tablet") >= 0 || tmp_type.indexOf("ereader") >= 0) {
            return R.drawable.ic_devices_ipad;
        }
        if(tmp_type.indexOf("chromecast") >= 0 )
            return R.drawable.chromecast;
        if(tmp_type.indexOf("apple-tv") >= 0 || tmp_type.indexOf("apple tv") >= 0 || tmp_type.indexOf("appletv") >= 0 )
            return R.drawable.appletv;
        if(tmp_type.indexOf("philips") >= 0 && tmp_type.indexOf("hue") >= 0)
            return R.drawable.ic_light_bulb;
        if(tmp_type.indexOf("mediarenderer") >= 0 )
            return R.drawable.ic_devices_television;
        if( tmp_type.indexOf("settopbox") >= 0 || tmp_type.indexOf("player") >= 0 ||
                tmp_type.indexOf("media") >= 0 || tmp_type.indexOf("streamer") >= 0 )
            return R.drawable.streamer;
        if(tmp_type.indexOf("iphone") >= 0 )
            return R.drawable.ic_devices_iphone;
        if(tmp_type.indexOf("mobile/android") >= 0 )
            return R.drawable.ic_devices_iphone;
        if(tmp_type.indexOf("phone") >= 0 || tmp_type.indexOf("mobile") >= 0) {
            return R.drawable.ic_devices_iphone;
        }
        if(tmp_type.indexOf("laptop") >= 0 || tmp_type.indexOf("macbook") >= 0 ) {
            return R.drawable.ic_devices_laptop;
        }
        if(tmp_type.indexOf("camera") >= 0 || tmp_type.indexOf("ipcam") >= 0 )
            return R.drawable.ipcam;
        if(tmp_type.indexOf("print") >= 0 || tmp_type.indexOf("scanner") >= 0)
            return R.drawable.printer;
        if(tmp_type.indexOf("computer") >= 0 || tmp_type.indexOf("workstation") >= 0  || tmp_type.indexOf("genericserver") >= 0 || tmp_type.indexOf("windowsserver") >= 0)
            return R.drawable.desktop;
        if(tmp_type.indexOf("router") >= 0 )
            return R.drawable.router;
        if(tmp_type.indexOf("adapter") >= 0 )
            return R.drawable.router;
        if( tmp_type.indexOf("access")  >= 0 || tmp_type.indexOf("internet")   >= 0 ||
                tmp_type.indexOf("gateway") >= 0 || tmp_type.indexOf("wifidevice") >= 0 ||
                tmp_type.indexOf("embeddednetdevice") >= 0 || tmp_type.indexOf("networking") >= 0
                || tmp_type.indexOf("networkappliance") >= 0)
            return R.drawable.router;
        if(tmp_type.indexOf("television") >= 0 || tmp_type.indexOf("smart-tv") >= 0 )
            return R.drawable.ic_devices_television;
        if( tmp_type.indexOf("minipc") >= 0 || tmp_type.indexOf("dial") >= 0 || tmp_type.indexOf("nas") >= 0 ||
                tmp_type.indexOf("dvr") >= 0)
            return R.drawable.nas;
        if( tmp_type.indexOf("audio") >= 0 )
            return R.drawable.speaker;
        if( tmp_type.indexOf("gameconsole") >= 0 )
            return R.drawable.gameconsole;
        if( tmp_type.indexOf("hub") >= 0)
            return R.drawable.hub;
        if( tmp_type.indexOf("videoconf") >= 0)
            return R.drawable.video;
        if( tmp_type.indexOf("security") >= 0)
            return R.drawable.alarm;
        if( tmp_type.indexOf("smart-watch") >= 0)
            return R.drawable.watch;
        if( tmp_type.indexOf("irrigation") >= 0)
            return R.drawable.waterdrop;

        return R.drawable.box;

    }


    public void updateInfo(JSONObject device) throws JSONException {
        //merge the two devices and their services:
        JSONObject old_services = null;
        JSONObject new_services = null;
        if(mDevice.has("services"))
            old_services = mDevice.getJSONObject("services");
        if(device.has("services"))
            new_services = device.getJSONObject("services");

        String services_str = mergeServices(old_services, new_services);

        mDevice = device;

        if(services_str != null){
            mDevice.put("services", new JSONObject(services_str));
        }
    }

    private static String mergeServices(JSONObject s1, JSONObject s2) throws JSONException {
        if(s1 == null && s2 == null)
            return null;
        if(s1 == null)
            return s2.toString();
        if(s2 == null)
            return s1.toString();

        //ok they're both not null - merge them:
        for(Iterator<String> iter = s1.keys(); iter.hasNext();) {
            String service_id = iter.next();
            if(s2.has(service_id))
                continue;
            s2.put(service_id, s1.getJSONObject(service_id));
        }
        return s2.toString();
    }

    @Override
    public String getStatus() {
        String status = getLastSeenAsString();
        return status;
    }


    protected Long getSecondsSinceLastSeen(){
        if( mLastSeen == null )
            return null;
        Calendar now = Calendar.getInstance();
        return Math.abs((now.getTimeInMillis() - mLastSeen.getTimeInMillis()) / 1000);
    }

    public String getLastSeenAsString()
    {
        Long seconds = getSecondsSinceLastSeen();
        if( seconds == null )
            return "";

        if( seconds < 60 || seconds <= (3*60) ) //less than 60 seconds
            return "now";
        if( seconds <= (5*60) ) //less than 5 minutes
        {
            long minutes = seconds/60;
            if(minutes == 1)
                return "a minute ago";
            else
                return seconds/60 + " minutes ago";
        }
        if( seconds < (60*60*24)) //less than a day
            return DateTimeFormatterEx.getTime(DateTimeFormatterEx.FORMAT_HOURS, mLastSeen);
        if( seconds < (60*60*24)*31*3 ) //less than 3 months
            return DateTimeFormatterEx.getTime(DateTimeFormatterEx.FORMAT_MONTH, mLastSeen);
        //anything larger
        return DateTimeFormatterEx.getTime(DateTimeFormatterEx.FORMAT_YEAR, mLastSeen);
    }

    public Calendar getLastSeen(){
        return mLastSeen;
    }

    public int getTransportIcon(){
        return isBluetoothDevice()? R.drawable.bluetooth: R.drawable.wifi;
    }


    public boolean isBluetoothDevice(){
        try {
            String ip = mDevice.getString("ip");
            if(ip.contains(":"))
                return true;
        } catch (JSONException e) {

        }
        return false;

    }


}
