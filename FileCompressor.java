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
            System.out.println("Compression starting");
            lz = new LempelZivCompressor(inputFile);
            f = new File(lz.compress("lzCompressed_" + PATH_NAME));
            hf = new HuffmanCompressor(f);
            System.out.println("Finished Lempel-Ziv compression\nHuffman compression starting");
            hf.compress("Compressed_" + PATH_NAME);

        } else if (choice == 2) {
            System.out.println("Decompression starting");
            System.out.println("Enter wished name of file for the decompressed file (with fileType): ");
            String fileName = scanner.nextLine();
            hf = new HuffmanCompressor(inputFile);
            f = new File(hf.decompress("hfCompressed_" + PATH_NAME));
            lz = new LempelZivCompressor(f);
            System.out.println("Finished Huffman decompression\nLempel-ziv decompression starting");
            lz.decompress(fileName);
        } else {
            System.out.println("Choice not valid");
        }
        f.delete();
        System.out.println("Compression finished");
        scanner.close();
    }

}
