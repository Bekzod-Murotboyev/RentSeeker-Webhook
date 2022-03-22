package uz.pdp.rentseekerwebhook.service;

import com.google.gson.Gson;
import lombok.NonNull;
import uz.pdp.rentseekerwebhook.model.locationModels.LocationsItem;
import uz.pdp.rentseekerwebhook.model.locationModels.MapQuest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService {
    private static final String url = "https://open.mapquestapi.com/geocoding/v1/reverse?key=iFaKV4Rp3rWRtGeJoQQYBEUnqJcxF8Cl&location=";

    public static LocationsItem getData(@NonNull Double lat, @NonNull Double lon) {
        Gson gson = new Gson();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url1 = new URL(url + lat + "," + lon);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url1.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String row;
            while ((row = bufferedReader.readLine()) != null) {
                stringBuilder.append(row);
            }
            MapQuest mapQuest = gson.fromJson(stringBuilder.toString(), MapQuest.class);
            return mapQuest.getResults().get(0).getLocations().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
