package introdb.heap;

import java.io.Serializable;

class Serializer {


    Serializer() {
    }

    void serialize(Entry entry, PageBuffer buffer) {
    }

    void serialize(Serializable key, byte[] keyBuffer) {
    }

    Entry deserialize(byte[] serialized) {
        return null;
    }
}
