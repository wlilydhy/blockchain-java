package one.wangwei.blockchain.pb.protocols.Inv;

import lombok.Getter;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.Version.VersionProtocol;

@Getter
public class InvRequest extends Message{

    static final public String name = "InvRequest";
    private String RequestType;
    private String BlockHash;
    private byte[] TxHash;


    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public InvRequest() {
        super(name, InvProtocol.protocolName, Message.Type.Request);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessage if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public InvRequest(Document doc) throws InvalidMessage {
        super(name,InvProtocol.protocolName,Message.Type.Request,doc); // really just testing the name, otherwise nothing more to test
        this.doc=doc;
        this.RequestType=(String)doc.get("RequestType");
        if((doc.get("RequestType")).equals("Block")) {
            this.BlockHash = (String) doc.get("Hash");
        }
        else if((doc.get("RequestType")).equals("Block")){
            this.TxHash=(byte[])doc.get("Hash");
        }
    }
}
