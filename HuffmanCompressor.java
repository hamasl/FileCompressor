import java.io.*;
import java.nio.file.Files;

public class HuffmanCompressor {
    private final File file;
    private final String fileName;
    private final String fileType;
    private final int[] frequencies = new int[256];
    private int uniqueChars;
    private final byte[] input;
    private final static int BYTE_VALUE_ADJUSTMENT_CONSTANT = 128;

    public HuffmanCompressor(File file) throws IOException {
        this.file = file;
        this.fileName = file.getName().split("\\.")[0];
        this.fileType = file.getName().split("\\.")[1];
        this.input = Files.readAllBytes(file.toPath());
    }

    public int[] getFrequencies() {
        return frequencies;
    }

    private void resetFrequencies() {
        for (int i = 0; i < frequencies.length; i++)
            frequencies[i] = 0;
        uniqueChars = 0;
    }

    private void countFrequencies() {
        resetFrequencies();
        for (byte b : input) {
            if (frequencies[BYTE_VALUE_ADJUSTMENT_CONSTANT + b] == 0)
                uniqueChars++;
            frequencies[BYTE_VALUE_ADJUSTMENT_CONSTANT + b]++;
        }
    }

    private FrequencyTreeNode[] prepareHuffmanHeap() {
        int addedNodes = 0;
        FrequencyTreeNode[] huffmanTreeNodes = new FrequencyTreeNode[uniqueChars];
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                huffmanTreeNodes[addedNodes] = new HuffmanTreeNode((byte) (i - BYTE_VALUE_ADJUSTMENT_CONSTANT),
                        frequencies[i]);
                addedNodes++;
            }
        }
        return huffmanTreeNodes;
    }

    public String compress() throws IOException {
        String fileName = "hufmannCompressed_" + this.fileName + ".txt";
        File outputFile = new File(fileName);
        outputFile.createNewFile();
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

        countFrequencies();
        HuffmanTree ht = HuffmanTree.createHuffmanTree(prepareHuffmanHeap());
        BitString[] codes = ht.getBinaryTable();
        for (int frequency : frequencies)
            out.writeInt(frequency);

        int i = 0;
        BitString bits = null;
        BitString toBeAdded = null;
        while (i < input.length) {
            if (toBeAdded != null && toBeAdded.getBitsUsed() <= 0)
                toBeAdded = null;
            if (bits == null && toBeAdded != null && toBeAdded.getBitsUsed() > 0) {
                bits = toBeAdded;
                toBeAdded = null;
            } else if (bits == null)
                bits = new BitString(codes[(input[i++] + BYTE_VALUE_ADJUSTMENT_CONSTANT)]);
            if (bits.getBitsUsed() < 64 && i /* + 1 */ < input.length) {
                if (toBeAdded == null)
                    toBeAdded = new BitString(codes[(input[i++] + BYTE_VALUE_ADJUSTMENT_CONSTANT)]);
                // In case the previous loop took all the bits from toBeAdded
                if (toBeAdded.getBitsUsed() > 0) {
                    int bitsToBeAdded = (64 - bits.getBitsUsed() < toBeAdded.getBitsUsed()) ? 64 - bits.getBitsUsed()
                            : toBeAdded.getBitsUsed();
                    bits.appendBitString(toBeAdded.shiftBits(bitsToBeAdded));
                }
            } else if (bits.getBitsUsed() < 64)
                i++;
            if (bits.getBitsUsed() == 64) {
                out.writeLong(bits.getBits());
                bits = null;
            }
        }
        // Handles a situation where the last index is of less than 64 bytes
        if (bits != null) {
            if (toBeAdded != null)
                bits.appendBitString(toBeAdded);
            while (bits.getBitsUsed() >= 8)
                out.writeByte((byte) bits.shiftBits(8).getBits());
            if (bits.getBitsUsed() > 0)
                out.writeByte((byte) bits.getBits());
        }

        out.flush();
        out.close();
        return fileName;
    }

    public String decompress() throws IOException {
        String fileName = "huffmanDecompressed_" + this.fileName + "." + fileType;
        File outputFile = new File(fileName);
        outputFile.createNewFile();
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        for (int i = 0; i < frequencies.length; i++) {
            int frequency = in.readInt();
            frequencies[i] = frequency;
            if (frequency > 0)
                uniqueChars++;
        }

        // Building the binary table
        HuffmanTree ht = HuffmanTree.createHuffmanTree(prepareHuffmanHeap());

        FrequencyTreeNode node;
        BitString bits = null;
        while (in.available() > 0) {
            node = ht.getRoot();
            while (!(node instanceof HuffmanTreeNode)) {
                if (bits == null || bits.getBitsUsed() <= 0)
                    bits = new BitString(in.readByte(), 8);
                byte nextBit = (byte) bits.shiftBits(1).getBits();
                if (nextBit == 0)
                    node = node.getLeftChild();
                else if (nextBit == 1)
                    node = node.getRightChild();
                else
                    throw new IllegalStateException("No such node exists");
            }
            out.writeByte(((HuffmanTreeNode) node).getContent());
        }
        in.close();
        out.flush();
        out.close();
        return fileName;
    }

    public static void main(String[] args) throws IOException {
        HuffmanCompressor hf = new HuffmanCompressor(new File("diverse.txt"));

        HuffmanCompressor fh = new HuffmanCompressor(new File(hf.compress()));
        fh.decompress();

        // System.out.println(hf.intArrayEquals(hf.frequencies, fh.frequencies));
    }
}
