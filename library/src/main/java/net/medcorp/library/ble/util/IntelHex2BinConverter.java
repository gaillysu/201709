package net.medcorp.library.ble.util;

/**
 * Created by gaillysu on 15/6/5.
 */
public class IntelHex2BinConverter {

    static public byte ascii2char(byte ascii)
    {
        if (ascii >= 'A')
        return (byte)(ascii - 0x37);

        if (ascii >= '0')
        return (byte)(ascii - '0');
        return -1;
    }

    static public byte readByte(byte[] pointer)
    {
        byte first  = ascii2char(pointer[0]);
        byte second = ascii2char(pointer[1]);

        return (byte)((first << 4) | second);
    }

    static public char readAddress(byte[] pointer)
    {
        byte msb = readByte(new byte[] {pointer[0],pointer[1]});
        byte lsb = readByte(new byte[] {pointer[2],pointer[3]});

        return (char)((msb << 8) | lsb);
    }

    static public int calculateBinLength(final byte[] hex)
    {
        if (hex == null || hex.length == 0)
        {
            return 0;
        }

        int binLength = 0;
        final int hexLength = hex.length;
        int pointer = 0;
        char lastULBA = 0;

        do
        {
            final byte semicollon = hex[pointer++];

            // Validate - each line of the file must have a semicollon as a firs char
            if (semicollon != ':')
            {
                return 0;
            }

            char reclen = (char) readByte(new byte[]{hex[pointer],hex[pointer+1]}); pointer += 2;
            char offset = readAddress(new byte[]{hex[pointer],hex[pointer+1],hex[pointer+2],hex[pointer+3]}); pointer += 4;
            char rectype = (char) readByte(new byte[]{hex[pointer],hex[pointer+1]}); pointer += 2;

            switch (rectype) {
                case 0x04: {
                    // Only consistent hex files are supported. If there is a jump (0x04) to non-following address skip the rest of the file
                    final char newULBA = readAddress(new byte[]{hex[pointer],hex[pointer+1],hex[pointer+2],hex[pointer+3]});
                    if (binLength > 0 && newULBA != lastULBA + 1)
                        return binLength;
                    lastULBA = newULBA;
                    break;
                }
                case 0x02:
                    // Should here be the same as for 0x04?
                    break;
                case 0x00:
                    // If record type is Data Record (rectype = 0), add it's length (only it the address is >= 0x1000, MBR is skipped)
                    if ((lastULBA << 16) + offset >= 0x1000)
                        binLength += reclen;
                default:
                    break;
            }

            pointer += (reclen << 1);  // Skip the data when calculating length
            pointer += 2;   // Skip the checksum
            // Skip new line
            if (hex[pointer] == '\r') pointer++;
            if (hex[pointer] == '\n') pointer++;
        } while (pointer != hexLength);

        return binLength;
    }

    static public byte[] convert(final byte[]hex)
    {
        final int binLength = calculateBinLength(hex);
        final int hexLength = hex.length;
        int pointer = 0;
        int bytesCopied = 0;
        char lastULBA = 0;

        byte[] bytes = new byte[binLength];
        int  output = 0;

        do
        {
            byte semicollon = hex[pointer++];

            // Validate - each line of the file must have a semicollon as a firs char
            if (semicollon != ':')
            {
                return null;
            }

            char reclen = (char) readByte(new byte[]{hex[pointer],hex[pointer+1]}); pointer += 2;
            char offset = readAddress(new byte[]{hex[pointer],hex[pointer+1],hex[pointer+2],hex[pointer+3]}); pointer += 4;
            char rectype = (char) readByte(new byte[]{hex[pointer],hex[pointer+1]}); pointer += 2;

            switch (rectype) {
                case 0x04: {
                    // Only consistent hex files are supported. If there is a jump (0x04) to non-following address skip the rest of the file
                    final char newULBA = readAddress(new byte[]{hex[pointer],hex[pointer+1],hex[pointer+2],hex[pointer+3]}); pointer += 4;
                    if (bytesCopied > 0 && newULBA != lastULBA + 1)
                    {
                        byte[] outbytes = new byte[bytesCopied];
                        System.arraycopy(bytes,0,outbytes,0,bytesCopied);
                        return outbytes;
                    }
                    lastULBA = newULBA;
                    break;
                }
                case 0x00:
                    // If record type is Data Record (rectype = 0), copy data to output buffer
                    // Skip data below 0x1000 address (MBR)
                    if ((lastULBA << 16) + offset >= 0x1000)
                    {
                        for (int i = 0; i < reclen; i++)
                        {
                            bytes[output++] = readByte(new byte[]{hex[pointer],hex[pointer+1]}); pointer += 2;
                            bytesCopied++;
                        }
                    }
                    else
                    {
                        pointer += (reclen << 1);  // Skip the data
                    }
                    break;
                case 0x02:
                    // Should here be the same as for 0x04?
                default:
                    pointer += (reclen << 1);  // Skip the data when calculating length
                    break;
            }

            pointer += 2;   // Skip the checksum
            // Skip new line
            if (hex[pointer] == '\r') pointer++;
            if (hex[pointer] == '\n') pointer++;
        } while (pointer !=  hexLength);

        byte[] outbytes = new byte[bytesCopied];
        System.arraycopy(bytes,0,outbytes,0,bytesCopied);
        return outbytes;
    }

}
