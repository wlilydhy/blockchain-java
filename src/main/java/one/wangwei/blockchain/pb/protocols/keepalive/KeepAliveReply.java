package one.wangwei.blockchain.pb.protocols.keepalive;

import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;

/**
 * Reply message for the KeepAlive protocol.
 * @see {@link one.wangwei.blockchain.pb.protocols.keepalive.KeepAliveProtocol}
 * @author aaron
 *
 */
public class KeepAliveReply extends Message {
	static final public String name = "KeepAliveReply";
	
	/**
	 * Initialiser when given message parameters explicitly. Note that
	 * in this message there are no additional parameters.
	 */
	public KeepAliveReply() {
		super(name,KeepAliveProtocol.protocolName,Message.Type.Reply);
	}
	
	/**
	 * Initialiser when given message parameters in a doc. Must throw
	 * InvalidMessag if any of the required parameters are not
	 * in the doc, including the appropriate msg parameter.
	 * @param doc with the message details
	 * @throws InvalidMessage when the doc does not contain all of the required parameters
	 */
	public KeepAliveReply(Document doc) throws InvalidMessage {
		super(name,KeepAliveProtocol.protocolName,Message.Type.Reply,doc); // really just testing the name, otherwise nothing more to test
		this.doc=doc;
	}

}
