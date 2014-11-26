package hhl3eq.virginia.edu.soundlights;

import java.util.ArrayList;

/**
 * Created by lovo-h on 11/25/2014.
 */
public class MergeSort {
    public ArrayList<Double> mergeSort(ArrayList<Double> a) {
            /* returns a sorted arrayList<doubles> */
        if (a.size() <= 1) {
            return a;
        }
        ArrayList<Double> firstHalf = new ArrayList<Double>();
        ArrayList<Double> secondHalf = new ArrayList<Double>();
        for (int i = 0; i < a.size() / 2; i++) {
            firstHalf.add(a.get(i));
        }
        for (int i = a.size() / 2; i < a.size(); i++) {
            secondHalf.add(a.get(i));
        }
        return merge(mergeSort(firstHalf), mergeSort(secondHalf));
    }

    public ArrayList<Double> merge(ArrayList<Double> l1, ArrayList<Double> l2) {
            /* merges to arrayList<doubles> and returns a sorted arrayList<doubles> */
        if (l1.size() == 0) {
            return l2;
        }
        if (l2.size() == 0) {
            return l1;
        }
        ArrayList<Double> result = new ArrayList<Double>();
        Double nextElement;
        if (l1.get(0) > l2.get(0)) {
            nextElement = l2.get(0);
            l2.remove(0);
        } else {
            nextElement = l1.get(0);
            l1.remove(0);
        }
        result.add(nextElement);
        result.addAll(merge(l1, l2));
        return result;
    }
}
