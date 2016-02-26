/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.util;



public class Constants {
    //TODO best to do in core_keys.xml
    public static final String PREF_NAME = "NevoPrefs";
    public static final String SAVE_MAC_ADDRESS = "savemacaddress";
    public static final String LAST_SYNC = "last_sync";
    public static final String LAST_SYNC_TIME_ZONE = "last_sync_time_zone";
    public static final String FIRST_FLAG = "first_flag";

    static public class DFUResponse
    {
        byte responseCode;
        byte requestedCode;
        byte responseStatus;

        public DFUResponse(byte responseCode,byte requestedCode,byte responseStatus)
        {
            this.responseCode = responseCode;
            this.requestedCode = requestedCode;
            this.responseStatus = responseStatus;
        }
        public byte getresponseCode(){return responseCode;}
        public byte getrequestedCode(){return requestedCode;}
        public byte getresponseStatus(){return responseStatus;}

        public void setresponseCode(byte responseCode){this.responseCode=responseCode;}
        public void setrequestedCode(byte requestedCode){this.requestedCode=requestedCode;}
        public void setresponseStatus(byte responseStatus){this.responseStatus=responseStatus;}
    }

    public enum  enumFileExtension{
        HEX(0),ZIP(1),BIN(2);
        private int mValue;
        private enumFileExtension(int value){this.mValue = value;}
        public  int rawValue() {return mValue;}
    }

    public enum  enumPacketOption{
        PACKETS_NOTIFICATION_INTERVAL(10),PACKET_SIZE (20);
        private int mValue;
        private enumPacketOption(int value){this.mValue = value;}
        public  int rawValue() {return mValue;}
    }

    public enum DfuOperations {
         START_DFU_REQUEST(0x01),
            INITIALIZE_DFU_PARAMETERS_REQUEST (0x02),
                    RECEIVE_FIRMWARE_IMAGE_REQUEST (0x03),
                    VALIDATE_FIRMWARE_REQUEST (0x04),
                    ACTIVATE_AND_RESET_REQUEST (0x05),
                    RESET_SYSTEM (0x06),
                    PACKET_RECEIPT_NOTIFICATION_REQUEST (0x08),
                    RESPONSE_CODE (0x10),
                    PACKET_RECEIPT_NOTIFICATION_RESPONSE (0x11);

        private int mValue;
        private DfuOperations(int value){this.mValue = value;}
        public  int rawValue() {return mValue;}
    }

    public enum DfuOperationStatus{
            OPERATION_SUCCESSFUL_RESPONSE (0x01),
            OPERATION_INVALID_RESPONSE (0x02),
                    OPERATION_NOT_SUPPORTED_RESPONSE (0x03),
                    DATA_SIZE_EXCEEDS_LIMIT_RESPONSE (0x04),
                    CRC_ERROR_RESPONSE (0x05),
                    OPERATION_FAILED_RESPONSE (0x06);
        private int mValue;
        private DfuOperationStatus(int value){this.mValue = value;}
        public  int rawValue() {return mValue;}
    }

    public enum DFUControllerState
    {
        INIT,
        DISCOVERING,
        IDLE,
        SEND_NOTIFICATION_REQUEST,
        SEND_START_COMMAND,
        SEND_RECEIVE_COMMAND,
        SEND_FIRMWARE_DATA,
        SEND_VALIDATE_COMMAND,
        SEND_RESET,
        WAIT_RECEIPT,
        FINISHED,
        CANCELED,
        SEND_RECONNECT
    }

    public enum DfuFirmwareTypes{
          SOFTDEVICE (0x01),
          BOOTLOADER (0x02),
          SOFTDEVICE_AND_BOOTLOADER (0x03),
          APPLICATION (0x04);
        private int mValue;
        private DfuFirmwareTypes(int value){this.mValue = value;}
        public  int rawValue() {return mValue;}
    }
}
	