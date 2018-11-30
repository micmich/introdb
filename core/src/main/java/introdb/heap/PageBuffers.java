package introdb.heap;

class PageBuffers {

    private final int pageSize;
    private final int headerSizeBytes;
    private final int maxNrPages;

    PageBuffers(int pageSize, int headerSizeBytes, int maxNrPages) {

        this.pageSize = pageSize;
        this.headerSizeBytes = headerSizeBytes;
        this.maxNrPages = maxNrPages;
    }

    PageBuffer getForAppend(int resultSizeBytes) {
        return null;
    }
}
