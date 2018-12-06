package introdb.heap;

class KeyBuffers {

    byte[] key;
    boolean loaned = false;

    KeyBuffers(int maxKeySize) {
        key = new byte[maxKeySize];
    }

    byte[] getKeyBuffer() {
        loaned = true;
        return key;
    }

    void noLongerInteresting(byte[] keyBuffer) {
        loaned = false;
    }
}
