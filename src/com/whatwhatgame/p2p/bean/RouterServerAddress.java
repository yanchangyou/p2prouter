package com.whatwhatgame.p2p.bean;

/**
 * p2p 路由器和服务器抽象信息定义<br>
 * 路由器ip，端口（连接注册的端口），以及对外映射服务的端口
 * 
 * @author yanchangyou
 *
 */
public class RouterServerAddress {

	String host;
	int port;
	int servicePort;

	public RouterServerAddress() {
	}

	public RouterServerAddress(String host, int port, int servicePort) {
		this.host = host;
		this.port = port;
		this.servicePort = servicePort;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getServicePort() {
		return servicePort;
	}

	public void setServicePort(int servicePort) {
		this.servicePort = servicePort;
	}

	@Override
	public String toString() {
		return host + ":" + port + "(" + host + ":" + servicePort + ")";
	}
}
