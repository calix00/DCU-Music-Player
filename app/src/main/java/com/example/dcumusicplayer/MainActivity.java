package com.example.dcumusicplayer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listViewMP3;
    ArrayList<String> mp3files;
    String selectedMP3;
    com.example.dcumusicplayer.ListViewMP3Adapter adapter;

    String mp3path = Environment.getExternalStorageDirectory().getPath() + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_MEDIA_AUDIO}, MODE_PRIVATE);

        mp3files = new ArrayList<>();
        File[] files = new File(mp3path).listFiles();
        String filename, ext;
        assert files != null;
        for(File file : files) {
            filename = file.getName();
            ext = filename.substring(filename.length() - 3);
            Log.i("DCU_MP", filename);
            if(ext.equals("mp3")) {
                mp3files.add(mp3path + filename);
            }
        }

        Log.i("DCU_MP", mp3files.toString());

        listViewMP3 = findViewById(R.id.listViewMP3);
        adapter = new com.example.dcumusicplayer.ListViewMP3Adapter(this, mp3files);
        listViewMP3.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listViewMP3.setAdapter(adapter);
        listViewMP3.setOnItemClickListener((parent, view, position, id) -> {
            selectedMP3 = mp3files.get(position);
            Log.i("DCU_MP", selectedMP3);

            Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
            intent.putExtra("mp3", selectedMP3);
            startActivity(intent);
        });

        class MusicData implements Parcelable{

            private String musicTitle;
            private String singer;
            private Uri musicImg;
            private String albumId;
            private String musicId;

            public String getMusicTitle() {
                return musicTitle;
            }

            public void setMusicTitle(String musicTitle) {
                this.musicTitle = musicTitle;
            }

            public String getSinger() {
                return singer;
            }

            public void setSinger(String singer) {
                this.singer = singer;
            }

            public Uri getMusicImg() {
                return musicImg;
            }

            public void setMusicImg(Uri musicImg) {
                this.musicImg = musicImg;
            }

            public String getAlbumId() {
                return albumId;
            }

            public void setAlbumId(String albumId) {
                this.albumId = albumId;
            }

            public String getMusicId() {
                return musicId;
            }

            public void setMusicId(String musicId) {
                this.musicId = musicId;
            }

            @Override
            public void writeToParcel(Parcel parcel, int i) {
                parcel.writeString(musicId);
                parcel.writeString(albumId);
                parcel.writeString(musicImg.toString());
                parcel.writeString(musicTitle);
                parcel.writeString(singer);

            }

            @Override
            public int describeContents() {
                return 0;
            }

            public MusicData() {

            }

            public MusicData(Parcel in) {
                readFromParcel(in);
            }


            private void readFromParcel(Parcel in){
                musicId = in.readString();
                albumId = in.readString();
                musicImg = Uri.parse(in.readString());
                musicTitle = in.readString();
                singer = in.readString();
            }

            public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
                public MusicData createFromParcel(Parcel in) {
                    return new MusicData(in);
                }

                public MusicData[] newArray(int size) {
                    return new MusicData[size];
                }
            };
        }

        public class MusicService extends Service{

            private final String TAG = "MusicService";

            private ArrayList<MusicData> list;
            private MediaPlayer mediaPlayer;
            private RemoteViews contentView;

            private NotificationManager mNoti;
            private Notification noti;

            private IntentFilter filter;
            private int nowPosotion;

            public static String MUSIC_SERVICE_FILTER = "MUSIC_SERVICE_FILTER";

            String MUSIC_PREV = "MUSIC_PREV";
            String MUSIC_NOW = "MUSIC_NOW";
            String MUSIC_NEXT = "MUSIC_NEXT";
            String MUSIC_CLOSE = "MUSIC_CLOSE";


            @Override
            public void onDestroy() {
                super.onDestroy();
                Log.e(TAG, "MusicService onDestroy");
                unregisterReceiver(btnReceiver);
                mediaPlayer.release();
                mediaPlayer = null;
                stopForeground(true);
            }

            @Override
            public void onCreate() {
                super.onCreate();

                mediaPlayer = new MediaPlayer();
                list = new ArrayList<>();

                filter = new IntentFilter();
                filter.addAction(MUSIC_PREV);
                filter.addAction(MUSIC_NOW);
                filter.addAction(MUSIC_NEXT);
                filter.addAction(MUSIC_CLOSE);

                registerReceiver(btnReceiver, filter);

                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        Log.e(TAG, "mediaPlayer error i : " + i + " , i1 : " + i1);
                        return false;
                    }
                });


            }

            private void MusicOn(int position) {

                mediaPlayer.reset();
                Uri musicURI = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + list.get(position).getMusicId());
                mediaPlayer = MediaPlayer.create(this, musicURI);
                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Log.e(TAG, "onCompletion");

                        if(list.size() == nowPosotion + 1){
                            nowPosotion = 0;
                        } else{
                            nowPosotion += 1;
                        }

                        ChangeNotiInfomation();
                        MusicOn(nowPosotion);
                    }
                });
            }

            private void ChangeNotiInfomation() {

                contentView.setTextViewText(R.id.txt_title_pend, list.get(nowPosotion).getMusicTitle());
                contentView.setTextViewText(R.id.txt_singer_pend, list.get(nowPosotion).getSinger());
                contentView.setImageViewUri(R.id.img_pend, list.get(nowPosotion).getMusicImg());

                noti.bigContentView = contentView;
                startForeground(2127, noti);
            }


            @Nullable
            @Override
            public IBinder onBind(Intent intent) {
                Log.e(TAG, "onBind");
                return null;
            }

            @Override
            public int onStartCommand(Intent intent, int flags, int startId) {

                nowPosotion = intent.getExtras().getInt("position");
                Log.e(TAG, "message : " + nowPosotion);

                list = intent.getParcelableArrayListExtra("list");
                Log.e(TAG, "message : " + list.get(nowPosotion).getMusicTitle());

                Intent tent = new Intent(MUSIC_SERVICE_FILTER);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, tent, PendingIntent.FLAG_UPDATE_CURRENT);

                contentView = new RemoteViews(getPackageName(), R.layout.layout_notification);

                Intent prevIntent = new Intent(MUSIC_PREV);
                Intent nowIntent = new Intent(MUSIC_NOW);
                Intent nextIntent = new Intent(MUSIC_NEXT);
                Intent closeIntent = new Intent(MUSIC_CLOSE);

                PendingIntent pdIntentPrev = PendingIntent.getBroadcast(this, 0, prevIntent, 0);
                PendingIntent pdIntentNow = PendingIntent.getBroadcast(this, 0, nowIntent, 0);
                PendingIntent pdIntentNext = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
                PendingIntent pdIntentClose = PendingIntent.getBroadcast(this, 0, closeIntent, 0);

                contentView.setTextViewText(R.id.txt_title_pend, list.get(nowPosotion).getMusicTitle());
                contentView.setTextViewText(R.id.txt_singer_pend, list.get(nowPosotion).getSinger());
                contentView.setImageViewUri(R.id.img_pend, list.get(nowPosotion).getMusicImg());

                contentView.setOnClickPendingIntent(R.id.music_prev, pdIntentPrev);
                contentView.setOnClickPendingIntent(R.id.music_now, pdIntentNow);
                contentView.setOnClickPendingIntent(R.id.music_next, pdIntentNext);
                contentView.setOnClickPendingIntent(R.id.music_close, pdIntentClose);

                Notification.Builder mBuilder = new Notification.Builder(this);

                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mBuilder.setWhen(System.currentTimeMillis());
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setContent(contentView);
                noti = mBuilder.build();
                noti.flags = Notification.FLAG_NO_CLEAR;

                final Notification.BigPictureStyle big = new Notification.BigPictureStyle();



                MusicOn(nowPosotion);
//        mNoti.notify(2127, noti);
                startForeground(2127, noti);

                return START_NOT_STICKY;
            }

            BroadcastReceiver btnReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();

                    if (action.equals(MUSIC_PREV)) {
                        Log.e(TAG, "MUSIC_PREV");

                        if (nowPosotion > 0) {

                            nowPosotion -= 1;
                            ChangeNotiInfomation();
                            MusicOn(nowPosotion);
                        } else {    //다시 시작으로
                            ChangeNotiInfomation();
                            MusicOn(nowPosotion);
                        }
                    }

                    if (action.equals(MUSIC_NOW)) {
                        Log.e(TAG, "MUSIC_NOW");
                    }

                    if (action.equals(MUSIC_NEXT)) {
                        Log.e(TAG, "MUSIC_NEXT");
                        nowPosotion += 1;
                        ChangeNotiInfomation();
                        MusicOn(nowPosotion);
                    }

                    if (action.equals(MUSIC_CLOSE)) {
                        Log.e(TAG, "MUSIC_CLOSE");
//                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancel(2127);
                        stopForeground(true);
                        mediaPlayer.reset();

                        Intent closeIntent = new Intent(MUSIC_SERVICE_FILTER);
                        sendBroadcast(closeIntent);
                    }
                }
            };

            private AudioManager.OnAudioFocusChangeListener focusChangeListener =
                    new AudioManager.OnAudioFocusChangeListener() {
                        public void onAudioFocusChange(int focusChange) {
                            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            switch (focusChange) {

                                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                                    Log.e(TAG, "OnAudioFocusChangeListener AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                                    break;
                                case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                                    Log.e(TAG, "OnAudioFocusChangeListener AUDIOFOCUS_LOSS_TRANSIENT");
                                    break;

                                case (AudioManager.AUDIOFOCUS_LOSS):
                                    Log.e(TAG, "OnAudioFocusChangeListener AUDIOFOCUS_LOSS");

                                    mediaPlayer.stop();
                                    contentView.setImageViewResource(R.id.music_now, R.drawable.play_pause);

                                    noti.bigContentView = contentView;
                                    startForeground(2127, noti);

                                    break;

                                case (AudioManager.AUDIOFOCUS_GAIN):
                                    Log.e(TAG, "OnAudioFocusChangeListener AUDIOFOCUS_GAIN");
                                    break;
                                default:
                                    break;
                            }
                        }
                    };

            @Override
            public void onTaskRemoved(Intent rootIntent) {
                super.onTaskRemoved(rootIntent);
                Log.e(TAG, "onTaskRemoved");
            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                super.onConfigurationChanged(newConfig);
                Log.e(TAG, "onConfigurationChanged");
            }

            @Override
            public boolean onUnbind(Intent intent) {

                Log.e(TAG, "onUnbind");
                return super.onUnbind(intent);
            }
        }

    }
}