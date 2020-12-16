package one.wangwei.blockchain.pb.protocols.getIp;

import lombok.Getter;
import lombok.Setter;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.Inv.InvProtocol;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;


@Getter
@Setter
public class getIpReply extends Message{

    static final public String name = "getIpReply";
    String ipString;

    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public getIpReply() {
        super(name, getIpProtocol.protocolName, Type.Reply);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessag if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public getIpReply(Document doc) throws InvalidMessage {
        super(name, getIpProtocol.protocolName, Type.Reply, doc); // really just testing the name, otherwise nothing more to test
        this.doc = doc;
        this.ipString=(String)doc.get("ipString");

    }


}
