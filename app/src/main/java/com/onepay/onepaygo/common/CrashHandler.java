package com.onepay.onepaygo.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.onepay.onepaygo.Application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    /** TAG */
    private static final String TAG = "CrashHandler";

    private Throwable throwable;

    public static String error = null;
    /**
     * localFileUrl

     */
    private static String localFileUrl = "";
    /** mDefaultHandler */
    private Thread.UncaughtExceptionHandler defaultHandler;

    /** instance */
    private static CrashHandler instance = new CrashHandler();

    /** infos */
    private Map<String, String> infos = new HashMap<String, String>();

    /** formatter */
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /** context*/
    private Application context;
    private CrashHandler() {}

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    /**
     *
     * @param ctx
     */
    public void init(Application ctx) {
        this.context = ctx;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * uncaughtException
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if(!handleException(throwable) && defaultHandler != null){
            defaultHandler.uncaughtException(thread, throwable);
        }else {
            restart();
        }
        handleException(throwable);
        defaultHandler.uncaughtException(thread, throwable);
    }
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        collectDeviceInfo(context);
        throwable = ex;
        ex.printStackTrace();
        LoggerUtil.logMessagesToFile(context);
        return true;
    }

    /**
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
                infos.put("crashTime", formatter.format(new Date()));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field: fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     *
     * @param ex
     */


    /**
     *
     * @param log
     * @param name
     */
    private String writeLog(String log, String name)
    {
        Calendar c = Calendar.getInstance();
        CharSequence  timestamp  = "crash-" ;
        String filename = name + timestamp + c.get(Calendar.YEAR)  +(c.get(Calendar.MONTH)+1) +"-"+ c.get(Calendar.DAY_OF_MONTH) +c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE)  + ".log";

        File file = new File(filename);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        try
        {
            //			FileOutputStream stream = new FileOutputStream(new File(filename));
            //			OutputStreamWriter output = new OutputStreamWriter(stream);
            file.createNewFile();
            FileWriter fw=new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(log);
            bw.newLine();
            bw.close();
            fw.close();
            return filename;
        }
        catch (IOException e)
        {
            Log.e(TAG, "an error occured while writing file...", e);
            e.printStackTrace();
            return null;
        }
    }


    private void restart(){
        try{
            Thread.sleep(10);
        }catch (InterruptedException e){
            Log.e(TAG, "error : ", e);
        }

    }
}
