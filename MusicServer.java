package BeatBox;

import java.io.*;
import java.net.*;
import java.util.*;

public class MusicServer {
	ArrayList<ObjectOutputStream> clientOutputStreams;

	public static void main(String[] args) {
		new MusicServer().go();
	}

	public class ClientHandler implements Runnable {

		ObjectInputStream in;
		Socket clientSocket;

		public ClientHandler(Socket socket) {
			try {
				this.clientSocket = socket;
				this.in = new ObjectInputStream(this.clientSocket.getInputStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			Object o1 = null;
			Object o2 = null;

			try {
				while((o1 = in.readObject()) != null){
					
					o2 = in.readObject();
					
					System.out.println("read two objects");
					tellEveryone(o1,o2);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void go() {
		this.clientOutputStreams = new ArrayList<ObjectOutputStream>();

		try {
			ServerSocket serverSock = new ServerSocket(4242);
			System.out.println("Server Started");

			while (true) {
				Socket clientSocket = serverSock.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				this.clientOutputStreams.add(out);

				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();

				System.out.println("got a connection");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void tellEveryone(Object one, Object two) {

		Iterator<ObjectOutputStream> it = this.clientOutputStreams.iterator();

		while (it.hasNext()) {
			try {
				ObjectOutputStream out = it.next();
				out.writeObject(one);
				out.writeObject(two);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
