package com.fudan.sw.dsa.project2.bean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class MyKdTree<T> {
    protected static final int defaultBucketSize = 48;
    private final int dimensions;
    private final int bucketSize;
    private NodeKD root;

    /**
     * Constructor with value for dimensions.
     *
     * @param dimensions
     *            - Number of dimensions
     */
    public MyKdTree(int dimensions) {
        this.dimensions = dimensions;
        this.bucketSize = defaultBucketSize;
        this.root = new NodeKD();
    }

    /**
     * Constructor with value for dimensions and bucket size.
     *
     * @param dimensions
     *            - Number of dimensions
     * @param bucket
     *            - Size of the buckets.
     */
    public MyKdTree(int dimensions, int bucket) {
        this.dimensions = dimensions;
        this.bucketSize = bucket;
        this.root = new NodeKD();
    }

    /**
     * Add a key and its associated value to the tree.
     *
     * @param key
     *            - Key to add
     * @param val
     *            - object to add
     */
    public void add(double[] key, T val) {
        root.addPoint(key, val);
    }

    /**
     * Returns all PointKD within a certain range defined by an upper and lower
     * PointKD.
     *
     * @param low
     *            - lower bounds of area
     * @param high
     *            - upper bounds of area
     * @return - All PointKD between low and high.
     */
    @SuppressWarnings("unchecked")
    public List<T> getRange(double[] low, double[] high) {
        Object[] objs = root.range(high, low);
        ArrayList<T> range = new ArrayList<T>(objs.length);
        for(int i=0; i<objs.length; ++i) {
            range.add((T)objs[i]);
        }
        return range;
    }

    /**
     * Gets the N nearest neighbors to the given key.
     *
     * @param key
     *            - Key
     * @param num
     *            - Number of results
     * @return Array of Item Objects, distances within the items are the square
     *         of the actual distance between them and the key
     */
    public ResultHeap<T> getNearestNeighbors(double[] key, int num) {
        ResultHeap<T> heap = new ResultHeap<T>(num);
        root.nearest(heap, key);
        return heap;
    }


    // Internal tree node
    private class NodeKD {
        private NodeKD left, right;
        private double[] maxBounds, minBounds;
        private Object[] bucketValues;
        private double[][] bucketKeys;
        private boolean isLeaf;
        private int current, sliceDimension;
        private double slice;

        private NodeKD() {
            bucketValues = new Object[bucketSize];
            bucketKeys = new double[bucketSize][];
            left = right = null;
            maxBounds = minBounds = null;
            isLeaf = true;
            current = 0;
        }

        // what it says on the tin
        private void addPoint(double[] key, Object val) {
            if(isLeaf) {
                addLeafPoint(key,val);
            } else {
                extendBounds(key);
                if (key[sliceDimension] > slice) {
                    right.addPoint(key, val);
                } else {
                    left.addPoint(key, val);
                }
            }
        }

        private void addLeafPoint(double[] key, Object val) {
            extendBounds(key);
            if (current + 1 > bucketSize) {
                splitLeaf();
                addPoint(key, val);
                return;
            }
            bucketKeys[current] = key;
            bucketValues[current] = val;
            ++current;
        }

        /**
         * Find the nearest neighbor recursively.
         */
        @SuppressWarnings("unchecked")
        private void nearest(ResultHeap<T> heap, double[] data) {
            if(current == 0)
                return;
            if(isLeaf) {
                //IS LEAF
                for (int i = 0; i < current; ++i) {
                    double dist = pointDistSq(bucketKeys[i], data);
                    heap.offer(dist, (T) bucketValues[i]);
                }
            } else {
                //IS BRANCH
                if (data[sliceDimension] > slice) {
                    IsBranch(heap, data, right, left);
                } else {
                    IsBranch(heap, data, left, right);
                }
            }
        }

        private void IsBranch(ResultHeap<T> heap, double[] data, NodeKD right, NodeKD left) {
            right.nearest(heap, data);
            if(left.current == 0)
                return;
            if (!heap.isFull() || regionDistSq(data, left.minBounds, left.maxBounds) < heap.getMaxKey()) {
                left.nearest(heap, data);
            }
        }

        // gets all items from within a range
        private Object[] range(double[] upper, double[] lower) {
            if (bucketValues == null) {
                // Branch
                Object[] tmp = new Object[0];
                if (intersects(upper, lower, left.maxBounds, left.minBounds)) {
                    tmp = left.range(upper, lower);
                }
                if (intersects(upper, lower, right.maxBounds, right.minBounds)) {
                    Object[] tmpr = right.range(upper, lower);
                    if (0 == tmp.length)
                        tmp = tmpr;
                    else if (0 < tmpr.length) {
                        Object[] tmp2 = new Object[tmp.length + tmpr.length];
                        System.arraycopy(tmp, 0, tmp2, 0, tmp.length);
                        System.arraycopy(tmpr, 0, tmp2, tmp.length, tmpr.length);
                        tmp = tmp2;
                    }
                }
                return tmp;
            }
            // Leaf
            Object[] tmp = new Object[current];
            int n = 0;
            for (int i = 0; i < current; ++i) {
                if (contains(upper, lower, bucketKeys[i])) {
                    tmp[n++] = bucketValues[i];
                }
            }
            Object[] tmp2 = new Object[n];
            System.arraycopy(tmp, 0, tmp2, 0, n);
            return tmp2;
        }

        // These are helper functions from here down
        // check if this hyper rectangle contains a give hyper-point
        public boolean contains(double[] upper, double[] lower, double[] point) {
            if (current == 0) return false;
            for (int i = 0; i < point.length; ++i) {
                if (point[i] > upper[i] || point[i] < lower[i]) return false;
            }
            return true;
        }

        // checks if two hyper-rectangles intersect
        public boolean intersects(double[] up0, double[] low0, double[] up1, double[] low1) {
            return IntStream.range(0, up0.length).noneMatch(i -> up1[i] < low0[i] || low1[i] > up0[i]);
        }

        private void splitLeaf() {
            double bestRange = 0;
            for(int i = 0; i < dimensions; ++i) {
                double range = maxBounds[i] - minBounds[i];
                if(range > bestRange) {
                    sliceDimension = i;
                    bestRange = range;
                }
            }

            left = new NodeKD();
            right = new NodeKD();

            slice = (maxBounds[sliceDimension] + minBounds[sliceDimension]) * 0.5;

            for (int i = 0; i < current; ++i) {
                if (bucketKeys[i][sliceDimension] > slice) {
                    right.addLeafPoint(bucketKeys[i], bucketValues[i]);
                } else {
                    left.addLeafPoint(bucketKeys[i], bucketValues[i]);
                }
            }

            bucketKeys = null;
            bucketValues = null;
            isLeaf = false;
        }

        // expands this hyper rectangle
        private void extendBounds(double[] key) {
            if (maxBounds == null) {
                maxBounds = Arrays.copyOf(key, dimensions);
                minBounds = Arrays.copyOf(key, dimensions);
                return;
            }
            for (int i = 0; i < key.length; ++i) {
                if (maxBounds[i] < key[i]) maxBounds[i] = key[i];
                if (minBounds[i] > key[i]) minBounds[i] = key[i];
            }
        }
    }

    /* I may have borrowed these from an early version of Red's tree. I however forget. */
    private static final double pointDistSq(double[] p1, double[] p2) {
        double radLat1 = Math.toRadians(p1[1]);
        double radLat2 = Math.toRadians(p2[1]);
        double angle1 = radLat1 - radLat2;
        double angle2 = Math.toRadians(p1[0] - p2[0]);
        double dis = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(angle1 / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(angle2 / 2), 2)));
        dis *= 6371.393;
        dis = Math.round(dis * 1000);
        return dis;
    }

    private static final double regionDistSq(double[] point, double[] min, double[] max) {
        double d = 0;
        double q = 0;
        for (int i = 0; i < point.length; ++i) {
            if (point[i] > max[i]) {
                d = pointDistSq(point, min);
            } else if (point[i] < min[i]) {
                d = pointDistSq(point, max);
            }
        }
        return d;
    }
}
