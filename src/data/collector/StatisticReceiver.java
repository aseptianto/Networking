package data.collector;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class StatisticReceiver extends Thread {

	private ServerSocket receiver;
	private Socket clientSocket;
	private DataInputStream in;
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
	
	public List<String> lockAndReadFile(File file){
		List<String> result = new ArrayList<String>();
		System.out.println("Read file without locking...");
		try{
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
	
	public String formatNameToFile(String generatorName){
		String result = generatorName;
		result = result.replace(":", "_");
		return result + ".txt";
	}
	
	public File getTXTFile(String generatorName){
		File curDir = new File(".");
		String requestedFile = formatNameToFile(generatorName);
		for(File file : curDir.listFiles()){
			if(file.isFile()){
                String fileName = file.getName();
                if(fileName.equals(requestedFile)) return file;
            }
		}
		return null;
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
				in = new DataInputStream(clientSocket.getInputStream());
				// retrieve generator name
				// input is 127.0.0.1:4221 -> 127.0.0.1_4221.txt
				String generatorName = in.readUTF();
				File file = getTXTFile(generatorName);
				if(file == null){
					out = new DataOutputStream(clientSocket.getOutputStream());
					System.out.println("Requested Generator not available -> " + generatorName);
					out.writeUTF("-1");
				}
				else{
					System.out.println("Requested " + file.getName());
					out = new DataOutputStream(clientSocket.getOutputStream());
					out.writeUTF("1");
					List<String> strings = lockAndReadFile(file);
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
				}
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
