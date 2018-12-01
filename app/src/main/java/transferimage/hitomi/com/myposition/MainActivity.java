package transferimage.hitomi.com.myposition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.SpatialRelationUtil;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, AMap.OnMapLoadedListener {
    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    private static final String TAG = "MianActivity";
    MapView mMapView;
    AMap aMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*String sHA1 = sHA1(this);
        Log.e(TAG, "onCreate: "+sHA1 );*/
        //定义了一个地图view
        mMapView =  findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
       if(aMap==null){
           aMap = mMapView.getMap();
           // 设置定位监听
           aMap.setLocationSource(this);
           // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
           aMap.setMyLocationEnabled(true);
           // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
           aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
           aMap.setOnMapLoadedListener(this);
           aMap.showIndoorMap(true);
           //SetLanguage();
            //添加Line 线
           AddLine();

           // 获取轨迹坐标点
           List<LatLng> points = readLatLngs();
           LatLngBounds bounds = new LatLngBounds(points.get(0), points.get(points.size() - 2));
           aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
           SmoothMoveMarker smoothMarker = new SmoothMoveMarker(aMap);
            // 设置滑动的图标
           smoothMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
           LatLng drivePoint = points.get(0);
           Pair<Integer, LatLng> pair = SpatialRelationUtil.calShortestDistancePoint(points, drivePoint);
           points.set(pair.first, drivePoint);
           List<LatLng> subList = points.subList(pair.first, points.size());

          // 设置滑动的轨迹左边点
           smoothMarker.setPoints(subList);
          // 设置滑动的总时间
           smoothMarker.setTotalDuration(40);
        // 开始滑动
           smoothMarker.startSmoothMove();
       }
    }

    private List<LatLng> readLatLngs() {
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(39.999391,116.135972));
        latLngs.add(new LatLng(39.898323,116.057694));
        latLngs.add(new LatLng(39.900430,116.265061));
        latLngs.add(new LatLng(39.955192,116.140092));
        return latLngs;
    }

    private void AddLine() {
        List<LatLng> latLngs = readLatLngs();
        aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));
     }

    private void SetLanguage() {
        //设置英文
        aMap.setMapLanguage(AMap.ENGLISH);
    }

    private void setUp(AMap amap) {
        UiSettings uiSettings = amap.getUiSettings();
        amap.showIndoorMap(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setScaleControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        //设置指南针
        uiSettings.setCompassEnabled(true);
        //控制比例尺控件是否显示
        uiSettings.setScaleControlsEnabled(false);
        //设置logo位置
        uiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        if(mMapView!=null){
            mMapView.onResume();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        if(mMapView!=null){
            mMapView.onPause();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位

           /* //绘制marker
            LatLng latLng = new LatLng(39.906901,116.397972);
            final Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).title("北京").snippet("DefaultMarker").draggable(true));*/
            //绘制自定义marker
           // ZiDingYiMarker();

        }
    }

    private void ZiDingYiMarker() {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(new LatLng(34.341568,108.940174));
        markerOption.title("西安市").snippet("西安市：34.341568, 108.940174");

        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(),R.drawable.huang)));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(true);//设置marker平贴地图效果
        aMap.addMarker(markerOption);
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null&&amapLocation != null) {
            if (amapLocation != null
                    &&amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        if(mMapView!=null){
            mMapView.onDestroy();
        }
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }

    @Override
    public void onMapLoaded() {
        setUp(aMap);
    }

    public void biaozhun(View view) {
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
    }

    public void weixing(View view) {
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
    }

    public void yejian(View view) {
        aMap.setMapType(AMap.MAP_TYPE_NIGHT);
    }

    public void daohang(View view) {
        aMap.setMapType(AMap.MAP_TYPE_NAVI);
    }

    public void lixianditu(View view) {
        //在Activity页面调用startActvity启动离线地图组件
        startActivity(new Intent(this.getApplicationContext(),
                com.amap.api.maps.offlinemap.OfflineMapActivity.class));
    }


    //截图
    /**
     * 对地图进行截屏
     */

    public void ScroonJieTu(){
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {

            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int status) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                if(null == bitmap){
                    return;
                }
                try {
                    FileOutputStream fos = new FileOutputStream(
                            Environment.getExternalStorageDirectory() + "/test_"
                                    + sdf.format(new Date()) + ".png");
                    boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    try {
                        fos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    StringBuffer buffer = new StringBuffer();
                    if (b)
                        buffer.append("截屏成功 ");
                    else {
                        buffer.append("截屏失败 ");
                    }
                    if (status != 0)
                        buffer.append("地图渲染完成，截屏无网格");
                    else {
                        buffer.append( "地图未渲染完成，截屏有网格");
                    }
                    ToastUtils.showToast(MainActivity.this, buffer.toString());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void jietu(View view) {
        ScroonJieTu();
    }

}
