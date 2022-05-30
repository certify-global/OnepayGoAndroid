package com.onepay.onepaygo.database;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.onepay.onepaygo.controller.DatabaseController;
import com.onepay.onepaygo.database.secureDB.SafeHelperFactory;
import com.onepay.onepaygo.model.ReportRecords;
import com.onepay.onepaygo.model.TransactionDB;

@androidx.room.Database(entities = {ReportRecords.class}, version = DatabaseController.DB_VERSION, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class Database extends RoomDatabase {
    public abstract DatabaseStore databaseStore();

    public static final String DB_NAME = "one_pay_go.db";
    private static volatile Database INSTANCE=null;

    public static Database create(Context ctxt, String passphrase) {
        Builder<Database> b;
        Log.i("Database -> create",""+passphrase);
        b=Room.databaseBuilder(ctxt.getApplicationContext(), Database.class, DB_NAME).allowMainThreadQueries();
          //    b.openHelperFactory(SafeHelperFactory.fromUser(new SpannableStringBuilder(passphrase)));
        b.addCallback(new Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);

            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
            }
        });
        return(b.build());
    }
}
