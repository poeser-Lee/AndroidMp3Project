package com.example.androidmp3project;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Fragment_list extends Fragment {

    private ArrayList<MusicData> musicDataArrayList = new ArrayList<MusicData>();
    private MusicAdapter adapter;
    private MusicDBHelper musicDB;
    private RecyclerView recyclerView_playList;
    private MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        //객체 찾는 함수
        findViewByIdFunc(view);

        //MP3 파일 관리하는 함수
        musicDB= MusicDBHelper.getInstance(mainActivity.getApplicationContext());
        musicDataArrayList = musicDB.findMusic();

        //어뎁터 만들기
        makeAdapter(container);

        //음악리스트 가져오기
        musicDataArrayList = musicDB.compareArrayList();
        //음악DB저장
        musicDB.insertMusicDataToDB(musicDataArrayList);
        //어뎁터에 데이터 세팅하기
        settingAdapterDataList(musicDataArrayList);

        //
        eventHandler();

        return view;
    }

    public void eventHandler() {
        adapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(View view, int position) {
                mainActivity.setPlayData(position, true);
            }
        });
    }

    public void settingAdapterDataList(ArrayList<MusicData> musicDataArrayList) {

        adapter.setMusicList(musicDataArrayList);

        recyclerView_playList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    //어뎁터 만들기
    public void makeAdapter(ViewGroup container) {

        adapter = new MusicAdapter(getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());
        recyclerView_playList.setAdapter(adapter);
        recyclerView_playList.setLayoutManager(linearLayoutManager);

    }


    //View 아이디 연결
    public void findViewByIdFunc(View view) {
        recyclerView_playList = view.findViewById(R.id.recyclerView_playList);
    }

}
