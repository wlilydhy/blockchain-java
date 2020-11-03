package one.wangwei.blockchain.pb.server;

import lombok.SneakyThrows;
import one.wangwei.blockchain.pb.*;
import one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol;
import one.wangwei.blockchain.pb.protocols.Protocol;
import one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Manages all of the clients for the server and the server's state.
 * 
 * @see {@link one.wangwei.blockchain.pb.Manager}
 * @see {@link one.wangwei.blockchain.pb.server.IOThread}
 * @see {@link one.wangwei.blockchain.pb.Endpoint}
 * @see {@link one.wangwei.blockchain.pb.protocols.Protocol}
 * @see {@link one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol}
 * @author aaron
 *
 */
public class ServerManager extends Manager {
	private static Logger log = Logger.getLogger(ServerManager.class.getName());
	private IOThread ioThread;
	private Integer numLiveClients=0;
	
	/**
	 * Initialise the ServerManager with a port number for the io thread to listen on.
	 * @param port to use when creating the io thread
	 * @throws IOException whenever the exception is deemed unrecoverable
	 */
	public ServerManager(int port) throws IOException {
//		log.info("initializing");
//		// when the IO thread terminates, and all endpoints have terminated,
//		// then the server will terminate
//		ioThread = new IOThread(port,this);
//		try {
//			// just wait for this thread to terminate
//			ioThread.join();
//		} catch (InterruptedException e) {
//			// just make sure the ioThread is going to terminate
//			ioThread.shutDown();
//		}
//		// At this point, there still may be some endpoints that have not
//		// terminated, and so the JVM will remain running until they do.
//		// However no new endpoints can be created.
//
//		// let's wait for the remaining clients if we can
//		while(numLiveClients>0) {
//			log.warning("still waiting for "+numLiveClients+" to finish");
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				if(numLiveClients>0) {
//					log.severe("terminating server with "+numLiveClients+
//							" still unfinished");
//				}
//				System.exit(-1);
//			}
//		}
//
//		// there are no live clients, so let's clean up
//		Utils.getInstance().cleanUp();
//		log.info("server terminated cleanly");
	}

	@SneakyThrows
	@Override
	public void run(){
		log.info("initializing");
		// when the IO thread terminates, and all endpoints have terminated,
		// then the server will terminate
		ioThread = new IOThread(9999,this);
		try {
			// just wait for this thread to terminate
			ioThread.join();
		} catch (InterruptedException e) {
			// just make sure the ioThread is going to terminate
			ioThread.shutDown();
		}
		// At this point, there still may be some endpoints that have not
		// terminated, and so the JVM will remain running until they do.
		// However no new endpoints can be created.

		// let's wait for the remaining clients if we can
		while(numLiveClients>0) {
			log.warning("still waiting for "+numLiveClients+" to finish");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				if(numLiveClients>0) {
					log.severe("terminating server with "+numLiveClients+
							" still unfinished");
				}
				System.exit(-1);
			}
		}

		// there are no live clients, so let's clean up
		Utils.getInstance().cleanUp();
		log.info("server terminated cleanly");
	}
	/**
	 * A new client has connected to the server. We need to keep
	 * a set of all clients that have connected, so that we can
	 * do global operations, like broadcast data to all clients.
	 * @param clientSocket the socket connection for the client.
	 */
	public void acceptClient(Socket clientSocket) {
		Endpoint endpoint = new Endpoint(clientSocket,this);
		endpoint.start();
	}
	
	/**
	 * Called by a client endpoint to signal that it is now ready for
	 * use, the server can send data and it may start receiving messages
	 * from the client, etc. The server will now start the KeepAlive protocol
	 * so as to detect clients that are dead. The server will wait for the
	 * client to start the session protocol, or else terminate the connection
	 * if it does not stay alive.
	 * @param endpoint
	 */
	@Override
	public void
	endpointReady(Endpoint endpoint) {
		KeepAliveProtocol protocol = new KeepAliveProtocol(endpoint,this);
		try {
			// we need to add it to the endpoint before starting it
			endpoint.handleProtocol(protocol);
			protocol.startAsServer();
		} catch (ProtocolAlreadyRunning e) {
			// hmmm... already requested by the client
		}
		synchronized(numLiveClients) {
			numLiveClients++;
		}
	}
	
	/**
	 * The endpoint close() method has been called and completed.
	 * @param endpoint
	 */
	@Override
	public void endpointClosed(Endpoint endpoint) {
		synchronized(numLiveClients) {
			numLiveClients--;
		}
	}

	/**
	 * The session has started for this client endpoint. Other protocols
	 * may now be started, etc.
	 * @param endpoint
	 */
	@Override
	public void sessionStarted(Endpoint endpoint) {
		log.info("session has started with client: "+endpoint.getOtherEndpointId());
		
		// we can now engage with higher level protocols
		
	}

	/**
	 * The session has been stopped (usually by the client). The session should
	 * be last protocol to stop, other than the KeepAlive protocol. Server should now
	 * clean up any data relating to the client and any remaining protocols
	 * should be stopped, e.g. KeepAlive.
	 * @param endpoint
	 */
	@Override
	public void sessionStopped(Endpoint endpoint) {
		log.info("session has stopped with client: "+endpoint.getOtherEndpointId());
		
		
		// we can now signal the client endpoint to close and forget this client
		endpoint.close(); // will stop all remaining protocols
	}
	
	/**
	 * The endpoint has requested a protocol to start. If the protocol
	 * is allowed then the manager should tell the endpoint to handle it
	 * using {@link one.wangwei.blockchain.pb.Endpoint#handleProtocol(Protocol)}
	 * before returning true.
	 * @param protocol
	 * @return true if the protocol was started, false if not (not allowed to run)
	 */
	@Override
	public boolean protocolRequested(Endpoint endpoint, Protocol protocol) {
		// the only protocols in this system are this kind...
		try {
			((IRequestReplyProtocol)protocol).startAsServer();
			endpoint.handleProtocol(protocol);
			return true;
		} catch (EndpointUnavailable e) {
			// very weird...
			log.severe("endpoint unavailable "+endpoint.getOtherEndpointId());
			return false;
		} catch (ProtocolAlreadyRunning e) {
			// even more weird...
			return true;
		}
		
	}

	
	/*
	 * Everything below here is handling error conditions that could
	 * arise with the client connection. Typically on error we terminate
	 * connection with the client, and we may need to clean up other stuff
	 * as well.
	 */

	/**
	 * The client has violated one of the protocols. Usual practice is to
	 * terminate the client connection.
	 * @param endpoint
	 * @param protocol
	 */
	@Override
	public void protocolViolation(Endpoint endpoint, Protocol protocol) {
		log.severe("client "+endpoint.getOtherEndpointId()+" violated the protocol "+protocol.getProtocolName());
		endpoint.close();
	}
	
	/**
	 * The client connection died without warning. 
	 * Server needs to clean up client data and possibly recover
	 * from any faults that may occur due to this.
	 * @param endpoint
	 */
	@Override
	public void endpointDisconnectedAbruptly(Endpoint endpoint) {
		log.severe("client disconnected abruptly "+endpoint.getOtherEndpointId());
		endpoint.close();
	}
	
	/**
	 * The client sent a message that is invalid. Usual practice is to 
	 * terminate the client connection.
	 * @param endpoint
	 */
	@Override
	public void endpointSentInvalidMessage(Endpoint endpoint) {
		log.severe("client sent an invalid message "+endpoint.getOtherEndpointId());
		endpoint.close();
	}

	/**
	 * The client has timed out.
	 * Usual practice is to terminate the client connection.
	 * @param endpoint
	 * @param protocol
	 */
	@Override
	public void endpointTimedOut(Endpoint endpoint, Protocol protocol) {
		log.severe("client "+endpoint.getOtherEndpointId()+" has timed out on protocol "+protocol.getProtocolName());
		endpoint.close();
	}

	@Override
	public void VersionStarted(){

	};
}
