package introdb.heap;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class UnorderedHeapFile implements Store{

	private static final int HEADER_SIZE_BYTES = 1;

	private final Path path;
	private final int maxNrPages;
	private final int pageSize;

	private FileChannel fileChannel;
	private PageBuffers pageBuffers;
	private Serializer serializer;

	UnorderedHeapFile(Path path, int maxNrPages, int pageSize) {
		this.path = path;
		this.maxNrPages = maxNrPages;
		this.pageSize = pageSize;

		pageBuffers = new PageBuffers(pageSize, HEADER_SIZE_BYTES, maxNrPages);
		serializer = new Serializer(pageSize - HEADER_SIZE_BYTES);
	}

	@Override
	public void put(Entry entry) throws IOException, ClassNotFoundException {
		if (fileChannel == null) {
			openFile();
		}

		serializer.serialize(entry);
		PageBuffer buffer = pageBuffers.getForAppend(serializer.getResultSizeBytes());
		buffer.append(serializer.getContent());
		buffer.flush();
		serializer.dumpContent();
	}

	@Override
	public Object get(Serializable key) throws IOException, ClassNotFoundException {
		if (fileChannel == null) {
			openFile();
		}

		throw new UnsupportedOperationException();
	}

	public Object remove(Serializable key) throws IOException, ClassNotFoundException {
		if (fileChannel == null) {
			openFile();
		}

		throw new UnsupportedOperationException();
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

