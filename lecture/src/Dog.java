public class Dog implements Comparable<Dog>{
    int size;
    String name;
    public Dog(String name, int size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public int compareTo(Dog o) {
        return this.size - o.size;
        /*
        if (otherDog.size > this.size)
            return -1;
        else if (otherDog.size < this.size)
            return 1;
        return 0;
         */
    }
}
