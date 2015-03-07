package com.whatwhatgame.p2p.router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class P2PRouterServer {

	static Logger log = Logger.getAnonymousLogger();

	public static void main(String[] args) throws IOException {

		int port = 8888;

		if (args.length > 0) {// 参数端口，默认8888
			port = Integer.parseInt(args[0]);
		}

		ServerSocket serverSocket = new ServerSocket(port);
		log.info("server start at : " + port);
		try {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					log.info("注册请求开始：" + socket);
					P2PInnerServerProxy.service(socket);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			log.info("server stop at : " + port);
		}
	}
}
