package com.whatwhatgame.p2p.chart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ChartServer {

	static Logger log = Logger.getAnonymousLogger();

	public static void main(String[] args) throws IOException {

		int port = 1111;
		ServerSocket serverSocket = new ServerSocket(port);
		log.info("server start at : " + port);
		try {
			while (true) {
				Socket socket = serverSocket.accept();
				SocketService.service(socket);
			}
		} finally {
			log.info("server stop at : " + port);
		}
	}
}

class SocketService extends Thread {
	Socket socket;

	BufferedReader reader;
	BufferedWriter writer;

	static SocketService serverSocket;// 服务器
	static List<SocketService> clientSockets = new ArrayList<SocketService>();// 客户端

	public SocketService(Socket socket) throws IOException {
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	public static void service(Socket socket) throws IOException {
		SocketService socketService = new SocketService(socket);
		if (serverSocket == null) {
			serverSocket = socketService;
		} else {
			clientSockets.add(socketService);
		}
		socketService.start();
	}

	@Override
	public void run() {
		try {
			if (serverSocket == this) {

				writer.write("welcome to p2p server!\r\n");
				writer.write("listen all clients:\r\n");

				writer.flush();

				String line = null;
				while ((line = reader.readLine()) != null) {
					line = new String(line);
					if (line.matches("^(quit|exit)$")) {
						closeAll();
						serverSocket = null;
						break;
					}
					writer.write(line + "\r\n");
					writer.flush();
					for (SocketService socketService : clientSockets) {
						if (socketService.socket.isClosed()) {
							continue;
						}
						String msg = packMessage(socket, line);
						socketService.writer.write(msg);
						socketService.writer.flush();
					}

					System.out.println(line);
				}

			} else {

				writer.write("welcome to p2p server!\r\n");
				writer.write("speak to server:\r\n");

				writer.flush();
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.matches("^(quit|exit)$")) {
						close(this);
						break;
					}
					if (serverSocket.socket.isClosed()) {
						break;
					}
					writer.write(line + "\r\n");
					writer.flush();

					String msg = packMessage(socket, line);
					serverSocket.writer.write(msg);
					serverSocket.writer.flush();

					System.out.println(line);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String packMessage(Socket socket, String msg) {
		return socket.getInetAddress().getHostName() + ":" + socket.getPort() + "\r\n\t" + msg
				+ "\r\n";
	}

	public static void close(SocketService service) throws IOException {
		if (service.socket.isClosed()) {
			return;
		}
		service.reader.close();
		service.writer.close();
		service.socket.close();
	}

	public static void closeAll() throws IOException {
		close(serverSocket);
		for (SocketService socketService : clientSockets) {
			close(socketService);
		}
	}
}
