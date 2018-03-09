package www.tupobi.top.between.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.hyphenate.chat.EMClient;
import com.orhanobut.logger.Logger;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import www.tupobi.top.between.R;
import www.tupobi.top.between.utils.ToastUtil;

public class AtySplash extends AppCompatActivity {

    @BindView(R.id.rl_splash_root)
    RelativeLayout mRlSplashRoot;

    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_splash);
        ButterKnife.bind(this);

        //渐变动画
        AlphaAnimation aa = new AlphaAnimation(0, 1);
        //渐变动画参数0,1从看不见到看得见。
//        aa.setDuration(500);
        //设置渐变动画播放时长，500毫秒
        aa.setFillAfter(true);
        //设置渐变动画播放完成后的状态

        //缩放动画
        ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        //X轴缩放从无到有，Y轴缩放从无到有....自身所放
//        sa.setDuration(500);
        sa.setFillAfter(true);

        //旋转动画
        RotateAnimation ra = new RotateAnimation(0, 360, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        //以0，360区间旋转。自身旋转
//        ra.setDuration(500);
        ra.setFillAfter(true);

        //添加这三个动画，没有先后顺序
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(ra);
        animationSet.addAnimation(sa);
        animationSet.addAnimation(aa);
        animationSet.setDuration(1100);
        //覆盖掉上面的500ms动画时间

        //播放动画，动画默认为整个页面？
        mRlSplashRoot.startAnimation(animationSet);

        //设置动画监听
        animationSet.setAnimationListener(new MyAnimationListener());


    }

    private void checkIsLogin() {
        if (EMClient.getInstance().isLoggedInBefore()){
            Logger.e("登陆过：hxid == " + EMClient.getInstance().getCurrentUser());
            String hxid = EMClient.getInstance().getCurrentUser();
            if ("cjm520".equals(hxid)) {
                AtyMain.actionStart(AtySplash.this, "lzj7800623");
//                EaseUser easeUser = new EaseUser("cjm520");
//                easeUser.setAvatar("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=3813947250,918889652&fm=27&gp=0.jpg");
            } else if ("lzj7800623".equals(hxid)) {
                AtyMain.actionStart(AtySplash.this, "cjm520");
            } else {
                ToastUtil.showShort(AtySplash.this, "非法用户..");
            }

        }else {
            AtyLogin.actionStart(AtySplash.this);
        }
    }

    class MyAnimationListener implements Animation.AnimationListener {
        /**
         * 当动画开始播放的时候回调这个方法
         * @param animation
         */
        @Override
        public void onAnimationStart(Animation animation) {
        }

        /**
         * 动画结束时回调该方法
         * @param animation
         */
        @Override
        public void onAnimationEnd(Animation animation) {
            checkIsLogin();
            getLocationAndSaveToDatabase();//start就有问题了我日。
            finish();//关闭splash页面
        }

        /**
         * 动画重复播放时回调该方法
         * @param animation
         */
        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private void getLocationAndSaveToDatabase() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        AndPermission.with(AtySplash.this)
                .requestCode(100)
                .permission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框；
                // 这样避免用户勾选不再提示，导致以后无法申请权限。
                // 你也可以不设置。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
                        // 自定义对话框。
                        com.yanzhenjie.alertdialog.AlertDialog.newBuilder(AtySplash.this)
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
                } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
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
        }
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
                    ToastUtil.showShort(AtySplash.this, "权限被拒绝");
                    break;
                }
            }

            // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
            if (AndPermission.hasAlwaysDeniedPermission(AtySplash.this, deniedPermissions)) {
                // 第一种：用默认的提示语。
//                AndPermission.defaultSettingDialog(MainActivity.this, 300).show();
                AndPermission.defaultSettingDialog(AtySplash.this, 300)
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

    private void requestLocation() {
        initLocation();
//        mLocationClient.start();//冲突
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 300:
                ToastUtil.showShort(AtySplash.this, "您取消了权限设置");
                break;
        }
    }
}
