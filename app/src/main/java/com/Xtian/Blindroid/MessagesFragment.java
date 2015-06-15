/**
 * Created by xtianrock on 15/05/2015.
 */
package com.Xtian.Blindroid;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;

/**
 * Lista de fragments que contienen los
 * mensajes queconforman la pantalla de chat
 */
public class MessagesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private OnFragmentInteractionListener mListener;
    private CursorAdapter chatCursorAdapter;
    private Date now;
    private RoundedBitmapDrawable myPhoto;
    private RoundedBitmapDrawable friendPhoto;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        now = new Date();
        chatCursorAdapter = new ChatCursorAdapter(getActivity(), null);
        setListAdapter(chatCursorAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setDivider(null);
        getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        getListView().setStackFromBottom(true);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        getListView().setLayoutParams(params);
        Bundle args = new Bundle();
        args.putString(DataProvider.COL_PHONE, mListener.getProfilePhone());
        getLoaderManager().initLoader(0, args, this);

        friendPhoto=Commons.getContactPhoto(getActivity(),mListener.getProfilePhone());
        myPhoto=Commons.getContactPhoto(getActivity(),Commons.getPhoneNumber());


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public String getProfilePhone();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String profileEmail = args.getString(DataProvider.COL_PHONE);
        return new CursorLoader(getActivity(),
                DataProvider.CONTENT_URI_MESSAGES,
                new String[]{
                        DataProvider.COL_ID,
                        DataProvider.COL_TYPE,
                        DataProvider.COL_SENDER_PHONE,
                        DataProvider.COL_RECEIVER_PHONE,
                        DataProvider.COL_MESSAGE,
                        "datetime("+DataProvider.COL_TIME+", 'localtime')"},
                DataProvider.COL_SENDER_PHONE + " = ? or " + DataProvider.COL_RECEIVER_PHONE + " = ?",
                new String[]{profileEmail, profileEmail},
                DataProvider.COL_TIME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        chatCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        chatCursorAdapter.swapCursor(null);
    }

    public class ChatCursorAdapter extends CursorAdapter {

        public ChatCursorAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override public int getCount() {
            return getCursor() == null ? 0 : super.getCount();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int _position) {
            Cursor cursor = (Cursor) getItem(_position);
            return getItemViewType(cursor);
        }

        private int getItemViewType(Cursor _cursor) {
            int typeIdx = _cursor.getColumnIndex(DataProvider.COL_TYPE);
            int type = _cursor.getInt(typeIdx);
            return type == 0 ? 0 : 1;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            View itemLayout = null;
            switch(getItemViewType(cursor)){
                case 0:
                    itemLayout = LayoutInflater.from(context).inflate(R.layout.chat_left, parent, false);
                    break;
                case 1:
                    itemLayout = LayoutInflater.from(context).inflate(R.layout.chat_right, parent, false);
                    break;
            }
            itemLayout.setTag(holder);
            holder.photo = (ImageView) itemLayout.findViewById(R.id.avatar);
            holder.time = (TextView) itemLayout.findViewById(R.id.text1);
            holder.message = (TextView) itemLayout.findViewById(R.id.text2);
            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            String phone = cursor.getString(cursor.getColumnIndex(DataProvider.COL_SENDER_PHONE));
            holder.time.setText(Commons.getDisplayTime(cursor.getString(5)));
            holder.message.setText(cursor.getString(cursor.getColumnIndex(DataProvider.COL_MESSAGE)));
            if(phone.equals(Commons.getPhoneNumber()))
            {
                holder.photo.setImageDrawable(myPhoto);
            }
            else
            {
                holder.photo.setImageDrawable(friendPhoto);
            }

        }
    }
    /**
     * Contiene las vistas usadas por el elemento holder
     */
    private static class ViewHolder {
        TextView time;
        TextView message;
        ImageView photo;
    }


}