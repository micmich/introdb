package introdb.heap;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class UnorderedHeapFile implements Store{

	private static final int PAGE_HEADER_SIZE_BYTES = 16;

	private final Path path;
	private final int maxNrPages;
	private final int pageSize;

	private PageBuffers writeBuffers;
	private KeyBuffers keyBuffers;
	private Serializer serializer;

	UnorderedHeapFile(Path path, int maxNrPages, int pageSize) {
		this.path = path;
		this.maxNrPages = maxNrPages;
		this.pageSize = pageSize;

		writeBuffers = new PageBuffers(path, pageSize, PAGE_HEADER_SIZE_BYTES, maxNrPages);
		keyBuffers = new KeyBuffers(pageSize);
		serializer = new Serializer();
	}

	@Override
	public void put(Entry entry) throws IOException, ClassNotFoundException {

		PageBuffer buffer = writeBuffers.getForAppend();
		serializer.serialize(entry, buffer);
		if (buffer.overflowed()) {
			buffer.deleteLast();
			serializer.serialize(entry, buffer);
			if (buffer.overflowed()) {
				throw new IllegalArgumentException("Entry overflows buffer");
			}
		}
		writeBuffers.commit(buffer);
	}

	@Override
	public Object get(Serializable key) throws IOException, ClassNotFoundException {
		byte[] keyBuffer = keyBuffers.getKeyBuffer();
		PageBuffer buffer = writeBuffers.getContaining(keyBuffer);
		byte[] serialized = buffer.entryFor(keyBuffer);
		keyBuffers.noLongerInteresting(keyBuffer);
		Object result = serializer.deserialize(serialized);
		writeBuffers.noLongerInteresting(buffer);
		return result;


//
//		byte[] keyBuffer = keyBuffers.getKeyBuffer();
//		serializer.serialize(key, keyBuffer);
//		PageBuffer buffer = writeBuffers.getContaining(key);
//		if (buffer == null) {
//			return null;
//		}
//		byte[] serialized = buffer.entryFor(keyBuffer);
//		keyBuffers.noLongerInteresting(keyBuffer);
//		Object result = serializer.deserialize(serialized);
//		writeBuffers.noLongerInteresting(buffer);
//		return result;
	}

	public Object remove(Serializable key) throws IOException, ClassNotFoundException {
		byte[] keyBuffer = keyBuffers.getKeyBuffer();
		PageBuffer buffer = writeBuffers.getContaining(keyBuffer);
		byte[] serialized = buffer.entryFor(keyBuffer);
		Object result = serializer.deserialize(serialized);
		buffer.delete(keyBuffer);
		keyBuffers.noLongerInteresting(keyBuffer);
		writeBuffers.commit(buffer);
		return result;
	}


}

