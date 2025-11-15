/**
 * HuffmanNode models a single node within the Huffman coding tree.
 * Each node stores a byte symbol, its frequency count, and optional
 * references to left and right child nodes. Internal nodes typically
 * do not represent actual symbols but store combined frequencies.
 *
 * Files are part of a larger team project.
 * @author Adesoye Oyeyiola
 */

public class HuffmanNode {

    byte symbol;       // The character/byte stored in this node (valid only for leaf nodes)
    final int freq;    // Frequency of this symbol or combined frequency for internal nodes
    HuffmanNode left;  // Left child in the Huffman tree
    HuffmanNode right; // Right child in the Huffman tree

    /**
     * Checks if this node is a leaf of the Huffman tree.
     * A node is considered a leaf if both child references are null.
     *
     * @return true if the node has no children, false otherwise
     */
    boolean isLeaf() {
        return left == null && right == null;
    }

    /**
     * Constructs a HuffmanNode which may represent either:
     * - A leaf node: storing an actual byte symbol and its frequency.
     * - An internal node: storing a combined frequency and child references.
     *
     * @param symbol the byte stored at this node (ignored for internal nodes)
     * @param freq the frequency of the symbol or combined frequency of children
     * @param left reference to the left child node
     * @param right reference to the right child node
     */
    public HuffmanNode(byte symbol, int freq, HuffmanNode left, HuffmanNode right) {
        this.symbol = symbol;
        this.freq = freq;
        this.left = left;
        this.right = right;
    }
}
