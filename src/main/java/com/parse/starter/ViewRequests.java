package com.parse.starter;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequests extends AppCompatActivity implements LocationListener {

    ListView listView;
    ArrayList<String> listViewContent;
    ArrayList<String> usernames;
    ArrayList<Double> latitudes;
    ArrayList<Double> longitudes;
    ArrayAdapter arrayAdapter;

    Location location;

    LocationManager locationManager;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        listView = (ListView) findViewById(R.id.listView);
        listViewContent = new ArrayList<String>();
        usernames = new ArrayList<String>();
        latitudes = new ArrayList<Double>();
        longitudes = new ArrayList<Double>();

        listViewContent.add("Finding nearby requests...");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listViewContent);

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(getApplicationContext(), ViewRiderLocation.class);
                i.putExtra("username", usernames.get(position));
                i.putExtra("latitude", latitudes.get(position));
                i.putExtra("longitude", longitudes.get(position));
                i.putExtra("userLatitude", location.getLatitude());
                i.putExtra("userLongitude", location.getLongitude());
                startActivity(i);

            }
        });


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        locationManager.requestLocationUpdates(provider, 400, 1, this);

        location = locationManager.getLastKnownLocation(provider);

        if (location != null) {


            updateLocation();

        }




    }


    public void updateLocation() {

        final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        ParseUser.getCurrentUser().put("location", userLocation);
        ParseUser.getCurrentUser().saveInBackground();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");

        query.whereNear("requesterLocation", userLocation);
        query.setLimit(100);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e == null) {

                    Log.i("MyApp", objects.toString());

                    if (objects.size() > 0) {

                        listViewContent.clear();
                        usernames.clear();
                        latitudes.clear();
                        longitudes.clear();

                        for (ParseObject object : objects) {

                            if (object.get("driverUsername") == null) {

                                Log.i("MyApp", object.toString());

                                Double distanceInMiles = userLocation.distanceInMilesTo((ParseGeoPoint) object.get("requesterLocation"));

                                Double distanceOneDP = (double) Math.round(distanceInMiles * 10) / 10;

                                listViewContent.add(distanceOneDP.toString() + " miles");

                               usernames.add(object.getString("requesterUsername"));
                               latitudes.add(object.getParseGeoPoint("requesterLocation").getLatitude());
                                longitudes.add(object.getParseGeoPoint("requesterLocation").getLongitude());

                            }

                        }

                        arrayAdapter.notifyDataSetChanged();


                    }


                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_requests, menu);
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

    @Override
    protected void onPause() {
        super.onPause();

        // locationManager.removeUpdates(this);

    }

    @Override
    public void onLocationChanged(Location location) {


        updateLocation();


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
