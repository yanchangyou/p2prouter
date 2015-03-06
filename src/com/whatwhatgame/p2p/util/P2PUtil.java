package com.whatwhatgame.p2p.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.whatwhatgame.p2p.bean.RouterServerAddress;

public class P2PUtil {

	static String OS_CHARSET = System.getProperty("file.encoding");

	public static Socket generateRouterSocket(RouterServerAddress address) throws IOException {
		return new Socket(address.getHost(), address.getPort());
	}

	public static BufferedWriter convertToWriter(OutputStream out) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(out));
	}

	public static BufferedReader convertToReader(InputStream in) throws IOException {
		return new BufferedReader(new InputStreamReader(in));
	}

	public static void write(OutputStream out, String content) throws IOException {
		BufferedWriter writer = convertToWriter(out);
		write(writer, content);
	}

	public static void write(BufferedWriter writer, int content) throws IOException {
		write(writer, "" + content);
	}

	public static void write(BufferedWriter writer, String content) throws IOException {
		writer.write(content);
		if (content != null) {
			writer.write("\r\n");
		}
		writer.flush();
	}

	static String ERROR_MESSAGE = "参数错误：至少输入3个参数【host，port，serverPort】";

	public static RouterServerAddress parsingRouterServerAddress(String[] args) {

		if (args.length < 3) {
			throw new RuntimeException(ERROR_MESSAGE);
		}
		RouterServerAddress RouterServerAddress = new RouterServerAddress();
		try {

			RouterServerAddress.setHost(args[0]);
			RouterServerAddress.setPort(Integer.parseInt(args[1]));
			RouterServerAddress.setServicePort(Integer.parseInt(args[2]));

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(ERROR_MESSAGE);
		}
		return RouterServerAddress;
	}

	public static String execute(String command) throws Exception {

		File file = new File(".");
		String path = file.getAbsolutePath();
		System.out.println("command : " + path.replace(".", "") + command);
		Runtime runtime = Runtime.getRuntime();
		
		//安全性考虑
		command = converToSecurityCommand(command);
		
		Process process = runtime.exec("command" + File.separatorChar + command);

		BufferedReader read = convertToReader(process.getInputStream());

		return readContent(read);
	}

	/**
	 * 不能以 / 开头<br>
	 * 不能有.. 父路径，只能在本目录执行<br>
	 * 
	 * @param command
	 * @return
	 */
	public static String converToSecurityCommand(String command) {
		
		if (command.startsWith("/") || command.startsWith("\\")) {
			command = command.substring(1);
		}
		command = command.replaceAll("\\.\\.", "");

		return command;
	}

	public static String readContent(BufferedReader read) throws IOException {
		String line = null;
		StringBuffer buf = new StringBuffer();
		while (!isP2PContentEndLine(line = read.readLine())) {
			buf.append(new String(line.getBytes(), P2PUtil.OS_CHARSET)).append("\r\n");
		}

		return buf.toString();
	}
	
	public static String covertContentByCharset(String content) throws IOException {
		return new String(content.getBytes("gbk"));
	}

	/**
	 * 内容传输的结束标志:是否结束
	 * 
	 * @param line
	 * @return
	 */
	public static boolean isP2PContentEndLine(String line) {
		return line == null || line.equals(getP2PContentEndLine());
	}

	/**
	 * 内容传输的结束标志
	 * 
	 * @return
	 */
	public static String getP2PContentEndLine() {
		return P2P_CONTENT_END_LINE;
	}

	public static String P2P_CONTENT_END_LINE = "P2P_CONTENT_END_LINE";

	public static String COMMAND_BEGIN = "?command=";

	public static String parsingCommand(String uri) {
		int indexOfCommand = uri.indexOf(COMMAND_BEGIN);
		if (indexOfCommand == -1) {
			return null;
		}
		return uri.substring(indexOfCommand + COMMAND_BEGIN.length());
	}
	
	public static String P2P_ERROR_MESSAGE_PREFIX = "P2P-ERROR:";
	
	public static String packageErrorMessage(String message) {
		return P2P_ERROR_MESSAGE_PREFIX + convertToOneLine(message);
	}
	
	public static String convertToOneLine(String content) {
		if(content == null) {
			return content;
		}
		return content.replace('\r', ';').replace('\n', ';');
	}
	
	public static boolean isP2PErrorMessage(String message) {
		return message.startsWith(P2P_ERROR_MESSAGE_PREFIX);
	}
	
}
