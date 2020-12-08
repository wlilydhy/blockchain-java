package one.wangwei.blockchain.block;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.wangwei.blockchain.transaction.MerkleTree;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * @author Dhy
 */
public class BlockHead {
    private String hash;
    private String prevBlockHash;
    private long timeStamp;
    private long nonce;
    private byte[] merkleRootHash;

    public BlockHead(Block block){
        this.hash=block.getHash();
        this.prevBlockHash=block.getPrevBlockHash();
        this.timeStamp=block.getTimeStamp();
        this.nonce=block.getNonce();
        this.merkleRootHash=block.getMerkleTree().getRoot().getHash();
    }
}
