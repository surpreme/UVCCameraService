package com.mbcq.cameratest;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.mbcq.cameratest.USBReceiver.ACTION_USB_PERMISSION;

public class MainActivity extends AppCompatActivity {
    private UsbManager usbManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {

            //没有悬浮窗权限m,去开启悬浮窗权限
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, 12);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
        registerReceiver(this);
        UsbDevice usbDevice = getUsbDevice(13028, 37424);
        if (!hasPermission(usbDevice)) {
            requestPermission(usbDevice);
        }
    }

    public void toActivity(View view) {
        startActivity(new Intent(this, PreviewActivity.class));
    }

    public void toWindow(View view) {
        startService(new Intent(this, PreviewService.class));
        finish();
    }

    public void toNoView(View view) {
        startActivity(new Intent(this, NoViewActivity.class));
    }

    /**
     * 判断对应 USB 设备是否有权限
     */
    public boolean hasPermission(UsbDevice device) {
        return usbManager.hasPermission(device);
    }

    /**
     * 请求获取指定 USB 设备的权限
     */
    public void requestPermission(UsbDevice device) {
        if (device != null) {
            if (usbManager.hasPermission(device)) {
                Toast.makeText(this, "已经获取到权限", Toast.LENGTH_SHORT).show();
            } else {
                if (mPermissionIntent != null) {
                    usbManager.requestPermission(device, mPermissionIntent);
                    Toast.makeText(this, "请求USB权限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请注册USB广播", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private PendingIntent mPermissionIntent;
    private USBReceiver usbReceiver;

    private void registerReceiver(Activity context) {
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        context.registerReceiver(usbReceiver, filter);
    }

    public List<UsbDevice> getDeviceList() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        List<UsbDevice> usbDevices = new ArrayList<>();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            usbDevices.add(device);
            Log.e("USBUtil", "getDeviceList: " + device.getDeviceName());
        }
        return usbDevices;
    }

    public UsbDevice getUsbDevice(int vendorId, int productId) {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (device.getVendorId() == vendorId && device.getProductId() == productId) {
                Log.e("USBUtil", "getDeviceList: " + device.getDeviceName());
                return device;
            }
        }
        Toast.makeText(this, "没有对应的设备", Toast.LENGTH_SHORT).show();
        return null;
    }
}
