package one.wangwei.blockchain.pb.protocols.SPV;

import lombok.Getter;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.Inv.InvProtocol;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;

@Getter
public class SPVRequest extends Message{

    static final public String name = "SPVRequest";

    private String Hash;


    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public SPVRequest() {
        super(name, SPVProtocol.protocolName, Type.Request);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessage if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public SPVRequest(Document doc) throws InvalidMessage {
        super(name,InvProtocol.protocolName, Type.Request,doc); // really just testing the name, otherwise nothing more to test
        this.doc=doc;
        this.Hash=(String)doc.get("Hash");

    }
}
