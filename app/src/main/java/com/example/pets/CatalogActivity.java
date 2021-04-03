package com.example.pets;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pets.data.PetContract.PetEntry;
import com.example.pets.data.PetDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;

    PetCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView petListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        //setup an adapter to create list item for each row of the pet data in the cursor
        // there is no pet data yet (until the loader finishes) so pass is null for the Cursor.
        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        //setup the item click listener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /**create new intent to go to {@link EditorActivity}*/
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                /**
                 * form the content uri that represents the specific pet that was clicked on,
                 * by appending the id (passed as input to this method ) onto the {@link PetEntry#CONTENT_URI}
                 */
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                intent.setData(currentPetUri);

                startActivity(intent);

            }
        });

        //Kick off the loader
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    private void insertPet() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        //Perform a query on the provider using content resolver.
        //use the {@link PetEntry#CONTENT_URI} to access the pet data.
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        //this loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,   //Parent activity context
                PetEntry.CONTENT_URI,           //Provider content URI to query
                projection,                     //Columns to include in the resulting cursor.
                null,                  //No selection clause
                null,               //No selection arguments
                null);                 //Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        /**
         Update {@link PetCursorAdapter} with this new cursor containing updated pet data
         */
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when data needs to be deleted.
        mCursorAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.delete_all_pets_dialog_msg));
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllPets();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //delete all pets from database
    private void deleteAllPets() {
        //delete all pets from database.
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);

        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.editor_delete_pet_failed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, rowsDeleted + getString(R.string.catalog_delete_all_pets), Toast.LENGTH_SHORT).show();
        }
    }
}