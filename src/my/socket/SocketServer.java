package my.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketServer {
	
	private ConcurrentHashMap<Integer, String> entries= new ConcurrentHashMap<>();
	
	private AtomicInteger counter = new AtomicInteger(1);
	private AtomicInteger iConnections = new AtomicInteger(1);
	
	private ServerSocket serverSocket;
	
	private int port;
	
	private String file_name;
	
	public SocketServer(String file_name) {
		this.file_name = file_name;
	}
	
	private void acceptConnection()   {
		try {
			port = 9876;
			serverSocket = new ServerSocket(port);
			System.out.println("Server Connection Open. Waiting for Client to be connected");
			
			while (iConnections.get() < 3) {
				Thread t = new Thread(new My_Thread(serverSocket));
				t.start();
				System.out.println(LocalDateTime.now());
				iConnections.getAndIncrement();
			}
			TimeUnit.SECONDS.sleep(9);
			Thread writer_thread = new Thread(new Writer_Thread(file_name));
			writer_thread.start();
			
		}catch (UnknownHostException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch(InterruptedException e) {
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
		private ServerSocket serverSocket;
		
		private Socket socket;
		
		public  My_Thread( ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}
		
		@Override
		public void run() {
			try {
				socket = serverSocket.accept();
				System.out.println("Client accepted");
				System.out.println(LocalDateTime.now());
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line = reader.readLine();
				while (line != null) {
					System.out.println("Message : " + line);
					//write the response to the server file
					String[] msg = line.split(":");
					entries.put(Integer.parseInt(msg[0].trim()), msg[1].trim());
					line = reader.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				iConnections.getAndDecrement();
				try {
					reader.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Writer_Thread implements Runnable {
		private String file_name;
		private BufferedWriter writer;
		public Writer_Thread (String file_name) {
			this.file_name = file_name;
		}
		
		@Override
		public void run() {
			try {
				writer = new BufferedWriter(new FileWriter(this.file_name));
				//This thread has to keep running until the client has finished publishing
				//until the entries hashmap is emptied
				while(iConnections.get() > 1 || !entries.isEmpty()) {
					int key = counter.get();
					if (entries.containsKey(key)) {
						writer.write(String.valueOf(key).concat(" : ").concat(entries.get(key)));
						writer.newLine();
						writer.flush();
						entries.entrySet().removeIf(e -> e.getKey().equals(key));
					}
					counter.getAndIncrement();
				}
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
