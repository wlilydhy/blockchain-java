package one.wangwei.blockchain.pb.protocols.getIp;

import lombok.Getter;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.Inv.InvProtocol;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;

@Getter
public class getIpRequest extends Message{

    static final public String name = "getIpRequest";

    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public getIpRequest() {
        super(name, InvProtocol.protocolName, Type.Request);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessage if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public getIpRequest(Document doc) throws InvalidMessage {
        super(name,InvProtocol.protocolName, Type.Request,doc); // really just testing the name, otherwise nothing more to test
        this.doc=doc;
    }
}
