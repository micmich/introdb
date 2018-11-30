package introdb.heap;

class Serializer {

    byte[] buffer;
    int bufferOffsetBeyondContent = 0;

    Serializer(int maxBuffer) {
        this.buffer = new byte[maxBuffer];
    }

    void serialize(Entry entry) {
        if (bufferOffsetBeyondContent != 0) {
            throw new IllegalStateException("Serializer used before buffer dumped.");
        }
    }

    int getResultSizeBytes() {
        return 0;
    }

    byte[] getContent() {
        if (bufferOffsetBeyondContent == 0) {
            throw new IllegalStateException("Retrieving content of an empty buffer");
        }

        throw new UnsupportedOperationException();
    }

    void dumpContent() {
        bufferOffsetBeyondContent = 0;
    }

}
