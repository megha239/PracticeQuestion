package my.socket;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {

	private Socket socket;
	private String host;
	
	private int port;
	private BufferedWriter writer;
	private BufferedReader reader;
	
	private String client_name;
	private String file_name;
	
	public SocketClient(String client_name, String file_name) {
		this.client_name = client_name;
		this.file_name = file_name;
	}
	
	private void initiateConnection() {
		try {
			this.host = InetAddress.getLocalHost().getHostName();
			this.port = 9876;
			this.socket = new Socket(host, port);
			
		} catch(UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void communicate() {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new FileReader(file_name));
			String line = reader.readLine();
			while (line != null) {
				System.out.println("Messge From" + client_name + ":" + line);
				writer.write(line);
				writer.newLine();
				writer.flush();
				line = reader.readLine();
			}
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
				reader.close();
				writer.close();
			} catch(IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public static void main(String[] args) {
		String client_name = args[0];
		String file_name = args[1];
		SocketClient client = new SocketClient(client_name,file_name);
		client.initiateConnection();
		client.communicate();
	}
}


