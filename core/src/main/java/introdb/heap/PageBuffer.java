package introdb.heap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class PageBuffer {

    private long startOffset;
    int highWaterMark;
    byte[] pageBuffer;
    private final ObjectOutputStream objectOutputStream;

    PageBuffer(long startOffset, int pageSize) throws IOException {
        this.startOffset = startOffset;
        pageBuffer = new byte[pageSize];

        objectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream(pageSize));
    }

    void append(byte[] content) {
        objectOutputStream.writeObject(content);
        objectOutputStream.
    }

    boolean overflowed() {
        return false;
    }

    byte[] entryFor(byte[] keyBuffer) {
        return new byte[0];
    }

    void deleteLast() {

    }

    void delete(byte[] keyBuffer) {

    }

    int freeSize() {
        return 0;
    }

    boolean isEmpty() {
        return false;
    }

    boolean isFull() {
        return false;
    }

    boolean containsKey(byte[] key) {
        return false;
    }
}
