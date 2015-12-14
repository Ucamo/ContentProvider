package carrillo.uriel.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import java.util.HashMap;

public class CustomContentProvider extends ContentProvider{
    //fields for the content provider
    static final String PROVIDER_NAME = "carrillo.uriel.contentprovider";
    static final String URL ="content://"+PROVIDER_NAME+"/nicknames";
    static final Uri CONTENT_URI = Uri.parse(URL);

    //fields for the database
    static final String ID ="id";
    static final String NAME="name";
    static final String NICK_NAME="nickname";

    //Integer values used in content URI
    static final int NICKNAME =1;
    static final int NICKNAME_ID=2;

    DBHelper dbHelper;

    //projection map for a query
    private static HashMap<String, String> NicknameMap;

    //maps contents URI "paterns" to the integer values that were set above
    static final UriMatcher uriMatcher;
    static{
        uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME,"nicknames",NICKNAME);
        uriMatcher.addURI(PROVIDER_NAME,"nicknames/#",NICKNAME_ID);
    }

    //database declarations
    private SQLiteDatabase database;
    static final String DATABASE_NAME ="NicknamesDirectory";
    static final String TABLE_NAME="nicknames";
    static final int DATABASE_VERSION=1;
    static final String CREATE_TABLE=
            "CREATE TABLE "+ TABLE_NAME+
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    " name TEXT NOT NULL, "+
                    " nickname TEXT NOT NULL);";

    //class that creates and manages the provider's database
    private static class DBHelper extends SQLiteOpenHelper{

        public DBHelper(Context context){
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            Log.w(DBHelper.class.getName(),
            "Upgrading database from version "+oldVersion+
                    " to "+newVersion+
                    ". Old data will be destroyed");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate(){
        Context context = getContext();
        dbHelper = new DBHelper(context);
        //permision to be writable
        database = dbHelper.getWritableDatabase();

        if(database==null)
            return false;
        else
            return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //the TABLE_NAME to query on
        queryBuilder.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)){
            //maps all database column names
            case NICKNAME:
                queryBuilder.setProjectionMap(NicknameMap);
                break;
            case NICKNAME_ID:
                queryBuilder.appendWhere(ID+"="+uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }
        if(sortOrder==null || sortOrder==""){
            //No sorting -> sort on names by default
            sortOrder=NAME;
        }
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        //register to watch a content URI for changes
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        long row = database.insert(TABLE_NAME, "",values);

        //If record is added succesfully
        if(row>0){
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI,row);
            getContext().getContentResolver().notifyChange(newUri,null);
            return newUri;
        }
        throw new SQLException("Fail to add a new record into "+uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        int count=0;

        switch (uriMatcher.match(uri)){
            case NICKNAME:
                count = database.update(TABLE_NAME,values,selection,selectionArgs);
                break;
            case NICKNAME_ID:
                count = database.update(TABLE_NAME,values,
                        ID+" = "+uri.getLastPathSegment()+
                                (!TextUtils.isEmpty(selection)? "AND ("+
                                selection+ ")": ""),selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI "+uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        int count=0;

        switch (uriMatcher.match(uri)){
            case NICKNAME:
                //delete all the records of the table
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case NICKNAME_ID:
                String id= uri.getLastPathSegment();
                count = database.delete(TABLE_NAME, ID + " = " +
                        id + (!TextUtils.isEmpty(selection) ? " AND (" +
                        selection + ")" : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI "+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return  count;
    }

    @Override
    public String getType(Uri uri){
        switch(uriMatcher.match(uri)){
            //Get all nicknames records
            case NICKNAME:
                return "vnd.android.cursor.dir/vnd.example.nicknames";
            //Get a particular name
            case NICKNAME_ID:
                return "vnd.android.cursor.item/vnd.example.nicknames";
            default:
                throw new IllegalArgumentException("Unsupported URI: "+uri);
        }
    }
}
