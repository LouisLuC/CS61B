package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private static class BSTNode<K extends Comparable<K>, V> {
        K key;
        V value;
        BSTNode<K, V> left;
        BSTNode<K, V> right;
        int size;

        BSTNode(K k, V v) {
            key = k;
            value = v;
            size = 1;
        }
    }

    private BSTNode<K, V> root;

    private class BSTIterator implements Iterator<K> {
        // Cast problem

        LinkedList<BSTNode<K,V>> list;

        BSTIterator() {
            list = new LinkedList<>();
            list.addLast(root);
        }

        @Override
        public boolean hasNext() {
            return !list.isEmpty();
        }

        @Override
        public K next() {
            BSTNode<K,V> node = list.removeFirst();
            if (node.left!=null) {
                list.addLast(node.left);
            }
            if (node.left!=null) {
                list.addLast(node.right);
            }
            return node.key;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTIterator();
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
        // return size(node.left) + size(node.right) + 1; // bad recursive
        return node.size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    /**
     * Recursively helper method for put interface
     *
     * @param node the root to put the key and value on it or its sub-tree
     */
    private BSTNode<K, V> put(BSTNode<K, V> node, K key, V value) {
        if (node == null) {
            return new BSTNode<>(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else {
            node.value = value;
        }
        node.size = 1 + size(node.left) + size(node.right);
        return node;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        addKeysInSet(root, set);
        return set;
    }

    private void addKeysInSet(BSTNode<K, V> node, Set<K> set) {
        if (node != null) {
            addKeysInSet(node.left, set);
            set.add(node.key);
            addKeysInSet(node.right, set);
        }
    }

    @Override
    public V remove(K key) {
        V ret = get(key);
        if (ret != null) {
            root = remove(root, key);
        }
        return ret;
    }

    private BSTNode<K, V> remove(BSTNode<K, V> node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = remove(node.right, key);
            node.size = 1 + size(node.right) + size(node.left);
        } else if (cmp < 0) {
            node.left = remove(node.left, key);
            node.size = 1 + size(node.right) + size(node.left);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            BSTNode<K, V> minInRight = findMin(node.right);
            node.key = minInRight.key;
            node.value = minInRight.value;

            removeMin(node.right);
        }
        return node;
    }

    private BSTNode<K, V> removeMin(BSTNode<K, V> node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = removeMin(node.left);
        node.size = 1 + size(node.left) + size(node.right);
        return node;
    }

    private BSTNode<K, V> findMin(BSTNode<K, V> node) {
        if (node.left == null) {
            return node;
        }
        return findMin(node.left);
    }

    private BSTNode<K, V> findMax(BSTNode<K, V> node) {
        if (node.right == null) {
            return node;
        }
        return findMax(node.right);
    }

    /* non-typical recursive
    @Override
    public V remove(K key) {
        int cmp = key.compareTo(root.key);
        if (cmp == 0) {
            V ret = root.value;
            if (root.right != null) {
                setValueOfSmallestRight(root);
            } else if (root.left != null) {
                setValueOfLargestLeft(root);
            } else {
                root = null;
            }
            return ret;
        }
        return remove(root, key, cmp > 0);
    }

    private void setValueOfSmallestRight(BSTNode<K, V> node) {
        BSTNode<K, V> currNode = node.right;
        BSTNode<K, V> parent = node;
        while (currNode.left != null) {
            parent.size--;
            parent = currNode;
            currNode = currNode.left;
        }
        parent.size--;
        node.value = currNode.value;
        node.key = currNode.key;
        if (parent == node) {
            parent.right = currNode.right;
        } else {
            parent.left = currNode.right;
        }
    }

    private void setValueOfLargestLeft(BSTNode<K, V> node) {
        BSTNode<K, V> currNode = node.left;
        BSTNode<K, V> parent = node;
        while (currNode.right != null) {
            parent.size--;
            parent = currNode;
            currNode = currNode.right;
        }
        parent.size--;
        node.value = currNode.value;
        node.key = currNode.key;
        if (parent == node) {
            parent.left = currNode.left;
        } else {
            parent.right = currNode.left;
        }
    }

    /*
    *  Recursively remove. Maintain the connection of 'node' and its child
    *  @param node 被删除节点可能在其子树的节点
    *  @param keyIsLarger 上轮递归中,@key和@node之间的比较结果
    private V remove(BSTNode<K, V> node, K key, boolean keyIsLarger) {
        V ret;
        BSTNode<K, V> nextNode = keyIsLarger ? node.right : node.left;
        if (nextNode == null) {
            return null;
        }
        int cmp = key.compareTo(nextNode.key);
        if (cmp == 0) {
            ret = nextNode.value;
            if (nextNode.left == null && nextNode.right == null) {
                // no child
                if (keyIsLarger) node.right = null;
                else node.left = null;
            } else if (nextNode.right != null) {
                setValueOfSmallestRight(nextNode);
            } else {
                setValueOfLargestLeft(nextNode);
            }
        } else {
            ret = remove(nextNode, key, cmp > 0);
        }
        if (ret != null) {
            node.size--;
        }
        return ret;
    }
    */

    /**
     * Remove the map where its key and value identical to both given key and value
     *
     * @param key   key to remove
     * @param value value to remove
     * @return the value had been removed. return null if there is no such map
     */
    @Override
    public V remove(K key, V value) {
        V ret = get(key);
        if (ret.equals(value)) {
            root = remove(root, key, value);
        } else {
            ret = null;
        }
        return ret;
    }

    private BSTNode<K, V> remove(BSTNode<K, V> node, K key, V value) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = remove(node.right, key, value);
            node.size = 1 + size(node.right) + size(node.left);
        } else if (cmp < 0) {
            node.left = remove(node.left, key, value);
            node.size = 1 + size(node.right) + size(node.left);
        } else {
            /*
            if (!node.value.equals(value)) { // always false
                return node;
            }
            */
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            BSTNode<K, V> minInRight = findMin(node.right);
            node.key = minInRight.key;
            node.value = minInRight.value;

            removeMin(node.right);
        }
        return node;
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