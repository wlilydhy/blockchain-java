package one.wangwei.blockchain.pb.protocols.DownloadBlocksHead;

import lombok.Getter;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksProtocol;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;

/**
 * @author Dhy
 */
@Getter
public class DownloadBlocksHeadRequest extends Message {
    static final public String name = "DownloadBlocksHeadRequest";

    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public DownloadBlocksHeadRequest() {
        super(name, DownloadBlocksProtocol.protocolName, Type.Request);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessage if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public DownloadBlocksHeadRequest(Document doc) throws InvalidMessage {
        super(name,getDataProtocol.protocolName, Type.Request,doc); // really just testing the name, otherwise nothing more to test
        this.doc=doc;
    }
}
