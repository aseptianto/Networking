package data.collector;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class StatisticReceiver extends Thread {

	private ServerSocket receiver;
	private Socket clientSocket;
	private DataOutputStream out;
	private boolean shutdown;

	public StatisticReceiver(int port) throws IOException {
		receiver = new ServerSocket(port);
		shutdown = false;
	}
	
	public void shutDown(){
		try {
			if(out != null) out.close();
			if(clientSocket != null) clientSocket.close();
			if(receiver != null) receiver.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		shutdown = true;
	}
	
	public List<String> lockAndReadFile(){
		List<String> result = new ArrayList<String>();
		System.out.println("Read file without locking...");
		try{
			File file = new File("storedWords.txt");
			FileInputStream in = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				out.append(line);
				result.add(out.toString());
				out.delete(0, out.length());
			}
			reader.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (!shutdown) {
			try {
				clientSocket = receiver.accept();
				System.out.println("Got request from "
						+ clientSocket.getInetAddress().getHostAddress() + ":"
						+ clientSocket.getPort());
				out = new DataOutputStream(clientSocket.getOutputStream());
				List<String> strings = lockAndReadFile();
//				System.out.print("Locking and reading file...");
//				File file = new File("storedWords.txt");
//				FileInputStream in = new FileInputStream(file);
//				RandomAccessFile randomAccess = new RandomAccessFile(file, "rw");
//				FileChannel channel = randomAccess.getChannel();
//				FileLock lock = channel.lock();
//				try {
//					lock = channel.tryLock();
//					try {
//						BufferedReader reader = new BufferedReader(
//								new InputStreamReader(in));
//						StringBuilder out = new StringBuilder();
//						String line;
//						while ((line = reader.readLine()) != null) {
//							out.append(line);
//							strings.add(out.toString());
//						}
//						reader.close();
//					} finally {
//						if( lock != null ) {
//				            lock.release();
//				        }
//						channel.close();
//					}
//				} finally {
//					in.close();
//					channel.close();
//				}
//				randomAccess.close();
				System.out.println("Done!");
				System.out.print("Sending to client...");
				// more processing time, but at least file is not locked too
				// long
				for (int i = 0; i < strings.size(); i++) {
					out.writeUTF(strings.get(i));
					out.flush();
				}
				// send end message "!{EOF}"
				out.writeUTF("!{EOF}");
				out.flush();
				System.out.println("Done!");
			} catch(SocketException e){
				System.out.println("Server Socket closed!");
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.print("Closing connection...");
			try {
				out.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Done!");
		}
	}

}
