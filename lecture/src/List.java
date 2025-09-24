public interface List<Item> {
    public Item get(int i);
    public Item getLast();
    public Item getFirst();

    public void addLast(Item x);
    public void addFirst(Item x);
    public void insert(Item x, int pos);


    public Item removeLast();
    public Item removeFirst();

    public int size();
}
