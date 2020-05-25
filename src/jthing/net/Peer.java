package jthing.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import toools.io.file.RegularFile;

/**
 * Specifies how to reach a given peer.
 * 
 * @author lhogie
 *
 */
public class Peer implements Serializable {
	public enum names {
		ssh_username, inbox_directory, ips, id, tcp_port, udp_port, web_servers;
	}

	final Map<String, Object> map = new HashMap<>();

	public Peer() {
		set(names.ips.name(), new ArrayList<InetAddress>());
	}

	public void loadFrom(RegularFile f) {
		try {
			InputStream is = f.createReadingStream();
			Properties p = new Properties();
			p.load(is);
			is.close();
			map.clear();
			p.entrySet()
					.forEach(e -> map.put((String) e.getKey(), (String) e.getValue()));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveTo(RegularFile f) {
		try {
			Properties p = new Properties();
			map.entrySet().forEach(e -> p.put(e.getKey(), e.getValue()));
			OutputStream out = f.createWritingStream();
			p.store(out, "");
			out.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Object get(String name) {
		return map.get(name);
	}

	public void set(String key, Object value) {
		map.put(key, value);
	}

	@Override
	public String toString() {
		return getID().toString();
	}

	public String toHTML() {
		StringBuilder s = new StringBuilder("<ul>\n");
		map.entrySet().forEach(
				e -> s.append("\t<li>" + e.getKey() + ": " + e.getValue() + "\n"));
		s.append("</ul>\n");
		return s.toString();
	}

	@Override
	public int hashCode() {
		return getID().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Peer && obj.hashCode() == hashCode();
	}

	public void fromCommandLine(String s) {
		s = s.replaceAll(" +", " ");
		s = s.replaceAll(",", "\n");

		Properties p = new Properties();

		try {
			p.load(new StringReader(s));
		}
		catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		p.entrySet().forEach(e -> map.put((String) e.getKey(), (String) e.getValue()));
	}

	public List<Protocol> getUseableProtocols() {
		List<Protocol> r = new ArrayList<>();
		UDP udp = new UDP();

		if (udp.canSendTo(this)) {
			r.add(udp);
		}

		return r;
	}

	public boolean hasInfo(String key) {
		return map.containsKey(key);
	}

	public PeerID getID() {
		return (PeerID) get(names.id.name());
	}

	public List<InetAddress> ips() {
		return (List<InetAddress>) get(names.ips.name());
	}

	public void setTCPPort(int port) {
		map.put(names.tcp_port.name(), port);
	}

	public void setUDPPort(int port) {
		map.put(names.udp_port.name(), port);
	}

	public int getTCPPort() {
		return (Integer) map.get(names.tcp_port.name());
	}

	public int getUDPPort() {
		return (Integer) map.get(names.udp_port.name());
	}

	public String getSSH_USERNAME() {
		return (String) map.get(names.ssh_username.name());
	}

	public boolean hasInfo(names n) {
		return hasInfo(n.name());
	}

}
