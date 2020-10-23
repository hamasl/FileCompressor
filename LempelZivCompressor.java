import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LempelZivCompressor {
    private final File file;
    private final byte[] input;
    private final static int LENGTH_OF_SEARCH = 2048;
    private final static int BYTES_TO_BE_SEARCHED_START_LENGTH = 150;
    private final static int BYTES_TO_BE_SEARCHED_MIN_LENGTH = 9;

    public LempelZivCompressor(File file) throws IOException {
        this.file = file;
        this.input = Files.readAllBytes(file.toPath());
    }

    public String compress(String fileName) throws IOException {
        File outputFile = new File(fileName);
        outputFile.createNewFile();

        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

        int i = 0;
        List<Byte> currentSequence = new ArrayList<>();
        while (i < input.length) {
            short[] compressed = null;

            // Cannot search more forward than what is left of the input
            int bytesForwardToSearch = (input.length - i < BYTES_TO_BE_SEARCHED_START_LENGTH) ? input.length - i
                    : BYTES_TO_BE_SEARCHED_START_LENGTH;

            // Have to have at least 9 to make the compression useful since the forward
            // reference is an int and both the backwards reference and length are shorts
            while (i >= BYTES_TO_BE_SEARCHED_MIN_LENGTH && bytesForwardToSearch + i < input.length
                    && bytesForwardToSearch >= BYTES_TO_BE_SEARCHED_MIN_LENGTH && compressed == null) {
                if (i < LENGTH_OF_SEARCH)
                    compressed = backwardsSearch(i, bytesForwardToSearch, i);
                else
                    compressed = backwardsSearch(i, bytesForwardToSearch, LENGTH_OF_SEARCH);
                bytesForwardToSearch--;
            }
            if (compressed == null) {
                currentSequence.add(input[i++]);
                // In case it is the end of the input file
                if (i >= input.length) {
                    out.writeInt(currentSequence.size());
                    for (int j = 0; j < currentSequence.size(); j++)
                        out.writeByte(currentSequence.get(j));
                }
            } else {
                out.writeInt(currentSequence.size());
                for (int j = 0; j < currentSequence.size(); j++)
                    out.writeByte((byte) currentSequence.get(j));

                out.writeShort(compressed[0]);
                out.writeShort(compressed[1]);
                i += compressed[1];

                currentSequence.clear();
            }
        }
        out.close();
        return fileName;
    }

    private short[] backwardsSearch(int startIndex, int numberOfBytes, int searchLength) {
        for (int i = startIndex - 1; i >= startIndex - searchLength; i--) {
            int count = 0;
            int maxSearchLength = (startIndex - i < numberOfBytes) ? startIndex - i : numberOfBytes;
            if (maxSearchLength >= BYTES_TO_BE_SEARCHED_MIN_LENGTH) {
                for (int j = 0; j < maxSearchLength; j++) {
                    if (input[startIndex + j] == input[i + j])
                        count++;
                }

                if (count == maxSearchLength)
                    return new short[] { (short) ((short) startIndex - i), (short) maxSearchLength };
            }
        }
        return null;
    }

    public String decompress(String fileName) throws IOException {
        File outputFile = new File(fileName);
        outputFile.createNewFile();

        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

        LZByteBuffer buffer = new LZByteBuffer(LENGTH_OF_SEARCH);

        int bytesRecieved = 0;
        while (in.available() > 0) {
            int readForward = in.readInt();
            for (int i = 0; i < readForward; i++) {
                byte b = in.readByte();
                out.writeByte(b);
                buffer.add(b);
                bytesRecieved++;
            }
            if (in.available() > 0) {
                short backwardsReference = in.readShort();
                short length = in.readShort();
                byte[] bytes = getBytes(buffer, (short) (bytesRecieved - backwardsReference), length);
                for (int i = 0; i < bytes.length; i++) {
                    byte b = bytes[i];
                    out.writeByte(b);
                    buffer.add(b);
                    bytesRecieved++;
                }
            }
        }

        in.close();
        out.close();
        return fileName;
    }

    private byte[] getBytes(LZByteBuffer buffer, short startIndex, short length) {
        byte[] bytes = new byte[length];
        for (short i = 0; i < length; i++) {
            bytes[i] = buffer.get(i + startIndex);
        }
        return bytes;
    }
}
