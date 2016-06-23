package net.medcorp.library.android.notificationsdk.gatt;

import android.bluetooth.*;
import net.medcorp.library.android.notificationsdk.gatt.uuid.*;
import net.medcorp.library.android.notificationsdk.config.*;
import android.support.v4.content.*;
import android.content.*;
import java.nio.*;
import net.medcorp.library.android.notificationsdk.listener.parcelable.*;
import net.medcorp.library.ble.util.HexUtils;

import android.util.*;
import java.util.*;
import android.os.*;

import org.apache.commons.codec.binary.Hex;

public class GattServer
{
    private static final String TAG;
    private static BluetoothGattCharacteristic mAlertCharacteristic;
    private static BluetoothGattDescriptor mAlertDescriptor;
    private static BluetoothReceiver mBluetoothReceiver;
    private static Context mContext;
    private static BluetoothGattCharacteristic mControlCharacteristic;
    private static BluetoothGattDescriptor mControlDescriptor;
    private static BluetoothGattCharacteristic mDataCharacteristic;
    private static BluetoothGattDescriptor mDataDescriptor;
    private static Map<BluetoothDevice, Integer> mDeviceMtus;
    private static GattReceiver mGattReceiver;
    private static BluetoothGattServer mGattServer;
    private static boolean mInitialized;
    private static BluetoothManager mManager;
    private static NotificationHandler mNotificationHandler;
    private static BluetoothGattService mNotificationService;
    private static Runnable mStartRunnable;
    private static boolean mStarted;
    private static Runnable mStopRunnable;
    private static Map<BluetoothDevice, Boolean> mSubscribedAlert;
    private static Map<BluetoothDevice, Boolean> mSubscribedData;
    
    static {
        TAG = "Karl";
        GattServer.mSubscribedAlert = new HashMap<BluetoothDevice, Boolean>();
        GattServer.mSubscribedData = new HashMap<BluetoothDevice, Boolean>();
        GattServer.mDeviceMtus = new HashMap<BluetoothDevice, Integer>();
        GattServer.mStartRunnable = new Start();
        GattServer.mStopRunnable = new Stop();
    }
    
    public static boolean connect(final BluetoothDevice bluetoothDevice) {
        Log.w(TAG,"Gatt connect, is it started?" + isStarted());
        return GattServer.mGattServer != null && GattServer.mGattServer.connect(bluetoothDevice, true);
    }
    
    public static boolean disconnect(final BluetoothDevice bluetoothDevice) {
        if (GattServer.mGattServer != null) {
            GattServer.mGattServer.cancelConnection(bluetoothDevice);
            return true;
        }
        return false;
    }
    
    public static void initialize(final Context context) {
        Log.d(GattServer.TAG, "Gatt server initialized");
        GattServer.mContext = context.getApplicationContext();
        GattServer.mBluetoothReceiver = new BluetoothReceiver();
        GattServer.mNotificationHandler = new NotificationHandler();
        if (GattServer.mBluetoothReceiver.isBluetoothOn()) {
            start();
        }
        GattServer.mContext.registerReceiver((BroadcastReceiver)GattServer.mBluetoothReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        GattServer.mInitialized = true;
    }
    
    public static boolean isStarted() {
        return GattServer.mStarted;
    }
    
    private static void start() {
        Log.d(GattServer.TAG, "Gatt server started");
        if (isStarted()) {
            Log.w(GattServer.TAG, "Gatt server was already started");
            return;
        }
        GattServer.mManager = (BluetoothManager)GattServer.mContext.getSystemService("bluetooth");
        GattServer.mGattServer = GattServer.mManager.openGattServer(GattServer.mContext, (BluetoothGattServerCallback)new GattServerCallback());
        GattServer.mAlertCharacteristic = new BluetoothGattCharacteristic(ServerUUIDs.CHARACTERISTIC_ALERT, 16, 0);
        GattServer.mAlertDescriptor = new BluetoothGattDescriptor(ServerUUIDs.DESCRIPTOR, 16);
        GattServer.mAlertCharacteristic.addDescriptor(GattServer.mAlertDescriptor);
        GattServer.mControlCharacteristic = new BluetoothGattCharacteristic(ServerUUIDs.CHARACTERISTIC_CONTROL, 8, 16);
        GattServer.mControlDescriptor = new BluetoothGattDescriptor(ServerUUIDs.DESCRIPTOR, 0);
        GattServer.mControlCharacteristic.addDescriptor(GattServer.mControlDescriptor);
        GattServer.mDataCharacteristic = new BluetoothGattCharacteristic(ServerUUIDs.CHARACTERISTIC_DATA, 16, 0);
        GattServer.mDataDescriptor = new BluetoothGattDescriptor(ServerUUIDs.DESCRIPTOR, 16);
        GattServer.mDataCharacteristic.addDescriptor(GattServer.mDataDescriptor);
        final String notificationServiceUUID = ConfigHelper.getNotificationServiceUUID(GattServer.mContext);
        UUID uuid;
        if (notificationServiceUUID != null) {
            uuid = UUID.fromString(notificationServiceUUID);
        }
        else {
            uuid = ServerUUIDs.SERVICE_NOTIFICATION;
        }
        (GattServer.mNotificationService = new BluetoothGattService(uuid, 0)).addCharacteristic(GattServer.mAlertCharacteristic);
        GattServer.mNotificationService.addCharacteristic(GattServer.mControlCharacteristic);
        GattServer.mNotificationService.addCharacteristic(GattServer.mDataCharacteristic);
        GattServer.mGattServer.addService(GattServer.mNotificationService);
        GattServer.mGattReceiver = new GattReceiver();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_POSTED");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_UPDATED");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_REMOVED");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ATTRIBUTES_READ");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ACTIONS_READ");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ACTION_TRIGGERED");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_NOTIFICATION_ID");
        intentFilter.addAction("net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_ACTION");
        LocalBroadcastManager.getInstance(GattServer.mContext).registerReceiver(GattServer.mGattReceiver, intentFilter);
        GattServer.mStarted = true;
    }
    
    private static void stop() {
        Log.d(GattServer.TAG, "Gatt server stopped");
        if (!isStarted()) {
            Log.w(GattServer.TAG, "Gatt server was already stopped");
            return;
        }
        GattServer.mStarted = false;
        LocalBroadcastManager.getInstance(GattServer.mContext).unregisterReceiver(GattServer.mGattReceiver);
        GattServer.mGattReceiver = null;
        GattServer.mManager = null;
        if (GattServer.mGattServer != null) {
            GattServer.mGattServer.close();
            GattServer.mGattServer = null;
        }
        GattServer.mNotificationHandler.clear();
        GattServer.mNotificationService = null;
        GattServer.mAlertCharacteristic = null;
        GattServer.mControlCharacteristic = null;
        GattServer.mDataCharacteristic = null;
        GattServer.mAlertDescriptor = null;
        GattServer.mControlDescriptor = null;
        GattServer.mDataDescriptor = null;
        GattServer.mSubscribedAlert.clear();
        GattServer.mSubscribedData.clear();
        GattServer.mDeviceMtus.clear();
    }
    
    public static void terminate() {
        Log.d(GattServer.TAG, "Gatt server terminated");
        GattServer.mInitialized = false;
        GattServer.mContext.unregisterReceiver((BroadcastReceiver)GattServer.mBluetoothReceiver);
        if (GattServer.mBluetoothReceiver.isBluetoothOn()) {
            stop();
        }
        GattServer.mNotificationHandler = null;
        GattServer.mBluetoothReceiver = null;
        GattServer.mContext = null;
    }
    
    public boolean isInitialized() {
        return GattServer.mInitialized;
    }

    private static class BluetoothReceiver extends BroadcastReceiver
    {
        private static final String TAG;
        private boolean mBluetoothOn;
        private Handler mHandler;
        
        static {
            TAG = "Karl";
        }
        
        public BluetoothReceiver() {
            this.mHandler = new Handler(GattServer.mContext.getMainLooper());
            this.mBluetoothOn = (GattServer.mContext.getSystemService("bluetooth") != null && ((BluetoothManager)GattServer.mContext.getSystemService("bluetooth")).getAdapter() != null && ((BluetoothManager)GattServer.mContext.getSystemService("bluetooth")).getAdapter().isEnabled());
            Log.w(TAG,"Hello?");
        }
        
        public boolean isBluetoothOn() {
            return this.mBluetoothOn;
        }
        
        public void onReceive(final Context context, final Intent intent) {
            Log.d(BluetoothReceiver.TAG, "Bluetooth state received");
            if (intent == null || intent.getAction() == null) {
                Log.w(BluetoothReceiver.TAG, "No specified action");
            }
            else {
                switch (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE)) {
                    default: {
                        if (this.mBluetoothOn) {
                            this.mHandler.removeCallbacks(GattServer.mStartRunnable);
                            this.mHandler.removeCallbacks(GattServer.mStopRunnable);
                            this.mHandler.post(GattServer.mStopRunnable);
                            this.mBluetoothOn = false;
                            return;
                        }
                        break;
                    }
                    case 12: {
                         if (!this.mBluetoothOn) {
                            this.mHandler.removeCallbacks(GattServer.mStartRunnable);
                            this.mHandler.removeCallbacks(GattServer.mStopRunnable);
                            this.mHandler.postDelayed(GattServer.mStartRunnable, 5000L);
                            this.mBluetoothOn = true;
                            return;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    private static class GattReceiver extends BroadcastReceiver
    {
        static final String TAG = "Karl";

        
        private static byte[] getActionsPacket(final NotificationActionList list) {

            int n = 1 + 5;
            for (final NotificationAction notificationAction : list.getActions()) {
                int length;
                if (notificationAction.getLabel() != null) {
                    length = notificationAction.getLabel().length();
                }
                else {
                    length = 0;
                }
                n += length + 2;
            }
            final ByteBuffer order = ByteBuffer.allocate(n).order(ByteOrder.LITTLE_ENDIAN);
            order.put(GattUtils.getByte(2));
            order.putInt(list.getId()).put(GattUtils.getByte(list.getActions().size()));
            for (final NotificationAction notificationAction2 : list.getActions()) {
                order.put(GattUtils.getByte(notificationAction2.getCode()));
                if (notificationAction2.getLabel() != null && notificationAction2.getLabel().length() > 0) {
                    order.put(GattUtils.getByte(notificationAction2.getLabel().length()));
                    order.put(GattUtils.getBytes(notificationAction2.getLabel()));
                }
                else {
                    order.put(GattUtils.getByte(0));
                }
            }
            return order.array();
        }
        
        private static byte[] getAlertPacket(final int n, final NotificationSummary notificationSummary) {
            return ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN).put(GattUtils.getByte(n)).putInt(notificationSummary.getId()).put(GattUtils.getByte(notificationSummary.getCategory())).putInt(notificationSummary.getNumber()).put(GattUtils.getByte(notificationSummary.getPriority())).put(GattUtils.getByte(notificationSummary.getVisibility())).put((byte) 1).array();
        }
        
        private static byte[] getAttributesPacket(final NotificationAttributeList list) {
            int n = 1 + 5;
            for (final NotificationAttribute notificationAttribute : list.getAttributes()) {
                int length;
                if (notificationAttribute.getValue() != null) {
                    length = notificationAttribute.getValue().length;
                }
                else {
                    length = 0;
                }
                n += length + 3;
            }
            final ByteBuffer order = ByteBuffer.allocate(n).order(ByteOrder.LITTLE_ENDIAN);
            order.put(GattUtils.getByte(1));
            order.putInt(list.getId()).put(GattUtils.getByte(list.getAttributes().size()));
            for (final NotificationAttribute notificationAttribute2 : list.getAttributes()) {
                order.put(GattUtils.getByte(notificationAttribute2.getCode()));
                if (notificationAttribute2.getValue() != null && notificationAttribute2.getValue().length > 0) {
                    order.putShort(GattUtils.getShort(notificationAttribute2.getValue().length));
                    order.put(notificationAttribute2.getValue());
                }
                else {
                    order.putShort(GattUtils.getShort(0));
                }
            }
            return order.array();
        }
        
        private static void notifyAlertPacket(final byte[] array) {
            final Iterator<Map.Entry<BluetoothDevice, Integer>> iterator = GattServer.mDeviceMtus.entrySet().iterator();
            while (iterator.hasNext()) {
                GattServer.mNotificationHandler.sendAlertRequestMessage(array, iterator.next().getKey());
            }
        }
        
        private static void notifyAlertPacket(final byte[] array, final BluetoothDevice bluetoothDevice) {
            final Iterator<Map.Entry<BluetoothDevice, Integer>> iterator = GattServer.mDeviceMtus.entrySet().iterator();
            while (iterator.hasNext()) {
                GattServer.mNotificationHandler.sendAlertRequestMessage(array, iterator.next().getKey());
            }
        }
        
        private static void notifyDataPacket(final byte[] array, final BluetoothDevice bluetoothDevice) {
            GattServer.mNotificationHandler.sendDataRequestMessage(array, bluetoothDevice);
        }
        
        private static void onReceiveActionTriggered(final Bundle bundle) {
            Log.d(GattReceiver.TAG, "Received a result (action triggered)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID") == null) {
                Log.w(GattReceiver.TAG, "Broadcast is missing extras");
                return;
            }
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE");
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID");
            GattServer.mGattServer.sendResponse(bluetoothDevice, int1, 0, 0, (byte[])null);
            Log.d(GattReceiver.TAG, bluetoothDevice.getAddress() + ":" + int1 + " - Success");
        }
        
        private static void onReceiveActionsRead(final Bundle bundle) {
            Log.d(GattReceiver.TAG, "Received a data packet (actions)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_ACTIONS") == null) {
                Log.w(GattReceiver.TAG, "Broadcast is missing extras");
                return;
            }
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE");
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID");
            final NotificationActionList list = (NotificationActionList)bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_ACTIONS");
            GattServer.mGattServer.sendResponse(bluetoothDevice, int1, 0, 0, (byte[])null);
            Log.d(GattReceiver.TAG, bluetoothDevice.getAddress() + ":" + int1 + " - Success");
            notifyDataPacket(getActionsPacket(list), bluetoothDevice);
        }
        
        private static void onReceiveAlert(final Bundle bundle, final int n) {
            Log.w("Karl","Test on receive alert");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_SUMMARY") == null) {
                Log.w(GattReceiver.TAG, "Broadcast is missing extras");
                return;
            }
            final Parcelable parcelable = bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE");
            final byte[] alertPacket = getAlertPacket(n, (NotificationSummary)bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_SUMMARY"));
            if (parcelable != null) {
                notifyAlertPacket(alertPacket, (BluetoothDevice)parcelable);
                return;
            }
            notifyAlertPacket(alertPacket);
        }
        
        private static void onReceiveAttributesRead(final Bundle bundle) {
            Log.d(GattReceiver.TAG, "Received a data packet (attributes)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID") == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_ATTRIBUTES") == null) {
                Log.w(GattReceiver.TAG, "Broadcast is missing extras");
                return;
            }
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE");
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID");
            final NotificationAttributeList list = (NotificationAttributeList)bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_NOTIFICATION_ATTRIBUTES");
            GattServer.mGattServer.sendResponse(bluetoothDevice, int1, 0, 0, (byte[])null);
            byte[] payloadAttributes = getAttributesPacket(list);
            Log.d(GattReceiver.TAG, bluetoothDevice.getAddress() + ":" + int1 + " - Success");
            Log.d(GattReceiver.TAG, "onReceiveAttributesRead, send data: " + new String(Hex.encodeHexString(payloadAttributes)));
            notifyDataPacket(payloadAttributes, bluetoothDevice);
        }
        
        private static void onReceiveInvalidAction(final Bundle bundle) {
            Log.d(GattReceiver.TAG, "Received a result (invalid action)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID") == null) {
                Log.w(GattReceiver.TAG, "Broadcast is missing extras");
                return;
            }
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE");
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID");
            GattServer.mGattServer.sendResponse(bluetoothDevice, int1, 131, 0, (byte[])null);
            Log.d(GattReceiver.TAG, bluetoothDevice.getAddress() + ":" + int1 + " - Invalid action");
        }
        
        private static void onReceiveInvalidNotificationID(final Bundle bundle) {
            Log.d(GattReceiver.TAG, "Received a result (invalid notification ID)");
            if (bundle == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE") == null || bundle.get("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID") == null) {
                Log.w(GattReceiver.TAG, "Broadcast is missing extras");
                return;
            }
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("net.medcorp.library.android.notificationserver.gatt.EXTRA_BLUETOOTH_DEVICE");
            final int int1 = bundle.getInt("net.medcorp.library.android.notificationserver.gatt.EXTRA_REQUEST_ID");
            GattServer.mGattServer.sendResponse(bluetoothDevice, int1, 130, 0, (byte[])null);
            Log.d(GattReceiver.TAG, bluetoothDevice.getAddress() + ":" + int1 + " - Invalid notification ID");
        }
        
        private static void onReceivePosted(final Bundle bundle) {
            Log.d(GattReceiver.TAG, "Received an alert (posted)");
            onReceiveAlert(bundle, 1);
        }
        
        private static void onReceiveRemoved(final Bundle bundle) {
            Log.d(GattReceiver.TAG, "Received an alert (removed)");
            onReceiveAlert(bundle, 3);
        }
        
        private static void onReceiveUnknown() {
            Log.w(GattReceiver.TAG, "Unknown broadcast received");
        }
        
        private static void onReceiveUpdated(final Bundle bundle) {
            Log.d(GattReceiver.TAG, "Received an alert (updated)");
            onReceiveAlert(bundle, 2);
        }
        
        public void onReceive(final Context context, final Intent intent) {
            Log.w(TAG,"on receive!?");
            if (intent == null || intent.getAction() == null) {
                Log.w(GattReceiver.TAG, "Received broadcast with no specified action");
                return;
            }
            final Bundle extras = intent.getExtras();
            final String action = intent.getAction();
            switch (action) {
                default: {
                    onReceiveUnknown();
                    break;
                }
                case "net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_POSTED": {
                    onReceivePosted(extras);
                    break;
                }
                case "net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_UPDATED": {
                    onReceiveUpdated(extras);
                    break;
                }
                case "net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_REMOVED": {
                    onReceiveRemoved(extras);
                    break;
                }
                case "net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ATTRIBUTES_READ": {
                    onReceiveAttributesRead(extras);
                    break;
                }
                case "net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ACTIONS_READ": {
                    onReceiveActionsRead(extras);
                    break;
                }
                case "net.medcorp.library.android.notificationserver.gatt.ACTION_NOTIFICATION_ACTION_TRIGGERED": {
                    onReceiveActionTriggered(extras);
                    break;
                }
                case "net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_NOTIFICATION_ID": {
                    onReceiveInvalidNotificationID(extras);
                    break;
                }
                case "net.medcorp.library.android.notificationserver.gatt.ACTION_INVALID_ACTION": {
                    onReceiveInvalidAction(extras);
                    break;
                }
            }
        }
    }
    
    private static class GattServerCallback extends BluetoothGattServerCallback
    {
        private static final String TAG;
        
        static {
            TAG = "Karl";
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.w(TAG,"Service added with status " + status);
        }

        private void onAlertDescriptorWriteRequest(final BluetoothDevice bluetoothDevice, final int n, final byte[] array) {
            if (Arrays.equals(array, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Subscribed to alert");
                GattServer.mSubscribedAlert.put(bluetoothDevice, true);
                GattServer.mGattServer.sendResponse(bluetoothDevice, n, 0, 0, (byte[])null);
                LocalBroadcastManager.getInstance(GattServer.mContext).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.listener.ACTION_LIST").putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice));
                return;
            }
            Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Not a subscription request");
            GattServer.mGattServer.sendResponse(bluetoothDevice, n, 257, 0, (byte[])null);
        }
        
        private void onDataDescriptorWriteRequest(final BluetoothDevice bluetoothDevice, final int n, final byte[] array) {
            if (Arrays.equals(array, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Subscribed to data");
                GattServer.mSubscribedData.put(bluetoothDevice, true);
                GattServer.mGattServer.sendResponse(bluetoothDevice, n, 0, 0, (byte[])null);
                return;
            }
            Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Not a subscription request");
            GattServer.mGattServer.sendResponse(bluetoothDevice, n, 257, 0, (byte[])null);
        }
        
        private void onReadAttributesCommand(final BluetoothDevice bluetoothDevice, final int n, final byte[] array) {
            Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Read attributes command");
            final ByteBuffer order = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN);
            order.position(1);
            if (order.remaining() < 5) {
                Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Malformed payload");
                GattServer.mGattServer.sendResponse(bluetoothDevice, n, 129, 0, (byte[])null);
                return;
            }
            final int int1 = order.getInt();
            final int int2 = GattUtils.getInt(order.get());
            if (order.remaining() == int2) {
                final int[] array2 = new int[order.remaining()];
                for (int i = 0; i < int2; ++i) {
                    array2[i] = GattUtils.getInt(order.get());
                }
                LocalBroadcastManager.getInstance(GattServer.mContext).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.listener.ACTION_READ_ATTRIBUTES").putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID", int1).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID", n).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_ATTRIBUTES", array2));
                return;
            }
            Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Malformed payload");
            GattServer.mGattServer.sendResponse(bluetoothDevice, n, 129, 0, (byte[])null);
        }
        
        private void onReadCustomActionsCommand(final BluetoothDevice bluetoothDevice, final int n, final byte[] array) {
            Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Read custom actions command");
            final ByteBuffer order = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN);
            order.position(1);
            if (order.remaining() == 4) {
                LocalBroadcastManager.getInstance(GattServer.mContext).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.listener.ACTION_READ_ACTIONS").putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID", order.getInt()).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID", n));
                return;
            }
            Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Malformed payload");
            GattServer.mGattServer.sendResponse(bluetoothDevice, n, 129, 0, (byte[])null);
        }
        
        private void onTriggerCustomActionCommand(final BluetoothDevice bluetoothDevice, final int n, final byte[] array) {
            Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Trigger action command");
            final ByteBuffer order = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN);
            order.position(1);
            if (order.remaining() == 5) {
                final int int1 = order.getInt();
                switch (GattUtils.getInt(order.get())) {
                    default: {
                        Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Unsupported action");
                        GattServer.mGattServer.sendResponse(bluetoothDevice, n, 131, 0, (byte[])null);
                        break;
                    }
                    case 2: {
                        Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Action is open");
                        LocalBroadcastManager.getInstance(GattServer.mContext).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_OPEN").putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID", int1).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID", n));
                        break;
                    }
                    case 1: {
                        Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Action is dismiss");
                        LocalBroadcastManager.getInstance(GattServer.mContext).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_DISMISS").putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID", int1).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID", n));
                        break;
                    }
                }
            }
            else {
                if (order.remaining() != 6) {
                    Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Malformed payload");
                    GattServer.mGattServer.sendResponse(bluetoothDevice, n, 129, 0, (byte[])null);
                    return;
                }
                final int int2 = order.getInt();
                final int int3 = GattUtils.getInt(order.get());
                final int int4 = GattUtils.getInt(order.get());
                if (int3 == 255) {
                    Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Action is custom");
                    LocalBroadcastManager.getInstance(GattServer.mContext).sendBroadcast(new Intent("net.medcorp.library.android.notificationserver.listener.ACTION_TRIGGER_CUSTOM").putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_NOTIFICATION_ID", int2).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_BLUETOOTH_DEVICE", (Parcelable)bluetoothDevice).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_REQUEST_ID", n).putExtra("net.medcorp.library.android.notificationserver.listener.EXTRA_ACTION", int4));
                    return;
                }
                Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Unsupported action");
                GattServer.mGattServer.sendResponse(bluetoothDevice, n, 131, 0, (byte[])null);
            }
        }
        
        private void onUnknownCommand(final BluetoothDevice bluetoothDevice, final int n) {
            Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Invalid command");
            GattServer.mGattServer.sendResponse(bluetoothDevice, n, 128, 0, (byte[])null);
        }
        
        public void onCharacteristicWriteRequest(final BluetoothDevice bluetoothDevice, final int n, final BluetoothGattCharacteristic bluetoothGattCharacteristic, final boolean b, final boolean b2, final int n2, final byte[] array) {
            super.onCharacteristicWriteRequest(bluetoothDevice, n, bluetoothGattCharacteristic, b, b2, n2, array);
            Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Characteristic write request");
            Log.d(GattServerCallback.TAG,"<<<<<<<<< onCharacteristicWriteRequest, data:"+new String(Hex.encodeHex(array)) + ",bluetoothGattCharacteristic: " + bluetoothGattCharacteristic.getUuid().toString() + ",bluetoothDevice: " + bluetoothDevice.getAddress());
            if (bluetoothDevice.getBondState() != 12) {
                Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Not bonded");
                GattServer.mGattServer.sendResponse(bluetoothDevice, n, 5, 0, (byte[])null);
                return;
            }
            if (bluetoothGattCharacteristic == GattServer.mControlCharacteristic) {
                this.onDataCharacteristicWriteRequest(bluetoothDevice, n, array);
                return;
            }
            Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Wrote to wrong characteristic");
            GattServer.mGattServer.sendResponse(bluetoothDevice, n, 3, 0, (byte[])null);
        }
        
        public void onConnectionStateChange(final BluetoothDevice bluetoothDevice, final int n, final int n2) {
            super.onConnectionStateChange(bluetoothDevice, n, n2);
            Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + " - Connection state changed from " + n + " to " + n2);
            switch (n2) {
                default: {}
                case 2: {
                    Log.w("Karl","Put false from data & alert for device: "  + bluetoothDevice.getAddress());
                    GattServer.mSubscribedAlert.put(bluetoothDevice, false);
                    GattServer.mSubscribedData.put(bluetoothDevice, false);
                    GattServer.mDeviceMtus.put(bluetoothDevice, 20);
                    GattServer.mNotificationHandler.put(bluetoothDevice);
                    break;
                }
                case 0: {
                    Log.w("Karl","Removed from data & alert for device: "  + bluetoothDevice.getAddress());
                    GattServer.mSubscribedAlert.remove(bluetoothDevice);
                    GattServer.mSubscribedData.remove(bluetoothDevice);
                    GattServer.mDeviceMtus.remove(bluetoothDevice);
                    GattServer.mNotificationHandler.remove(bluetoothDevice);
                    break;
                }
            }
        }
        
        public void onDataCharacteristicWriteRequest(final BluetoothDevice bluetoothDevice, final int n, final byte[] array) {
            if (array == null || array.length <= 0) {
                Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Missing payload");
                GattServer.mGattServer.sendResponse(bluetoothDevice, n, 129, 0, (byte[])null);
                return;
            }
            Log.d(GattServerCallback.TAG,"onDataCharacteristicWriteRequest, data: " + new String(Hex.encodeHex(array)) + ",request ID: " + n + ",notification ID: " + HexUtils.bytesToInt(new byte[]{array[1],array[2],array[3],array[4]}));
            switch (array[0]) {
                default: {
                    this.onUnknownCommand(bluetoothDevice, n);
                    break;
                }
                case 1: {
                    this.onReadAttributesCommand(bluetoothDevice, n, array);
                    break;
                }
                case 2: {
                    this.onReadCustomActionsCommand(bluetoothDevice, n, array);
                    break;
                }
                case 3: {
                    this.onTriggerCustomActionCommand(bluetoothDevice, n, array);
                    break;
                }
            }
        }

        public void onDescriptorWriteRequest(final BluetoothDevice bluetoothDevice, final int n, final BluetoothGattDescriptor bluetoothGattDescriptor, final boolean b, final boolean b2, final int n2, final byte[] array) {
            super.onDescriptorWriteRequest(bluetoothDevice, n, bluetoothGattDescriptor, b, b2, n2, array);
            Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Descriptor write request");
            if (bluetoothDevice.getBondState() != 12) {
                Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Not bonded");
                GattServer.mGattServer.sendResponse(bluetoothDevice, n, 5, 0, (byte[])null);
                return;
            }
            if (bluetoothGattDescriptor == GattServer.mAlertDescriptor) {
                this.onAlertDescriptorWriteRequest(bluetoothDevice, n, array);
                return;
            }
            if (bluetoothGattDescriptor == GattServer.mDataDescriptor) {
                this.onDataDescriptorWriteRequest(bluetoothDevice, n, array);
                return;
            }
            Log.w(GattServerCallback.TAG, bluetoothDevice.getAddress() + ":" + n + " - Wrote to wrong descriptor");
            GattServer.mGattServer.sendResponse(bluetoothDevice, n, 3, 0, (byte[])null);
        }
        
        public void onMtuChanged(final BluetoothDevice bluetoothDevice, final int n) {
            super.onMtuChanged(bluetoothDevice, n);
            Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + " - MTU changed to " + n);
            GattServer.mDeviceMtus.put(bluetoothDevice, n);
        }
        
        public void onNotificationSent(final BluetoothDevice bluetoothDevice, final int n) {
            super.onNotificationSent(bluetoothDevice, n);
            Log.d(GattServerCallback.TAG, bluetoothDevice.getAddress() + " - Notification sent with status " + n);
            if (n == 0) {
                GattServer.mNotificationHandler.sendSentMessage(bluetoothDevice);
                return;
            }
            GattServer.mNotificationHandler.sendSentMessage(bluetoothDevice);
        }
    }
    
    private static class NotificationHandler extends Handler
    {
        public static final String NOTIFICATION_DEVICE = "device";
        public static final String NOTIFICATION_PAYLOAD = "payload";
        public static final int NOTIFICATION_REQUESTED = 1;
        public static final int NOTIFICATION_SENT = 2;
        public static final String NOTIFICATION_TYPE = "type";
        private static final String TAG;
        public static final int TYPE_ALERT = 1;
        public static final int TYPE_DATA = 2;
        private Map<BluetoothDevice, Queue<Pair<ByteBuffer, Integer>>> mQueueMap;
        private Map<BluetoothDevice, Boolean> mWaitingMap;
        
        static {
            TAG = "Karl";
        }
        
        public NotificationHandler() {
            super(GattServer.mContext.getMainLooper());
            this.mQueueMap = new HashMap<BluetoothDevice, Queue<Pair<ByteBuffer, Integer>>>();
            this.mWaitingMap = new HashMap<BluetoothDevice, Boolean>();
            Log.w(TAG,"Notification handler init");
        }
        
        private boolean isConnected(final BluetoothDevice bluetoothDevice) {
            final Iterator<Map.Entry<BluetoothDevice, Integer>> iterator = GattServer.mDeviceMtus.entrySet().iterator();
            while (iterator.hasNext()) {
                if (bluetoothDevice.equals(iterator.next().getKey())) {
                    return true;
                }
            }
            return false;
        }
        
        private boolean isSubscribed(final BluetoothDevice bluetoothDevice, final Integer n) {
            switch (n) {
                default: {
                    return false;
                }
                case 1: {
                    return this.isSubscribed(bluetoothDevice, GattServer.mSubscribedAlert);
                }
                case 2: {
                    return this.isSubscribed(bluetoothDevice, GattServer.mSubscribedData);
                }
            }
        }
        
        private boolean isSubscribed(final BluetoothDevice bluetoothDevice, final Map<BluetoothDevice, Boolean> map) {
            for (final Map.Entry<BluetoothDevice, Boolean> entry : map.entrySet()) {
                if (bluetoothDevice.equals((Object)entry.getKey()) && entry.getValue()) {
                    return true;
                }
            }
            return false;
        }
        
        private void onHandleNotificationRequested(final Bundle bundle) {
            Log.d(NotificationHandler.TAG, "Handling a message (notification requested) Je bana stinkt  ");
            if (bundle == null || bundle.getByteArray("payload") == null || bundle.getParcelable("device") == null) {
                Log.w(NotificationHandler.TAG, "Message is missing extras");
            } else {
                final ByteBuffer wrap = ByteBuffer.wrap(bundle.getByteArray("payload"));
                final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("device");
                final Integer value = bundle.getInt("type");
                if (!this.isConnected(bluetoothDevice)) {
                    if(this.mQueueMap.containsKey(bluetoothDevice)) {
                        this.mQueueMap.remove(bluetoothDevice);
                    }
                    if(this.mWaitingMap.containsKey(bluetoothDevice)) {
                        this.mWaitingMap.remove(bluetoothDevice);
                    }
                    Log.w(NotificationHandler.TAG, "Attempt to notify a device that is not connected");
                    return;
                }
                if (!this.isSubscribed(bluetoothDevice, value)) {
                    Log.w(NotificationHandler.TAG, "Attempt to notify a device that is not subscribed");
                    return;
                }
                Log.d(NotificationHandler.TAG, bluetoothDevice.getAddress() + " - Payload pushed to queue");
                this.mQueueMap.get(bluetoothDevice).add((Pair<ByteBuffer, Integer>)new Pair((Object)wrap, (Object)value));
                if (!this.mWaitingMap.get(bluetoothDevice)) {
                    Log.d(NotificationHandler.TAG, bluetoothDevice.getAddress() + " - Payload pushed to device");
                    this.sendNotification(wrap, bluetoothDevice, value);
                    this.mWaitingMap.put(bluetoothDevice, true);
                }
            }
        }
        
        private void onHandleNotificationSent(final Bundle bundle) {
            Log.d(NotificationHandler.TAG, "Handling a message (notification sent)");
            if (bundle == null || bundle.getParcelable("device") == null) {
                Log.w(NotificationHandler.TAG, "Message is missing extras");
                return;
            }
            final BluetoothDevice bluetoothDevice = (BluetoothDevice)bundle.getParcelable("device");
            if (!this.isConnected(bluetoothDevice)) {
                if (this.mQueueMap.containsKey(bluetoothDevice)) {
                    this.mQueueMap.remove(bluetoothDevice);
                }
                if (this.mWaitingMap.containsKey(bluetoothDevice)) {
                    this.mWaitingMap.remove(bluetoothDevice);
                }
                Log.w(NotificationHandler.TAG, "Attempt to notify a device that is not connected");
                return;
            }
            if (!this.mQueueMap.containsKey(bluetoothDevice)) {
                this.mQueueMap.put(bluetoothDevice, new ArrayDeque<Pair<ByteBuffer, Integer>>());
            }
            if (!this.mWaitingMap.containsKey(bluetoothDevice)) {
                this.mWaitingMap.put(bluetoothDevice, false);
            }
            Pair<ByteBuffer, Integer> pair;
            for (pair = this.mQueueMap.get(bluetoothDevice).peek(); pair != null && (!((ByteBuffer)pair.first).hasRemaining() || !this.isSubscribed(bluetoothDevice, (Integer)pair.second)); pair = this.mQueueMap.get(bluetoothDevice).peek()) {
                Log.d(NotificationHandler.TAG, bluetoothDevice.getAddress() + " - Payload dropped from queue");
                this.mQueueMap.get(bluetoothDevice).poll();
            }
            if (pair != null) {
                Log.d(NotificationHandler.TAG, bluetoothDevice.getAddress() + " - Payload pushed to device");
                this.sendNotification((ByteBuffer)pair.first, bluetoothDevice, (Integer)pair.second);
                return;
            }
            this.mWaitingMap.put(bluetoothDevice, false);
        }
        
        private void onHandleUnknown() {
            Log.w(NotificationHandler.TAG, "Unknown message received");
        }
        
        private void sendNotification(final ByteBuffer byteBuffer, final BluetoothDevice bluetoothDevice, final BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            final byte[] value = new byte[Math.min(GattServer.mDeviceMtus.get(bluetoothDevice), byteBuffer.remaining())];
            byteBuffer.get(value);
            Log.d(NotificationHandler.TAG,">>>>>>sendNotification,data: " + new String(Hex.encodeHex(value)) + ",bluetoothGattCharacteristic: " + bluetoothGattCharacteristic.getUuid().toString() + ",bluetoothDevice: " + bluetoothDevice.getAddress());
            bluetoothGattCharacteristic.setValue(value);
            GattServer.mGattServer.notifyCharacteristicChanged(bluetoothDevice, bluetoothGattCharacteristic, false);
            if (Build.VERSION.SDK_INT < 21) {
                this.sendSentMessage(bluetoothDevice);
            }
        }
        
        private void sendNotification(final ByteBuffer byteBuffer, final BluetoothDevice bluetoothDevice, final Integer n) {
            switch (n) {
                default: {}
                case 1: {
                    this.sendNotification(byteBuffer, bluetoothDevice, GattServer.mAlertCharacteristic);
                    break;
                }
                case 2: {
                    this.sendNotification(byteBuffer, bluetoothDevice, GattServer.mDataCharacteristic);
                    break;
                }
            }
        }
        
        private void sendRequestMessage(final byte[] array, final BluetoothDevice bluetoothDevice, final int n) {
            final Message obtainMessage = this.obtainMessage(1);
            final Bundle data = new Bundle();
            data.putByteArray("payload", array);
            data.putParcelable("device", (Parcelable)bluetoothDevice);
            data.putInt("type", n);
            obtainMessage.setData(data);
            this.sendMessage(obtainMessage);
        }
        
        public void clear() {
            this.removeMessages(1);
            this.removeMessages(2);
            this.mQueueMap.clear();
            this.mWaitingMap.clear();
        }
        
        public void handleMessage(final Message message) {
            if (message == null) {
                Log.w(NotificationHandler.TAG, "Message is empty");
                return;
            }
            switch (message.what) {
                default: {
                    this.onHandleUnknown();
                    break;
                }
                case 1: {
                    this.onHandleNotificationRequested(message.getData());
                    break;
                }
                case 2: {
                    this.onHandleNotificationSent(message.getData());
                    break;
                }
            }
        }
        
        public void put(final BluetoothDevice bluetoothDevice) {
            this.mQueueMap.put(bluetoothDevice, new ArrayDeque<Pair<ByteBuffer, Integer>>());
            this.mWaitingMap.put(bluetoothDevice, false);
        }
        
        public void remove(final BluetoothDevice bluetoothDevice) {
            this.mQueueMap.remove(bluetoothDevice);
            this.mWaitingMap.remove(bluetoothDevice);
        }
        
        public void sendAlertRequestMessage(final byte[] array, final BluetoothDevice bluetoothDevice) {
            this.sendRequestMessage(array, bluetoothDevice, 1);
        }
        
        public void sendDataRequestMessage(final byte[] array, final BluetoothDevice bluetoothDevice) {
            this.sendRequestMessage(array, bluetoothDevice, 2);
        }
        
        public void sendSentMessage(final BluetoothDevice bluetoothDevice) {
            final Message obtainMessage = this.obtainMessage(2);
            final Bundle data = new Bundle();
            data.putParcelable("device", (Parcelable)bluetoothDevice);
            obtainMessage.setData(data);
            this.sendMessage(obtainMessage);
        }
    }
    
    private static class Start implements Runnable
    {
        @Override
        public void run() {
            start();
        }
    }
    
    private static class Stop implements Runnable
    {
        @Override
        public void run() {
            stop();
        }
    }
}
