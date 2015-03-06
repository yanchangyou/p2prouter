package com.whatwhatgame.p2p.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import com.whatwhatgame.p2p.bean.RouterServerAddress;
import com.whatwhatgame.p2p.util.P2PUtil;

/**
 * 
 * 内部服务器：局域网内<br>
 * 
 * 输入：路由服务器 ip：port，以及要申请注册的对外服务器端口<br>
 * 过程：连接路由服务器，申请服务端口，等待浏览器传递的命令
 * 
 * @author yanchangyou
 *
 */
public class P2PInnerServer {

	static Logger log = Logger.getAnonymousLogger();

	RouterServerAddress address;
	Socket routerServerSocket;
	BufferedWriter writer;
	BufferedReader reader;

	public P2PInnerServer(RouterServerAddress address) throws Exception {

		this.address = address;
	}

	public void register() throws IOException {

		routerServerSocket = P2PUtil.generateRouterSocket(address);

		reader = P2PUtil.convertToReader(routerServerSocket.getInputStream());
		writer = P2PUtil.convertToWriter(routerServerSocket.getOutputStream());

		P2PUtil.write(writer, address.getServicePort());
		String registerInfo = P2PUtil.readContent(reader);
		if (P2PUtil.isP2PErrorMessage(registerInfo)) {
			log.info("注册失败：" + registerInfo);
			throw new RuntimeException("注册失败，系统退出!");
		} else {
			log.info(registerInfo);
		}
	}

	public void start() throws IOException {

		String command = null;
		while ((command = reader.readLine()) != null) {
			if (P2PUtil.isP2PErrorMessage(command)) {
				log.info(command);
				continue;
			}
			try {
				String result = P2PUtil.execute(command);
				P2PUtil.write(writer, result);
				P2PUtil.write(writer, P2PUtil.getP2PContentEndLine());
			} catch (Exception e) {
				e.printStackTrace();
				P2PUtil.write(writer, P2PUtil.packageErrorMessage(e.getMessage()));
				P2PUtil.write(writer, P2PUtil.getP2PContentEndLine());
			}
		}

	}

	public static void main(String[] args) throws Exception {

		if (args == null || args.length == 0) {
			args = new String[] { "127.0.0.1", "8888", "18888" };
		}

		RouterServerAddress address = P2PUtil.parsingRouterServerAddress(args);

		log.info("地址：" + address.toString());

		P2PInnerServer server = new P2PInnerServer(address);

		log.info("注册");

		server.register();

		log.info("启动");

		server.start();

	}
}
