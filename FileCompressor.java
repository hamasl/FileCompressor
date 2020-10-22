import java.io.*;
import java.util.Scanner;

public class FileCompressor {
    public static void main(String[] args) throws IOException {
        final String PATH_NAME = args[0];

        File inputFile = new File(PATH_NAME);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter:\n1 to compress\n2 to decompress");
        int choice = scanner.nextInt();
        scanner.nextLine();

        LempelZivCompressor lz;
        HuffmanCompressor hf;

        File f = null;
        if (choice == 1) {
            lz = new LempelZivCompressor(inputFile);
            f = new File(lz.compress());
            hf = new HuffmanCompressor(f);
            System.out.println("Finished Lempel-Ziv compression\nHuffman compression starting");
            hf.compress();

        } else if (choice == 2) {
            System.out.println("Enter wished filetype for the decompressed file: ");
            String fileType = scanner.nextLine();
            hf = new HuffmanCompressor(inputFile);
            f = new File(hf.decompress());
            lz = new LempelZivCompressor(f);
            System.out.println("Finished Huffman decompression\nLempel-ziv decompression starting");
            lz.decompress(fileType);
        } else {
            System.out.println("Choice not valid");
        }
        f.delete();
        System.out.println("Compression finished");
        scanner.close();
    }

}
