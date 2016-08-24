package sip_stack_v3.netas.com.sip_stack_v3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.UUID;


/**
 * Created by ayildiz on 11/27/2015.
 */
public class DatabaseOper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "sipins_DB";
    public static final String TABLE_NAME = "Sipins_TBL";
    public static final String SIPINS_ID = "sipins_id";
    public static final String SIPINS_VALUE = "sipins_value";
    public static final int DATABASE_VERSION = 1;

    public static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
            +SIPINS_ID+" INTEGER PRIMARY KEY,"+SIPINS_VALUE+" TEXT);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " +TABLE_NAME;

    public DatabaseOper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("Database Oper","Database created");
    }

    @Override
    public void onCreate(SQLiteDatabase sdb) {
        sdb.execSQL(CREATE_QUERY);
        Log.d("Database Oper","Table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sdb, int oldVersion, int newVersion) {
        sdb.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sdb);
        Log.d("onUpgrade","Old tables droped");
    }

    public String createSipInstance(DatabaseOper dop) {
        Cursor cursor;
        String sipins_value = "";
        SQLiteDatabase db = this.getReadableDatabase();
        try{
            cursor = db.rawQuery("SELECT sipins_value FROM Sipins_TBL WHERE sipins_id=?", new String[] {1 + ""});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                sipins_value = cursor.getString(cursor.getColumnIndex("sipins_value"));
            }
        }catch (Exception e){
            Log.d("getsipins","error");
        }

        try {
            if (sipins_value.equals("")){
                SQLiteDatabase db2 = dop.getWritableDatabase();
                sipins_value = UUID.randomUUID().toString();
                ContentValues values = new ContentValues();
                values.put(SIPINS_ID,1);
                values.put(SIPINS_VALUE,sipins_value);
                db2.insert(TABLE_NAME,null,values);
                Log.d("Database Oper","One raw inserted2");
            }
        }catch (Exception e){
            Log.d("createsipins","hata");
        }
        Log.d("Database Oper","Sipins Returned "+sipins_value);
        return sipins_value;
    }

    public void deleterow(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_NAME,SIPINS_ID+"="+id,null);
        //Log.d("Database Oper","One row deleted");
    }
}
