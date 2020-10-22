import java.util.Arrays;
import java.util.PriorityQueue;

public class HuffmanTree {
    private FrequencyTreeNode root;
    private static final int BYTE_VALUE_ADJUSTMENT_CONSTANT = 128;

    private HuffmanTree(FrequencyTreeNode root) {
        this.root = root;
    }

    public FrequencyTreeNode getRoot() {
        return root;
    }

    public static HuffmanTree createHuffmanTree(FrequencyTreeNode[] frequencies) {
        PriorityQueue<FrequencyTreeNode> priorityQueue = new PriorityQueue<>();
        priorityQueue.addAll(Arrays.asList(frequencies));

        FrequencyTreeNode root = null;
        while (priorityQueue.size() > 0) {
            FrequencyTreeNode min1 = priorityQueue.poll();
            FrequencyTreeNode min2 = priorityQueue.poll();
            root = new FrequencyTreeNode(min1.getFrequency() + min2.getFrequency());
            if (priorityQueue.size() > 0)
                priorityQueue.add(root);
            min1.setParent(root);
            root.setLeftChild(min1);
            min2.setParent(root);
            root.setRightChild(min2);
        }

        return new HuffmanTree(root);
    }

    private void walkTree(BitString[] binaryTable, BitString bitString, FrequencyTreeNode frequencyTreeNode) {
        // Do not address if the child is null since this should not happen
        // If the node is an instance of HuffmanTreeNode then it is an end node
        if (frequencyTreeNode instanceof HuffmanTreeNode) {
            HuffmanTreeNode huffmanTreeNode = (HuffmanTreeNode) frequencyTreeNode;
            binaryTable[huffmanTreeNode.getContent() + BYTE_VALUE_ADJUSTMENT_CONSTANT] = bitString;
        } else {
            // Using copy constructor to achieve composition to hinder modifying the same
            // bit string
            BitString leftChild = new BitString(bitString);
            BitString rightChild = new BitString(bitString);
            walkTree(binaryTable, leftChild.appendBit(0b0), frequencyTreeNode.getLeftChild());
            walkTree(binaryTable, rightChild.appendBit(0b1), frequencyTreeNode.getRightChild());
        }
    }

    public BitString[] getBinaryTable() {
        BitString[] binaryTable = new BitString[256];
        walkTree(binaryTable, new BitString(0b0, 0), this.root);
        return binaryTable;
    }

}
