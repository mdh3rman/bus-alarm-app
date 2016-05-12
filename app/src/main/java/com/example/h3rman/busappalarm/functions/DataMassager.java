package com.example.h3rman.busappalarm.functions;

import com.example.h3rman.busappalarm.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by muhammadhh on 12/5/2016.
 */
public final class DataMassager {

    public static String getBusDuration(String duration_in_milliseconds){
        try {
            int durationMin = Integer.parseInt(duration_in_milliseconds) / 60000;
            if (durationMin < 1) {
                return "Arr";
            } else {
                return Integer.toString(durationMin) + " mins";
            }
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace();
            return "NA";
        }
    }

    public static String getBusLoad(String load) {
        if (load.equals("Seats Available")){
            return "green";
        } else if (load.equals("Standing Available")) {
            return "yellow";
        } else if (load.equals("Limited Standing")) {
            return "red";
        } else {
            return "NA";
        }
    }

    public static int convertStringBusToInt(String busNo){
        try{
            return Integer.parseInt(busNo);
        }catch (NumberFormatException e){
            String numberOnly = "";
            for (char c:busNo.toCharArray()) {
                if(Character.isDigit(c)){
                    numberOnly += c;
                }
            }
            return Integer.parseInt(numberOnly);
        }
    }
}
