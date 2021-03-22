package com.example.androidmp3project;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Fragment_like extends Fragment {
    private ArrayList<MusicData> musicDataArrayList = new ArrayList<>();
    private MusicAdapter musicAdapter;
    private MusicDBHelper dbHelper;
    private RecyclerView recyclerView_like;
    private DrawerLayout drawerLayout;
    private MainActivity mainActivity;

    private Fragment musicPlayer;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_like, container, false);

        findViewByIdFunc(view);
        //MP3 관리하는함수
        dbHelper = MusicDBHelper.getInstance(mainActivity.getApplicationContext());
        musicDataArrayList = dbHelper.saveLikeList();
        //어뎁터 만들기
        makeAdapter(container);
        //음악리스트 가져오기
        musicDataArrayList = dbHelper.compareArrayList();
        //음악 DB저장
        dbHelper.queryMusicTbl(musicDataArrayList);
        //어뎁터 데이터 셋팅하기
        settingAdapterList(mainActivity.getLikeList());
        //이벤트 처리하기
        eventHandler();

        return view;
    }
    //이벤트 처리함수
    private void eventHandler() {
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                mainActivity.setPlayData(position,false);
            }
        });
    }
    //어뎁터에 데이터 셋팅하기
    private void settingAdapterList(ArrayList<MusicData> musicDataArrayList) {
        musicAdapter.setMusicList(musicDataArrayList);
        recyclerView_like.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();
    }
    //어뎁터만들기
    private void makeAdapter(ViewGroup container) {
        musicAdapter = new MusicAdapter(container.getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());

        recyclerView_like.setLayoutManager(linearLayoutManager);
        recyclerView_like.setAdapter(musicAdapter);
        recyclerView_like.setLayoutManager(linearLayoutManager);
    }

    private void findViewByIdFunc(View view) {
        recyclerView_like = view.findViewById(R.id.recyclerView_like);
        drawerLayout = view.findViewById(R.id.drawerLayout);

    }




}
