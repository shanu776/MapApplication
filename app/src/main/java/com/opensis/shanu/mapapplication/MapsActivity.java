package com.opensis.shanu.mapapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.maps.android.PolyUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    TextView textView;
    double latitude;
    double longitude;
    Button button;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        url = getString(R.string.url);
        textView = (TextView) findViewById(R.id.latlong);
        button = (Button) findViewById(R.id.getlatlong);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
       /* checkPermission();*/
        button.setOnClickListener(this);
//calling bus position from server
       Thread t=new Thread(new Runnable() {
           @Override
           public void run() {
               while (true){
                   VehiclePosition v=new VehiclePosition();
                           v.execute();
                   try {
                       Thread.sleep(3000);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }
           }
       });
        t.start();

        DirectionData d=new DirectionData();
        d.execute();
    }

    public void onSearch(View view) throws IOException {
        EditText editText = (EditText) findViewById(R.id.mlocation);
        String location = editText.getText().toString();
        if (location != null && !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(18.51220526, 73.86637056);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in pune"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        checkPermission();
        mMap.setMyLocationEnabled(true);
        LatLng second=new LatLng(18.511788, 73.866177);
        LatLng third=new LatLng(18.510750, 73.868345);
        LatLng forth=new LatLng(18.509204, 73.868205);
       /* mMap.addPolyline(new PolylineOptions().add(
           sydney,second,third,forth
        )
                .width(10)
                .color(Color.RED)
        );*/
    }


    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                }, 10);
                return;
            } else {
               /* configureButton();*/
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  /*  configureButton();*/
                    return;
                }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        textView.setText(latitude + " " + longitude);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                sendLatLong(latitude + "," + longitude);
            }
        });
        t.start();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public void sendLatLong(String latlong) {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url + "getLatLong.htm");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("latlong", latlong));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("message", "location update working");
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, MapsActivity.this);
       /* locationManager.requestLocationUpdates("gps",2000, 0, MapsActivity.this);*/
    }

    Marker vehicleMarker;
    class VehiclePosition extends AsyncTask{
        List<String> list=new ArrayList<>();
        String location;
        Double lat;
        Double lng;


        @Override
        protected void onPreExecute() {
          /*  vehicleMarker=mMap.addMarker(new MarkerOptions().position(new LatLng(18.51883,73.87389)));*/
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                HttpClient httpClient=new DefaultHttpClient();
                HttpPost httpPost=new HttpPost(url+"sendCurrentPosition.htm");
                HttpResponse response=httpClient.execute(httpPost);
                InputStream inputStream=response.getEntity().getContent();
                InputStreamReader reader=new InputStreamReader(inputStream);
                BufferedReader bufferedReader=new BufferedReader(reader);

                while ((location=bufferedReader.readLine())!=null){

                    StringTokenizer st=new StringTokenizer(location,",");
                    while (st.hasMoreElements()){
                        list.add(st.nextToken());
                    }
                    Log.d("location",list.get(0)+"  "+list.get(1));
                    lat=Double.parseDouble(list.get(0));
                    lng=Double.parseDouble(list.get(1));
                    list.clear();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
          if(vehicleMarker != null){
              vehicleMarker.remove();
          }
          if(lat!=null)
           vehicleMarker=mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title("vehicle"));

        }
    }

    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    List<LatLng> latLngs;
    class DirectionData extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });

                GenericUrl url = new GenericUrl("http://maps.googleapis.com/maps/api/directions/json");
                url.put("origin", "18.518715, 73.873026");
                url.put("destination", "wakad");
                url.put("sensor",false);

                HttpRequest request = requestFactory.buildGetRequest(url);
                com.google.api.client.http.HttpResponse httpResponse = request.execute();
                DirectionsResult directionsResult = httpResponse.parseAs(DirectionsResult.class);
                String encodedPoints = directionsResult.routes.get(0).overviewPolyLine.points;
                latLngs = PolyUtil.decode(encodedPoints);

                Log.d("EncodedPoints",encodedPoints);
                Log.d("Direction",latLngs.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            LatLng[] latlngstr=new LatLng[latLngs.size()];
            for(int i=0;i<latLngs.size();i++){
                latlngstr[i]=latLngs.get(i);
               /* Log.d("latlngs", ""+latlngstr[i]);*/
            }
            mMap.addPolyline(new PolylineOptions().add(latlngstr).width(10).color(Color.BLUE));
            mMap.addMarker(new MarkerOptions().position(latLngs.get(0)).title("Start"));
            Marker mark=mMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size()-1)).title("End"));
        }
    }

    public static class DirectionsResult {
        @Key("routes")
        public List<Route> routes;
    }

    public static class Route {
        @Key("overview_polyline")
        public OverviewPolyLine overviewPolyLine;
    }

    public static class OverviewPolyLine {
        @Key("points")
        public String points;
    }



}
