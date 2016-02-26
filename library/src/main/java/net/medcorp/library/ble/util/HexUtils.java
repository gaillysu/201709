/*
 * COPYRIGHT (C) 2014 MED Enterprises LTD. All Rights Reserved.
 */
package net.medcorp.library.ble.util;


/*
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public class HexUtils {

	public static int getValueForOctet(String octet, int startIndex,
			int length, int powerValue) {
		int value = 0;

		if ((startIndex + length - 1) < octet.length()) {
			String bit = octet.substring(startIndex, startIndex + length);

			int iBit = 0;

			iBit = Integer.parseInt(bit, 16);

			value = (int) (iBit * Math.pow(16, powerValue));
		} else
			value = 0;
		return value;
	}
	
	public static byte[] DecToBCDArray(int num) {
		
		if(num==0) return new byte[]{0x00};
		
		int digits = 0;
 
		long temp = num;
		while (temp != 0) {
			digits++;
			temp /= 10;
		}
 
		int byteLen = digits % 2 == 0 ? digits / 2 : (digits + 1) / 2;
		boolean isOdd = digits % 2 != 0;
 
		byte bcd[] = new byte[byteLen];
 
		for (int i = 0; i < digits; i++) {
			byte tmp = (byte) (num % 10);
 
			if (i == digits - 1 && isOdd)
				bcd[i / 2] = tmp;
			else if (i % 2 == 0)
				bcd[i / 2] = tmp;
			else {
				byte foo = (byte) (tmp << 4);
				bcd[i / 2] |= foo;
			}
 
			num /= 10;
		}
 
		for (int i = 0; i < byteLen / 2; i++) {
			byte tmp = bcd[i];
			bcd[i] = bcd[byteLen - i - 1];
			bcd[byteLen - i - 1] = tmp;
		}
 
		return bcd;
	}

    //Little endian bytes !
    public static int bytesToInt( byte[] bytes ) {
        int result = 0;
        for (int i=0; i<bytes.length; i++) {
            result =  (int) (result + ( (int)( bytes[i] & 0xFF) ) * Math.pow(2, 8*i));
        }
        return result;
    }
	
}
