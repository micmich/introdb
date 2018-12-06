package introdb.heap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

class PageBuffers {

    private static final long MEMORY_BUFFERS_ALLOCATION = Runtime.getRuntime().maxMemory() / 2;

    private final int pageSize;
    private final int headerSizeBytes;
    private final int maxNrPages;
    private final Path path;

    private FileChannel fileChannel;
    private long fileHighWaterMark = 0;
    private final HDD hdd = new HDD();


    Deque<PageBuffer> emptyPages = new LinkedList<>();
    Deque<PageBuffer> fullPages = new LinkedList<>();
    SortedSet<PageBuffer> partialPages = new TreeSet<>(Comparator.comparingInt(PageBuffer::freeSize));
    Deque<PageBuffer> onLoan = new LinkedList<>();

    PageBuffers(Path path, int pageSize, int headerSizeBytes, int maxNrPages) {
        this.path = path;
        this.pageSize = pageSize;
        this.headerSizeBytes = headerSizeBytes;
        this.maxNrPages = maxNrPages;

        System.out.println("Max memory allocated for buffers: " + MEMORY_BUFFERS_ALLOCATION / (1024 * 1024));
    }

    PageBuffer getForAppend() throws IOException {
        if (fileChannel == null) {
            openFile();
        }

        if (!partialPages.isEmpty()) {
            PageBuffer mostSpaceAvailable = partialPages.first();
            partialPages.remove(mostSpaceAvailable);
            onLoan.add(mostSpaceAvailable);
            return mostSpaceAvailable;
        }

        if (emptyPages.isEmpty()) {
            allocatePages();
        }
        PageBuffer newlyAllocated = emptyPages.remove();
        onLoan.add(newlyAllocated);
        return newlyAllocated;
    }

    void commit(PageBuffer buffer) {
        boolean removed = onLoan.remove(buffer);
        if (!removed) {
            throw new IllegalStateException("Committing unloaned page!");
        }
        if (buffer.isEmpty()) {
            emptyPages.push(buffer);
            hdd.purgeBuffer(buffer);
            return;
        }
        if (buffer.isFull()) {
            fullPages.push(buffer);
            hdd.synch(buffer);
            return;
        }
        partialPages.add(buffer);
    }

    void noLongerInteresting(PageBuffer buffer) {
        boolean removed = onLoan.remove(buffer);
        if (!removed) {
            throw new IllegalStateException("Committing unloaned page!");
        }
        if (buffer.isEmpty()) {
            emptyPages.push(buffer);
            return;
        }
        if (buffer.isFull()) {
            fullPages.push(buffer);
            return;
        }
        partialPages.add(buffer);
    }



    PageBuffer getContaining(byte[] keyBuffer) {
        if (fileChannel == null) {
            openFile();
        }

        for (PageBuffer fullPage : fullPages) {
            if (fullPage.containsKey(keyBuffer)) {
                if (!onLoan.contains(fullPage)) {
                    onLoan.push(fullPage);
                }
                fullPages.remove(fullPage);
                return fullPage;
            }
        }
        for (PageBuffer partialPage : partialPages) {
            if (partialPage.containsKey(keyBuffer)) {
                if (!onLoan.contains(partialPage)) {
                    onLoan.push(partialPage);
                }
                partialPages.remove(partialPage);
                return partialPage;
            }
        }
        return null;
    }




    private void allocatePages() throws IOException {
        while (fileHighWaterMark < (maxNrPages * pageSize)
            && (fileHighWaterMark < MEMORY_BUFFERS_ALLOCATION)
        ) {
            emptyPages.push(new PageBuffer(fileHighWaterMark, pageSize));
            fileHighWaterMark += pageSize;
        }

        if (emptyPages.size() == 0) {
            throw new IllegalStateException("Failed empty buffer allocation."
                    + " Have partial " + partialPages.size()
                    + " Have full " + fullPages.size()
                    + " Page size: " + pageSize
                    + " maxNrPages: " + maxNrPages
                    + " file highWaterMark " + fileHighWaterMark);
        }
    }

    private void openFile() {
        if (!Files.exists(path) && Files.isReadable(path)) {
            throw new IllegalStateException("Unable to read file " + path.toAbsolutePath());
        }

        try {
            fileChannel = FileChannel.open(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
