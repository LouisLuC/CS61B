public class SLList<Item> implements List<Item> {
    private class Node<Item> {
        Item item;
        Node<Item> next;

        public Node(Item x) {
            this.item = x;
            this.next = null;
        }

        public Node(Item x, Node next) {
            this.item = x;
            this.next = next;
        }
    }

    private Node<Item> sentinel = new Node<Item>(null, null);
    private int size;

    @Override
    public Item get(int i) {
        if (i + 1 > this.size || i < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        Node<Item> iter = this.sentinel.next;

        for (int j = 0; j < i; j++)
            iter = iter.next;
        return iter.item;
    }

    @Override
    public Item getLast() {
        if (this.size == 0)
            return null;
        return this.get(this.size - 1);
    }

    @Override
    public Item getFirst() {
        if (this.size == 0)
            return null;
        return this.sentinel.next.item;
    }

    @Override
    public void addLast(Item x) {
        this.insert(x, this.size);
    }

    @Override
    public void addFirst(Item x) {
        this.sentinel.next = new Node<Item>(x, sentinel.next);
        this.size++;
    }

    /**
     * 0, insert at sentinel.next;
     **/
    @Override
    public void insert(Item x, int pos) {
        if (pos < 0 || this.size < pos)
            throw new IndexOutOfBoundsException();
        int n = 0;
        Node<Item> iter = this.sentinel;
        for (int i = 0; i < pos; i++)
            iter = iter.next;
        iter.next = new Node<Item>(x, iter.next);
        this.size++;
    }

    @Override
    public Item removeLast() {
        if(this.size==0)
            return null;
        Node<Item> it;
        for (it = this.sentinel; it.next != null; it = it.next) {
            continue;
        }
        return it.item;
    }

    @Override
    public Item removeFirst() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    public void sslist() {
        System.out.println(":D");
    }

    public static void main(String[] args) {
        List<Integer> l = new SLList<>();
        // l.sslist();  that is not ok

    }
}
