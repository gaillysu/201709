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
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import net.medcorp.library.ble.event.BLEServerConnectionStateChangedEvent;
import net.medcorp.library.ble.event.BLEServerNotificationSentEvent;
import net.medcorp.library.ble.event.BLEServerReadRequestEvent;
import net.medcorp.library.ble.event.BLEServerServiceAddedEvent;
import net.medcorp.library.ble.event.BLEServerWriteRequestEvent;
import net.medcorp.library.ble.kernel.MEDBT;

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
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;

    private List<BluetoothDevice> mConnectedDevices = new ArrayList<>();

    private final UUID serviceUUID = UUID.fromString("F0BA3124-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID controlPointCharacteristics = UUID.fromString("F0BA3126-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID dataSourceCharacteristicsUUID = UUID.fromString("F0BA3127-6CAC-4C99-9089-4B0A1DF45002");
    private final UUID alertSourceUUID = UUID.fromString("F0BA3125-6CAC-4C99-9089-4B0A1DF45002");
    private Context context;


    public BLEServiceProvider(Context context) {
        this.context = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
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
        BluetoothGattCharacteristic alertSourceCharacteristics= new BluetoothGattCharacteristic(alertSourceUUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattCharacteristic dataSourceCharacteristics = new BluetoothGattCharacteristic(dataSourceCharacteristicsUUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattCharacteristic offsetCharacteristic = new BluetoothGattCharacteristic(controlPointCharacteristics,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(dataSourceCharacteristics);
        service.addCharacteristic(alertSourceCharacteristics);
        service.addCharacteristic(offsetCharacteristic);
        mGattServer.addService(service);
    }

    public void closeServer(){
        Log.w("Karl","Close server");
        if (mGattServer == null) return;

        mGattServer.close();
    }

    public void startAdvertising() {
        Log.w("Karl","Start advertising");
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(serviceUUID))
                .build();
        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }

    public void stopAdvertising() {
        Log.w("Karl","Stop server");
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    public void sendNotificationAlert(byte[] data)
    {
        Log.w("Karl","Sending notifications alert");
        for (BluetoothDevice device : mConnectedDevices) {
            BluetoothGattCharacteristic characteristic = mGattServer.getService(serviceUUID)
                    .getCharacteristic(alertSourceUUID);
            if(characteristic!=null) {
                Log.i("Karl", characteristic.getUuid().toString() + " sendNotificationAlert " + new String(Hex.encodeHex(data)));
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
                Log.i("Karl", "sendNotificationData " + new String(Hex.encodeHex(data)));
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
                    .getCharacteristic(controlPointCharacteristics);
            //characteristic.setValue(serviceUUID.toString());
            //mGattServer.notifyCharacteristicChanged(device, characteristic, false);
            EventBus.getDefault().post(new BLEServerConnectionStateChangedEvent(connected));
        }
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i("Karl", "Peripheral Advertise Started.");
            Log.i("Karl", "GATT Server Ready to go bro!.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w("Karl", "Peripheral Advertise Failed: "+errorCode);
        }
    };

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
            Log.w("Karl","Added service");
            EventBus.getDefault().post(new BLEServerServiceAddedEvent(status,service.getUuid().toString()));
        }

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                postDeviceChange(device, true);
                Log.w("Karl","Connected to service");
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
            Log.i("Karl", "onCharacteristicReadRequest,characteristic = " + characteristic);
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
            Log.i("Karl", "3 " + characteristic.getUuid().toString() + ",device: " + device);
            Log.i("Karl", "onCharacteristicWriteRequest value: " + new String(Hex.encodeHex(value)) + ",responseNeeded: " + responseNeeded);
            if (controlPointCharacteristics.equals(characteristic.getUuid())) {
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
            Log.i("Karl", "onNotificationSent,status = success");
            EventBus.getDefault().post(new BLEServerNotificationSentEvent(status));
        }
    };
}
