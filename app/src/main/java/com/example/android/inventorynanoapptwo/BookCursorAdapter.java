package com.example.android.inventorynanoapptwo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorynanoapptwo.data.BookContract.BookEntry;


public class BookCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Get the current position of the cursor in order to set a TAG with it on the sell button
        final int position = cursor.getPosition();

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.unit_price_value);
        TextView quantityTextView = view.findViewById(R.id.stock_level_value);
        ImageView sellNowButtonImageView = view.findViewById(R.id.sell_button);
        ImageView editButtonImageView = view.findViewById(R.id.edit_button);

        // Set a TAG on the sell button with current position of cursor
        sellNowButtonImageView.setTag(position);

        // Set a TAG on the edit button with current position of cursor
        editButtonImageView.setTag(position);

        // Find the columns of book attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        // Read the book attributes from the Cursor for the current book
        String bookName = cursor.getString(nameColumnIndex);
        int bookPrice = cursor.getInt(priceColumnIndex);
        int bookQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current book
        nameTextView.setText(bookName);
        priceTextView.setText(context.getString(R.string.currency) + String.format("%,d", bookPrice));
        quantityTextView.setText(String.valueOf(bookQuantity));

        sellNowButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer position = (Integer) v.getTag();
                cursor.moveToPosition(position);
                Long rowId = cursor.getLong(cursor.getColumnIndex(BookEntry._ID));
                int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
                int currentQuantity = cursor.getInt(quantityColumnIndex);

                // If the stock level is still positive, then proceed with the sell of 1 unit
                if (currentQuantity > 0) {
                    sellBookUnit(context, rowId, currentQuantity);
                } else {
                    // Otherwise, show a toast message saying that the sell action is not possible
                    // as the stock level has reached 0.
                    Toast.makeText(context, context.getString(R.string.main_sell_book_item_failed_stock_empty),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        editButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), EditorActivity.class);
                Integer position = (Integer) v.getTag();
                cursor.moveToPosition(position);
                Long rowId = cursor.getLong(cursor.getColumnIndex(BookEntry._ID));
                Uri currentBookURI = ContentUris.withAppendedId(BookEntry.CONTENT_URI, rowId);
                intent.setData(currentBookURI);
                context.startActivity(intent);
            }
        });
    }

    private void sellBookUnit(Context context, Long rowId, int quantity) {
        // sell 1 unit of the book.
        quantity--;
        // Form the content URI that represents the specific book that was clicked on.
        Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, rowId);
        // Create a ContentValues objectURI and put the decremented stock level in it
        final ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_QUANTITY, quantity);


        int rowsAffected = context.getContentResolver().update(currentBookUri, values, null, null);
        // Show a toast message depending on whether or not the update was successful.
        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(context, context.getString(R.string.main_sell_book_item_failed), Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the sell was successful and we can display a toast.
            Toast.makeText(context, context.getString(R.string.main_sell_book_item_successful), Toast.LENGTH_SHORT).show();
        }
    }
}