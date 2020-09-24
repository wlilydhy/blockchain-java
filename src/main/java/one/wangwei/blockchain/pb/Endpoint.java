package one.wangwei.blockchain.pb;

import one.wangwei.blockchain.pb.protocols.IRequestReplyProtocol;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.Protocol;
import one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol;
import one.wangwei.blockchain.pb.protocols.session.SessionProtocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The endpoint is a thread that blocking reads incoming messages (on a socket)
 * and sends them to the appropriate protocol for processing; thus a
 * thread-per-connection model is being used. It also provides a synchronized
 * method to send data to the socket which will be sent to the other endpoint.
 * Any number of protocols can be handled by the endpoint, but there can be only
 * one instance of each protocol running at a time.
 * 
 * @see {@link one.wangwei.blockchain.pb.Manager}
 * @see {@link one.wangwei.blockchain.pb.protocols.session.SessionProtocol}
 * @see {@link one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol}
 * @author aaron
 *
 */
public class Endpoint extends Thread {
	private static Logger log = Logger.getLogger(Endpoint.class.getName());
	
	/**
	 * The socket this endpoint is wrapped around.
	 */
	private Socket socket;
	
	/**
	 * The manager to report to when things happen.
	 */
	private Manager manager;
	
	/**
	 * The input data stream on the socket.
	 */
	private DataInputStream in=null;
	
	/**
	 * The output data stream on the socket.
	 */
	private DataOutputStream out=null;
	
	/**
	 * A protocol name to protocol map, of protocols in use.
	 */
	private Map<String,Protocol> protocols;
	
	/**
	 * Initialise the endpoint with a socket and a manager.
	 * @param socket
	 * @param manager
	 */
	public Endpoint(Socket socket, Manager manager) {
		this.socket = socket;
		this.manager = manager;
		protocols = new HashMap<>();
	}
	
	/**
	 * Send a Message on the socket for this endpoint. This is synchronized
	 * to avoid multiple concurrent messages overwriting each other on the socket.
	 * @param msg
	 * @return true if the message was sent, false otherwise
	 * @throws EndpointUnavailable if the endpoint is not yet ready 
	 * or if the endpoint is terminated
	 */
	public synchronized boolean send(Message msg) throws EndpointUnavailable {
		if(out==null) {
			throw new EndpointUnavailable();
		}
		try {
			log.info("sending "+msg.getName()+" for protocol "+msg.getProtocolName()+" to "+getOtherEndpointId());
			out.writeUTF(msg.toJsonString());
			out.flush();
		} catch (IOException e) {
			manager.endpointDisconnectedAbruptly(this);
			return false;
		}
		return true;
	}
	
	/**
	 * Closes the endpoint, which closes the socket
	 */
	public synchronized void close() {
		// make sure all of the protocols have stopped
		Set<String> protocolNames;
		synchronized(protocols) {
			protocolNames = new HashSet<String>(protocols.keySet());
		}
		if(protocolNames!=null)
			protocolNames.forEach((protocolName)->{stopProtocol(protocolName);});
		interrupt();
		try {
			if(out!=null) out.close();
			out=null;
		} catch (IOException e) {
			log.warning("connection did not close properly: "+e.getMessage());
		}
		try {
			socket.close();
		} catch (IOException e) {
			log.warning("socket did not close properly: "+e.getMessage());
		}
		manager.endpointClosed(this);
	}
	
	/**
	 * Continue to read messages from the socket until interrupted.
	 */
	@Override
	public void run() {
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e){
			manager.endpointDisconnectedAbruptly(this);
			return;
		}
		//发送了一条消息
		manager.endpointReady(this);
		log.info("endpoint has started to: "+getOtherEndpointId());
		while(!isInterrupted()) {
			try {
				String line=in.readUTF();
				System.out.println("msg is " + line);
				Message msg = Message.toMessage(line);
				Protocol protocol=null;
				synchronized(protocols) {
					protocol=protocols.get(msg.getProtocolName());
				}
				if(protocol==null) {
					switch(msg.getProtocolName()) {
					case SessionProtocol.protocolName:
						protocol=new SessionProtocol(this,manager);
						break;
					case KeepAliveProtocol.protocolName:
						protocol=new KeepAliveProtocol(this,manager);
					}
					if(!manager.protocolRequested(this,protocol)) {
						log.info("message dropped due to no protocol available: "+line);
						continue;
					}
				}
				log.info("received "+msg.getName()+" for protocol "+msg.getProtocolName()+" from "+getOtherEndpointId());
				switch(msg.getType()) {
				case Request:
					((IRequestReplyProtocol)protocol).receiveRequest(msg);
					break;
				case Reply:
					((IRequestReplyProtocol)protocol).receiveReply(msg);
					break;
				}
			} catch (IOException e) {
				manager.endpointDisconnectedAbruptly(this);
				// we can't continue here
				break;
			} catch (InvalidMessage e) {
				manager.endpointSentInvalidMessage(this);
				// up to the client what to do
			}catch (EndpointUnavailable e) {
				manager.endpointDisconnectedAbruptly(this);
				break;
			}
		}
		try {
			in.close();
		} catch (IOException e) {
			log.warning("connection did not close properly: "+e.getMessage());
		}
		log.info("endpoint has terminated to: "+getOtherEndpointId());
	}
	
	/**
	 * Start handling a protocol. Only one instance of a protocol can be handled
	 * at a time. Either client or server may start/initiate the use of the protocol.
	 * @see {@link one.wangwei.blockchain.pb.protocols.Protocol}
	 * @param protocol the protocol to handle
	 * @throws ProtocolAlreadyRunning if there is already an instance of this protocol
	 * running on this endpoint
	 */
	public void handleProtocol(Protocol protocol) throws ProtocolAlreadyRunning {
		synchronized(protocols) {
			if(protocols.containsKey(protocol.getProtocolName())){
				throw new ProtocolAlreadyRunning();
			} else {
				protocols.put(protocol.getProtocolName(),protocol);
				log.info("now handling protocol: "+protocol.getProtocolName());
			}
		}
	}
	
	/**
	 * Stop a protocol that is already being handled. It will be removed
	 * from the endpoints set of handled protocols.
	 * @see {@link one.wangwei.blockchain.pb.protocols.Protocol}
	 * @param protocolName the protocol name to stop
	 */
	public void stopProtocol(String protocolName) {
		synchronized(protocols) {
			if(!protocols.containsKey(protocolName)) {
				log.warning("no instance of protocol to stop: "+protocolName);
				return;
			}
			protocols.get(protocolName).stopProtocol();
			protocols.remove(protocolName);
		}
	}
	
	/**
	 * 
	 * @return the id of the other endpoint
	 */
	public String getOtherEndpointId() {
		return socket.getInetAddress().toString()+":"+socket.getPort();
	}
}
