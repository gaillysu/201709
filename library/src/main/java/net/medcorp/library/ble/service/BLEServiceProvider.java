package net.medcorp.library.ble.service;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import net.medcorp.library.ble.event.BLEServerConnectionStateChangedEvent;
import net.medcorp.library.ble.event.BLEServerNotificationSentEvent;
import net.medcorp.library.ble.event.BLEServerReadRequestEvent;
import net.medcorp.library.ble.event.BLEServerServiceAddedEvent;
import net.medcorp.library.ble.event.BLEServerWriteRequestEvent;

import org.apache.commons.codec.binary.Hex;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by karl-john on 6/6/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEServiceProvider {

    private Handler mHandler = new Handler();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
//    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;

    private List<BluetoothDevice> mConnectedDevices = new ArrayList<>();

    private final UUID serviceUUID = UUID.fromString("F0BA3124-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID controlPointCharacteristicsUUID = UUID.fromString("F0BA3126-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID dataSourceCharacteristicsUUID = UUID.fromString("F0BA3127-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID alertSourceUUID = UUID.fromString("F0BA3125-6CAC-4C99-9089-4B0A1DF45002");
    public Context context;
    private BluetoothGattCharacteristic alertSourceCharacteristics;
    private BluetoothGattCharacteristic dataSourceCharacteristics;
    private BluetoothGattCharacteristic controlPointCharacteristics;
    private BluetoothGattDescriptor alertSourceCharacteristicsDescriptor;
    private BluetoothGattDescriptor dataSourceCharacteristicsDescriptor;
    private BluetoothGattDescriptor controlPointCharacteristicsDescriptor;

    public Context getContext() {
        return context;
    }

    public BLEServiceProvider(Context context) {
        this.context = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                mGattServer = mBluetoothManager.openGattServer(context, mGattServerCallback);
                initServer();
                return;
            }
        }
        Toast.makeText(context, "Device does not support notifications.", Toast.LENGTH_LONG).show();
    }

    private void initServer() {
        BluetoothGattService service =new BluetoothGattService(serviceUUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        UUID test = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        alertSourceCharacteristics = new BluetoothGattCharacteristic(alertSourceUUID,
                16, 0);
        alertSourceCharacteristicsDescriptor = new BluetoothGattDescriptor(test, 16);
        alertSourceCharacteristicsDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        alertSourceCharacteristics.addDescriptor(alertSourceCharacteristicsDescriptor);

        controlPointCharacteristics = new BluetoothGattCharacteristic(controlPointCharacteristicsUUID,
                8, 16);
        controlPointCharacteristicsDescriptor = new BluetoothGattDescriptor(test, 0);
        controlPointCharacteristics.addDescriptor(controlPointCharacteristicsDescriptor);
        controlPointCharacteristicsDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

        dataSourceCharacteristics = new BluetoothGattCharacteristic(dataSourceCharacteristicsUUID,
                16, 0);
        dataSourceCharacteristicsDescriptor = new BluetoothGattDescriptor(test, 16);
        dataSourceCharacteristics.addDescriptor(dataSourceCharacteristicsDescriptor);
        dataSourceCharacteristicsDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

        service.addCharacteristic(dataSourceCharacteristics);
        service.addCharacteristic(alertSourceCharacteristics);
        service.addCharacteristic(controlPointCharacteristics);

        mGattServer.addService(service);
    }

    public void closeServer(){
        Log.w("Karl","Close server");
        if (mGattServer == null) return;

        mGattServer.close();
    }

    public void sendNotificationAlert(byte[] data)
    {
        Log.w("Karl","Sending notifications alert");
        for (BluetoothDevice device : mConnectedDevices) {
            BluetoothGattCharacteristic characteristic = mGattServer.getService(serviceUUID)
                    .getCharacteristic(alertSourceUUID);
            if(characteristic!=null) {
                Log.w("Karl", characteristic.getUuid().toString() + " sendNotificationAlert " + new String(Hex.encodeHex(data)));
                characteristic.setValue(data);
                mGattServer.notifyCharacteristicChanged(device, characteristic, false);
            }
        }
    }
    public void sendNotificationData(byte[] data)
    {
        Log.w("Karl","Sending notifications data");
        for (BluetoothDevice device : mConnectedDevices) {
            BluetoothGattCharacteristic characteristic = mGattServer.getService(serviceUUID)
                    .getCharacteristic(dataSourceCharacteristicsUUID);
            if(characteristic!=null) {
                Log.w("Karl", "sendNotificationData " + new String(Hex.encodeHex(data)));
                characteristic.setValue(data);
                mGattServer.notifyCharacteristicChanged(device, characteristic, false);
            }
        }
    }

    private BluetoothGattCallback mGattDeviceCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.w("Karl","Device/Watch connected");
            gatt.setCharacteristicNotification(alertSourceCharacteristics,true);
            gatt.setCharacteristicNotification(dataSourceCharacteristics,true);
            gatt.writeDescriptor(alertSourceCharacteristicsDescriptor);
            gatt.writeDescriptor(dataSourceCharacteristicsDescriptor);
            gatt.writeDescriptor(controlPointCharacteristicsDescriptor);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.w("Karl","2");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.w("Karl","3");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.w("Karl","4");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.w("Karl","5");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.w("Karl","6");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.w("Karl","7");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.w("Karl","8");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.w("Karl","9");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.w("Karl","10");
        }
    };

    private void notifyConnectedDevices(boolean connected) {
        for (BluetoothDevice device : mConnectedDevices) {
            BluetoothGattCharacteristic characteristic = mGattServer.getService(serviceUUID)
                    .getCharacteristic(controlPointCharacteristicsUUID);
            EventBus.getDefault().post(new BLEServerConnectionStateChangedEvent(connected));
        }
    }

    private void postDeviceChange(final BluetoothDevice device, final boolean toAdd) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //Trigger our periodic notification once devices are connected
                if(toAdd) {
                    mConnectedDevices.add(device);
                    device.connectGatt(context, true, mGattDeviceCallback);
                }
                else {
                    mConnectedDevices.remove(device);
                }
                Log.w("Karl","Advertise callback called");
                notifyConnectedDevices(toAdd);
            }
        });
    }

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.w("Karl","Added service " + status);
            EventBus.getDefault().post(new BLEServerServiceAddedEvent(status,service.getUuid().toString()));
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                postDeviceChange(device, true);
                Log.w("Karl","Connected to service");
                device.connectGatt(context,true, mGattDeviceCallback);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                postDeviceChange(device, false);
                Log.w("Karl","Disconnected to service");
            }
            Log.w("Karl","service state = " + status);
            Log.w("Karl","service new State = " + newState);

        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.w("Karl", "onCharacteristicReadRequest,characteristic = " + characteristic);
            if (dataSourceCharacteristicsUUID.equals(characteristic.getUuid())) {
                // todo Read response.
//                mGattServer.sendResponse(device,
//                        requestId,
//                        BluetoothGatt.GATT_SUCCESS,
//                        0,
//                        DeviceProfile.bytesFromInt(mTimeOffset));
            }

            if (alertSourceUUID.equals(characteristic.getUuid())) {
                // todo Read response.
            }

            mGattServer.sendResponse(device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    null);
            EventBus.getDefault().post(new BLEServerReadRequestEvent(characteristic.getUuid().toString()));
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.w("Karl", "3 " + characteristic.getUuid().toString() + ",device: " + device);
            Log.w("Karl", "onCharacteristicWriteRequest value: " + new String(Hex.encodeHex(value)) + ",responseNeeded: " + responseNeeded);
            if (controlPointCharacteristicsUUID.equals(characteristic.getUuid())) {
                if (responseNeeded) {
                    // todo Read response.
                    mGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            value);
                }
            }
            EventBus.getDefault().post(new BLEServerWriteRequestEvent(value, characteristic.getUuid().toString() + ",data: " + new String(Hex.encodeHex(value))));
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.w("Karl", "onNotificationSent,status = success");
            EventBus.getDefault().post(new BLEServerNotificationSentEvent(status));
        }
    };
    private BroadcastReceiver gattReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("Karl","Lol!");
        }
    };

}
