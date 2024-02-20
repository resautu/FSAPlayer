package com.resautu.fsaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.resautu.fsaplayer.adapter.MusicBaseAdapter;
import com.resautu.fsaplayer.data.ItemData;
import com.resautu.fsaplayer.data.ServerData;
import com.resautu.fsaplayer.databinding.ActivityMainBinding;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FSAPlayerApplication app;
    private TextView serverDesc;
    private ImageView serverImage;
    private ListView audioList;
    private TextView displayName;
    private List<ItemData> audioItemList;
    private String serverAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = FSAPlayerApplication.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        serverDesc = binding.appBarMain.serverDesc;
        serverImage = binding.appBarMain.serverImage;
        audioList = binding.appBarMain.listView;
        displayName = binding.navView.getHeaderView(0).findViewById(R.id.displayName);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_login)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        audioList.setOnItemClickListener(this);

        app.getDisplayName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    Snackbar.make(binding.getRoot(), "当前服务器地址ID：" + app.httpClient.getServerID(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    displayName.setText(s);
                    ServerData serverData = app.getServerData();
                    serverDesc.setText(serverData.getServerDesc());
                    audioItemList = serverData.getAudioList();
                    app.setAudioItemList(audioItemList);
                    audioList.setAdapter(new MusicBaseAdapter(MainActivity.this, audioItemList));
                    serverAddress = app.httpClient.getServerAddress();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, PlayerActivity.class);
        ItemData item = audioItemList.get(position);
        intent.putExtra("Id", position);
        intent.putExtra("Desc", "Artist");
        intent.putExtra("ServerAddress", serverAddress);
        startActivity(intent);
    }
}