package com.whatwhatgame.p2p.router;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.whatwhatgame.p2p.util.P2PUtil;

public class P2PHttpServer {

	static Logger log = Logger.getAnonymousLogger();

	HttpServer httpServer;
	P2PInnerServerProxy serverProxy;

	/**
	 * 端口与httpserver映射，便于inner-server的重启，异常等处理
	 */
	static Map<Integer, P2PHttpServer> portHttpServerMap = new HashMap<Integer, P2PHttpServer>();

	public P2PHttpServer(int port, final P2PInnerServerProxy serverProxy) throws IOException {
		this.serverProxy = serverProxy;
		InetSocketAddress inetSock = new InetSocketAddress(port);
		try {
			P2PHttpServer server = portHttpServerMap.get(port);
			if (server != null) {
				server.serverProxy.close();
			}

			httpServer = HttpServer.create(inetSock, 20000);

			portHttpServerMap.put(port, this);

			String message = "注册成功！(" + port + ")";
			log.info(message);

			P2PUtil.write(serverProxy.writer, message);
			P2PUtil.write(serverProxy.writer, P2PUtil.P2P_CONTENT_END_LINE);

		} catch (Exception e) {

			String message = "注册失败！(" + e.getMessage() + ")";
			log.info(message);

			e.printStackTrace();
			P2PUtil.write(serverProxy.writer, P2PUtil.packageErrorMessage(message));
			serverProxy.close();
			
			return;
		}

		httpServer.createContext("/cgi", new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {

				try {

					URI uri = exchange.getRequestURI();

					String command = P2PUtil.parsingCommand(uri.toString());

					log.info("command : " + command);
					String responseString = serverProxy.executeCommand(command);
					if (P2PUtil.isP2PErrorMessage(responseString)) {
						System.err.println(serverProxy.socket + " : " + responseString);
					}

					exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK,
							responseString.getBytes().length);
					
					OutputStream out = exchange.getResponseBody();

					out.write(responseString.getBytes());
					out.flush();
					out.close();
				} catch (IOException e) {
					throw e;
				} finally {
					exchange.close();
				}
			}
		});
	}

	public void start() {
		httpServer.start();
	}

	public void stop() throws IOException {
		httpServer.stop(0);
	}
}
