package pl.fl.music;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class MusicMediaCursorAdapter extends CursorAdapter {

    public MusicMediaCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView title_text = view.findViewById(R.id.item_top_text);
        TextView artist_name_text = view.findViewById(R.id.item_bottom_text);
        title_text.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
        artist_name_text.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
        long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        Uri ar = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
        ImageView a = view.findViewById(R.id.media_image);
//        a.setImageURI(ar);
//        if(a.getDrawable() == null){
            a.setImageResource(R.drawable.pobrane);
//        }

    }
}
