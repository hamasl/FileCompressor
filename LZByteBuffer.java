import java.util.Arrays;

public class LZByteBuffer {
    private int capacity;
    private byte[] buffer;
    private int items;

    public LZByteBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new byte[capacity];
        items = 0;
    }

    public void add(byte b) {
        buffer[items % capacity] = b;
        items++;
    }

    public int getFirstIndex() {
        return items % capacity;
    }

    public byte get(int index) {
        if (index < items - capacity)
            throw new IllegalArgumentException("Item no longer available");
        if (index >= items)
            throw new IllegalArgumentException("Cannot retrieve item with larger index than items in buffer");
        if (index < capacity)
            return buffer[index];
        return buffer[index % capacity];
    }

    public String toString() {
        return Arrays.toString(buffer);
    }
}
