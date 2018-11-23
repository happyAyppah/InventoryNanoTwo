package com.example.android.inventorynanoapptwo;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventorynanoapptwo.data.BookContract.BookEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int EXISTING_BOOK_LOADER = 0;
    private Uri currentBookUri;

    private boolean bookHasChanged = false;
    private boolean validBookData = true;

    @BindView(R.id.book_name_edit_text)
    EditText nameEditText;
    @BindView(R.id.book_price_edit_text)
    TextView priceEditText;
    @BindView(R.id.book_quantity_edit_text)
    TextView quantityEditText;
    @BindView(R.id.book_supplier_name)
    TextView supplierNameEditText;
    @BindView(R.id.book_supplier_phone_number_edit_text)
    TextView phoneNumberEditText;

    /**
     * OnTouchListener that listens for any touch on a View.
     */
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            bookHasChanged = true;
            Log.d("message", "onTouch");
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        Log.d("message", "onCreate");

        Intent intent = getIntent();
        currentBookUri = intent.getData();
        if (currentBookUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_book));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_book));
            getSupportLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }
        nameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        phoneNumberEditText.setOnTouchListener(touchListener);
    }

    private void saveBook() {

        String nameString = nameEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = phoneNumberEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();

        if (currentBookUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneNumberString) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString)) {
            return;
        }

        ContentValues values = new ContentValues();

        // Check that the book has a name.
        if (!TextUtils.isEmpty(nameString)) {
            values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
        } else {
            Toast.makeText(this, getString(R.string.editor_book_requires_name),
                    Toast.LENGTH_SHORT).show();
            validBookData = false;
            return;
        }

        // Check that the name of the supplier of the book is provided.
        if (!TextUtils.isEmpty(supplierNameString)) {
            values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        } else {
            Toast.makeText(this, getString(R.string.editor_book_requires_supplier_name),
                    Toast.LENGTH_SHORT).show();
            validBookData = false;

            return;
        }

        // Check that the book has a valid phone number.
        if (!TextUtils.isEmpty(supplierPhoneNumberString) && Integer.parseInt(supplierPhoneNumberString) > 0) {
            values.put(BookEntry.COLUMN_PHONE_NUMBER, supplierPhoneNumberString);
        } else if (supplierPhoneNumberString.length() == 0) {
            Toast.makeText(this, getString(R.string.editor_book_requires_supplier_phone_number),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (Integer.parseInt(supplierPhoneNumberString) == 0) {
            Toast.makeText(this, getString(R.string.editor_book_invalid_supplier_phone_number),
                    Toast.LENGTH_SHORT).show();
            validBookData = false;

            return;
        }

        // Check that the book has a valid unit price.
        if (!TextUtils.isEmpty(priceString) && Integer.parseInt(priceString) > 0) {
            values.put(BookEntry.COLUMN_PRICE, priceString);
        } else if (priceString.length() == 0) {
            Toast.makeText(this, getString(R.string.editor_book_requires_price),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (Integer.parseInt(priceString) == 0) {
            Toast.makeText(this, getString(R.string.editor_book_requires_positive_price),
                    Toast.LENGTH_SHORT).show();
            validBookData = false;

            return;
        }

        // Check that the book has a valid unit stock.
        if (!TextUtils.isEmpty(quantityString) && Integer.parseInt(quantityString) > 0) {
            values.put(BookEntry.COLUMN_QUANTITY, quantityString);
        } else if (quantityString.length() == 0) {
            Toast.makeText(this, getString(R.string.editor_book_requires_positive_stock_level),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (Integer.parseInt(quantityString) == 0) {
            Toast.makeText(this, getString(R.string.editor_book_requires_positive_stock_level),
                    Toast.LENGTH_SHORT).show();
            validBookData = false;

            return;
        }

        // If all the checks on the input values are passed,
        // then the data for the book are valid.
        validBookData = true;
        if (currentBookUri == null) {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentBookUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_PHONE_NUMBER
        };
        return new CursorLoader(this,
                currentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PHONE_NUMBER);

            String currentName = cursor.getString(nameColumnIndex);
            final int currentPrice = cursor.getInt(priceColumnIndex);
            final int currentQuantity = cursor.getInt(quantityColumnIndex);
            String currentSupplierName = cursor.getString(supplierNameColumnIndex);
            final int currentSupplierPhone = cursor.getInt(supplierPhoneColumnIndex);

            nameEditText.setText(currentName);
            priceEditText.setText(Integer.toString(currentPrice));
            quantityEditText.setText(Integer.toString(currentQuantity));
            phoneNumberEditText.setText(Integer.toString(currentSupplierPhone));
            supplierNameEditText.setText(currentSupplierName);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameEditText.setText("");
        supplierNameEditText.setText("");
        phoneNumberEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
    }

    private void deleteBook() {
        if (currentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBook();
                if (validBookData) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}