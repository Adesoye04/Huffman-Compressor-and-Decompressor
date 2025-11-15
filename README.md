# Huffman Compressor and Decompressor (Java)

This repository holds my own version of a Huffman compression and decompression setup, all coded in Java from scratch. It has all the pieces to turn any file into a tight bit-based Huffman file and then get it back exactly as it started. I first built this for a bigger school project, but here I have pulled out just my contributions and tidied them up for showing in my portfolio.

The program walks through the full Huffman coding process step by step. It starts by counting frequencies in the input data, then builds the tree structure, gives each symbol its bit code, packs those bits smartly into bytes, and saves everything in a special file layout. For unpacking, it reads the file header to remake the tree, pulls bits one by one, and traces through the tree to pull out the original bytes without any loss. Plus, there is a basic command line program that handles compressing or decompressing files right from your computer.

---

## Main Files

### `HuffmanNode.java`

Among the files here, `HuffmanNode.java` sets up each part of the Huffman tree. It tracks a byte for the symbol, counts its frequency, and points to child nodes on left and right sides. Both the packing and unpacking parts rely on these nodes.

### `HuffmanCompressor.java`

`HuffmanCompressor.java` takes charge of the core steps for squeezing and restoring files. It manages tasks like making the frequency counts, putting together the tree, linking codes to bytes, bundling bits into full bytes for saving, and creating a header with a marker string, file size, symbols list, and their codes. It also pulls that header apart to rebuild the tree and decodes the bits using the tree paths. Most of the real Huffman work happens inside this main class.

### `BitOutputStream` and `BitInputStream`

Then there are `BitOutputStream` and `BitInputStream`, which act as little support tools within the compressor. The output one lets you send bits rather than full bytes at once. The input version reads single bits as needed. They both handle buffering to group or split bits properly, which makes dealing with Huffman data smooth and effective.

### `HuffmanTool.java`

`HuffmanTool.java` provides a simple way to run the compressor from the command line. You can pick whether to compress or decompress, give it an input file location, set where to save the output, and then it runs the job with a quick status note. This setup lets you try things out easily, without needing to code in Java each time.

---

## File Format

The file format from this tool always begins with a few key parts in every compressed output. First comes the magic identifier `HUF1`. Next is the length of the original file before packing. Then it lists how many symbols appear in the table. For each one, it includes the byte itself, how long its code is in bits, and the bits packed for that code. The rest of the file holds the squeezed data as a stream of bits after the header. Since the header gives enough to rebuild the whole tree, the unpacking side skips needing extra details stored anywhere.

---

## Example Usage

For example usage, you would run a command to compress a file like this:
Input file path: input.txt
Output file path: output.huf


then for decompressing, you use:

Input file path: output.huf
Output file path: restored.txt


After the process finishes, the `restored.txt` file matches the starting input exactly, down to every bit.

---

## Concepts Demonstrated

The project covers a range of key concepts in depth. It shows Huffman encoding in action, along with using priority queues to form trees, handling input and output at the bit level, designing binary file structures from scratch, applying recursion to create codes, managing full encoding and decoding cycles, working with Java's I/O streams, and dealing carefully with tricky situations such as empty files or ones with just a single character. Overall, it proves how you can build a standard compression method in a straightforward and capable way, all with standard Java tools.

---

## Contact

If you need a detailed explanation of how I put this together or examples of other work I have done, reach out through these:

- GitHub: https://github.com/Adesoye04  
- LinkedIn: https://www.linkedin.com/in/adesoye-oyeyiola/  
- Email: `adesoyeoyeyiola44@gmail.com`
