
package org.ivy.settlement.ethereum.trie;

import org.ivy.settlement.infrastructure.bytes.ByteUtil;
import org.ivy.settlement.infrastructure.bytes.FastByteComparisons;
import org.ivy.settlement.infrastructure.crypto.HashUtil;
import org.ivy.settlement.infrastructure.rlp.RLP;
import org.ivy.settlement.infrastructure.string.Numeric;
import org.ivy.settlement.ethereum.model.EthReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.pqc.math.linearalgebra.ByteUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.apache.commons.lang3.concurrent.ConcurrentUtils.constantFuture;
import static org.ivy.settlement.infrastructure.bytes.ByteUtil.EMPTY_BYTE_ARRAY;
import static org.ivy.settlement.infrastructure.bytes.ByteUtil.toHexString;
import static org.ivy.settlement.infrastructure.crypto.HashUtil.EMPTY_TRIE_HASH;
import static org.ivy.settlement.infrastructure.rlp.RLP.*;

/**
 * Created by Anton Nashatyrev on 07.02.2017.
 */
public class Trie {

    private final static Object NULL_NODE = new Object();

    private static final Logger logger = LoggerFactory.getLogger("state");

    public enum NodeType {
        BranchNode,
        KVNodeValue,
        KVNodeNode
    }

    public final class Node {

        private byte[] hash = null;

        private byte[] rlp = null;

        private LList parsedRlp = null;

        private boolean dirty = false;

        private Object[] children = null;

        // new empty BranchNode
        public Node() {
            children = new Object[17];
            dirty = true;
        }

        // new KVNode with key and (value or node)
        public Node(TrieKey key, Object valueOrNode) {
            this(new Object[]{key, valueOrNode});
            dirty = true;
        }

        // new Node with hash or RLP
        public Node(byte[] hashOrRlp) {
            if (hashOrRlp.length == 32) {
                this.hash = hashOrRlp;
            } else {
                this.rlp = hashOrRlp;
            }
        }

        private Node(LList parsedRlp) {
            this.parsedRlp = parsedRlp;
            this.rlp = parsedRlp.getEncoded();
        }

        private Node(Object[] children) {
            this.children = children;
        }

        public boolean resolveCheck() {
            if (rlp != null || parsedRlp != null || hash == null) return true;
            rlp = getHash(hash);
            return rlp != null;
        }

        private void resolve() {
            if (!resolveCheck()) {
                logger.error("Invalid Trie state, can't resolve hash " + toHexString(hash));
                throw new RuntimeException("Invalid Trie state, can't resolve hash " + toHexString(hash));
            }
        }

        public byte[] encode() {
            return encode(1, true);
        }

        private byte[] encode(final int depth, boolean forceHash) {
            if (!dirty) {
                return hash != null ? encodeElement(hash) : rlp;
            } else {
                var type = getType();
                byte[] ret;
                if (type == NodeType.BranchNode) {
                    if (depth == 1 && async) {
                        // parallelize encode() on the first trie level only and if there are at least
                        // MIN_BRANCHES_CONCURRENTLY branches are modified
                        final var encoded = new Object[17];
                        //int encodeCnt = 0;
                        for (int i = 0; i < 16; i++) {
                            final var child = branchNodeGetChild(i);
                            if (child == null) {
                                encoded[i] = EMPTY_ELEMENT_RLP;
                            } else if (!child.dirty) {
                                encoded[i] = child.encode(depth + 1, false);
                            } else {
                                //encodeCnt++;
                            }
                        }
                        for (var i = 0; i < 16; i++) {
                            if (encoded[i] == null) {
                                final var child = branchNodeGetChild(i);
                                encoded[i] = child.encode(depth + 1, false);
                            }
                        }
                        var value = branchNodeGetValue();
                        encoded[16] = constantFuture(encodeElement(value));
                        try {
                            ret = encodeRlpListFutures(encoded);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        var encoded = new byte[17][];
                        for (var i = 0; i < 16; i++) {
                            var child = branchNodeGetChild(i);
                            encoded[i] = child == null ? EMPTY_ELEMENT_RLP : child.encode(depth + 1, false);
                        }
                        var value = branchNodeGetValue();
                        encoded[16] = encodeElement(value);
                        ret = encodeList(encoded);
                    }
                } else if (type == NodeType.KVNodeNode) {
                    ret = encodeList(encodeElement(kvNodeGetKey().toPacked()), kvNodeGetChildNode().encode(depth + 1, false));
                } else {
                    var value = kvNodeGetValue();
                    ret = encodeList(encodeElement(kvNodeGetKey().toPacked()),
                            encodeElement(value == null ? EMPTY_BYTE_ARRAY : value));
                }
                if (hash != null) {
                    deleteHash(hash);
                }
                dirty = false;
                if (ret.length < 32 && !forceHash) {
                    rlp = ret;
                    return ret;
                } else {
                    hash = HashUtil.sha3(ret);
                    addHash(hash, ret);
                    return encodeElement(hash);
                }
            }
        }

        @SafeVarargs
        private final byte[] encodeRlpListFutures(Object... list) throws ExecutionException, InterruptedException {
            var vals = new byte[list.length][];
            for (var i = 0; i < list.length; i++) {
                if (list[i] instanceof Future) {
                    vals[i] = ((Future<byte[]>) list[i]).get();
                } else {
                    vals[i] = (byte[]) list[i];
                }
            }
            return encodeList(vals);
        }

        private void parse() {
            if (children != null) return;
            resolve();
            var list = parsedRlp == null ? RLP.decodeLazyList(rlp) : parsedRlp;
            if (list.size() == 2) {
                children = new Object[2];
                var key = TrieKey.fromPacked(list.getBytes(0));
                children[0] = key;
                if (key.isTerminal()) {
                    children[1] = list.getBytes(1);
                } else {
                    children[1] = list.isList(1) ? new Node(list.getList(1)) : new Node(list.getBytes(1));
                }
            } else {
                children = new Object[17];
                parsedRlp = list;
            }
        }

        public Node branchNodeGetChild(int hex) {
            parse();
            assert getType() == NodeType.BranchNode;
            var n = children[hex];
            if (n == null && parsedRlp != null) {
                if (parsedRlp.isList(hex)) {
                    n = new Node(parsedRlp.getList(hex));
                } else {
                    var bytes = parsedRlp.getBytes(hex);
                    if (bytes.length == 0) {
                        n = NULL_NODE;
                    } else {
                        n = new Node(bytes);
                    }
                }
                children[hex] = n;
            }
            return n == NULL_NODE ? null : (Node) n;
        }

        public Node branchNodeSetChild(int hex, Node node) {
            parse();
            assert getType() == NodeType.BranchNode;
            children[hex] = node == null ? NULL_NODE : node;
            dirty = true;
            return this;
        }

        public byte[] branchNodeGetValue() {
            parse();
            assert getType() == NodeType.BranchNode;
            var n = children[16];
            if (n == null && parsedRlp != null) {
                byte[] bytes = parsedRlp.getBytes(16);
                if (bytes.length == 0) {
                    n = NULL_NODE;
                } else {
                    n = bytes;
                }
                children[16] = n;
            }
            return n == NULL_NODE ? null : (byte[]) n;
        }

        public Node branchNodeSetValue(byte[] val) {
            parse();
            assert getType() == NodeType.BranchNode;
            children[16] = val == null ? NULL_NODE : val;
            dirty = true;
            return this;
        }

        public int branchNodeCompactIdx() {
            parse();
            assert getType() == NodeType.BranchNode;
            var cnt = 0;
            var idx = -1;
            for (var i = 0; i < 16; i++) {
                if (branchNodeGetChild(i) != null) {
                    cnt++;
                    idx = i;
                    if (cnt > 1) return -1;
                }
            }
            return cnt > 0 ? idx : (branchNodeGetValue() == null ? -1 : 16);
        }

        public TrieKey kvNodeGetKey() {
            parse();
            assert getType() != NodeType.BranchNode;
            return (TrieKey) children[0];
        }

        public Node kvNodeGetChildNode() {
            parse();
            assert getType() == NodeType.KVNodeNode;
            return (Node) children[1];
        }

        public byte[] kvNodeGetValue() {
            parse();
            assert getType() == NodeType.KVNodeValue;
            return (byte[]) children[1];
        }

        public Node kvNodeSetValue(byte[] value) {
            parse();
            assert getType() == NodeType.KVNodeValue;
            children[1] = value;
            dirty = true;
            return this;
        }

        public Object kvNodeGetValueOrNode() {
            parse();
            assert getType() != NodeType.BranchNode;
            return children[1];
        }

        public Node kvNodeSetValueOrNode(Object valueOrNode) {
            parse();
            assert getType() != NodeType.BranchNode;
            children[1] = valueOrNode;
            dirty = true;
            return this;
        }

        public NodeType getType() {
            parse();
            return children.length == 17 ? NodeType.BranchNode :
                    (children[1] instanceof Node ? NodeType.KVNodeNode : NodeType.KVNodeValue);
        }

        public void dispose() {
            if (hash != null) {
                deleteHash(hash);
            }
        }

        public Node invalidate() {
            dirty = true;
            return this;
        }

        @Override
        public String toString() {
            return getType() + (dirty ? " *" : "") + (hash == null ? "" : "(hash: " + toHexString(hash) + " )");
        }
    }

    private HashMapDBSimple<byte[]> cache;

    private Node root;

    private boolean async = true;

    private Trie() {
        this(null);
    }

    private Trie(byte[] root) {
        this(new HashMapDBSimple<byte[]>(), root);
    }

    private Trie(HashMapDBSimple<byte[]> cache, byte[] root) {
        this.cache = cache;
        setRoot(root);
    }

    private void encode() {
        if (root != null) {
            root.encode();
        }
    }

    public void setRoot(byte[] root) {
        if (root != null && !FastByteComparisons.equal(root, EMPTY_TRIE_HASH)) {
            this.root = new Node(root);
        } else {
            this.root = null;
        }
    }

    private boolean hasRoot() {
        return root != null && root.resolveCheck();
    }

    private byte[] getHash(byte[] hash) {
        return cache.get(hash);
    }

    private void addHash(byte[] hash, byte[] ret) {
        cache.put(hash, ret);
    }

    private void deleteHash(byte[] hash) {
        cache.delete(hash);
    }

    public byte[] get(byte[] key) {
        if (!hasRoot()) return null; // treating unknown root hash as empty trie
        TrieKey k = TrieKey.fromNormal(key);
        return get(root, k);
    }

    private byte[] get(Node n, TrieKey k) {
        if (n == null) return null;
        var type = n.getType();
        if (type == NodeType.BranchNode) {
            if (k.isEmpty()) return n.branchNodeGetValue();
            var childNode = n.branchNodeGetChild(k.getHex(0));
            return get(childNode, k.shift(1));
        } else {
            var k1 = k.matchAndShift(n.kvNodeGetKey());
            if (k1 == null) return null;
            if (type == NodeType.KVNodeValue) {
                return k1.isEmpty() ? n.kvNodeGetValue() : null;
            } else {
                return get(n.kvNodeGetChildNode(), k1);
            }
        }
    }

    public void put(byte[] key, byte[] value) {
        var k = TrieKey.fromNormal(key);
        if (root == null) {
            if (value != null && value.length > 0) {
                root = new Node(k, value);
            }
        } else {
            if (value == null || value.length == 0) {
                root = delete(root, k);
            } else {
                root = insert(root, k, value);
            }
        }
    }

    private Node insert(Node n, TrieKey k, Object nodeOrValue) {
        var type = n.getType();
        if (type == NodeType.BranchNode) {
            if (k.isEmpty()) return n.branchNodeSetValue((byte[]) nodeOrValue);
            var childNode = n.branchNodeGetChild(k.getHex(0));
            if (childNode != null) {
                return n.branchNodeSetChild(k.getHex(0), insert(childNode, k.shift(1), nodeOrValue));
            } else {
                var childKey = k.shift(1);
                Node newChildNode;
                if (!childKey.isEmpty()) {
                    newChildNode = new Node(childKey, nodeOrValue);
                } else {
                    newChildNode = nodeOrValue instanceof Node ?
                            (Node) nodeOrValue : new Node(childKey, nodeOrValue);
                }
                return n.branchNodeSetChild(k.getHex(0), newChildNode);
            }
        } else {
            var currentNodeKey = n.kvNodeGetKey();
            var commonPrefix = k.getCommonPrefix(currentNodeKey);
            if (commonPrefix.isEmpty()) {
                var newBranchNode = new Node();
                insert(newBranchNode, currentNodeKey, n.kvNodeGetValueOrNode());
                insert(newBranchNode, k, nodeOrValue);
                n.dispose();
                return newBranchNode;
            } else if (commonPrefix.equals(k)) {
                return n.kvNodeSetValueOrNode(nodeOrValue);
            } else if (commonPrefix.equals(currentNodeKey)) {
                insert(n.kvNodeGetChildNode(), k.shift(commonPrefix.getLength()), nodeOrValue);
                return n.invalidate();
            } else {
                var newBranchNode = new Node();
                var newKvNode = new Node(commonPrefix, newBranchNode);
                // TODO can be optimized
                insert(newKvNode, currentNodeKey, n.kvNodeGetValueOrNode());
                insert(newKvNode, k, nodeOrValue);
                n.dispose();
                return newKvNode;
            }
        }
    }

    private Node delete(Node n, TrieKey k) {
        var type = n.getType();
        Node newKvNode;
        if (type == NodeType.BranchNode) {
            if (k.isEmpty()) {
                n.branchNodeSetValue(null);
            } else {
                var idx = k.getHex(0);
                var child = n.branchNodeGetChild(idx);
                if (child == null) return n; // no key found

                var newNode = delete(child, k.shift(1));
                n.branchNodeSetChild(idx, newNode);
                if (newNode != null) return n; // newNode != null thus number of children didn't decrease
            }

            // child node or value was deleted and the branch node may need to be compacted
            var compactIdx = n.branchNodeCompactIdx();
            if (compactIdx < 0) return n; // no compaction is required

            // only value or a single child left - compact branch node to kvNode
            n.dispose();
            if (compactIdx == 16) { // only value left
                return new Node(TrieKey.empty(true), n.branchNodeGetValue());
            } else { // only single child left
                newKvNode = new Node(TrieKey.singleHex(compactIdx), n.branchNodeGetChild(compactIdx));
            }
        } else { // n - kvNode
            var k1 = k.matchAndShift(n.kvNodeGetKey());
            if (k1 == null) {
                // no key found
                return n;
            } else if (type == NodeType.KVNodeValue) {
                if (k1.isEmpty()) {
                    // delete this kvNode
                    n.dispose();
                    return null;
                } else {
                    // else no key found
                    return n;
                }
            } else {
                var newChild = delete(n.kvNodeGetChildNode(), k1);
                if (newChild == null) throw new RuntimeException("Shouldn't happen");
                newKvNode = n.kvNodeSetValueOrNode(newChild);
            }
        }

        // if we get here a new kvNode was created, now need to check
        // if it should be compacted with child kvNode
        var newChild = newKvNode.kvNodeGetChildNode();
        if (newChild.getType() != NodeType.BranchNode) {
            // two kvNodes should be compacted into a single one
            var newKey = newKvNode.kvNodeGetKey().concat(newChild.kvNodeGetKey());
            var newNode = new Node(newKey, newChild.kvNodeGetValueOrNode());
            newChild.dispose();
            newKvNode.dispose();
            return newNode;
        } else {
            // no compaction needed
            return newKvNode;
        }
    }

    public byte[] getRootHash() {
        encode();
        return root != null ? root.hash : EMPTY_TRIE_HASH;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var trieImpl1 = (Trie) o;
        return FastByteComparisons.equal(getRootHash(), trieImpl1.getRootHash());
    }

    public static byte[] calcLocalReceiptsTrie(List<EthReceipt> ethReceipts) {
        var receiptsTrie = new Trie();
        if (ethReceipts != null && !ethReceipts.isEmpty()) {
            for (var i = 1; i < ethReceipts.size() && i <= 0x7f; i++) {
                receiptsTrie.put(RLP.encodeInt(i), calcLocalReceiptTrieEncoded(ethReceipts.get(i)));
            }
            receiptsTrie.put(RLP.encodeInt(0), calcLocalReceiptTrieEncoded(ethReceipts.get(0)));
            for (int i = 0x80; i < ethReceipts.size(); i++) {
                receiptsTrie.put(RLP.encodeInt(i), calcLocalReceiptTrieEncoded(ethReceipts.get(i)));
            }
            return receiptsTrie.getRootHash();
        } else {
            return HashUtil.EMPTY_TRIE_HASH;
        }
    }

    public static byte[] calcLocalReceiptTrieEncoded(EthReceipt ethReceipt) {
        var receiptTrieEncoded = ethReceipt.getReceiptTrieEncoded();
        var receiptType = Numeric.toBigInt(ethReceipt.getType()).intValue();
        if (receiptType != 0) {
            receiptTrieEncoded = ByteUtils.concatenate(ByteUtil.intToBytesNoLeadZeroes(receiptType), receiptTrieEncoded);
        }

        return receiptTrieEncoded;
    }
}
