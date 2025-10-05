package bstmap;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private static class BSTNode<K extends Comparable<K>, V> {
        K key;
        V value;
        // BSTMap<K, V> parent;
        BSTNode<K, V> left;
        BSTNode<K, V> right;
        int size;

        BSTNode(K k, V v, BSTNode<K, V> l, BSTNode<K, V> r) {
            key = k;
            value = v;
            left = l;
            right = r;
            size = 1;
            if (l != null) {
                size += l.size;
            }
            if (r != null) {
                size += r.size;
            }
        }
    }

    private BSTNode<K, V> root;

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<K> spliterator() {
        return Map61B.super.spliterator();
    }

    /* remove all the mapping in the map */
    @Override
    public void clear() {
        this.root = null;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(BSTNode<K, V> node, K key) {
        if (node == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            return containsKey(node.right, key);
        }
        if (cmp < 0) {
            return containsKey(node.left, key);
        }
        return true;
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode<K, V> node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            return get(node.right, key);
        }
        if (cmp < 0) {
            return get(node.left, key);
        }
        return node.value;
    }

    @Override
    public int size() {
        return size(root);
    }

    private int size(BSTNode<K, V> node) {
        if (node == null) {
            return 0;
        }
        return size(node.left) + size(node.right) + 1;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private BSTNode<K, V> put(BSTNode<K, V> node, K key, V value) {
        if (node == null) {
            return new BSTNode<>(key, value, null, null);
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else {
            node.value = value;
        }
        // node.size = 1 + size(node.left) + size(node.right);
        int rightSize = node.right != null ? node.right.size : 0;
        int leftSize = node.left != null ? node.left.size : 0;
        node.size = 1 + leftSize + rightSize;
        return node;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    /* prints out your BSTMap in order of increasing Key. */
    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(BSTNode<K, V> node) {
        if (node == null) {
        } else {
            printInOrder(node.left);
            System.out.println(node.value);
            printInOrder(node.right);
        }
    }
}