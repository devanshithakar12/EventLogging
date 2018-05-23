package com.example.devanshithakar.eventloggingproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    public JSONObject jo = null;
    public JSONArray ja = null;

    private final String TAG1 = "TESTGPS";
    public static TextView latText;
    public static TextView longText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
        String date = format.format(calendar.getTime());

        TextView textView = findViewById(R.id.textView10);
        textView.setText(date);


        Calendar calendar2 = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("hh:mm:ss a");
        String strDate = mdformat.format(calendar2.getTime());
        TextView textView2 = findViewById(R.id.timeText);
        textView2.setText(strDate);


        // References for the widgets
        latText = findViewById(R.id.latitude);
        longText = findViewById(R.id.longitude);

        // This statement requests permission to the user.
        // If permissions are not set in the Manifest file, then access
        // will automatically be denied. Once the user chooses an option,
        // onRequestPermissionsResult is called.
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                99);


        @SuppressLint("MissingPermission")
        // A reference to the location manager. The LocationManager has already
                // been set up in MyService, we're just getting a reference here.
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l;
        // Go through the location providers starting with GPS, stop as soon
        // as we find one.
        for (int i = providers.size() - 1; i >= 0; i--) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            l = lm.getLastKnownLocation(providers.get(i));
                    double precision = Math.pow(10,6);
                    longText.setText(Double.toString((int)(precision * l.getLongitude())/precision));

                    latText.setText(Double.toString((int)(precision * l.getLatitude())/precision));
                    if (l != null) break;
                }

        Intent i = getIntent();
        String title = i.getStringExtra("first");
        String description = i.getStringExtra("second");

        final TextView t = (TextView)findViewById(R.id.textView3);
        final TextView d = (TextView)findViewById(R.id.textView4);

        Button deleteEvent = (Button)findViewById(R.id.deleteButton);

        t.setText(title);
        d.setText(description);

        try{
            File f = new File(getFilesDir(), "file.ser");
            FileInputStream fi = new FileInputStream(f);
            ObjectInputStream o = new ObjectInputStream(fi);
            // Notice here that we are de-serializing a String object (instead of
            // a JSONObject object) and passing the String to the JSONObject’s
            // constructor. That’s because String is serializable and
            // JSONObject is not. To convert a JSONObject back to a String, simply
            // call the JSONObject’s toString method.
            String j = null;
            try{
                j = (String) o.readObject();
            }
            catch(ClassNotFoundException c){
                c.printStackTrace();
            }
            try {
                jo = new JSONObject(j);
                ja = jo.getJSONArray("data");
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        catch(IOException e){
            // Here, initialize a new JSONObject
            jo = new JSONObject();
            ja = new JSONArray();
            try{
                jo.put("data", ja);
            }
            catch(JSONException j){
                j.printStackTrace();
            }
        }
        deleteEvent.setOnClickListener(new Button.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onClick(View v){

                    String firstText = t.getText().toString();
                    // String secondText = d.getText().toString();
                    String temp=null;

                    for(int j = 0; j < ja.length(); j++){

                        ListData ld = new ListData();
                        try {

                            temp= ja.getJSONObject(j).getString("first");


                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        if (firstText.equals(temp)) {
                            ja.remove(j);
//                            if (ja.length() == 0) {
//                                //System.out.println("Nothing to Show");
//
//                            }
                        }

                    }



                    // write the file
                    try{
                        File f = new File(getFilesDir(), "file.ser");
                        FileOutputStream fo = new FileOutputStream(f);
                        ObjectOutputStream o = new ObjectOutputStream(fo);
                        String j = jo.toString();
                        o.writeObject(j);
                        o.close();
                        fo.close();
                    }
                    catch(IOException e){

                    }

                    //pop the activity off the stack
                    Intent i = new Intent(DetailActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                        }



        });

    }

    // This class implements OnRequestPermissionsResultCallback, so when the
    // user is prompted for location permission, the below method is called
    // as soon as the user chooses an option.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG1, "callback");
        switch (requestCode) {
            case 99:
                // If the permissions aren't set, then return. Otherwise, proceed.
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                                , 10);
                    }
                    Log.d(TAG1, "returning program");
                    return;
                }
                else{
                    // Create Intent to reference MyService, start the Service.
                    Log.d(TAG1, "starting service");
                    Intent i = new Intent(this, MyService.class);
                    if(i==null)
                        Log.d(TAG1, "intent null");
                    else{
                        startService(i);
                    }

                }
                break;
            default:
                break;
        }
    }
    // Used for debugging. Below method is extraneous
    @SuppressLint("MissingPermission")
    public void doSomething(View view){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l;
        for (int i=providers.size()-1; i>=0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            longText.setText(Double.toString(l.getLongitude()));
            latText.setText(Double.toString(l.getLatitude()));
            if (l != null) break;
        }
    }


    }

