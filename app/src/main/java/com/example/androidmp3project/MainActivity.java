package com.example.androidmp3project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{
    private MusicAdapter adapter;
    private MusicAdapter adapter_like;
/////////////////////////////////////////////////////////
    private MediaPlayer mPlayer;
    private MusicDBHelper dbHelper;
/////////////////////////////////////////////////////////////////////////////
    private FrameLayout frameLayout_main;
    private DrawerLayout drawerLayout;
    private Fragment_list fragment_list;
    private Fragment_like fragment_like;
//////////////////////////////////////////////////////////////////////////////
    private ArrayList<MusicData> musicList = new ArrayList<>();
    private ArrayList<MusicData> likeList = new ArrayList<>();
    private ArrayList<MusicData> musicLikeArrayList = new ArrayList<>();
    private ArrayList<MusicData> arrayList = new ArrayList<>();
    private MediaPlayer mediaPlayer = new MediaPlayer();
/////////////////////////////////////////////////////////////////////////////
    private BottomNavigationView mainBar;
    private ImageView ivAlbum;
    private TextView tvStartTime,tvTitle,tvArtist,tvDuration;
    private SeekBar seekBar;
    private ImageButton btnBack, btnPlay, btnNext,btnLike;
    private MainActivity mainActivity;
    private MusicData musicData;
    private int index;
    private boolean flag;
    private boolean nowPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);

        findViewByIdFunc();

        dbHelper = MusicDBHelper.getInstance(getApplicationContext());

        musicList = dbHelper.compareArrayList();
        likeList = dbHelper.saveLikeList();

        dbHelper.insertMusicDataToDB(musicList);


        eventHandlerFunc();

        changFragment();
    }
    //아이디 연결
    private void findViewByIdFunc() {
        frameLayout_main = findViewById(R.id.frameLayout_main);
        ivAlbum = findViewById(R.id.ivAlbum);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        btnBack = findViewById(R.id.btnBack);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        seekBar = findViewById(R.id.seekBar);
        tvDuration = findViewById(R.id.tvDuration);
        mainBar = findViewById(R.id.mainBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        btnLike = findViewById(R.id.btnLike);

        fragment_list = new Fragment_list();
        fragment_like = new Fragment_like();
    }
    //화면전환
    public void changFragment() {

        mainBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuList:
                        setFragmentChange(1);
                        break;
                    case R.id.menuLike:
                        setFragmentChange(2);
                        break;
                    default:
                        Log.d("MainActivity", "menuBar error");
                        break;
                }
                return true;
            }
        });
        setFragmentChange(1);

    }
    //프레그먼트 전환
    public void setFragmentChange(int i) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (i) {
            case 1:
                ft.replace(R.id.frameLayout_main, fragment_list);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.frameLayout_main, fragment_like);
                ft.commit();
                break;
        }

    }
    // 이벤트 함수
    private void eventHandlerFunc() { //MainActivity 에서 playbackground로 연결함수

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean flag) {
                if(flag){
                    mediaPlayer.seekTo(position);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnPlay.setOnClickListener((View v) -> {
            if(nowPlaying == true) {
                nowPlaying = false;
                mediaPlayer.pause();
                btnPlay.setImageResource(R.drawable.play);
            } else {
                nowPlaying = true;
                mediaPlayer.start();
                btnPlay.setImageResource(R.drawable.pause);
                seekbarThread();
            }
        });
        btnBack.setOnClickListener((View v) -> {
            SimpleDateFormat sdfS = new SimpleDateFormat("ss");
            int nowDurationForSec =  Integer.parseInt(sdfS.format(mediaPlayer.getCurrentPosition()));

            mediaPlayer.stop();
            mediaPlayer.reset();
            nowPlaying =false;
            btnPlay.setImageResource(R.drawable.play);
            try {
                if(nowDurationForSec <=5) {
                    if(index == 0)  {
                        index = musicList.size() -1;
                        setPlayData(index, true);
                    } else {
                        index--;
                        setPlayData(index, true);
                    }
                } else {
                    setPlayData(index, true);
                }
            } catch (Exception e) {
                Log.d("Prev", e.getMessage());
            }
        });
        btnNext.setOnClickListener((View v) -> {
            mediaPlayer.stop();
            mediaPlayer.reset();
            nowPlaying =false;
            btnPlay.setImageResource(R.drawable.play);
            try {
                if(index == musicList.size() -1) {
                    index = 0;
                    setPlayData(index, true);
                } else {
                    index++;
                    setPlayData(index, true);
                }
            } catch (Exception e) {
                Log.d("Next", e.getMessage());
            }
        });

        //좋아요 버튼
        btnLike.setOnClickListener((View v) -> {
            try {
                if (musicData.getLiked() == 1) {
                    musicData.setLiked(0);
                    musicLikeArrayList.remove(musicData);
                    btnLike.setImageResource(R.drawable.nolikestar);
                } else {
                    musicData.setLiked(1);
                    musicLikeArrayList.add(musicData);
                    btnLike.setImageResource(R.drawable.likestar);
                }
                dbHelper.updateMusicDataToDB(musicData);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "노래를 골라주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //플레이 데이타
    public void setPlayData(int position,boolean flag){
        index = position;

        drawerLayout.openDrawer(Gravity.LEFT);

        mediaPlayer.stop();
        mediaPlayer.reset();

        MusicAdapter musicAdapter = new MusicAdapter(mainActivity,arrayList);
        //즐겨찾기 리스트에 재생관련
        if(flag == true) {
            musicData = musicList.get(position);
        } else {
            musicData = dbHelper.saveLikeList().get(position);
        }
        //즐겨찾기
        if (musicData.getLiked() == 1) {
            btnLike.setImageResource(R.drawable.likestar);
        } else {
            btnLike.setImageResource(R.drawable.nolikestar);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        tvTitle.setText(musicData.getTitle());
        tvArtist.setText(musicData.getArtist());
        tvDuration.setText(simpleDateFormat.format(Integer.parseInt(musicData.getDuration())));

        Bitmap albumImage = musicAdapter.getAlbumImg(this, Long.parseLong(musicData.getAlbumCover()),200);
        if(albumImage !=null){
            ivAlbum.setImageBitmap(albumImage);
        }else{
            ivAlbum.setImageResource(R.drawable.background);
        }

        Uri musicUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,musicData.getId());
        try {
            mediaPlayer.setDataSource(this, musicUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.pause);
            nowPlaying = true;
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(musicData.getDuration()));
            btnPlay.setActivated(true);

            seekbarThread();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btnNext.callOnClick();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    //시크바 스레드
    private void seekbarThread(){
        Thread thread = new Thread(new Runnable() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
            @Override
            public void run() {
                while (mediaPlayer.isPlaying()){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStartTime.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                        }
                    });
                    SystemClock.sleep(100);
                }
            }
        });
        thread.start();
    }
    // 좋아요 리스트 가져오기
    public ArrayList<MusicData> getLikeList(){

        musicLikeArrayList = dbHelper.saveLikeList();

        if(musicLikeArrayList.isEmpty()){
            Toast.makeText(getApplicationContext(), "가져오기 실패", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "가져오기 성공", Toast.LENGTH_SHORT).show();
        }

        return musicLikeArrayList;
    }
}
