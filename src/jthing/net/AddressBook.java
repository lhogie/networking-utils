package jthing.net;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import toools.io.Cout;
import toools.io.file.Directory;
import toools.io.file.RegularFile;
import toools.thread.Threads;

public class AddressBook implements Serializable {
	public final static Directory d = new Directory(
			"$HOME/" + AddressBook.class.getPackage().getName() + "/addressBooks");

	private final List<Peer> cards = new ArrayList<>();

	public AddressBook() {
	}

	public AddressBook(Directory directory) {
		if ( ! directory.exists())
			Cout.warning("address book directory " + directory + " cannot be found");

		Threads.loop(1000, () -> true, () -> {
			if (directory.exists())
				loadCards(directory);
		});
	}

	public List<Peer> lookup(Predicate<Peer> p) {
		return cards.stream().filter(p).collect(Collectors.toList());
	}

	public Peer lookupSingle(Predicate<Peer> p) {
		List<Peer> r = cards.stream().filter(p).collect(Collectors.toList());

		if (r.size() == 1) {
			return r.get(0);
		}

		throw new IllegalStateException();
	}

	public Peer lookup(InetAddress ip) {
		return lookupSingle(p -> p.ips().contains(ip));
	}

	public Peer lookupByID(PeerID id) {
		return lookupSingle(p -> p.getID().equals(id));
	}

	public void addAll(AddressBook ab) {
		cards.addAll(ab.cards);
	}

	public void add(Peer e) {
		cards.add(e);
	}

	public void loadCards(Directory d) {
		for (RegularFile file : d.listRegularFiles()) {
			Peer c = new Peer();
			cards.add(c);
			c.loadFrom(file);
		}
	}

	public List<Peer> getCards() {
		return cards;
	}

	public Set<PeerID> ids() {
		Set<PeerID> r = new HashSet<>(cards.size());

		for (Peer c : cards) {
			PeerID id = c.getID();

			if (id != null) {
				r.add(id);
			}
		}

		return r;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		cards.forEach(c -> s.append(c + "\n"));
		return s.toString();
	}

	public String toHTML() {
		StringBuilder s = new StringBuilder("<ul>\n");
		cards.forEach(c -> s.append("\t<h1>Card</h1><li>" + c.toHTML() + "\n"));
		s.append("</ul>\n");
		return s.toString();
	}

	public void addByIP(String... ips) throws UnknownHostException {
		for (String ip : ips) {
			Peer c = new Peer();
			c.ips().add(InetAddress.getByName(ip));
			c.setTCPPort(IP.DEFAULT_PORT);
			c.setUDPPort(IP.DEFAULT_PORT);
			cards.add(c);
		}
	}

	public void addLocalPeerByPort(int... ports) throws UnknownHostException {
		for (int port : ports) {
			Peer c = new Peer();
			c.ips().add(InetAddress.getLocalHost());
			c.setTCPPort(port);
			c.setUDPPort(port);
			cards.add(c);
		}
	}

	public static Set<InetAddress> sshKnownHosts() {
		RegularFile knownHostsFile = new RegularFile("$HOME/.ssh/known_hosts");
		Set<String> firstElementOfLines = new HashSet<>();

		for (String l : knownHostsFile.getLines()) {
			String e = l.split(" ")[0];
			int i = e.indexOf(",");

			if (i < 0) {
				firstElementOfLines.add(e);
			}
			else {
				firstElementOfLines.add(e.substring(0, i));
			}
		}

		Set<InetAddress> ips = new HashSet<>();

		for (String e : firstElementOfLines) {
			try {
				ips.add(InetAddress.getByName(e));
			}
			catch (UnknownHostException e1) {
			}
		}

		return ips;
	}

	public static Set<InetAddress> oarNodes() {
		String oarNodesFileName = System.getenv("OAR_NODEFILE");
		RegularFile f = new RegularFile(oarNodesFileName);

		if (f.exists()) {
			return nodeList(f);
		}
		else {
			return null;
		}
	}

	public static Set<InetAddress> nodeList(RegularFile f) {
		Set<InetAddress> ips = new HashSet<>();

		for (String l : f.getLines()) {
			try {
				ips.add(InetAddress.getByName(l));
			}
			catch (UnknownHostException e1) {
			}
		}

		return ips;
	}

	public static AddressBook ab(Set<InetAddress> ips) {
		AddressBook ab = new AddressBook();

		for (InetAddress ip : ips) {
			Peer c = new Peer();
			c.ips().add(ip);
			ab.add(c);
		}

		return ab;
	}
}
