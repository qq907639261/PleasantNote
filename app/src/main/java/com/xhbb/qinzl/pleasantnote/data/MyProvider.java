package com.xhbb.qinzl.pleasantnote.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class MyProvider extends ContentProvider {

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            mDbHelper = new DbHelper(context.getApplicationContext());
        } else {
            mDbHelper = new DbHelper(null);
        }
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String table = uri.getLastPathSegment();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        Context context = getContext();
        if (context != null) {
            context = context.getApplicationContext();
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] valueses) {
        String table = uri.getLastPathSegment();
        SQLiteDatabase db = getWritableDb();

        db.beginTransaction();
        try {
            for (ContentValues values : valueses) {
                db.insert(table, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        int insertedRows = super.bulkInsert(uri, valueses);
        notifyChange(uri);

        return insertedRows;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String table = uri.getLastPathSegment();
        long insertedId = getWritableDb().insert(table, null, values);
        return ContentUris.withAppendedId(uri, insertedId);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        String table = uri.getLastPathSegment();
        return getWritableDb().update(table, values, selection, selectionArgs);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String table = uri.getLastPathSegment();
        return getWritableDb().delete(table, selection, selectionArgs);
    }

    private SQLiteDatabase getWritableDb() {
        return mDbHelper.getWritableDatabase();
    }

    private void notifyChange(@NonNull Uri uri) {
        Context context = getContext();
        if (context != null) {
            context = context.getApplicationContext();
            context.getContentResolver().notifyChange(uri, null);
        }
    }
}
