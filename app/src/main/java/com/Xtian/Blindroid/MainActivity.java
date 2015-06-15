package com.Xtian.Blindroid;

/**
 * Created by xtianrock on 22/05/2015.
 */

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Clase que contiene la activity principal de la aplicacion
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

    ListView listView;
    TextView emptyChats;
    private ContactCursorAdapter ContactCursorAdapter;
    private Toolbar toolbar;
    private static String CHATLIST="chatlist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.contactslist);
        listView.setOnItemClickListener(this);
        ContactCursorAdapter = new ContactCursorAdapter(this, null);
        listView.setAdapter(ContactCursorAdapter);
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        emptyChats=(TextView)findViewById(R.id.emptyChats);

        //actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_CUSTOM);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle("Chats");


        getSupportLoaderManager().initLoader(0, null, this);

        //check version for overrinding recents style
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.blindroid_logo);
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, bm, color);

            setTaskDescription(td);
            bm.recycle();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automat**cally handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.tutorial) {
            Intent intent = new Intent().setClass(this, SplashScreenActivity.class);
            intent.setAction("tutorial");
            startActivity(intent);
            finish();

        } else if (id == R.id.compartir) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.Xtian.Blindroid");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Prueba mi nueva app: Blindroid, cambiaras tu forma de llamar!");
            startActivity(Intent.createChooser(intent, getString(R.string.compartir)));
        } else if (id == R.id.info) {
            String url = "https://plus.google.com/communities/102190305058584818425";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);


        } else if (id == R.id.contacto) {

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"xtianrock89@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Blindroid");
            emailIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(emailIntent, "Email "));
        }
        else if (id == R.id.settings) {

            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Commons.PROFILE_ID,(int)arg3);
        Log.i("profile_id", "" + arg3);
        startActivity(intent);
    }
    @Override
    public void onRestart() {
        super.onRestart();
        DataProvider.refreshChatlist(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(0, null, this);
    }
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this,
                Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE,CHATLIST),
                new String[]{"p.*","m.message","m.time"},
                null,
                null,
                DataProvider.COL_TIME + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> arg0, Cursor arg1) {
        ContactCursorAdapter.swapCursor(arg1);
        if(arg1.getCount()==0)
            emptyChats.setVisibility(View.VISIBLE);
        else
            emptyChats.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0) {
        ContactCursorAdapter.swapCursor(null);
    }


    public class ContactCursorAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public ContactCursorAdapter(Context context, Cursor c) {
            super(context, c, 0);
            this.mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override public int getCount() {
            return getCursor() == null ? 0 : super.getCount();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View itemLayout = mInflater.inflate(R.layout.chat_list, parent, false);
            ViewHolder holder = new ViewHolder();
            itemLayout.setTag(holder);
            holder.name = (TextView) itemLayout.findViewById(R.id.contact_name);
            holder.count = (TextView) itemLayout.findViewById(R.id.message_count);
            holder.lastMessage = (TextView) itemLayout.findViewById(R.id.last_message);
            holder.time = (TextView) itemLayout.findViewById(R.id.message_time);
            holder.photo = (ImageView) itemLayout.findViewById(R.id.contact_photo);
            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.name.setText(cursor.getString(cursor.getColumnIndex(DataProvider.COL_NAME)));
            String phone=cursor.getString(cursor.getColumnIndex(DataProvider.COL_PHONE));
            String message=cursor.getString(cursor.getColumnIndex(DataProvider.COL_MESSAGE));
            if (message.length()>25)
                message = message.substring(0,22) + "...";
            holder.lastMessage.setText(message);
            holder.time.setText(Commons.getDisplayTime(cursor.getString(cursor.getColumnIndex(DataProvider.COL_TIME))));

            int count = cursor.getInt(cursor.getColumnIndex(DataProvider.COL_COUNT));
            if (count > 0){
                holder.count.setVisibility(View.VISIBLE);
                holder.count.setText(String.valueOf(count));
            }else
                holder.count.setVisibility(View.GONE);
            holder.photo.setImageDrawable(Commons.getContactPhoto(context, phone));

        }
    }

    /**
     * Contiene las vistas usadas por el elemento holder
     */
    private static class ViewHolder {
        TextView name;
        TextView count;
        TextView lastMessage;
        TextView time;
        ImageView photo;
    }





}
