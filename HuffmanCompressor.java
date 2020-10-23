import java.io.*;
import java.nio.file.Files;

public class HuffmanCompressor {
    private final File file;
    private final int[] frequencies = new int[256];
    private int uniqueChars;
    private final byte[] input;
    private final static int BYTE_VALUE_ADJUSTMENT_CONSTANT = 128;

    public HuffmanCompressor(File file) throws IOException {
        this.file = file;
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

    public String compress(String fileName) throws IOException {
        File outputFile = new File(fileName);
        outputFile.createNewFile();
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

        countFrequencies();
        HuffmanTree ht = HuffmanTree.createHuffmanTree(prepareHuffmanHeap());
        BitString[] codes = ht.getBinaryTable();
        for (int frequency : frequencies)
            out.writeInt(frequency);

        BitString[] output = new BitString[input.length];
        int bitStringsReceived = 0;

        BitString bits = null;
        BitString toBeAdded = null;
        while (in.available() > 0) {
            if (bits == null || bits.getBitsUsed() <= 0)
                bits = new BitString(codes[in.readByte() + BYTE_VALUE_ADJUSTMENT_CONSTANT]);
            if (bits.getBitsUsed() < 64) {
                if (toBeAdded == null || toBeAdded.getBitsUsed() <= 0)
                    toBeAdded = new BitString(codes[in.readByte() + BYTE_VALUE_ADJUSTMENT_CONSTANT]);
                int bitsToBeAdded = (64 - bits.getBitsUsed() < toBeAdded.getBitsUsed()) ? 64 - bits.getBitsUsed()
                        : toBeAdded.getBitsUsed();
                bits.appendBitString(toBeAdded.shiftBits(bitsToBeAdded));
            }
            if (bits.getBitsUsed() == 64) {
                output[bitStringsReceived++] = bits;
                if (toBeAdded == null || toBeAdded.getBitsUsed() <= 0)
                    bits = null;
                else {
                    bits = toBeAdded;
                    toBeAdded = null;
                }
            }
        }
        // Handles a situation where the last index is of less than 64 bytes

        byte restBits = 0;
        if (bits != null) {
            if (toBeAdded != null && toBeAdded.getBitsUsed() > 0)
                bits.appendBitString(toBeAdded);
            while (bits.getBitsUsed() >= 8)
                output[bitStringsReceived++] = bits.shiftBits(8);
            if (bits.getBitsUsed() > 0) {
                restBits = (byte) (8 - bits.getBitsUsed());
                for (int i = 0; i < restBits; i++)
                    bits.appendBit(0b0);
                output[bitStringsReceived++] = bits;
            }
        }

        out.writeByte(restBits);
        int i = 0;
        while (output[i] != null) {
            BitString b = output[i++];
            if (b.getBitsUsed() == 64)
                out.writeLong((long) b.getBits());
            else if (b.getBitsUsed() == 8)
                out.writeByte((byte) b.getBits());
        }

        in.close();
        out.flush();
        out.close();
        return fileName;
    }

    public String decompress(String fileName) throws IOException {
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

        byte restBits = in.readByte();

        // Building the huffman tree
        HuffmanTree ht = HuffmanTree.createHuffmanTree(prepareHuffmanHeap());

        FrequencyTreeNode node;
        BitString bits = null;
        while (in.available() > 0) {
            node = ht.getRoot();
            while (!(node instanceof HuffmanTreeNode)) {
                if (bits == null || bits.getBitsUsed() <= 0) {
                    bits = new BitString(in.readByte(), 8);
                    if (in.available() == 0)
                        bits.popBits(restBits);
                }
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
}
