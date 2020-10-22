package one.wangwei.blockchain.pb.protocols.Inv;

import lombok.Getter;
import lombok.Setter;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.Version.VersionProtocol;


@Getter
@Setter
public class InvReply extends Message{

    static final public String name = "InvReply";
    private String ReplyType;

    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public InvReply() {
        super(name, InvProtocol.protocolName, Message.Type.Reply);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessag if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public InvReply(Document doc) throws InvalidMessage {
        super(name, InvProtocol.protocolName, Message.Type.Reply, doc); // really just testing the name, otherwise nothing more to test
        this.doc = doc;
        this.ReplyType = (String) doc.get("ReplyType");
    }


}
