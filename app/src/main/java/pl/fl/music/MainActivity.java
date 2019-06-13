package pl.fl.music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import java.io.FileNotFoundException;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private int chosen_track = -1;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;
    private MediaPlayer mediaPlayer;
    ListView musicMediaListView;
    Cursor musicMediaCursor;
    TextView titleTextView;
    TextView artistTextView;
    TextView durationn;
    TextView actualTime;
    SeekBar bar;
    Boolean start;
    ImageView cover, cover1;
    Uri[] ALBUMS_URI;

    ImageButton play, pause, play_main, pause_main, previous, next;
    private SlidingUpPanelLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ALBUMS_URI = new Uri[]{

                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, MediaStore.Audio.Albums.INTERNAL_CONTENT_URI
        };
        start = true;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = new MediaPlayer();

        checkForPermissions();
        initLaout();

        musicMediaCursor = getMusicMediaCursor();
        musicMediaListView.setAdapter(new MusicMediaCursorAdapter(this, musicMediaCursor));

        musicMediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                musicMediaCursor.moveToPosition(position);
                String path = musicMediaCursor.getString(musicMediaCursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                if (position != chosen_track) {
                    start = false;
                    playFromPath(path);
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mLayout.setPanelState(PanelState.EXPANDED);
                    } else {
                        start = false;
                        play_music_change_buttons();
                    }
                }
                chosen_track = position;
            }
        });


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play_music_change_buttons();
            }
        });
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.i("seekbar", String.valueOf(progress));
                    mediaPlayer.seekTo(mediaPlayer.getDuration() * progress / 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mediaPlayer.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {

                    }
                }
            }
        }).start();

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause_music_change_buttons();
            }
        });

        play_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play_music_change_buttons();
            }
        });

        pause_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause_music_change_buttons();
            }
        });

        next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chosen_track += 1;
                        musicMediaCursor.moveToNext();
                        String path = musicMediaCursor.getString(musicMediaCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        playFromPath(path);
                    }
                }
        );

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chosen_track != 0) {
                    chosen_track -= 1;
                    musicMediaCursor.moveToPrevious();

                }
                String path = musicMediaCursor.getString(musicMediaCursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                playFromPath(path);

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
            mLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    private Cursor getMusicMediaCursor() {
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1",
                null,
                MediaStore.Audio.Media.TITLE + " ASC");
        cursor.moveToFirst();
        return cursor;
    }

    private void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void pause_music_change_buttons() {
        pause.setVisibility(View.GONE);
        play.setVisibility(View.VISIBLE);
        mediaPlayer.pause();
        pause_main.setVisibility(View.GONE);
        play_main.setVisibility(View.VISIBLE);

    }

    private void play_music_change_buttons() {
        if (chosen_track == -1 && start) {
            musicMediaCursor.moveToFirst();
            String path = musicMediaCursor.getString(musicMediaCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            chosen_track = 0;
            playFromPath(path);
        }
        play_main.setVisibility(View.GONE);
        pause_main.setVisibility(View.VISIBLE);
        mediaPlayer.start();
        play.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
    }

    private String createTimeLabel(Integer time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;

    }

    private void playFromPath(String path) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            String title = musicMediaCursor.getString(musicMediaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = musicMediaCursor.getString(musicMediaCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            titleTextView.setText(title);
            artistTextView.setText(artist);
            durationn.setText(createTimeLabel(mediaPlayer.getDuration()));
            actualTime.setText("0:00");
            long albumId = musicMediaCursor.getLong(musicMediaCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            Uri ar = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
            Log.i("zz", String.valueOf(ar));
            cover1.setImageURI(ar);
            cover.setImageURI(ar);
            if (cover.getDrawable() == null) {
                cover1.setImageResource(R.drawable.pobrane);
                cover.setImageResource(R.drawable.pobrane);

            }

        } catch (Exception e) {
            e.printStackTrace();
            cover1.setImageResource(R.drawable.pobrane);
        }
        play_music_change_buttons();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        int lastTime = 0;

        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            if (mediaPlayer.getDuration() != 0) {
                int aaaaa = (currentPosition * 100) / (mediaPlayer.getDuration());
                bar.setProgress(aaaaa);
            }
            if (chosen_track != -1 && mediaPlayer.getDuration() / 1000 <= mediaPlayer.getCurrentPosition() / 1000 && !mediaPlayer.isPlaying()) {
                chosen_track += 1;
                musicMediaCursor.moveToNext();
                String path = musicMediaCursor.getString(musicMediaCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                playFromPath(path);
            }
            lastTime = currentPosition;
            String elapsedTime = createTimeLabel(currentPosition);
            actualTime.setText(elapsedTime);
        }
    };

    private void initLaout() {
        titleTextView = (TextView) findViewById(R.id.songs_title);
        artistTextView = (TextView) findViewById(R.id.songs_artist_name);
        durationn = (TextView) findViewById(R.id.endTime);
        actualTime = (TextView) findViewById(R.id.StartTime);
        cover = (ImageView) findViewById(R.id.songs_cover_one);
        cover1 = (ImageView) findViewById(R.id.duze);
        play = (ImageButton) findViewById(R.id.play_button);
        pause = (ImageButton) findViewById(R.id.pause_button);
        play_main = (ImageButton) findViewById(R.id.play_button_main);
        pause_main = (ImageButton) findViewById(R.id.pause_button_main);
        previous = (ImageButton) findViewById(R.id.previous);
        next = (ImageButton) findViewById(R.id.next);
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.activity_main);
        bar = (SeekBar) findViewById(R.id.seekBar3);
        musicMediaListView = (ListView) findViewById(R.id.music_media_listview);
    }

}