package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketServerSimple {
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	
	
	public void start(int port) {
		try {
			serverSocket = new ServerSocket(port);
			while(true) {
				clientSocket = serverSocket.accept();
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String greeting;
				while((greeting = in.readLine())!=null) {
					System.out.println(greeting);
					if("hello server".equals(greeting)) {
						out.println("hello client");
					} else {
						out.println("unrecognised greeting");
					}
				}
			}

			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			in.close();
			out.close();
			clientSocket.close();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SocketServerSimple server= new SocketServerSimple();
		server.start(8881);
	}
	

}