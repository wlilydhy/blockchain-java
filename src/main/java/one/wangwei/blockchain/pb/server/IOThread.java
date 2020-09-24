package one.wangwei.blockchain.pb.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Listen for connections on a given port number and pass them to the
 * {@link one.wangwei.blockchain.pb.server.ServerManager} using
 * {@link one.wangwei.blockchain.pb.server.ServerManager#acceptClient(Socket)}. Note that the
 * {@link one.wangwei.blockchain.pb.server.ServerManager} is responsible for creating a thread for this
 * connection, else the IOThread will not accept any more connections until this
 * connection is finished.
 * 
 * @see {@link one.wangwei.blockchain.pb.server.ServerManager}
 * @author aaron
 *
 */
public class IOThread extends Thread {
	private static Logger log = Logger.getLogger(IOThread.class.getName());
	private ServerSocket serverSocket=null;
	private int port;
	private ServerManager serverManager;
	
	/**
	 * Initialise the IOThread with a port number to listen on and reference
	 * to the {@link one.wangwei.blockchain.pb.server.ServerManager}.
	 * @param port to listen on
	 * @param serverManager to send connections to
	 * @throws IOException whenever the server socket can't be created
	 */
	public IOThread(int port, ServerManager serverManager) throws IOException{
		serverSocket = new ServerSocket(port); // let's throw this since its potentially unrecoverable
		this.port=port;
		this.serverManager=serverManager;
		start();
	}
	
	/**
	 * Close the server socket and make sure the thread terminates.
	 */
	public void shutDown() {
		if(serverSocket!=null)
			try {
				serverSocket.close();
			} catch (IOException e) {
				log.warning("exception closing server socket: "+e.getMessage());
			}
		interrupt();
	}
	
	/**
	 * Listen for connections and pass them to the ServerManager.
	 */
	@Override
	public void run() {
		log.info("listening for connections on port "+port);
		while(!isInterrupted() && !serverSocket.isClosed()){
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				log.info("Received connection from "+clientSocket.getInetAddress());
				serverManager.acceptClient(clientSocket);
			} catch (IOException e) {
				log.warning("exception accepting connection: "+e.getMessage());
			} 
		}
		log.info("IOThread terminating");
		try {
			serverSocket.close();
		} catch (IOException e) {
			log.warning("exception closing server socket: "+e.getMessage());
		}
	}
}
