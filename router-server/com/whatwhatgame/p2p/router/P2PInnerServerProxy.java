package com.whatwhatgame.p2p.router;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import com.whatwhatgame.p2p.util.P2PUtil;

/**
 * 单现场代理远程服务器<br>
 * 
 * @author yanchangyou
 *
 */
public class P2PInnerServerProxy extends Thread {

	static Logger log = Logger.getAnonymousLogger();

	Socket socket;
	BufferedWriter writer;
	BufferedReader reader;
	P2PHttpServer httpServer;

	public P2PInnerServerProxy(Socket socket) throws IOException {
		this.socket = socket;

		writer = P2PUtil.convertToWriter(socket.getOutputStream());
		reader = P2PUtil.convertToReader(socket.getInputStream());

	}

	public String executeCommand(String command) throws IOException {
		P2PUtil.write(writer, command);
		String result = P2PUtil.readContent(reader);
		return result;
	}

	public static void service(Socket socket) throws IOException {

		new P2PInnerServerProxy(socket).start();

	}

	public void run() {

		try {

			String firstLine = reader.readLine();
			int servicePort = Integer.parseInt(firstLine);

			httpServer = new P2PHttpServer(servicePort, this);

			httpServer.start();
			
			log.info("代理服务器启动(" + servicePort + ")。。。");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		if (httpServer != null) {
			httpServer.stop();
		}
		if (reader != null) {
			reader.close();
		}
		if (writer != null) {
			writer.close();
		}
		if (socket != null) {
			socket.close();
		}
	}

}
