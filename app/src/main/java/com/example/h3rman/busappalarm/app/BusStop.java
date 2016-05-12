package com.example.h3rman.busappalarm.app;

import android.app.Application;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by muhammadhh on 12/5/2016.
 */
public class BusStop extends Application {



    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("jsonResult.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
