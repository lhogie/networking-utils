package jthing.net;

import java.util.List;
import java.util.Random;

import jthing.net.Peer.names;
import toools.io.Cout;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.io.ser.Serializer;
import toools.thread.Threads;

public class FileSystemProtocol extends Protocol {
	final Directory baseDirectory;
	private final Directory inboxDirectory;

	public FileSystemProtocol(String peerName, Directory baseDirectory) {
		this.baseDirectory = baseDirectory;
		this.inboxDirectory = new Directory(baseDirectory, peerName);

		if (inboxDirectory.exists()) {
			inboxDirectory.clear();
		}
		else {
			inboxDirectory.mkdirs();
		}
	}

	@Override
	public void unicastImpl(Peer from, Message m, Peer to) {
		unicast(from, m, to, String.valueOf(Math.abs(new Random().nextLong())));
	}

	public void unicast(Peer from, Message msg, Peer to, String filename) {
		Directory toDir = new Directory(baseDirectory, to.toString());
		toDir.ensureExists();
		RegularFile f = new RegularFile(toDir, filename + ".ser");
		byte[] bytes = Serializer.getDefaultSerializer().toBytes(msg);
		f.setContent(bytes);
	}

	@Override
	public String getName() {
		return "shared-directory driver";
	}

	@Override
	public boolean canSendTo(Peer c) {
		return c.hasInfo(names.id);
	}

	@Override
	public void fill(Peer c) {
		c.set(names.inbox_directory.name(), inboxDirectory.getPath());
	}

	private boolean run = false;

	@Override
	protected void start() {
		Cout.info("monitoring directory " + inboxDirectory + " for message files");

		run = true;

		Threads.loop(1000, () -> run, () -> {
			List<RegularFile> files = inboxDirectory.listRegularFiles();
			files.sort((a, b) -> Long.compare(b.getAgeMs(), a.getAgeMs()));

			files.forEach(f -> {
				processNewMessage(extract(f));
				f.delete();
			});
		});
	}

	protected Message extract(RegularFile f) {
		try {
			Message msg = (Message) Serializer.getDefaultSerializer()
					.fromBytes(f.getContent());
			return msg;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void stop() {
		run = false;
	}
}
