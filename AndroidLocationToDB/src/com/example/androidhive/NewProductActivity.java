package com.example.androidhive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NewProductActivity extends Activity {
	private EditText Longitude;
	private EditText Latitude;
	private EditText inputDesc;
	private TextView provText;
	// Progress Dialog
	private ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();

	private LocationManager locationManager;
	private String provider;
	private MyLocationListener mylistener;
	private Criteria criteria;

	// url to create new product
	private static String url_create_product = "http://192.168.1.101/android_connect/create_product.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_location);

		// Edit Text
		Longitude = (EditText) findViewById(R.id.Longitude);
		Latitude = (EditText) findViewById(R.id.Latitude);
		inputDesc = (EditText) findViewById(R.id.inputDesc);
		provText = (TextView) findViewById(R.id.inputDesc);

		// Create button
		Button btnsave = (Button) findViewById(R.id.save);

		// button click event
		btnsave.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				// creating new product in background thread
				new CreateNewProduct().execute();
			}
		});

		Button btnLocate = (Button) findViewById(R.id.btnLocate);
		btnLocate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				// Get the location manager
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				// Define the criteria how to select the location provider
				criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_COARSE); // default

				// user defines the criteria
				/*
				 * choose.setOnClickListener(new OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { // TODO
				 * Auto-generated method stub if(fineAcc.isChecked()){
				 * criteria.setAccuracy(Criteria.ACCURACY_FINE);
				 * choice.setText("fine accuracy selected"); }else {
				 * criteria.setAccuracy(Criteria.ACCURACY_COARSE);
				 * choice.setText("coarse accuracy selected"); } } });
				 */
				criteria.setCostAllowed(false);
				// get the best provider depending on the criteria
				provider = locationManager.getBestProvider(criteria, false);

				// the last known location of this provider
				Location location = locationManager
						.getLastKnownLocation(provider);

				mylistener = new MyLocationListener();

				if (location != null) {
					mylistener.onLocationChanged(location);
				} else {
					// leads to the settings because there is no last known
					// location
					Intent intent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(intent);
				}
				// location updates: at least 1 meter and 200millsecs change
				locationManager.requestLocationUpdates(provider, 200, 1,
						mylistener);
			}
		});
	}

	/**
	 * Background Async Task to Create new product
	 * */
	class CreateNewProduct extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(NewProductActivity.this);
			pDialog.setMessage("Creating Location..");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/**
		 * Creating product
		 * */
		protected String doInBackground(String... args) {
			String longi = Longitude.getText().toString();
			String lati = Latitude.getText().toString();
			String description = inputDesc.getText().toString();

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("Longitude", longi));
			params.add(new BasicNameValuePair("Latitude", lati));
			params.add(new BasicNameValuePair("description", description));

			// getting JSON Object
			// Note that create product url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_create_product,
					"POST", params);

			// check log cat fro response
			Log.d("Create Response", json.toString());

			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// successfully created product
					Intent i = new Intent(getApplicationContext(),
							AllProductsActivity.class);
					startActivity(i);

					// closing this screen
					finish();
				} else {
					// failed to create product
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
		}

	}

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			// Initialize the location fields

			Latitude.setText(String.valueOf(location.getLatitude()));
			Longitude.setText(String.valueOf(location.getLongitude()));

			String Lat = String.valueOf(location.getLatitude());
			String Long = String.valueOf(location.getLongitude());
			/*String addresss = addressFetch(Long, Lat);
			provText.setText(addresss);*/

			Toast.makeText(NewProductActivity.this, "Location changed!",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Toast.makeText(NewProductActivity.this,
					provider + "'s status changed to " + status + "!",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(NewProductActivity.this,
					"Provider " + provider + " enabled!", Toast.LENGTH_SHORT)
					.show();

		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(NewProductActivity.this,
					"Provider " + provider + " disabled!", Toast.LENGTH_SHORT)
					.show();
		}
	}

		/*	public void addressFetch(double lat, double lng) {
				StringBuilder _homeAddress = null;
		        try{
		            _homeAddress = new StringBuilder();
		            Address address = null;
		            List<Address> addresses = coder.getFromLocation(lat,lng,1);
		            for(int index=0; index<addresses.size(); ++index)
		            {
		                address = addresses.get(index);
		                _homeAddress.append("Name: " + address.getLocality() + "\n");
		                _homeAddress.append("Sub-Admin Ares: " + address.getSubAdminArea() + "\n");
		                _homeAddress.append("Admin Area: " + address.getAdminArea() + "\n");
		                _homeAddress.append("Country: " + address.getCountryName() + "\n");
		                _homeAddress.append("Country Code: " + address.getCountryCode() + "\n");
		                _homeAddress.append("Latitude: " + address.getLatitude() + "\n");
		                _homeAddress.append("Longitude: " + address.getLongitude() + "\n\n");
		            }
		        }
		        catch(Exception e){

		        }*/

}
