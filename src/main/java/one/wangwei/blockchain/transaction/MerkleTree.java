package one.wangwei.blockchain.transaction;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.wangwei.blockchain.util.ByteUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 默克尔树
 *
 * @author wangwei
 * @date 2018/04/15
 */
@Data
@NoArgsConstructor
public class MerkleTree {

    /**
     * 根节点
     */
    private Node root;
    /**
     * 叶子节点Hash
     */
    private byte[][] leafHashes;

    public MerkleTree(byte[][] leafHashes) {
        constructTree(leafHashes);
    }


    /**
     * 从底部叶子节点开始往上构建整个Merkle Tree
     *
     * @param leafHashes
     */
    private void constructTree(byte[][] leafHashes) {
        System.out.println(leafHashes.length);
        if (leafHashes == null || leafHashes.length < 1) {
            throw new RuntimeException("ERROR:Fail to construct merkle tree ! leafHashes data invalid ! ");
        }
        this.leafHashes = leafHashes;
        List<Node> parents = bottomLevel(leafHashes);
        while (parents.size() > 1) {
            parents = internalLevel(parents);
        }
        root = parents.get(0);
    }

    /**
     * 构建一个层级节点
     *
     * @param children
     * @return
     */
    private List<Node> internalLevel(List<Node> children) {
        List<Node> parents = Lists.newArrayListWithCapacity(children.size() / 2);
        for (int i = 0; i < children.size() - 1; i += 2) {
            Node child1 = children.get(i);
            Node child2 = children.get(i + 1);

            Node parent = constructInternalNode(child1, child2);
            parents.add(parent);
        }

        // 内部节点奇数个，只对left节点进行计算
        if (children.size() % 2 != 0) {
            Node child = children.get(children.size() - 1);
            Node parent = constructInternalNode(child, null);
            parents.add(parent);
        }

        return parents;
    }

    /**
     * 底部节点构建
     *
     * @param hashes
     * @return
     */
    private List<Node> bottomLevel(byte[][] hashes) {
        List<Node> parents = Lists.newArrayListWithCapacity(hashes.length / 2);

        for (int i = 0; i < hashes.length - 1; i += 2) {
            Node leaf1 = constructLeafNode(hashes[i]);
            Node leaf2 = constructLeafNode(hashes[i + 1]);

            Node parent = constructInternalNode(leaf1, leaf2);
            parents.add(parent);
        }

        if (hashes.length % 2 != 0) {
            Node leaf = constructLeafNode(hashes[hashes.length - 1]);
            // 奇数个节点的情况，复制最后一个节点
            Node parent = constructInternalNode(leaf, leaf);
            parents.add(parent);
        }

        return parents;
    }

    /**
     * 构建叶子节点
     *
     * @param hash
     * @return
     */
    private static Node constructLeafNode(byte[] hash) {
        Node leaf = new Node();
        leaf.hash = hash;
        return leaf;
    }

    /**
     * 构建内部节点
     *
     * @param leftChild
     * @param rightChild
     * @return
     */
    private Node constructInternalNode(Node leftChild, Node rightChild) {
        Node parent = new Node();
        if (rightChild == null) {
            parent.hash = leftChild.hash;
        } else {
            parent.hash = internalHash(leftChild.hash, rightChild.hash);
        }
        parent.left = leftChild;
        parent.right = rightChild;
        return parent;
    }

    /**
     * 计算内部节点Hash
     *
     * @param leftChildHash
     * @param rightChildHash
     * @return
     */
    private byte[] internalHash(byte[] leftChildHash, byte[] rightChildHash) {
        byte[] mergedBytes = ByteUtils.merge(leftChildHash, rightChildHash);
        return DigestUtils.sha256(mergedBytes);
    }


    /**
     * Merkle Tree节点
     */
    @Data
    public static class Node {
        private byte[] hash;
        private Node left;
        private Node right;
    }

    public void print(){
        List<Node> nodes = new ArrayList<>();
        if(this.getRoot()!=null){
            nodes.add(this.root);
        }
        while(!nodes.isEmpty()){
            Node current = nodes.get(0);
            nodes.remove(0);
            System.out.println(current.hash.toString());
            if(current.getLeft()!=null){
                nodes.add(current.getLeft());
            }
            if(current.getRight()!=null){
                nodes.add(current.getRight());
            }
        }
    }


    public List<Node> MerkleProof(byte[] TxHash) {
        int index = -1;
        int length = this.getLeafHashes().length-1;
        for (int i = 0; i < length; i++) {
            if (byteEqual(this.getLeafHashes()[i], TxHash)) {
                index = i;
                //System.out.println("index="+index);
                break;
            }
        }
        if (index == -1) {
            return null;
        }
        List<Node> result = new ArrayList<>();
        result.add(this.getRoot());
        Node current =this.getRoot();
        int mid = length / 2;
        //System.out.println("mid="+mid);
        while (true) {
            if(isLeaf(current)){
                result.add(current.getLeft());
                result.add(current.getRight());
                break;
            }
            //右子树
            if (index > mid) {
                mid = (length+mid)/2;
                //System.out.println("mid="+mid);
                result.add(current.getLeft());
                result.add(current.getRight());

                current=current.getRight();
            }
            //左子树
            else if (index <= mid) {
                mid=mid/2;
                //System.out.println("mid="+mid);
                result.add(current.getLeft());
                result.add(current.getRight());
                current=current.getLeft();
            }
        }
        return result;
    }

    private static boolean isLeaf(Node node){
        if(node.getLeft().getLeft()==null&&node.getRight().getRight()==null){
            return true;
        }
        return false;
    }

    private static boolean byteEqual(byte[] a ,byte[] b) {
        if (a.length == b.length) {
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static int abs(int a,int b){
        int k=a-b;
        if(k>0){
            return k;
        }
        else if(k<=0){
            return -k;
        }
        return 0;
    }
}
