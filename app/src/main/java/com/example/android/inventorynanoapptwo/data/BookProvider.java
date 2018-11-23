package com.example.android.inventorynanoapptwo.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventorynanoapptwo.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    private static final int BOOKS = 100;

    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    private BookDbHelper DbHelper;

    @Override
    public boolean onCreate() {
        DbHelper = new BookDbHelper((getContext()));
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = DbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        String nameBook = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (nameBook == null) {
            throw new IllegalArgumentException("Book name required");
        }

        Integer priceBook = values.getAsInteger(BookEntry.COLUMN_PRICE);
        if (priceBook != null && priceBook < 0) {
            throw new IllegalArgumentException("Book requires a valid unit price");
        }

        Integer quantityBook = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
        if (quantityBook != null && quantityBook < 0) {
            throw new IllegalArgumentException("Book requires a valid unit quantity");
        }

        String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Book requires a supplier's name");
        }

        Integer supplierPhone = values.getAsInteger(BookEntry.COLUMN_PHONE_NUMBER);
        if (supplierPhone != null && supplierPhone < 0) {
            throw new IllegalArgumentException("Book requires a supplier's phone number");
        }

        SQLiteDatabase database = DbHelper.getWritableDatabase();
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.v("message:", "Failed to insert new row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[]
            selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String nameBook = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if (nameBook == null) {
                throw new IllegalArgumentException("Book name required");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_PRICE)) {
            Integer priceBook = values.getAsInteger(BookEntry.COLUMN_PRICE);
            if (priceBook != null && priceBook < 0) {
                throw new IllegalArgumentException("Book requires a valid unit price");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_QUANTITY)) {
            Integer quantityBook = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
            if (quantityBook != null && quantityBook < 0) {
                throw new IllegalArgumentException("Book requires a valid unit quantity");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Book requires a supplier's phone number");
            }
        }
        if (values.containsKey(BookEntry.COLUMN_PHONE_NUMBER)) {
            Integer supplierPhone = values.getAsInteger(BookEntry.COLUMN_PHONE_NUMBER);
            if (supplierPhone != null && supplierPhone < 0) {
                throw new IllegalArgumentException("Book requires a supplier's phone number");
            }
        }
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = DbHelper.getWritableDatabase();
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = DbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI" + uri + " with match " + match);
        }
    }
}