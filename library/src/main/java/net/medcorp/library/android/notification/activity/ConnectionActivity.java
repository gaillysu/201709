package net.medcorp.library.android.notification.activity;

import android.app.*;
import android.widget.*;
import android.view.*;
import net.medcorp.library.android.notificationsdk.gatt.*;
import android.content.*;
import android.os.*;
import android.bluetooth.*;
import net.medcorp.library.android.notificationsdk.config.*;

public class ConnectionActivity extends Activity
{
    EditText mAddress;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothManager mBluetoothManager;
    BluetoothAdapter.LeScanCallback mScanCallback;
    
    public void connect(final View view) {
        if (this.mBluetoothManager != null) {
            this.mBluetoothAdapter.startLeScan(this.mScanCallback);
        }
    }
    
    public void disconnect(final View view) {
        if (this.mBluetoothManager != null) {
            this.mBluetoothAdapter.stopLeScan(this.mScanCallback);
            GattServer.disconnect(this.mBluetoothAdapter.getRemoteDevice(this.mAddress.getText().toString()));
        }
    }
    
    public void notify(final View view) {
        Utils.notify((Context)this);
    }
    
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(2130968602);
        (this.mAddress = (EditText)this.findViewById(2131492948)).setText((CharSequence)"00:1B:DC:06:8C:06");
        this.mBluetoothManager = (BluetoothManager)this.getSystemService("bluetooth");
        this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
        this.mScanCallback = (BluetoothAdapter.LeScanCallback)new BluetoothAdapter.LeScanCallback() {
            public void onLeScan(final BluetoothDevice bluetoothDevice, final int n, final byte[] array) {
                if (bluetoothDevice.getAddress().equals(ConnectionActivity.this.mAddress.getText().toString())) {
                    GattServer.connect(bluetoothDevice);
                    ConnectionActivity.this.mBluetoothAdapter.stopLeScan(ConnectionActivity.this.mScanCallback);
                }
            }
        };
    }
    
    public void settings(final View view) {
        ConfigHelper.startNotificationListenerSettings((Context)this);
    }
}
