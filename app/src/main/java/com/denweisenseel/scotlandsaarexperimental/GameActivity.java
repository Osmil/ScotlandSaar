package com.denweisenseel.scotlandsaarexperimental;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.denweisenseel.scotlandsaarexperimental.adapter.BottomBarAdapter;
import com.denweisenseel.scotlandsaarexperimental.api.RequestBuilder;
import com.denweisenseel.scotlandsaarexperimental.customView.CustomViewPager;
import com.denweisenseel.scotlandsaarexperimental.data.ChatDataParcelable;
import com.denweisenseel.scotlandsaarexperimental.data.GameListInfoParcelable;
import com.denweisenseel.scotlandsaarexperimental.data.GameModelParcelable;
import com.denweisenseel.scotlandsaarexperimental.data.VolleyRequestQueue;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GameActivity extends AppCompatActivity implements ChatFragment.ChatFragmentInteractionListener, OnMapReadyCallback, DashboardFragment.DashboardInteractionListener {

    BottomBarAdapter adapter;
    CustomViewPager pager;
    //CHAT
    private ChatFragment chatFragment;
    private BroadcastReceiver chatMessageReceiver;
    private ArrayList<ChatDataParcelable> chatList;

    int unreadNotficationCounter = 0;

    private SupportMapFragment mapFragment;

    private  DashboardFragment dashboardFragment;

    GameModel gameModel = new GameModel();


    private String TAG = "GameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        pager = (CustomViewPager) findViewById(R.id.viewpager);
        pager.setPagingEnabled(false);

        BottomBarAdapter bottomBarAdapter = new BottomBarAdapter(getSupportFragmentManager());


        mapFragment = MapFragment.newInstance();
        mapFragment.getMapAsync(this);
        bottomBarAdapter.addFragments(mapFragment);

        chatFragment = ChatFragment.newInstance("null","null");
        bottomBarAdapter.addFragments(chatFragment);

        dashboardFragment = DashboardFragment.newInstance("null","null");
        bottomBarAdapter.addFragments(dashboardFragment);

        pager.setAdapter(bottomBarAdapter);

        pager.setCurrentItem(1);



        final AHBottomNavigation navigation = (AHBottomNavigation) findViewById(R.id.navigation);
        //TODO Beim Back Button drücken soll ein Quit Game Dialog angezeigt werden. (2 Hours)

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.game_map, R.drawable.ic_home_black_24dp, R.color.color_tab_1);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.game_chat, R.drawable.ic_notifications_black_24dp, R.color.color_tab_1);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.game_dashboard, R.drawable.ic_dashboard_black_24dp, R.color.color_tab_1);

        navigation.addItem(item1);
        navigation.addItem(item2);
        navigation.addItem(item3);

        navigation.setCurrentItem(1);

        // Set listeners
        navigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch(position) {
                    case 0:
                        if(pager.isActivated()) {
                            pager.setCurrentItem(0);
                        } else {
                            Toast.makeText(GameActivity.this, "Game hasnt started yet", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1: pager.setCurrentItem(1);
                        break;
                    case 2: pager.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });

        navigation.disableItemAtPosition(0);


        chatList = new ArrayList<ChatDataParcelable>();
        chatMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(getString(R.string.LOBBY_PLAYER_JOIN))) {
                    ArrayList<String> argList = intent.getStringArrayListExtra(getString(R.string.BROADCAST_DATA));

                    String playerName = argList.get(0);
                    ChatDataParcelable chatMessage = new ChatDataParcelable(playerName,"joined the lobby", new SimpleDateFormat("HH.mm").format(new Date()));
                    sendToChatFragment(chatMessage);
                    chatList.add(chatMessage);

                } else if(intent.getAction().equals(getString(R.string.LOBBY_PLAYER_MESSAGE))) {
                    ArrayList<String> argList = intent.getStringArrayListExtra(getString(R.string.BROADCAST_DATA));
                    ChatDataParcelable chatMessage = new ChatDataParcelable(argList.get(0),argList.get(1),argList.get(2));
                    chatList.add(chatMessage);
                    sendToChatFragment(chatMessage);
                    if(pager.getCurrentItem() != 1) {
                        unreadNotficationCounter++;
                        AHNotification notification = new AHNotification.Builder()
                                .setText(String.valueOf(unreadNotficationCounter))
                                .setBackgroundColor(ContextCompat.getColor(GameActivity.this, R.color.colorBottomNavigationNotification))
                                .setTextColor(ContextCompat.getColor(GameActivity.this, R.color.colorBottomNavigationDisable))
                                .build();
                        navigation.setNotification(notification, 1);
                    }
                } else if(intent.getAction().equals(getString(R.string.LOBBY_GAME_START))) {
                    Log.v(TAG, "Game Start!");
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(chatMessageReceiver, new IntentFilter(getString(R.string.LOBBY_PLAYER_JOIN)));
        LocalBroadcastManager.getInstance(this).registerReceiver(chatMessageReceiver, new IntentFilter(getString(R.string.LOBBY_PLAYER_MESSAGE)));
        LocalBroadcastManager.getInstance(this).registerReceiver(chatMessageReceiver, new IntentFilter(getString(R.string.LOBBY_GAME_START)));

    }

    private void sendToChatFragment(ChatDataParcelable chatMessage) {
        chatFragment.sendMessage(chatMessage);
    }

    @Override
    public void onFragmentInteraction(ChatDataParcelable chatDataParcelable) {
        //TODO save chat messages (1 hour)
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        //TODO Setup map constraints - Those var values should be finals somewhere! Please redo (5 min)

        LatLngBounds bounds = new LatLngBounds( new LatLng(49.234012, 6.995120),new LatLng(49.237760, 7.006214));
        googleMap.setLatLngBoundsForCameraTarget(bounds);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(49.236127, 7.000402)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo( 16.0f ) );

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    //SEND MoveRequest to server!
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                float minZoom = 16.0f;
                CameraPosition cameraPosition = googleMap.getCameraPosition();
                if(cameraPosition.zoom <minZoom) {
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(minZoom));
                }
            }
        });
    }

    @Override
    public void onStartGame() {

        String gameId = String.valueOf(getSharedPreferences(getString(R.string.gameData), MODE_PRIVATE).getLong(getString(R.string.gameId),0));
        String firebaseToken = FirebaseInstanceId.getInstance().getToken();
        String[] args = {gameId, firebaseToken};

        JsonObjectRequest gameRequest = new JsonObjectRequest(Request.Method.POST, RequestBuilder.buildRequestUrl(RequestBuilder.START_GAME, args ),null, new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                Log.i(TAG, "Started game" + response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
        }
    });

        VolleyRequestQueue.getInstance(this).addToRequestQueue(gameRequest);
    }
}
