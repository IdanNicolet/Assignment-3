package com.matchrace.matchrace.classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.matchrace.matchrace.R;
import com.matchrace.matchrace.modules.JsonReader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * AsyncTask for getting the sailor's locations from DB and adding them to the google map.
 *
 */
public class GetSailorsTask extends AsyncTask<String, Integer, Map<String, LatLng>> {

	// Application variables.
	private String name = "", fullUserName = "", event = "";

	// Views.
	private List<Marker> sailorMarkers = new ArrayList<Marker>();
	private GoogleMap googleMap;

	public GetSailorsTask(String name, GoogleMap googleMap, List<Marker> sailorMarkers, String fullUserName, String event) {
		super();
		this.name = name;
		this.googleMap = googleMap;
		this.sailorMarkers = sailorMarkers;
		this.fullUserName = fullUserName.substring(C.SAILOR_PREFIX.length());
		this.event = event;
	}

	protected Map<String, LatLng> doInBackground(String... urls) {
		Map<String, LatLng> sailorsLatLng = new HashMap<String, LatLng>();
		try {
            String m = C.URL_HISTORY_TABLE +"Race&Event=" + event+"&Information="+fullUserName;
			JSONObject json = JsonReader.readJsonFromUrl(m);
			JSONArray jsonArray = json.getJSONArray("Positions");
	    	JSONObject jsonObj = (JSONObject) jsonArray.get(0);
			String sailorFullName = jsonObj.getString("name");
			String lat = jsonObj.getString("lat");
			String lng = jsonObj.getString("lon");
			String sailorName = sailorFullName;
            sailorsLatLng.put(sailorName, new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
			Log.i(name + " " + sailorName + " " + event, "Lat: " + lat + ", Lng: " + lng);
			return sailorsLatLng;
		}
		catch (JSONException e) {
			Log.i(name, "JSONException");
			return null;
		}
		catch (IOException e) {
			Log.i(name, "IOException");
			return null;
		}
	}

	protected void onPostExecute(Map<String, LatLng> sailorsLatLng) {
		try {
			if (sailorsLatLng != null) {
				// Removes from map all previous sailors.
				if (!sailorMarkers.isEmpty()) {
					for (Marker markerSailor : sailorMarkers) {
						markerSailor.remove();
					}
					sailorMarkers.clear();
				}

				// Adds to map new sailors with new locations.
				for (Map.Entry<String, LatLng> entry : sailorsLatLng.entrySet()) {
					String sailorName = entry.getKey();
					LatLng sailorLatLng = entry.getValue();
					LatLng latLng = new LatLng(sailorLatLng.latitude, sailorLatLng.longitude);
					sailorMarkers.add(googleMap.addMarker(new MarkerOptions().position(latLng).title(sailorName).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sailor_low))));
				}
			}
		} catch (Exception e)
		{}
	}

}
