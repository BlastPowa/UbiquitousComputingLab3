package com.example.uclab3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import java.text.DateFormat;
import java.util.Date;

public class MotionService extends Service implements SensorEventListener {
    public static final String NOTIFICATION_CHANNEL_ID = "motion";
    SensorManager sensorManager;
    NotificationManager notificationManager;
    HandlerThread sensorThread;
    boolean firstReading = true;
    float lastX;
    float lastY;
    float lastZ;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.monitoring))
                .build();
        startForeground(1, notification);

        sensorThread = new HandlerThread("AccelerometerThread");
        sensorThread.start();
        Handler handler = new Handler(sensorThread.getLooper());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, handler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!firstReading) {
            float x = event.values[0] - lastX;
            float y = event.values[1] - lastY;
            float z = event.values[2] - lastZ;
            double movement = Math.sqrt(x * x + y * y + z * z);
            if (movement > 5) {
                String time = DateFormat.getDateTimeInstance().format(new Date());
                Intent intent = new Intent(this, MotionActivity.class);
                intent.putExtra("time", time);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getString(R.string.motion_detected))
                        .setContentText(time)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();
                notificationManager.notify(2, notification);
            }
        }
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];
        firstReading = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        sensorThread.quitSafely();
        super.onDestroy();
    }
}
