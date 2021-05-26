package com.dullyoung.wifip2p.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dullyoung.wifip2p.BaseExtendAdapter;
import com.dullyoung.wifip2p.PermissionHelper;
import com.dullyoung.wifip2p.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {


    @BindView(R.id.rv_list)
    RecyclerView mRvList;
    @BindView(R.id.btn_scan)
    Button mBtnScan;
    @BindView(R.id.btn_create)
    Button mBtnCreate;
    @BindView(R.id.btn_found)
    Button mBtnFound;
    @BindView(R.id.ll_bottom)
    LinearLayout mLlBottom;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        initRv();
        initWifiP2p();
        getPermissionHelper().setMustPermissions(new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CAMERA

        });
        getPermissionHelper().checkAndRequestPermission(this, new PermissionHelper.OnRequestPermissionsCallback() {
            @Override
            public void onRequestPermissionSuccess() {
                Log.i(TAG, "onRequestPermissionSuccess: ");
            }

            @Override
            public void onRequestPermissionError() {
                Log.i(TAG, "onRequestPermissionError: ");
            }
        });
    }

    private String TAG = "aaaa";
    Wifip2pReceiver mWifip2pReceiver;
    IntentFilter intentFilter;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;

    ItemAdapter mItemAdapter;

    @SuppressLint("MissingPermission")
    private void initRv() {
        mItemAdapter = new ItemAdapter(null);
        mRvList.setAdapter(mItemAdapter);
        mRvList.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mItemAdapter.setOnItemClickListener((adapter, view, position) -> {
            WifiP2pDevice device = mItemAdapter.getData().get(position);
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, "connect onSuccess: ");
                }

                @Override
                public void onFailure(int reason) {
                    //failure logic
                    Log.i(TAG, "connect onFailure: ");
                }
            });
        });
    }

    private void startService() {
        new Thread() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(8000);
                    Log.i(TAG, "startService: " + JSONObject.toJSONString(serverSocket));
                    serverSocket.accept();
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "链接建立完成", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initWifiP2p() {
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), () -> {
            Log.i(TAG, "initWifiP2p: ");
        });
        mWifip2pReceiver = new Wifip2pReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifip2pReceiver, intentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifip2pReceiver);
    }

    private void connectService(WifiP2pInfo info) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(info.groupOwnerAddress, 8000);
                    if (socket.isConnected()) {
                        Log.i(TAG, "sockect 已连接" + info.groupOwnerAddress + ":8000");
                    } else {
                        Log.i(TAG, "run:  connectService" + info.groupOwnerAddress + ":8000");
                        socket.connect(new InetSocketAddress(info.groupOwnerAddress, 8000), 0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        ;
    }


    private class Wifip2pReceiver extends BroadcastReceiver {

        public Wifip2pReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
            mManager = manager;
            mChannel = channel;
            mActivity = activity;
        }

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private MainActivity mActivity;

        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.i(TAG, "onReceive:  Wifi P2P is enabled");
                    startConnect();
                } else {
                    Log.i(TAG, "onReceive: Wi-Fi P2P is not enabled");
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (mManager != null) {
                    mManager.requestPeers(channel, peers -> {
                        List<WifiP2pDevice> devices = new ArrayList<>(peers.getDeviceList());

                        mItemAdapter.setNewInstance(devices);
                        Log.i(TAG, "onPeersAvailable: " + JSONObject.toJSONString(peers.getDeviceList()));
                    });
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    startConnect();
                }
                Log.i(TAG, "onReceive: WIFI_P2P_CONNECTION_CHANGED_ACTION" + JSONObject.toJSONString(networkInfo));
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice wifiP2pDevice = intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Log.i(TAG, "onReceive: WIFI_P2P_THIS_DEVICE_CHANGED_ACTION " + JSONObject.toJSONString(wifiP2pDevice));
                ;
                // Respond to this device's wifi state changing
            }
        }

    }

    private void startConnect() {
        wifiP2pManager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                Log.i(TAG, "onConnectionInfoAvailable: " + info);
                if (info.groupFormed && info.isGroupOwner) {
                    startService();
                } else {
                    if (info.groupFormed) {
                        connectService(info);
                    }
                }
            }
        });
    }


    @SuppressLint("MissingPermission")
    @OnClick({R.id.btn_scan, R.id.btn_create, R.id.btn_found})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                new IntentIntegrator(this).setCaptureActivity(ScanActivity.class).initiateScan();
                break;
            case R.id.btn_found:
                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.i(TAG, "onFailure: " + reason);
                    }
                });
                break;
            case R.id.btn_create:
                startActivity(new Intent(this, CreateActivity.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("扫码结果分析", "Cancelled");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "扫描结果为空", Toast.LENGTH_LONG).show();
            } else {
                String str = result.getContents();
                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ItemAdapter extends BaseExtendAdapter<WifiP2pDevice> {
        public ItemAdapter(List<WifiP2pDevice> data) {
            super(R.layout.item_devices, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, WifiP2pDevice wifiP2pDevice) {
            String text = JSONObject.toJSONString(wifiP2pDevice);
            holder.setText(R.id.tv_text, text);
        }
    }
}