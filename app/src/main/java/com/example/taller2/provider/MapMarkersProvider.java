package com.example.taller2.provider;

import android.content.Context;

import com.example.taller2.model.LocationMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MapMarkersProvider {

    public static String loadJSONFromAsset(Context context) {
        String json =null;

        try{
            InputStream is = context.getAssets().open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);is.close();
            json = new String(buffer,"UTF-8");
        }
        catch (IOException ex){
            ex.printStackTrace();  return null;
        }
        return json;
    }

    public static List<LocationMap> getLocations(Context context) {
        JSONObject json = null;
        List<LocationMap> locations = new ArrayList<LocationMap>();

        try {

            json = new JSONObject(loadJSONFromAsset(context));
            JSONArray countriesJsonArray = json.getJSONArray("locationsArray");

            for (int i = 0; i < countriesJsonArray.length(); i++) {
                JSONObject jsonObject = countriesJsonArray.getJSONObject(i);
                LocationMap location = new LocationMap(
                        Double.parseDouble(jsonObject.getString("latitude")),
                        Double.parseDouble(jsonObject.getString("longitude")),
                        jsonObject.getString("name")
                );
                locations.add(location);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locations;
    }

}
