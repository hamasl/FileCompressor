public class FrequencyTreeNode implements Comparable {
    private FrequencyTreeNode parent;
    private FrequencyTreeNode leftChild;
    private FrequencyTreeNode rightChild;
    private int frequency;

    public FrequencyTreeNode(int frequency) {
        this.frequency = frequency;
        leftChild = rightChild = parent = null;
    }

    public FrequencyTreeNode(FrequencyTreeNode parent, FrequencyTreeNode leftChild, FrequencyTreeNode rightChild,
            int frequency) {
        this.parent = parent;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.frequency = frequency;
    }

    public FrequencyTreeNode getParent() {
        return parent;
    }

    public void setParent(FrequencyTreeNode parent) {
        this.parent = parent;
    }

    public FrequencyTreeNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(FrequencyTreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public FrequencyTreeNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(FrequencyTreeNode rightChild) {
        this.rightChild = rightChild;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public int compareTo(Object o) {
        FrequencyTreeNode f = (FrequencyTreeNode) o;
        return this.frequency - f.frequency;
    }
}
