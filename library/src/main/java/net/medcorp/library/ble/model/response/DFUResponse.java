package net.medcorp.library.ble.model.response;

/**
 * Created by karl-john on 3/3/16.
 */
public class DFUResponse {

    private byte responseCode;
    private byte requestedCode;
    private byte responseStatus;

    public DFUResponse(byte responseCode,byte requestedCode,byte responseStatus) {
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
