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
        /*
        return new Iterator<K>() {
            BSTNode<K, V> currNode;

            @Override
            public boolean hasNext() {
                return currNode.left != null || currNode.right != null;
            }

            @Override
            public K next() {
                return null;
            }
        };
         */
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
        // return size(node.left) + size(node.right) + 1; // bad recursive
        return node.size;
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
        // int rightSize = node.right != null ? node.right.size : 0;
        // int leftSize = node.left != null ? node.left.size : 0;
        // node.size = 1 + leftSize + rightSize;
        node.size = 1 + size(node.left) + size(node.right);
        return node;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = Set.of();
        getKeys(root, set);
        return set;
    }


    private void getKeys(BSTNode<K, V> node, Set<K> set) {
        if (node != null) {
            getKeys(node.left, set);
            set.add(node.key);
            getKeys(node.right, set);
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

    /* 非典型递归
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

    /* 递归删除. 在方法里维护被删除节点@nextNode和其父节点@node之间的关系
    *  param
    *  @node 被删除节点可能在其子树的节点
    *  @keyIsLarger 上轮递归中,@key和@node之间的比较结果
    * @key 要删除的key
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