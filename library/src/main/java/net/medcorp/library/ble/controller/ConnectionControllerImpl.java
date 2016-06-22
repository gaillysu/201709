package net.medcorp.library.ble.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import net.medcorp.library.ble.ble.GattAttributes;
import net.medcorp.library.ble.datasource.GattAttributesDataSource;
import net.medcorp.library.ble.event.BLEConnectionStateChangedEvent;
import net.medcorp.library.ble.event.BLEFirmwareVersionReceivedEvent;
import net.medcorp.library.ble.event.BLENotificationEvent;
import net.medcorp.library.ble.event.BLEResponseDataEvent;
import net.medcorp.library.ble.event.BLESearchEvent;
import net.medcorp.library.ble.kernel.MEDBT;
import net.medcorp.library.ble.kernel.MEDBTImpl;
import net.medcorp.library.ble.model.request.BLERequestData;
import net.medcorp.library.ble.util.Constants;
import net.medcorp.library.ble.util.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
/*package*/ class ConnectionControllerImpl implements ConnectionController {

    private Timer mAutoReconnectTimer = null;
    private int  mTimerIndex = 0;

    private final int[] mReConnectTimerPattern = new int[]{1500,
            10000,10000,10000,10000,10000,10000,
            10000,10000,10000,10000,10000,10000,
            10000,10000,10000,10000,10000,10000,
            0x7FFFFFFF
            };

    //This boolean is the only reliable way to know if we are connected or not
    private boolean mIsConnected = false;

    private Context context;
    private MEDBT medBT;

    /**
     this parameter saved old BLE 's  address, when doing BLE OTA, the address has been changed to another one
     so, after finisned BLE ota, must restore it to normal 's address
     */
    private String mSavedAddress = "";
    private boolean inOTAMode = false;



    public ConnectionControllerImpl(Context ctx, GattAttributesDataSource dataSource){
        context = ctx;
        medBT = new MEDBTImpl(context, dataSource);
        EventBus.getDefault().register(this);
        //I remove it, when app starts, it will not auto connect watch,  my code will invoke @getModel().startConnectToWatch(boolean) function when need connect watch
        //This timer will retry to connect at given intervals
        //restartAutoReconnectTimer();
    }

    private void restartAutoReconnectTimer() {
        //first stop the timer thread!
        if(mAutoReconnectTimer!=null)mAutoReconnectTimer.cancel();
        mAutoReconnectTimer = new Timer();
        mAutoReconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mIsConnected) {
                    mTimerIndex = 0;
                } else {
                    mTimerIndex++;
                    if (mTimerIndex >= mReConnectTimerPattern.length - 1) {
                        mTimerIndex = mReConnectTimerPattern.length - 1;
                        EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_SEARCH_FAILURE));
                        Log.w(MEDBT.TAG, "connection timeout(3 minutes),stop searching.");
                    } else {
                        Log.w(MEDBT.TAG, "Connection lost, reconnecting in " + mReConnectTimerPattern[mTimerIndex - 1] / 1000 + "s");
                        connect();
                    }
                }
                restartAutoReconnectTimer();
            }
        }, mReConnectTimerPattern[mTimerIndex]);
    }

    @Override
    public void connect() {
        List<GattAttributes.SupportedService> servicelist = new ArrayList<GattAttributes.SupportedService>();
        if(inOTAMode())
        {
            forgetSavedAddress();
            servicelist.add(GattAttributes.SupportedService.OTA_SERVICE);
        } else {
            servicelist.add(GattAttributes.SupportedService.SERVICE);
        }
        Optional<String> preferredAddress = new Optional<String>();

        if(hasSavedAddress()){
            preferredAddress.set(getSaveAddress());
        }
        Log.w(MEDBT.TAG, "servicelist:" + servicelist.get(0) + ",address:" + (preferredAddress.isEmpty() ? "null" : preferredAddress.get()));

        medBT.startScan(servicelist, preferredAddress);
    }

    @Override
    public void reconnect()
    {
        List<GattAttributes.SupportedService> serviceList = new ArrayList<GattAttributes.SupportedService>();
        serviceList.add(GattAttributes.SupportedService.SERVICE);
        Optional<String> preferredAddress = new Optional<String>();
        if(hasSavedAddress()){
            preferredAddress.set(getSaveAddress());
        }
        medBT.startScan(serviceList, preferredAddress);
    }

   protected void destroy()
    {
        medBT.disconnect();
    }

    @Override
    public void sendRequest(BLERequestData request) {
        medBT.sendRequest(request);
    }

    private void currentlyConnected(boolean isConnected) {
        if(isConnected!=mIsConnected) {
            mIsConnected = isConnected;
            //stop ble scan for only one ble device can get connected
            if(isConnected)
            {
                medBT.stopScan();
            }
            Log.w("MED BT SDK", "Connected : " + mIsConnected);
            if (!mIsConnected){
                EventBus.getDefault().post(new BLESearchEvent(BLESearchEvent.SEARCH_EVENT.ON_SEARCHING));
            }
        }
    }

    @Subscribe
    public void onEvent(BLEConnectionStateChangedEvent eventData){
        Log.w("Karl", "On connection state changed @ connection controller");
        if(!eventData.getAddress().equals("") && eventData.isConnected() == true)
        {
            //firstly connected this SERVICE: such as: first run app, forget this SERVICE
            boolean firstConnected = !hasSavedAddress();
            setSaveAddress(eventData.getAddress());

            //http://stackoverflow.com/questions/21398766/android-ble-connection-time-interval
            //fix a bug:when BLE OTA done,need repair SERVICE, if not, must twice connect SERVICE that SERVICE can work fine, here use code do repair working or twice connection
            //call pairDevice() after every connected, if call it within connect() before startScan() invoke,
            //some smartphone will popup message ,this message comes from Android OS, such as samsung...
            if((firstConnected || needPair()) && !inOTAMode()) pairDevice();
        }

        currentlyConnected(eventData.isConnected());
        // TODO BLENNotificationEvent is already posted but no subscribers yet
        EventBus.getDefault().post(new BLENotificationEvent(eventData.isConnected()));
    }

    @Subscribe
    public void onEvent(BLEResponseDataEvent eventData){
        currentlyConnected(true);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public void disconnect() {
        //stop autoconnect timer
        if(mAutoReconnectTimer!=null)
        {
            mAutoReconnectTimer.cancel();
            mAutoReconnectTimer=null;
        }
        //disconnect it
        medBT.disconnect();
    }

    @Override
    public void forgetSavedAddress() {
        //save it for OTA using, add if() for avoid many times invoke "forgetSavedAddress"
        if(!getSaveAddress().equals(""))  mSavedAddress = getSaveAddress();
        setSaveAddress("");
    }

    @Override
    public boolean hasSavedAddress() {
        if(!getSaveAddress().equals(""))
        {
            return true;
        }
        return false;
    }

    private void setSaveAddress(String address)
    {
        context.getSharedPreferences(Constants.PREF_NAME, 0).edit().putString(Constants.SAVE_MAC_ADDRESS, address).commit();
    }

    public String getSaveAddress()
    {
        return context.getSharedPreferences(Constants.PREF_NAME, 0).getString(Constants.SAVE_MAC_ADDRESS, "");
    }

    @Override
    public String getBluetoothVersion() {
        return medBT.getBluetoothVersion();
    }

    @Override
    public String getSoftwareVersion() {
        return medBT.getSoftwareVersion();
    }

    @Override
    public void setOTAMode(boolean otaMode, boolean disConnect) {
        //No need to change the mode if we are already in OTA Mode
        if (inOTAMode() != otaMode ){
            inOTAMode = otaMode;
        }
        if (disConnect)
        {
            medBT.disconnect();
        }
            //restart timer and ping Timer
            mTimerIndex = 0; //after 1s ,do connect
            restartAutoReconnectTimer();
    }

    @Override
    public boolean inOTAMode() {
        return inOTAMode;
    }

    @Override
    public void restoreSavedAddress() {
        if(!mSavedAddress.equals(""))
        {
            setSaveAddress(mSavedAddress);
        }
    }

    @Subscribe
    public void onEvent(BLEFirmwareVersionReceivedEvent event){
        currentlyConnected(true);
    }

    @Override
    public void scan()
    {
        if(inOTAMode()) {
            return;
        }
       mTimerIndex = 0;
       restartAutoReconnectTimer();
    }

    @Override
    public void pairDevice()
    {
        if(!hasSavedAddress()){
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()){
            return;
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(getSaveAddress());
        int state = device.getBondState();
        if(state != BluetoothDevice.BOND_BONDED)
        {
            try {
                Log.i(MEDBT.TAG, "bind state: " + device.getBondState() +",createBond() return:" + createBond(BluetoothDevice.class, device));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void unPairDevice()
    {
        if(!hasSavedAddress()){
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            return;
        }

        BluetoothDevice   device = bluetoothAdapter.getRemoteDevice(getSaveAddress());
        int state = device.getBondState();
        if(state == BluetoothDevice.BOND_BONDED)
        {
            try {
                Log.i(MEDBT.TAG, "bind state: " + device.getBondState() + ",removeBond() return:" + removeBond(BluetoothDevice.class,device));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(state == BluetoothDevice.BOND_BONDING)
        {
            try {

                Log.i(MEDBT.TAG, "bind state: " + device.getBondState() + ",removeBond() return:" + removeBond(BluetoothDevice.class,device));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Set<BluetoothDevice> getDevice() {
        return medBT.getDevices();
    }

    private boolean createBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private boolean removeBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private boolean needPair()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(inOTAMode() || !hasSavedAddress() || !bluetoothAdapter.isEnabled()) {
            return false;
        }

        BluetoothDevice  device = bluetoothAdapter.getRemoteDevice(getSaveAddress());
        int state = device.getBondState();
        Log.i(MEDBT.TAG,"needPair(),current bind state: " + state);
        if(state != BluetoothDevice.BOND_BONDED) {
            return true;
        }
        return false;
    }


}
