package my.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketServer {
	private ServerSocket serverSocket;
	
	private int port;
	
	private BufferedWriter writer;
	
	private String file_name;
	
	public SocketServer(String file_name) {
		this.file_name = file_name;
	}
	
	private void acceptConnection() {
		try {
			int iConnections = 1;
			writer = new BufferedWriter(new FileWriter(file_name));
			port = 9876;
			serverSocket = new ServerSocket(port);
			System.out.println("Server Connection Open. Waiting for Client to be connected");
			
			while (iConnections < 3) {
				Thread t = new Thread(new My_Thread(writer,serverSocket));
				t.start();
				iConnections++;
			}
			
		}catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String file_name = args[0];
		SocketServer client = new SocketServer(file_name);
		client.acceptConnection();
	}
	
	class My_Thread implements Runnable {
		private BufferedReader reader;
		private BufferedWriter writer;
		private ServerSocket serverSocket;
		
		private Socket socket;
		
		public  My_Thread(BufferedWriter writer, ServerSocket serverSocket) {
			// TODO Auto-generated constructor stub
			this.writer = writer;
			this.serverSocket = serverSocket;
		}
		
		@Override
		public void run() {
			try {
				socket = serverSocket.accept();
				System.out.println("Client accepted");
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line = reader.readLine();
				while (line != null) {
					System.out.println("Message : " + line);
					//write the response to the server file
					writer.write(line);
					writer.newLine();
					writer.flush();
					line = reader.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
