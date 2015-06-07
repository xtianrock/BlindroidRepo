package com.Xtian.Blindroid;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;

import com.Xtian.Blindroid.Gcm.GcmUtil;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconGridView.OnEmojiconClickedListener;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.EmojiconsPopup.OnEmojiconBackspaceClickedListener;
import github.ankushsachdeva.emojicon.EmojiconsPopup.OnSoftKeyboardOpenCloseListener;
import github.ankushsachdeva.emojicon.emoji.Emojicon;


public class ChatActivity extends ActionBarActivity implements OnClickListener, MessagesFragment.OnFragmentInteractionListener {



    private long profileId;
    private String profileName;
    private String profilePhone;
    private GcmUtil gcmUtil;
    private Toolbar toolbar;
    private EmojiconEditText emojiconEditText;
    private View rootView;
    private ImageView emojiButton;
    private EmojiconsPopup popup;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        profileId = getIntent().getLongExtra(Commons.PROFILE_ID, 0);
        NotificationManager notificationManager =(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel((int) profileId);
        emojiButton = (ImageView) findViewById(R.id.send_btn);
        emojiButton.setOnClickListener(this);
        prepareEmojis();
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        Cursor c = getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE,String.valueOf(profileId)),null, null, null, null);
        if (c.moveToFirst()) {
            profileName = c.getString(c.getColumnIndex(DataProvider.COL_NAME));
            profilePhone = c.getString(c.getColumnIndex(DataProvider.COL_PHONE));
            toolbar.setTitle(profileName);
        }
        c.close();
        Log.i("profile_phone", ""+profilePhone);
        Bitmap bitmap= BitmapFactory.decodeStream(Commons.openPhoto(this,profilePhone));
        if(bitmap==null)
        {
            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.blindroid_icon);
        }
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        dr.setCornerRadius(Math.min(dr.getMinimumWidth(),dr.getMinimumHeight()));
        toolbar.setLogo(dr);

        getSupportActionBar().setTitle(profileName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child != null)
                if (child.getClass() == ImageView.class) {
                    ImageView iv2 = (ImageView) child;
                    if ( iv2.getDrawable() == dr ) {
                        iv2.setAdjustViewBounds(true);
                    }
                }
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //registerReceiver(registrationStatusReceiver, new IntentFilter(Common.ACTION_REGISTER));
        gcmUtil = new GcmUtil(getApplicationContext());


        //check version for overrinding recents style
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.blindroid_logo);
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(profileName, bm, color);

            setTaskDescription(td);
            bm.recycle();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                getContentResolver().delete(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES,""),
                        DataProvider.COL_SENDER_PHONE + " = ? or " + DataProvider.COL_RECEIVER_PHONE + " = ?",
                        new String[]{profilePhone, profilePhone});
                getContentResolver().delete(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE,String.valueOf(profileId)),null,null);
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.talk:
                Intent intentTalk = new Intent(this, RecognitionActivity.class);
                intentTalk.setAction(RecognitionActivity.PHONE_REPLY);
                intentTalk.putExtra(RecognitionActivity.REPLY_NUMBER, profilePhone);
                intentTalk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentTalk.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intentTalk);
        }
        return super.onOptionsItemSelected(item);
    }


   @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.send_btn:
                String newText = emojiconEditText.getText().toString();
                Message message=new Message();
                message.setText(newText);
                message.setSender(Commons.getPhoneNumber());
                message.setReceiver(profilePhone);
                message.setType(DataProvider.MessageType.OUTGOING.ordinal());
                message.send(this);
                message.register(this);
                emojiconEditText.getText().clear();
                break;
        }
    }

    @Override
    public String getProfilePhone() {
        return profilePhone;
    }

    @Override
    protected void onPause() {
        ContentValues values = new ContentValues(1);
        values.put(DataProvider.COL_COUNT, 0);
        getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, String.valueOf(profileId)), values, null, null);
        Commons.setCurrentChat("");
        Log.i("currentChat", Commons.getCurrentChat());
        super.onPause();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    protected void onResume() {
        super.onResume();
        Commons.setCurrentChat(profilePhone);
        NotificationManager notificationManager =(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i("currentChat",Commons.getCurrentChat());
        notificationManager.cancel((int) profileId);
    }

    @Override
    protected void onDestroy() {
        //unregisterReceiver(registrationStatusReceiver);
        gcmUtil.cleanup();
        super.onDestroy();
    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId){
        iconToBeChanged.setImageResource(drawableResourceId);
    }
    private void prepareEmojis() {
        //emojis
        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        rootView = findViewById(R.id.root_view);
        emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        popup = new EmojiconsPopup(rootView, this);
        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();
       // RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) rootView.getLayoutParams();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.emoticon);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiconEditText == null || emojicon == null) {
                    return;
                }

                int start = emojiconEditText.getSelectionStart();
                int end = emojiconEditText.getSelectionEnd();
                if (start < 0) {
                    emojiconEditText.append(emojicon.getEmoji());
                } else {
                    emojiconEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emojiconEditText.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if(!popup.isShowing()){

                    //If keyboard is visible, simply show the emoji popup
                    if(popup.isKeyBoardOpen()){
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else{
                        emojiconEditText.setFocusableInTouchMode(true);
                        emojiconEditText.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else{
                    popup.dismiss();
                }
            }
        });
    }

}