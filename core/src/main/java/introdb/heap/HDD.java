package introdb.heap;

class HDD {
    void purgeBuffer(PageBuffer buffer) {
        System.out.println("purgeBuffer is NOOP");
    }

    void synch(PageBuffer buffer) {
        System.out.println("synch is NOOP");
    }
}
