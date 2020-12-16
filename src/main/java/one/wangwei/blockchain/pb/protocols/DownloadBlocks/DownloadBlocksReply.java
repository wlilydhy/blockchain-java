package one.wangwei.blockchain.pb.protocols.DownloadBlocks;

import lombok.Getter;
import one.wangwei.blockchain.block.Block;
import one.wangwei.blockchain.pb.Strategy.DownloadBlocks;
import one.wangwei.blockchain.pb.Utils;
import one.wangwei.blockchain.pb.protocols.Document;
import one.wangwei.blockchain.pb.protocols.InvalidMessage;
import one.wangwei.blockchain.pb.protocols.Message;
import one.wangwei.blockchain.pb.protocols.getData.getDataProtocol;
import one.wangwei.blockchain.transaction.Transaction;
import org.apache.commons.codec.DecoderException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dhy
 */
@Getter
public class DownloadBlocksReply extends Message {
    static final public String name = "DownloadBlocksReply";
    private List<String> blockHashList = new ArrayList<>();

    /**
     * Initialiser when given message parameters explicitly. Note that
     * in this message there are no additional parameters.
     */
    public DownloadBlocksReply() {
        super(name, DownloadBlocksProtocol.protocolName, Type.Reply);
    }

    /**
     * Initialiser when given message parameters in a doc. Must throw
     * InvalidMessag if any of the required parameters are not
     * in the doc, including the appropriate msg parameter.
     * @param doc with the message details
     * @throws InvalidMessage when the doc does not contain all of the required parameters
     */
    public DownloadBlocksReply(Document doc) throws InvalidMessage, DecoderException {
        super(name, DownloadBlocksProtocol.protocolName, Type.Reply, doc); // really just testing the name, otherwise nothing more to test
        this.doc = doc;
        this.blockHashList = Utils.getInstance().stringToList((String)doc.get("blockHashes"));
    }


}
