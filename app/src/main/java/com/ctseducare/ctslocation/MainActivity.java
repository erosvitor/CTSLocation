package com.ctseducare.ctslocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView fldLatitude;
    private TextView fldLongitude;

    private TextView fldPostalCode;
    private TextView fldStreet;
    private TextView fldNumber;
    private TextView fldDistrict;
    private TextView fldCity;
    private TextView fldState;
    private TextView fldCountry;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        fldLatitude = findViewById(R.id.fldLatitude);
        fldLongitude = findViewById(R.id.fldLongitude);

        fldPostalCode = findViewById(R.id.fldPostalCode);
        fldStreet = findViewById(R.id.fldStreet);
        fldNumber = findViewById(R.id.fldNumber);
        fldDistrict = findViewById(R.id.fldDistrict);
        fldCity = findViewById(R.id.fldCity);
        fldState = findViewById(R.id.fldState);
        fldCountry = findViewById(R.id.fldCountry);

        Button btnGPSLocation = findViewById(R.id.btnUpdateLocation);
        btnGPSLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationData();
            }
        });

        getLocationData();
    }

    private void getLocationData() {
        locationManager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isLocationEnabled) {
            registerLocationListener();
        } else {
            activeLocation();
        }
    }

    private void registerLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                fldLatitude.setText(String.valueOf(location.getLatitude()));
                fldLongitude.setText(String.valueOf(location.getLongitude()));

                LocationData locationData = getAddressInfo(location.getLatitude(), location.getLongitude());
                if (locationData != null) {
                    fldPostalCode.setText(locationData.getPostalCode());
                    fldStreet.setText(locationData.getStreet());
                    fldNumber.setText(locationData.getNumber());
                    fldDistrict.setText(locationData.getDistrict());
                    fldCity.setText(locationData.getCity());
                    fldState.setText(locationData.getState());
                    fldCountry.setText(locationData.getCountry());
                }
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
        };

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }


    // Este evento ocorre quando o usuário permite que o aplicativo tenha acesso a 'Localização' (neste ponto a 'Localização' já esta ativa)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        }
    }


    private void activeLocation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Localização não esta ativa");
        alertDialog.setMessage("Você deseja ativar a Localização para continuar usando este aplicativo?");
        alertDialog.setPositiveButton("Ativar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, 10);
            }
        });
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    // Este evento ocorre quando o usuário ativa a 'Localização'
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 10) {
            registerLocationListener();
        }
    }


    private LocationData getAddressInfo(double latitude, double longitude) {
        LocationData locationData = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                locationData = new LocationData();
                locationData.setPostalCode(addresses.get(0).getPostalCode());
                locationData.setStreet(addresses.get(0).getThoroughfare());
                locationData.setNumber(addresses.get(0).getSubThoroughfare());
                locationData.setDistrict(addresses.get(0).getSubLocality());
                locationData.setCity(addresses.get(0).getSubAdminArea());
                locationData.setState(addresses.get(0).getAdminArea());
                locationData.setCountry(addresses.get(0).getCountryName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationData;
    }

    //----------------------------------------------------------------------------------------------------
    // MENU
    //----------------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    //@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mmiAbout:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //----------------------------------------------------------------------------------------------------
    // ABOUT
    //----------------------------------------------------------------------------------------------------
    private void showAbout() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }

}