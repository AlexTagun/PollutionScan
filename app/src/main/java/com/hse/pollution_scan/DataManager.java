package com.hse.pollution_scan;

public class DataManager {

    private static DataManager mDataManager;

    private DataManager(){

    }

    public static DataManager Instance(){
        if(mDataManager == null){
            mDataManager = new DataManager();
        }

        return mDataManager;
    }
}
