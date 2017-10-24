package com.example.admin.myspeedometerapplication;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by Admin on 22.10.2017.
 */

public class LocationService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long INTERVAL = 1000 * 3;
    private static final long FASTEST_INTERVAL = 1000 * 2;

    //используются для запроса качества обслуживания для обновлений местоположения FusedLocationProviderApi.
    LocationRequest mLocationRequest;

    //Основная точка входа для интеграции сервисов Google Play
    GoogleApiClient mGoogleApiClient;

    //получать информацию о местоположении
    Location mCurrentLocation, lStart, lEnd;

    static double distance = 0;
    double speed;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        creatLocationRequest();
        //подключиться к API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    protected void creatLocationRequest() {
        mLocationRequest = new LocationRequest();
        //устанавливает скорость в миллисекундах при которой ваше приложение предпочитает получат обновления местоположения
        mLocationRequest.setInterval(INTERVAL);
        //Этот метод устанавливает максимальную скорость в миллисекундах, при которой ваше приложение может обрабатывать обновления местоположения
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        //какие источники местоположения использовать
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    // начать делать запросы
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    protected void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        distance = 0;
    }

    //Вызывается, когда клиент временно находится в отключенном состоянии
    @Override
    public void onConnectionSuspended(int i) {

    }

    //Вызывается, когда произошла ошибка подключения клиента к службе.
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //Вызывается, когда местоположение изменилось.
    @Override
    public void onLocationChanged(Location location) {
        MainActivity.locate.dismiss();
        mCurrentLocation = location;
        if(lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else {
            lEnd = mCurrentLocation;
        }

        //Вызов метода ниже обновляет текущие значения расстояния и скорости
        updateUI();
        //Получите скорость, если она доступна, в метрах / секунду на земле.
        speed = location.getSpeed() * 18 / 5;
    }

    //«Расстояние и скорость» устанавливается в методе ниже
    private void updateUI() {

        if (MainActivity.p == 0) {
            //Возвращает приблизительное расстояние в метрах между этим местоположением и данным местом
            distance = distance + (lStart.distanceTo(lEnd) / 1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            MainActivity.time.setText("Total Time: " + diff + " minutes");
            if (speed > 0.0)
                MainActivity.speed.setText("Current speed: " + new DecimalFormat("#.##").format(speed) + " km/hr");
            else
                MainActivity.speed.setText(".......");

            MainActivity.dist.setText(new DecimalFormat("#.###").format(distance) + " Km's.");

            lStart = lEnd;

        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdate();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }

    }
}
