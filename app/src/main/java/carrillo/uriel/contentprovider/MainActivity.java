package carrillo.uriel.contentprovider;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public void addRecord(View view){
        //Add a new record
        ContentValues values = new ContentValues();
        if((!((EditText)findViewById(R.id.name)).getText().toString().isEmpty())&&
                (!((EditText)findViewById(R.id.nickname)).getText().toString().isEmpty())){
            values.put(CustomContentProvider.NAME,((EditText)findViewById(R.id.name)).getText().toString());

            values.put(CustomContentProvider.NICK_NAME,((EditText)findViewById(R.id.nickname)).getText().toString());



            Uri uri = getApplicationContext().getContentResolver().insert(CustomContentProvider.CONTENT_URI,values);
            Toast.makeText(getBaseContext(),"Record Inserted", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getBaseContext(),"Please enter the records first", Toast.LENGTH_LONG).show();
        }
    }

    public void showAllRecords(View view){
        //Show all the records sorted by friend's name
        String URL = "content://carrillo.uriel.contentprovider/nicknames";
        Uri friends = Uri.parse(URL);
        Cursor c = getContentResolver().query(friends,
                null, null, null, "name");
        String result = "Content Provider Results:";

        if(!c.moveToFirst()){
            Toast.makeText(this,result+" no content yet!", Toast.LENGTH_LONG).show();
        }else{
            do {
                result = result + "\n"
                        + c.getString(c.getColumnIndex(CustomContentProvider.NAME))
                        + " has nickname: "
                        + c.getString(c.getColumnIndex(CustomContentProvider.NICK_NAME));
            }   while(c.moveToNext());
            if(!result.isEmpty()) {
                Toast.makeText(this, result,
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this,"No Records present",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void deleteAllRecords (View view){
        //delete all ther ecords and the table of the database provider
        String URL ="content://carrillo.uriel.contentprovider/nicknames";
        Uri friends= Uri.parse(URL);
        int count = getContentResolver().delete(friends, null,null);
        String countNum=count+" records are deleted";
        Toast.makeText(getBaseContext(),countNum,Toast.LENGTH_LONG).show();
    }

}
