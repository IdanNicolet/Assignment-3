package com.matchrace.matchrace.classes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.matchrace.matchrace.R;
import com.matchrace.matchrace.modules.JsonReader;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * AsyncTask for getting the buoy's locations from DB and adding them to the google map.
 *
 */
public class GetBuoysTask extends AsyncTask<String, Integer, Map<String, LatLng>> {

	// Application variables.
	private String name = "", event = "";
	private Circle[] buoyRadiuses = new Circle[C.MAX_BUOYS];

	// Views.
	private GoogleMap googleMap;

	public GetBuoysTask(String name, GoogleMap googleMap, Circle[] buoyRadiuses, String event) {
		super();
		this.name = name;
		this.googleMap = googleMap;
		this.buoyRadiuses = buoyRadiuses;
		this.event = event;
	}

	protected Map<String, LatLng> doInBackground(String... urls) {
		Map<String, LatLng> buoysLatLng = new HashMap<String, LatLng>();
		try {
            String m = C.URL_CLIENTS_TABLE + "Buoys&Event=" + event;
			JSONObject json = JsonReader.readJsonFromUrl(m);
			JSONArray jsonArray = json.getJSONArray("Buoys");
			int countBouy = jsonArray.length();

            for (int i = 0; i < countBouy; i++) {
                JSONObject jsonObj = (JSONObject) jsonArray.get(i);

                String buoyName = "BuoyNum" + (i + 1);
                String lat = jsonObj.getString("lat");
                String lng = jsonObj.getString("lon");

                // Adds buoy with LatLng to HashMap.
                buoysLatLng.put(buoyName, new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));

                Log.i(name + " " + buoyName + " " + event, "Lat: " + lat + ", Lng: " + lng);
            }
			return buoysLatLng;
		}
		catch (JSONException e) {
			Log.i(name, "JSONException");
            e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			Log.i(name, "IOException");
            e.printStackTrace();
			return null;
		}
	}

	protected void onPostExecute(Map<String, LatLng> buoysLatLng) {
		if (buoysLatLng != null) {
            // Random latitude and longitude.
			LatLng latLng = new LatLng(32.1057, 35.1704);
			int j = 0;
			for (Map.Entry<String, LatLng> entry : buoysLatLng.entrySet()) {
				if (j < buoyRadiuses.length) {
					String buoyName = entry.getKey();
					LatLng buoyLatLng = entry.getValue();

                    // Adds a buoy on the google map.
					latLng = new LatLng(buoyLatLng.latitude, buoyLatLng.longitude);
					googleMap.addMarker(new MarkerOptions().position(latLng).title(buoyName).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_buoy_low)));

					// Adds circles/radiuses around each buoy on the google map.
					buoyRadiuses[j++] = googleMap.addCircle(new CircleOptions()
					.center(latLng)
					.radius(C.RADIUS_BUOY)
					.strokeColor(Color.RED)
					.strokeWidth(1L)
					.fillColor(Color.argb(50, 0, 0, 255)));
				}
			}






			// Focus the camera on the latest buoy added to HashMap.
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, C.ZOOM_LEVEL);
			googleMap.animateCamera(cameraUpdate);
		}
	}

}
