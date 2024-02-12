    package com.example.mybestlocation;

    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.os.Bundle;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.Marker;
    import com.google.android.gms.maps.model.MarkerOptions;

    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import androidx.fragment.app.FragmentActivity;
    import android.os.Handler;

    public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
        private GoogleMap mMap;
        private Marker marker;
        private LocationManager locationManager;
        private LocationListener locationListener;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


            // Initialize the location listener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Handle location changes if needed
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    // Handle status changes if needed
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // Handle provider enabled if needed
                }

                @Override
                public void onProviderDisabled(String provider) {
                    // Handle provider disabled if needed
                }
            };
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Set up a map click listener to allow the user to change the location
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (marker != null) {
                        marker.remove();
                    }

                    double latitude = latLng.latitude;
                    double longitude = latLng.longitude;

                    // Add a marker at the clicked location
                    marker = mMap.addMarker(new MarkerOptions().position(latLng).title("New Location"));

                    // Send the new latitude and longitude back to the HomeFragment
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("latitude", latitude);
                    resultIntent.putExtra("longitude", longitude);
                    setResult(RESULT_OK, resultIntent);
                }
            });



            // Retrieve the user's actual location using a location manager
            Location userLocation = getUserLocation();

            if (userLocation != null) {
                LatLng defaultLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                marker = mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Initial Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
            } else {
                // If unable to retrieve the user's location, you can set a default location
                LatLng defaultLocation = new LatLng(37.7749, -122.4194);
                marker = mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Initial Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
            }

        }

        // Implement the logic to retrieve the user's location using a LocationManager
        private Location getUserLocation() {
            Location lastKnownLocation = null;

            if (checkLocationPermission()) {
                // Get the last known location from the best available provider
                Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                // Choose the more accurate of the two locations, or use the last known location
                if (networkLocation != null && gpsLocation != null) {
                    if (networkLocation.getAccuracy() <= gpsLocation.getAccuracy()) {
                        lastKnownLocation = networkLocation;
                    } else {
                        lastKnownLocation = gpsLocation;
                    }
                } else if (networkLocation != null) {
                    lastKnownLocation = networkLocation;
                } else if (gpsLocation != null) {
                    lastKnownLocation = gpsLocation;
                }
            }

            return lastKnownLocation;
        }

        // Check for location permissions
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;

        private boolean checkLocationPermission() {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, you can proceed with location-related tasks
                return true;
            } else {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return false;
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();


            // Check if locationListener is not null before removing updates
            if (locationListener != null && checkLocationPermission()) {
                locationManager.removeUpdates(locationListener);
            }
        }
    }
