package org.altbeacon.beaconreference;

import java.io.BufferedReader;
import java.util.Random;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import android.app.Activity;

import android.content.Context;
import android.net.http.HttpResponseCache;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

public class RangingActivity extends Activity {
    static Beacon[] array = new Beacon[200];
//    static final Random userid = new Random(System.currentTimeMillis());
//    static final int userrealid = userid.nextInt();
    static final int userrealid = 1;
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconMan
        ger = BeaconManager.getInstanceForApplication(this);
   // protected Integer n = 1;
    Comparator<Beacon> distanceComparator = Comparator.comparing(Beacon::getDistance);
    TreeSet<Beacon> beaconsByDistance = new TreeSet<>(distanceComparator);
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);

        // webview
        webView = (WebView)findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://embershan.github.io/mdp_website/");

        // enable zoom and pinch controls
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false); //hide the zoom buttons
        // must enable javascript for the react website
        webSettings.setJavaScriptEnabled(true);

    }


    @Override
    protected void onResume() {
        super.onResume();
        RangeNotifier rangeNotifier = new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Iterator<Beacon> iterator = beacons.iterator();
                Beacon temp;
                String check;
//                count = 0;
                while(iterator.hasNext()){

                        temp = iterator.next();
                        check = temp.getBluetoothAddress();
                        if(check.charAt(0) == 'D' && check.charAt(1) =='D') {
                            if(temp!=null) {
                                if(!hasduplicate(beaconsByDistance,temp)){
                                    beaconsByDistance.add(temp);
                                }
                            }
//                            logToDisplay("" + count);
                            if(beaconsByDistance.size() >= 3) {
                                Beacon tt;
                                int i = 0;
                                Iterator<Beacon> it = beaconsByDistance.iterator();
                                logToDisplay(""+beaconsByDistance.size());
                                while(it.hasNext()) {
                                    tt= it.next();
                                    logToDisplay(tt.getBluetoothName());
                                    logToDisplay(tt.getBluetoothAddress());
                                }
                                postData(beaconsByDistance.toArray(array),userrealid);
                                beaconsByDistance.clear();

                            }
                        }
                }


            }


        };

        beaconManager.addRangeNotifier(rangeNotifier);
        beaconManager.startRangingBeacons(BeaconReferenceApplication.wildcardRegion);
    }
        @Override
    protected void onPause() {
        super.onPause();
        beaconManager.stopRangingBeacons(BeaconReferenceApplication.wildcardRegion);
        beaconManager.removeAllRangeNotifiers();
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                editText.append(line+"\n");
            }
        });
    }

    private void postData(Beacon[] array, int userrealid){
        new Thread(() -> {
            JsonObject json1 = new JsonObject();
            json1.addProperty("userId", userrealid);
            json1.addProperty("beacon1Distance", array[0].getDistance());
            json1.addProperty("beacon1Id", Integer.parseInt(array[0].getBluetoothName()));
            json1.addProperty("beacon1Address", array[0].getBluetoothAddress());
            json1.addProperty("beacon2Distance", array[1].getDistance());
            json1.addProperty("beacon2Id", Integer.parseInt(array[1].getBluetoothName()));
            json1.addProperty("beacon2Address",  array[1].getBluetoothAddress());
            json1.addProperty("beacon3Distance", array[2].getDistance());
            json1.addProperty("beacon3Id", Integer.parseInt(array[2].getBluetoothName()));
            json1.addProperty("beacon3Address", array[2].getBluetoothAddress());
            json1.addProperty("currentX", -30);
            json1.addProperty("currentY", -120);
            logToDisplay(json1.toString());
            try {
                URL url = new URL("https://mdpcasinoapi.azurewebsites.net/api/users/1");
                HttpURLConnection connect1 = (HttpURLConnection) url.openConnection();
                connect1.setDoOutput(true);
                connect1.setDoInput(true);
                connect1.setRequestMethod("PUT");
                connect1.setRequestProperty("Content-Type", "application/json");
                connect1.setUseCaches(false);
                connect1.connect();
                DataOutputStream wr = new DataOutputStream(connect1.getOutputStream());
                wr.writeBytes(json1.toString());
                wr.flush();
                wr.close();
                int response = connect1.getResponseCode();
                String rresoinse = String.valueOf(response);
                logToDisplay(rresoinse);
                if (response == HttpURLConnection.HTTP_OK) {
                    logToDisplay("" + response);
                    logToDisplay("send");
                    System.out.println(response);
                    InputStream input = connect1.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(input));
                    String line = null;
                    StringBuffer sb = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    JSONObject reinfo = new JSONObject(sb.toString());
                    logToDisplay(reinfo.get("info").toString());
                } else {
                    System.out.println(response);
                }
            } catch (Exception e) {
                logToDisplay(String.valueOf(e));
            }
        }).start();
    }

//    private int partition(@NonNull Beacon[] bec, int low, int high){
//        Beacon pivot = bec[high];
//        int i = (low -1);
//        for(int j = low; j<=high -1; j++){
//            logToDisplay(bec[j].getBluetoothAddress());
//            if(bec[j].getDistance()<= pivot.getDistance()){
//                i++;
//                Beacon temp = bec[i];
//                bec[i] = bec[j];
//                bec[j]= temp;
//            }
//        }
//        Beacon temp = bec[i+1];
//        bec[i+1] = bec[high];
//        bec[high]= temp;
//        return i+1;
//    }
//
//    private void quicksort(@NonNull Beacon[] bec, int low, int high){
//        if(bec[0] == null){
//            return;
//        }
//        if(low < high){
//            int pivotin = partition(bec,low,high);
//            quicksort(bec,low,pivotin -1);
//            quicksort(bec,pivotin+1, high);
//        }
//    }
     private boolean hasduplicate(TreeSet<Beacon> bbb, Beacon b){
         Beacon tebeacon;
         Iterator<Beacon> it1= bbb.iterator();
         while(it1.hasNext()){
             tebeacon = it1.next();
             if(tebeacon.getBluetoothAddress().equals(b.getBluetoothAddress())){
                 return true;
             }
         }
         return false;
     }


}

