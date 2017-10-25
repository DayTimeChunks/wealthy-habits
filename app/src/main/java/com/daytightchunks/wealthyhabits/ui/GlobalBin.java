package com.daytightchunks.wealthyhabits.ui;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static android.R.attr.value;

/**
 * Created by DayTightChunks on 05/09/2017.
 */

public class GlobalBin {

    public static final String LOG_TAG = GlobalBin.class.getSimpleName();

    private String binName;
    private Float wallet;
    private Float savings;

    private NavigableMap walletMap;
    private NavigableMap savingsMap;
    DecimalFormat df = new DecimalFormat("###.##");
    Object valueObj;
    Float value;

    public GlobalBin(String name){
        this.binName = name;
        this.wallet = 0.0f;
        this.savings = 0.0f;
        this.walletMap = new TreeMap();

    }

    public String getName(){ return this.binName; }

    public void updateWalletMap(String date, Float amount){
        /*
        * walletMap: stores cumulative Checking's, with date attached
        * wallet: only cumulative for quick ref. */
        Log.d(LOG_TAG, "updating wallet: " + walletMap.lastEntry());
        if (walletMap.lastEntry() != null){
            valueObj = walletMap.lastEntry().getValue();
            value = (Float) valueObj;
            value += amount;
            df.format(value); // two decimal places
            walletMap.put(date, value);
            updateWallet(value);
        } else {
            walletMap.put(date, amount);
            updateWallet(amount);
        }
    }

    public void updateSavingsMap(String date, Float amount){
        /*
        * Same as updateWalletMap
        * */
        Log.d(LOG_TAG, "updating wallet: " + savingsMap.lastEntry());
        if (savingsMap.lastEntry() != null){
            valueObj = savingsMap.lastEntry().getValue();
            value = (Float) valueObj;
            value += amount;
            df.format(value); // two decimal places
            savingsMap.put(date, value);
            updateWallet(value);
        } else {
            savingsMap.put(date, amount);
            updateWallet(amount);
        }
    }


    public Object printLastEntry(){
        Object key =  walletMap.lastEntry().getKey();
        Object value =  walletMap.lastEntry().getValue();
        return value;
    }

    public void updateWallet(Float amount){
        if (this.wallet == null || this.wallet == 0){
            this.wallet = amount;
        } else {
            this.wallet += amount;
        }
    }

    public void addToWallet(Float income){
        if (this.wallet == null || this.wallet == 0){
            this.wallet = income;
        } else {
            this.wallet += income;
        }
    }

    // If subtract, amount will be expense as negative
    public void subtractFromWallet(Float amount){
        if (this.wallet == null || this.wallet == 0){
            this.wallet = amount;
        } else {
            this.wallet += amount;
        }
    }

    public void addSaving(Float saving){
        if (this.savings == null || this.savings == 0){
            this.savings = saving;
        } else {
            this.savings += saving;
        }
    }

    public void subtractSaving(Float saving){
        if (this.savings == null || this.savings == 0){
            this.savings = saving*-1;
        } else {
            this.savings -= saving;
        }
    }

    public Float getWallet(){
        return this.wallet;
    }

    public NavigableMap getWalletMap(){
        return walletMap;
    }
    public Float getSavings(){
        return this.savings;
    }

}
