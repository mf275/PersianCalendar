package com.farashian;

import android.app.*;
import android.content.*;

import java.io.*;
import java.util.*;

public class MyApplication extends Application {

public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context=this.getApplicationContext();

    }

}
