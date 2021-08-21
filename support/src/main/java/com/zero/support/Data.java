package com.zero.support;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;

public class Data extends SQLiteOpenHelper
{

	public final static String DATABASE_NAME = "song_list.db"; 
	public final static String TABLE_NAME = "SONGS_LIST"; 
	public static final String ID = "ID"; 
	public static final String TITLE = "TITLE"; 
	public static final String ARTIST = "ARTIST"; 
	public static final String ALBUM = "ALBUM"; 
	public static final String ALBUM_ID = "ALBUM_ID";
	public static final String GENRE = "GENRE";
	public static final String PATH = "PATH";
	public static final String DURATION = "DURATION";
	public static final String IMAGE = "IMAGE";

	public Data(Context context)
	{ 
		super(context, DATABASE_NAME, null, 1); 
	} 

	@Override 
	public void onCreate(SQLiteDatabase db)
	{ 
		String query = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ( "+ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +TITLE+ " TEXT, " +ARTIST+" TEXT, "+ALBUM+" TEXT, "+ALBUM_ID+" LONG, "+GENRE+" TEXT, "+PATH+" TEXT, "+DURATION+" LONG, "+IMAGE+" BYTE"+ "[]" +" ); ";
		db.execSQL(query); 
	} 

	@Override 
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{ 
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); 
		onCreate(db); 
	} 
	
	public void update()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

	public boolean insertData(ContentValues cv)
	{ 
		SQLiteDatabase db = this.getWritableDatabase(); 
		long result = db.insert(TABLE_NAME, null, cv); 
		return (result != -1);
	} 

	public Cursor getCursor()
	{ 
		SQLiteDatabase db = this.getWritableDatabase();
		return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
	} 
	
	public boolean empty()
	{
		Cursor cursor = getCursor();
		int count = cursor.getCount();
		return (count == 0);
	}
} 
