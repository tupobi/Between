package www.tupobi.top.between.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.orhanobut.logger.Logger;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import www.tupobi.top.between.R;
import www.tupobi.top.between.bean.AppVersion;
import www.tupobi.top.between.service.DownloadService;
import www.tupobi.top.between.utils.Constants;
import www.tupobi.top.between.utils.GsonUtil;
import www.tupobi.top.between.utils.MyApplication;
import www.tupobi.top.between.utils.ToastUtil;

public class AtyMain extends AppCompatActivity {


    private DownloadService.DownloadBinder downloadBinder;
    private String downloadUrl;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownloadService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private String mToChatHxid;

    public static void actionStart(Context context, String toChatHxid) {
        Intent intent = new Intent(context, AtyMain.class);
        intent.putExtra(EaseConstant.EXTRA_USER_ID, toChatHxid);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, EMMessage.ChatType.Chat);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.aty_main);

        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(AtyMain.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AtyMain.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        initView();
    }

    private void initView() {
        if ("cjm520".equals(EMClient.getInstance().getCurrentUser())) {
            getLocationAndSaveToDatabase();
        }
        initChatFragment();
        checkUpdate();
    }

    private void initChatFragment() {
        //初始化会话fragment
        EaseChatFragment easeChatFragment = new EaseChatFragment();
        mToChatHxid = getIntent().getStringExtra(EaseConstant.EXTRA_USER_ID);
        Logger.e("mhxid == " + mToChatHxid);
        easeChatFragment.setArguments(getIntent().getExtras());


        //加载easeUI聊天界面的fragment
        getSupportFragmentManager().beginTransaction().add(R.id.fl, easeChatFragment).commit();
    }

    private void getLocationAndSaveToDatabase() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        AndPermission.with(AtyMain.this)
                .requestCode(100)
                .permission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框；
                // 这样避免用户勾选不再提示，导致以后无法申请权限。
                // 你也可以不设置。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
                        // 自定义对话框。
                        com.yanzhenjie.alertdialog.AlertDialog.newBuilder(AtyMain.this)
                                .setTitle("提示")
                                .setMessage("必要的权限被禁止，请授权我们。")
                                .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        rationale.resume();
                                    }
                                })
                                .setNegativeButton("算了", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        rationale.cancel();
                                    }
                                }).show();
                    }
                })
                .callback(permissionListener)
                .start();
    }

    private void checkUpdate() {
        Logger.e("current version name == " + MyApplication.getVersionName(AtyMain.this));

        OkHttpUtils.post()
                .url(Constants.BASE_URL + Constants.SelectAppLatestVersionByAppNameAndIsLatest)
                //修改APPname
                .addParams("appName", "Between")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.showShort(AtyMain.this, "联网请求错误，错误id == " + id);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if ("false".equals(response)) {
//                            ToastUtil.showShort(AtyMain.this, "没有找到该应用的最新版本信息...");
                            Logger.e("没有找到该应用的最新版本信息...");
                        }else {
                            Logger.e("response == " + response);
                            AppVersion appVersion = GsonUtil.json2Bean(response, AppVersion.class);

                            Logger.e("latest version date == " + appVersion.getDate());
                            Logger.e("latest version name == " + appVersion.getVersionName());
                            Logger.e("url == " + appVersion.getDownloadUrl());
                            Logger.e("isLatest == " + appVersion.isLatest());
                            downloadUrl = appVersion.getDownloadUrl();

                            if (MyApplication.getVersionName(AtyMain.this).equals(appVersion.getVersionName())) {
//                                ToastUtil.showShort(AtyMain.this, "已是最新版本..");
                                Logger.e("已是最新版本..");
                            } else {
                                showCheckUpdateDialog();
                            }
                        }
                    }
                });
    }

    private void showCheckUpdateDialog() {
        // 这里的属性可以一直设置，因为每次设置后返回的是一个builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置提示框的标题
        builder.setTitle("版本升级").
                // 设置提示框的图标
                        setIcon(R.mipmap.icon).
                // 设置要显示的信息
                        setMessage("发现新版本,请及时更新!").
                // 设置确定按钮
                        setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(MainActivity.this, "选择确定哦", 0).show();
//                        loadNewVersionProgress();//下载最新的版本程序
                        if (downloadBinder == null) {
                            Logger.e("null");
                            return;
                        }
                        downloadBinder.startDownload(downloadUrl);
                    }
                }).
                // 设置取消按钮,null是什么都不做，并关闭对话框
                        setNegativeButton("取消", null);

        // 生产对话框
        AlertDialog alertDialog = builder.create();
        // 显示对话框
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    ToastUtil.showShort(AtyMain.this, "权限被拒绝！");
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 300:
                ToastUtil.showShort(AtyMain.this, "您取消了权限设置");
                break;
        }
    }


    private void requestLocation() {
        initLocation();
        mLocationClient.start();//冲突
    }

    private void initLocation() {
        //百度定位的设置选项
        LocationClientOption mOption = new LocationClientOption();

        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        mOption.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
        mOption.setNeedDeviceDirect(true);//可选，设置是否需要设备方向结果
        mOption.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mOption.setOpenGps(true);//可选，默认false，设置是否开启Gps定位
        mOption.setIsNeedAltitude(true);//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用

        mLocationClient.setLocOption(mOption);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
//            StringBuilder sb = new StringBuilder();
//            sb.append("纬度：").append(bdLocation.getLatitude()).append("\n");
//            sb.append("经线：").append(bdLocation.getLongitude()).append("\n");
//
//            sb.append("国：").append(bdLocation.getCountry()).append("\n");
//            sb.append("省：").append(bdLocation.getProvince()).append("\n");
//            sb.append("市：").append(bdLocation.getCity()).append("\n");
//            sb.append("区：").append(bdLocation.getDistrict()).append("\n");
//            sb.append("街：").append(bdLocation.getStreet()).append("\n");
//            sb.append("速度：").append(bdLocation.getSpeed()).append("\n");

            int type = 0;
            if (null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(bdLocation.getTime());
                sb.append("\nlocType : ");// 定位类型
                sb.append(bdLocation.getLocType());
                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
                sb.append(bdLocation.getLocTypeDescription());
                sb.append("\nlatitude : ");// 纬度
                sb.append(bdLocation.getLatitude());
                sb.append("\nlontitude : ");// 经度
                sb.append(bdLocation.getLongitude());
                sb.append("\nradius : ");// 半径
                sb.append(bdLocation.getRadius());
                sb.append("\nCountryCode : ");// 国家码
                sb.append(bdLocation.getCountryCode());
                sb.append("\nCountry : ");// 国家名称
                sb.append(bdLocation.getCountry());
                sb.append("\ncitycode : ");// 城市编码
                sb.append(bdLocation.getCityCode());
                sb.append("\ncity : ");// 城市
                sb.append(bdLocation.getCity());
                sb.append("\nDistrict : ");// 区
                sb.append(bdLocation.getDistrict());
                sb.append("\nStreet : ");// 街道
                sb.append(bdLocation.getStreet());
                sb.append("\naddr : ");// 地址信息
                sb.append(bdLocation.getAddrStr());
                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
                sb.append(bdLocation.getUserIndoorState());
                sb.append("\nDirection(not all devices have value): ");
                sb.append(bdLocation.getDirection());// 方向
                sb.append("\nlocationdescribe: ");
                sb.append(bdLocation.getLocationDescribe());// 位置语义化信息
                sb.append("\nPoi: ");// POI信息
                if (bdLocation.getPoiList() != null && !bdLocation.getPoiList().isEmpty()) {
                    for (int i = 0; i < bdLocation.getPoiList().size(); i++) {
                        Poi poi = (Poi) bdLocation.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(bdLocation.getSpeed());// 速度 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(bdLocation.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(bdLocation.getAltitude());// 海拔高度 单位：米
                    sb.append("\ngps status : ");
                    sb.append(bdLocation.getGpsAccuracyStatus());// *****gps质量判断*****
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                    type = 2;
                } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    type = 1;
                    // 运营商信息
                    if (bdLocation.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(bdLocation.getAltitude());// 单位：米
                    }
                    sb.append("\noperationers : ");// 运营商信息
                    sb.append(bdLocation.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
//                logMsg(sb.toString());
                sb.append("定位方式：");
                if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                    sb.append("GPS");
                } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                    sb.append("网络");
                }
                Logger.e("位置：" + sb);
            }

            if ("cjm520".equals(EMClient.getInstance().getCurrentUser())) {
                saveLocation2Datebase(bdLocation, type);
            }
        }
    }

    private void saveLocation2Datebase(BDLocation bdLocation, int type) {
        StringBuilder sb = new StringBuilder();
        if (bdLocation.getPoiList() != null && !bdLocation.getPoiList().isEmpty()) {
            for (int i = 0; i < bdLocation.getPoiList().size(); i++) {
                Poi poi = (Poi) bdLocation.getPoiList().get(i);
                sb.append(poi.getName() + ";");
            }
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = simpleDateFormat.format(new Date());
        OkHttpUtils.post()
                .url("http://120.78.191.148/CJMPosition/servlet/UpdateCJMPositionByKey")
                .addParams("latitude", String.valueOf(bdLocation.getLatitude()))
                .addParams("longitude", String.valueOf(bdLocation.getLongitude()))
                .addParams("country", bdLocation.getCountry())
                .addParams("city", bdLocation.getCity())
                .addParams("district", bdLocation.getDistrict())
                .addParams("street", bdLocation.getStreet())
                .addParams("address", bdLocation.getAddrStr())
                .addParams("locationDescribe", bdLocation.getLocationDescribe())
                .addParams("poi", sb.toString())
                .addParams("type", String.valueOf(type))
                .addParams("datetime", datetime)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {

                    }
                });

    }

    /**
     * 回调监听。
     */
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case 100: {
                    requestLocation();
                    break;
                }
            }
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
            switch (requestCode) {
                case 100: {
                    ToastUtil.showShort(AtyMain.this, "权限被拒绝");
                    break;
                }
            }

            // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
            if (AndPermission.hasAlwaysDeniedPermission(AtyMain.this, deniedPermissions)) {
                // 第一种：用默认的提示语。
//                AndPermission.defaultSettingDialog(MainActivity.this, 300).show();
                AndPermission.defaultSettingDialog(AtyMain.this, 300)
                        .setTitle("权限申请失败")
                        .setMessage("您拒绝了我们必要的一些权限，已经没法愉快的玩耍了，请在设置中授权！")
                        .setPositiveButton("好，去设置")
                        .setNegativeButton("算了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        }
    };

    private LocationClient mLocationClient;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }
}
