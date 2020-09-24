package one.wangwei.blockchain.pb.protocols.session;

import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;

/**
 * Message sent to request the session to start.
 * @see {@link one.wangwei.blockchain.pb.protocols.session.SessionProtocol}
 * @author aaron
 *
 */
public class SessionStartRequest extends Message {
	static final public String name = "SessionStartRequest";
	
	/**
	 * Initialiser when given message parameters explicitly. Note that
	 * in this message there are no additional parameters.
	 */
	public SessionStartRequest() {
		super(name,SessionProtocol.protocolName,Message.Type.Request);
	}
	
	/**
	 * Initialiser when given message parameters in a doc. Must throw
	 * InvalidMessag if any of the required parameters are not
	 * in the doc, including the appropriate msg parameter.
	 * @param doc with the message details
	 * @throws InvalidMessage when the doc does not contain all of the required parameters
	 */
	public SessionStartRequest(Document doc) throws InvalidMessage {
		super(name,SessionProtocol.protocolName,Message.Type.Request,doc); // really just testing the name, otherwise nothing more to test
		this.doc=doc;
	}
}
