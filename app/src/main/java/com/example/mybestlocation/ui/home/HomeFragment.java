package com.example.mybestlocation.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.MainActivity;
import com.example.mybestlocation.MapActivity;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.databinding.FragmentHomeBinding;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private EditText latitudeEditText;
    private EditText longitudeEditText;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static final int MAPS_REQUEST_CODE = 1;

    private double currentLatitude;
    private double currentLongitude;
    private boolean newLocationSelected = false;

    private Handler locationUpdateHandler;  // Handler for periodic updates
    private static final int LOCATION_UPDATE_INTERVAL = 20000;  // 20 seconds

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        latitudeEditText = binding.editTextLatitude;
        longitudeEditText = binding.editTextLongitude;
        EditText descriptionEditText = binding.editTextDescription;
        Button addButton = binding.buttonNouveau;
        Button openMapsButton = binding.buttonMap;

        // Check if you have a last known location
        if (!newLocationSelected) {
            Location lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null) {
                currentLatitude = lastKnownLocation.getLatitude();
                currentLongitude = lastKnownLocation.getLongitude();
                updateLocationUI(lastKnownLocation);
            }
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String latitude = latitudeEditText.getText().toString();
                String longitude = longitudeEditText.getText().toString();
                String description = descriptionEditText.getText().toString();

                if (!latitude.isEmpty() && !longitude.isEmpty() && !description.isEmpty()) {
                    addPosition(latitude, longitude, description);
                }
            }
        });

        openMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGoogleMapsWithLocation();
            }
        });

        // Start the periodic location update
        locationUpdateHandler = new Handler();
        locationUpdateHandler.postDelayed(locationUpdateRunnable, LOCATION_UPDATE_INTERVAL);

        return root;
    }

    private Location getLastKnownLocation() {
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Location permissions are granted
            locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

            // Get the last known location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                return lastKnownLocation;
            }
        }

        return null;
    }

    private void updateLocationUI(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            latitudeEditText.setText(String.valueOf(latitude));
            longitudeEditText.setText(String.valueOf(longitude));
        }
    }

    private void addPosition(String latitude, String longitude, String description) {
        String url = MainActivity.url+"/ajout_position.php";
//        String url = baseUrl + "?longitude=" + longitude + "&latitude=" + latitude + "&description=" + description;
//        Position position = new Position(0,latitude,longitude,description);
        AddPositionTask addPositionTask = new AddPositionTask(getContext());
        addPositionTask.execute(url,description,longitude,latitude);
    }

    class AddPositionTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        AlertDialog alertDialog;

        AddPositionTask(Context context) {
            this.context = context;

        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            HashMap<String , String> params = new HashMap<>();
            String url = strings[0];
            params.put("description",strings[1]);
            params.put("longitude",strings[2]);
            params.put("latitude",strings[3]);
            JSONParser jsonParser = new JSONParser();
            JSONObject result = jsonParser.makeHttpRequest(url, "POST", params);
            try {
                if(result.getInt("success") == 1){
                    return  true;
                }
                else{
                    return false;
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Adding Position...");
            dialog.setMessage("Please wait");
            alertDialog = dialog.create();
            alertDialog.show();
        }



        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"success",Toast.LENGTH_LONG);
                    }
                });

            }
            else{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"Failure to add position",Toast.LENGTH_LONG);
                    }
                });
            }

            alertDialog.dismiss();
        }
    }

    private Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            // Periodically update the location
            Location lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null) {
                currentLatitude = lastKnownLocation.getLatitude();
                currentLongitude = lastKnownLocation.getLongitude();
                updateLocationUI(lastKnownLocation);
            }
            // Schedule the next update
            locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
        }
    };

    private void openGoogleMapsWithLocation() {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra("latitude", currentLatitude);
        intent.putExtra("longitude", currentLongitude);
        startActivityForResult(intent, MAPS_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAPS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);

            currentLatitude = latitude;
            currentLongitude = longitude;
            newLocationSelected = true;

            latitudeEditText.setText(String.valueOf(latitude));
            longitudeEditText.setText(String.valueOf(longitude));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationUpdateHandler.removeCallbacks(locationUpdateRunnable);  // Remove the location update handler
        binding = null;
    }
}
