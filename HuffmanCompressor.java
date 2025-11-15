import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * HuffmanCompressor handles compressing and decompressing byte data
 * using Huffman coding. It reads the raw bytes, builds a frequency table,
 * constructs a Huffman tree, assigns bit codes to each symbol, and then
 * writes a compact bit-level representation to an output file.
 *
 * The compressed file format starts with a small header containing:
 * - A magic string "HUF1" to identify the format
 * - The original uncompressed length (in bytes)
 * - The number of distinct symbols
 * - A code table mapping each symbol to a bit pattern
 * followed by the encoded data bits themselves.
 *
 * Files are part of a larger team project.
 * @author Adesoye Oyeyiola
 */
public class HuffmanCompressor {

    /**
     * Compresses the input file into a Huffman-encoded output file.
     * Builds a frequency table, Huffman tree, and code map for all bytes
     * in the input, then writes a header and the compressed bitstream.
     *
     * For an empty input file, this writes a minimal header and exits.
     *
     * @param input  path to the original uncompressed file
     * @param output path to the compressed output file
     * @throws IOException if any I/O errors occur while reading or writing
     */
    public void compress(Path input, Path output) throws IOException {
        byte[] data = Files.readAllBytes(input);
        Map<Byte, Integer> freq = buildFrequencyTable(data);
        HuffmanNode root = buildTree(freq);
        Map<Byte, String> codeMap = buildCodeMap(root);

        if (data.length == 0) {
            // Handles the special case of an empty file (no data to encode)
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(output))) {
                out.writeBytes("HUF1");
                out.writeLong(0L);   // originalLen = 0
                out.writeShort(0);   // symbolCount = 0
            }
            return;
        }

        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(output))) {
            // Write magic header and original length
            out.writeBytes("HUF1");
            out.writeLong(data.length);

            // Write the size of the code map (number of distinct symbols)
            out.writeShort(codeMap.size());

            // For each symbol, write its code length and the packed bits
            for (Map.Entry<Byte, String> entry : codeMap.entrySet()) {
                byte symbol = entry.getKey();
                String bits = entry.getValue();
                int L = bits.length();

                out.writeByte(symbol);
                out.writeByte(L);

                byte[] packed = packBitsString(bits);
                out.write(packed);
            }

            // Now write the actual compressed bitstream for the file contents
            BitOutputStream bitOut = new BitOutputStream(out);
            for (byte b : data) {
                bitOut.writeBits(codeMap.get(b));
            }
            bitOut.flush();
        }
    }

    /**
     * Packs a string of '0' and '1' characters into an array of bytes.
     * Bits are processed left to right and grouped into bytes. The last
     * byte is padded with zeros if the number of bits is not a multiple of 8.
     *
     * @param bits the string of bits ('0' or '1') to pack
     * @return a byte array where each byte holds up to 8 bits from the input
     * @throws IOException if an error occurs writing to the internal buffer
     */
    private byte[] packBitsString(String bits) throws IOException {
        ByteArrayOutputStream Rags = new ByteArrayOutputStream();
        int current = 0;
        int count = 0;

        for (int i = 0; i < bits.length(); i++) {
            int bit = bits.charAt(i) == '1' ? 1 : 0;
            current = (current << 1) | (bit & 1);
            count++;

            // Once we collect 8 bits, flush them as a full byte
            if (count == 8) {
                Rags.write(current);
                count = 0;
                current = 0;
            }
        }

        // Handle remaining bits by padding the last byte
        if (count > 0) {
            current <<= (8 - count);
            Rags.write(current);
        }

        return Rags.toByteArray();
    }

    /**
     * Decompresses a Huffman-encoded file produced by this class.
     * Reads the header to reconstruct the code table and rebuilds the
     * Huffman tree, then decodes bits back into the original bytes.
     *
     * @param input  path to the compressed input file
     * @param output path where the decompressed file will be written
     * @throws IOException if the file format is invalid or I/O fails
     */
    public void decompress(Path input, Path output) throws IOException {
        DataInputStream in = new DataInputStream(Files.newInputStream(input));

        // Root of the Huffman tree, to be reconstructed from the header
        HuffmanNode root = new HuffmanNode((byte) 0, 0, null, null);

        if (new String(in.readNBytes(4)).equals("HUF1")) {
            long originalLen = in.readLong();
            int symbolCount = in.readUnsignedShort();

            // Rebuild the Huffman tree by reading back each symbol's bit code
            for (int i = 0; i < symbolCount; i++) {
                StringBuilder sb = new StringBuilder();
                byte symbol = in.readByte();
                int L = in.readUnsignedByte();

                int numBytes = (L + 7) / 8;
                byte[] packed = in.readNBytes(numBytes);

                // Unpack bits from the stored bytes into a bit string
                for (byte b : packed) {
                    for (int k = 7; k >= 0; k--) {
                        int current = (b >> k) & 1;
                        sb.append(current == 1 ? '1' : '0');
                    }
                }

                String bits = sb.substring(0, L);

                // Walk the tree to assign this code path to the symbol
                HuffmanNode current = root;
                for (int j = 0; j < bits.length(); j++) {
                    if (bits.charAt(j) == '1') {
                        if (current.right == null) {
                            current.right = new HuffmanNode((byte) 0, 0, null, null);
                        }
                        current = current.right;
                    } else { // '0'
                        if (current.left == null) {
                            current.left = new HuffmanNode((byte) 0, 0, null, null);
                        }
                        current = current.left;
                    }
                }
                current.symbol = symbol;
            }

            // Now decode the bitstream using the reconstructed Huffman tree
            try (OutputStream out = Files.newOutputStream(output)) {
                BitInputStream bitIn = new BitInputStream(in);
                long produced = 0;

                while (produced < originalLen) {
                    HuffmanNode node = root;

                    // Traverse the tree until a leaf node is reached
                    while (!node.isLeaf()) {
                        int bit = bitIn.readBit();
                        if (bit == 0) {
                            node = node.left;
                        } else {
                            node = node.right;
                        }
                    }

                    out.write(node.symbol);
                    produced++;
                }
            }
        }
    }

    /**
     * Builds a frequency table for all bytes in the given array.
     * Each distinct byte is mapped to the number of times it appears.
     *
     * @param data the byte array to analyze
     * @return a map from byte values to their frequency counts
     */
    private Map<Byte, Integer> buildFrequencyTable(byte[] data) {
        Map<Byte, Integer> freq = new HashMap<>();
        for (byte b : data) {
            freq.merge(b, 1, Integer::sum);
        }
        return freq;
    }

    /**
     * Builds a Huffman tree from the frequency table. Leaf nodes represent
     * actual symbols, and internal nodes represent merged frequencies.
     *
     * For the special case where there is only one distinct symbol, a dummy
     * parent node is created to allow at least one bit of code.
     *
     * @param freq the map of byte values to their frequencies
     * @return the root of the Huffman tree
     */
    private HuffmanNode buildTree(Map<Byte, Integer> freq) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(
                Comparator.comparingInt(node -> node.freq)
        );

        // Initialize the priority queue with one node per symbol
        for (Map.Entry<Byte, Integer> entry : freq.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue(), null, null));
        }

        // Handle the edge case of only one distinct symbol
        if (pq.size() == 1) {
            HuffmanNode only = pq.remove();
            return new HuffmanNode((byte) 0, only.freq, only, null);
        }

        // Repeatedly merge the two smallest nodes until one tree remains
        while (pq.size() > 1) {
            HuffmanNode a = pq.remove();
            HuffmanNode b = pq.remove();
            HuffmanNode parent = new HuffmanNode((byte) 0, a.freq + b.freq, a, b);
            pq.add(parent);
        }

        return pq.remove();
    }

    /**
     * Builds a mapping from each symbol in the Huffman tree to its bit code.
     * The codes are derived by walking down the tree and appending '0' for a
     * left edge and '1' for a right edge.
     *
     * @param root the root of the Huffman tree
     * @return a map from byte symbols to their Huffman code strings
     */
    private Map<Byte, String> buildCodeMap(HuffmanNode root) {
        Map<Byte, String> codeMap = new HashMap<>();
        buildCodeMapRec(root, "", codeMap);
        return codeMap;
    }

    /**
     * Helper method that performs a recursive depth-first traversal of the
     * Huffman tree to assign codes to each leaf node.
     *
     * @param node   current node in the Huffman tree
     * @param prefix bit string built so far along the path
     * @param map    map used to store the final symbol-to-code assignments
     */
    private void buildCodeMapRec(HuffmanNode node, String prefix, Map<Byte, String> map) {
        if (node.isLeaf()) {
            // If there is only one symbol in the entire file, ensure it has a non-empty code
            String code = prefix.isEmpty() ? "0" : prefix;
            map.put(node.symbol, code);
            return;
        }
        if (node.left != null) {
            buildCodeMapRec(node.left, prefix + "0", map);
        }
        if (node.right != null) {
            buildCodeMapRec(node.right, prefix + "1", map);
        }
    }

    /**
     * BitOutputStream manages writing individual bits to an underlying
     * OutputStream. Bits are buffered into a full byte before being written.
     * The last byte is padded with zeros if necessary.
     *
     * Files are part of a larger team project.
     * @author Adesoye Oyeyiola
     */
    class BitOutputStream implements Closeable {
        private final OutputStream out;
        private int currentByte = 0;
        private int numBitsFilled = 0;

        BitOutputStream(OutputStream out) {
            this.out = out;
        }

        /**
         * Writes a single bit (0 or 1) to the stream. Bits are accumulated
         * until a full byte is ready to be written.
         *
         * @param bit the bit value to write (only the least significant bit is used)
         * @throws IOException if an I/O error occurs
         */
        public void writeBit(int bit) throws IOException {
            currentByte = (currentByte << 1) | (bit & 1);
            numBitsFilled++;

            if (numBitsFilled == 8) {
                out.write(currentByte);
                currentByte = 0;
                numBitsFilled = 0;
            }
        }

        /**
         * Writes a sequence of bits given as a string of '0' and '1'
         * characters. Each character is interpreted as a single bit.
         *
         * @param bits the bit string to write
         * @throws IOException if an I/O error occurs
         */
        public void writeBits(String bits) throws IOException {
            for (int i = 0; i < bits.length(); i++) {
                int bit = bits.charAt(i) == '1' ? 1 : 0;
                writeBit(bit);
            }
        }

        /**
         * Flushes any remaining bits in the buffer by padding the current
         * byte with zeros up to 8 bits and writing it out.
         *
         * @throws IOException if an I/O error occurs
         */
        public void flush() throws IOException {
            if (numBitsFilled > 0) {
                currentByte <<= (8 - numBitsFilled);
                out.write(currentByte);
                numBitsFilled = 0;
                currentByte = 0;
            }
        }

        /**
         * Flushes any pending bits and closes the underlying OutputStream.
         *
         * @throws IOException if an I/O error occurs
         */
        public void close() throws IOException {
            flush();
            out.close();
        }
    }

    /**
     * BitInputStream allows reading individual bits from an underlying
     * InputStream. Bits are taken one at a time from a buffered byte.
     * If the end of the stream is reached unexpectedly, an exception is thrown.
     *
     * Files are part of a larger team project.
     * @author Adesoye Oyeyiola
     */
    class BitInputStream implements Closeable {
        private final InputStream in;
        private int currentByte = 0;
        private int bitsRemaining = 0;

        BitInputStream(InputStream in) {
            this.in = in;
        }

        /**
         * Reads the next bit from the stream. If no bits are left in the
         * current byte, a new byte is read from the underlying stream.
         *
         * @return the next bit (0 or 1)
         * @throws IOException if the end of the stream is reached unexpectedly
         *                     or if an I/O error occurs
         */
        public int readBit() throws IOException {
            if (bitsRemaining == 0) {
                currentByte = in.read();
                if (currentByte == -1) {
                    throw new IOException("Unexpected end of file");
                }
                bitsRemaining = 8;
            }

            int bit = (currentByte >> 7) & 1;
            currentByte <<= 1;
            bitsRemaining--;
            return bit;
        }

        /**
         * Closes the underlying InputStream.
         *
         * @throws IOException if an I/O error occurs
         */
        public void close() throws IOException {
            in.close();
        }
    }
}
