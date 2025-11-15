import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * HuffmanTool provides a simple command-line interface for running the
 * Huffman compressor and decompressor. The user chooses whether to compress
 * or decompress a file, then enters the input and output paths.
 *
 * This class acts as a small utility wrapper around HuffmanCompressor.
 * Files are part of a larger team project.
 * @author Adesoye Oyeyiola
 */
public class HuffmanTool {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        HuffmanCompressor compressor = new HuffmanCompressor();

        // Display menu options for user interaction
        System.out.println("HuffZip");
        System.out.println("1) Compress");
        System.out.println("2) Decompress");
        System.out.print("Choose an option: ");

        int choice = Integer.parseInt(sc.nextLine().trim());

        // Get input and output file paths from the user
        System.out.print("Input file path: ");
        Path input = Path.of(sc.nextLine().trim());

        System.out.print("Output file path: ");
        Path output = Path.of(sc.nextLine().trim());

        // Perform compression or decompression based on user selection
        if (choice == 1) {
            compressor.compress(input, output);
            System.out.println("Compressed successfully.");
        } else if (choice == 2) {
            compressor.decompress(input, output);
            System.out.println("Decompressed successfully.");
        } else {
            System.out.println("Invalid choice.");
        }
    }
}


