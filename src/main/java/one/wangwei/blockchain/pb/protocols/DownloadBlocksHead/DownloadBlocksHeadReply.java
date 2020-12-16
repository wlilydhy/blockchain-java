package one.wangwei.blockchain.pb.protocols.DownloadBlocksHead;

import lombok.Getter;
import one.wangwei.blockchain.pb.Strategy.DownloadBlocksHead;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.DownloadBlocks.DownloadBlocksProtocol;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import org.apache.commons.codec.DecoderException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Dhy
 */
@Getter
public class DownloadBlocksHeadReply extends Message {
    static final public String name = "DownloadBlocksHeadReply";
    private Map<String,byte[]> blockHead;

    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public DownloadBlocksHeadReply() {
        super(name, DownloadBlocksHeadProtocol.protocolName, Type.Reply);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessage if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public DownloadBlocksHeadReply(Document doc) throws InvalidMessage, DecoderException {
        super(name, DownloadBlocksHeadProtocol.protocolName, Type.Reply, doc); // really just testing the name, otherwise nothing more to test
        this.doc = doc;
        this.blockHead = Utils.getInstance().stringToMap1((String)doc.get("blockHead"));
    }


}
