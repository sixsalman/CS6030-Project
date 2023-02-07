import java.util.*;

/**
 * This program implements two brute force and a branch and bound algorithm to solve the restriction mapping problem,
 * which is also known as the partial digest problem and the turnpike problem, from chapter 4 of the book Introduction
 * to Bioinformatics Algorithms by Jones and Pevzner. It outputs average running times for each of the algorithms along
 * with their return values. In addition to an example from the book, numerous random sets are generated with varying
 * number of elements and the value of the largest element - one at a time. These sets are then partially digested and
 * the digested multisets are passed to the three algorithms to get the original set.
 */
class Code {
    public static void main(String[] args) {
        System.out.println("An example from our book (Introduction to Bioinformatics Algorithms by Jones and " +
                "Pevzner - page 86):");
        List<Integer> x = new ArrayList<>();
        x.add(0);
        x.add(2);
        x.add(4);
        x.add(7);
        x.add(10);

        List<Integer> l = new ArrayList<>();
        l.add(2);
        l.add(2);
        l.add(3);
        l.add(3);
        l.add(4);
        l.add(5);
        l.add(6);
        l.add(7);
        l.add(8);
        l.add(10);

        runAlgorithmsAndPrint(x, l);

        System.out.println("Sets x generated at random and l created using them:");

        for (int n = 2; n <= 6; n++) {
            x = generateRandomX(n, 15);
            runAlgorithmsAndPrint(x, calculateMultiset(new HashSet<>(x)));
        }

        for (int m = 10; m <= 14; m++) {
            x = generateRandomX(7, m);
            runAlgorithmsAndPrint(x, calculateMultiset(new HashSet<>(x)));
        }
    }

    /**
     * Times the algorithms (10 times each) and prints average times along with return values to console
     * @param x a list of unique integers
     * @param l the multiset of pairwise distances corresponding to x
     */
    private static void runAlgorithmsAndPrint(List<Integer> x, List<Integer> l) {
        long stTime, enTime;
        int repeatCount = 10;
        List<Integer> listRes = null;
        Set<List<Integer>> setRes = null;

        System.out.println("n = " + x.size() + "; m = " + Collections.max(l) + "; x = " + x + "; l = " + l);

        stTime = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) listRes = bruteForcePDP(l, x.size());
        enTime = System.nanoTime();
        System.out.println("Brute Force PDP; Average Time Taken: " + (enTime - stTime) / repeatCount +
                " nanoseconds; Return Value: " + listRes);

        stTime = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) listRes = anotherBruteForcePDP(l, x.size());
        enTime = System.nanoTime();
        System.out.println("Another Brute Force PDP; Average Time Taken: " + (enTime - stTime) / repeatCount +
                " nanoseconds; Return Value: " + listRes);

        stTime = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) setRes = partialDigest(l);
        enTime = System.nanoTime();
        System.out.println("Branch and Bound PDP; Average Time Taken: " + (enTime - stTime) / repeatCount +
                " nanoseconds; Return Value: " + setRes);

        System.out.println();
    }

    /**
     * Creates a list of unique and ordered integers corresponding to the received arguments
     * @param n the numbers of integers the return value should contain
     * @param m the maximum integer that can part of the return value
     * @return a list of unique and ordered integers
     */
    private static List<Integer> generateRandomX(int n, int m) {
        List<Integer> x = new ArrayList<>();
        x.add(0);
        x.add(m);

        Random rand = new Random();

        while (x.size() < n) {
            Integer thisNum = rand.nextInt(m - 1) + 1;

            if (!x.contains(thisNum)) x.add(thisNum);
        }

        Collections.sort(x);

        return x;
    }

    /**
     * Implementation of the algorithm mentioned on page 88 of Introduction to Bioinformatics Algorithms Book. Uses all
     * integers between 0 and the largest value in l as possible values in x during its search.
     * @param l multiset of pairwise distances
     * @param n length of the return value x
     * @return an x whose partial digest leads to l
     */
    private static List<Integer> bruteForcePDP(List<Integer> l, int n) {
        int m = Collections.max(l);

        Set<Integer> possibleNumbers = new HashSet<>();
        for (int i = 1; i <= m - 1; i++) possibleNumbers.add(i);

        return bruteForcePDPCommon(l, n, m, possibleNumbers);
    }

    /**
     * Implementation of the algorithm mentioned on page 88 of Introduction to Bioinformatics Algorithms Book. Uses the
     * values in l as possible values in x during its search.
     * @param l multiset of pairwise distances
     * @param n length of the return value x
     * @return an x whose partial digest leads to l
     */
    private static List<Integer> anotherBruteForcePDP(List<Integer> l, int n) {
        int m = Collections.max(l);

        Set<Integer> possibleNumbers = new HashSet<>();
        for (Integer number : l) if (number != m) possibleNumbers.add(number);

        return bruteForcePDPCommon(l, n, m, possibleNumbers);
    }

    /**
     * Performs common actions of bruteForcePDP and anotherBruteForcePDP methods
     * @param l multiset of pairwise distances
     * @param n length of the return value x
     * @param m largest value in l
     * @param possibleNumbers set of possible numbers in the return value in addition to 0 and m
     * @return an x whose partial digest leads to l
     */
    private static List<Integer> bruteForcePDPCommon(List<Integer> l, int n, int m, Set<Integer> possibleNumbers) {
        Set<Set<Integer>> combinations = new HashSet<>();
        addCombinations(combinations, possibleNumbers, n - 2, new HashSet<>());

        for (Set<Integer> combination : combinations) {
            combination.addAll(Arrays.asList(0, m));

            List<Integer> multiset = calculateMultiset(combination);

            if (multiset.equals(l)) {
                List<Integer> combinationList = new ArrayList<>(combination);
                Collections.sort(combinationList);

                return combinationList;
            }
        }

        return null;
    }

    /**
     * Finds all possible combinations of length equal to the received combinationLength that can be reached using the
     * received possibleNumbers
     * @param combinations all possible combinations
     * @param possibleNumbers the set of numbers using which the combinations are created
     * @param combinationLength the length of each combination
     * @param combination current combination - grows recursively
     */
    private static void addCombinations(Set<Set<Integer>> combinations, Set<Integer> possibleNumbers,
                                        int combinationLength, Set<Integer> combination) {
        if (combination.size() == combinationLength) {
            combinations.add(combination);
        } else {
            Set<Integer> updatedPossibleNumbers = new HashSet<>(possibleNumbers);
            Iterator<Integer> updatedPossibleNumbersItr = updatedPossibleNumbers.iterator();

            while (updatedPossibleNumbersItr.hasNext()) {
                Integer number = updatedPossibleNumbersItr.next();
                updatedPossibleNumbersItr.remove();

                Set<Integer> updatedCombination = new HashSet<>(combination);
                updatedCombination.add(number);

                addCombinations(combinations, updatedPossibleNumbers, combinationLength, updatedCombination);
            }
        }
    }

    /**
     * Calculates the multiset of pairwise distances for the received x
     * @param x a set of integers
     * @return the multiset of pairwise distances
     */
    private static List<Integer> calculateMultiset(Set<Integer> x) {
        List<Integer> xList = new ArrayList<>(x);

        List<Integer> multiset = new ArrayList<>();

        for (int i = 0; i < xList.size(); i++) for (int j = i + 1; j < xList.size(); j++)
            multiset.add(Math.abs(xList.get(i) - xList.get(j)));

        Collections.sort(multiset);

        return multiset;
    }

    /**
     * Implementation of the algorithm mentioned on page 90 of Introduction to Bioinformatics Algorithms Book. Performs
     * some steps within itself and uses the place method to reach its return value.
     * @param l multiset of pairwise distances
     * @return all x whose partial digest leads to l
     */
    private static Set<List<Integer>> partialDigest(List<Integer> l) {
        HashSet<Integer> x = new HashSet<>();
        x.add(0);
        x.add(Collections.max(l));

        l.remove(Collections.max(l));

        Set<List<Integer>> allX = new HashSet<>();

        place(l, x, allX);

        l.add(Collections.max(x));
        Collections.sort(l);

        return allX;
    }

    /**
     * Implementation of the algorithm mentioned on page 90 of Introduction to Bioinformatics Algorithms Book. Finds
     * all the x whose partial digest leads to the received multiset l.
     * @param l multiset of pairwise distances - shrinks recursively
     * @param x current candidate set whose partial digest might lead to l - grows recursively
     * @param allX the set to which all satisfying x are added
     */
    private static void place(List<Integer> l, HashSet<Integer> x, Set<List<Integer>> allX) {
        if (l.size() == 0) {
            List<Integer> xList = new ArrayList<>(x);
            Collections.sort(xList);

            allX.add(xList);

            return;
        }

        int y = Collections.max(l);

        if (hasAll(new ArrayList<>(l), differences(y, x))) {
            List<Integer> removed = differences(y, x);

            x.add(y);
            for (Integer intToRem : removed) l.remove(intToRem);

            place(l, x, allX);

            x.remove(y);
            l.addAll(removed);
        }

        if (hasAll(new ArrayList<>(l), differences(Collections.max(x) - y, x))) {
            List<Integer> removed = differences(Collections.max(x) - y, x);

            x.add(Collections.max(x) - y);
            for (Integer intToRem : removed) l.remove(intToRem);

            place(l, x, allX);

            x.remove(Collections.max(x) - y);
            l.addAll(removed);
        }
    }

    /**
     * Calculates the differences between every value of the received set x and integer y
     * @param y an integer
     * @param x a set
     * @return a list containing the result
     */
    private static List<Integer> differences(int y, HashSet<Integer> x) {
        List<Integer> differences = new ArrayList<>();

        for (Integer entry : x) differences.add(Math.abs(entry - y));

        return differences;
    }

    /**
     * Checks whether all elements of the received lTwo exist in lOne - sensitive to number of occurrences
     * @param lOne a list
     * @param lTwo another list
     * @return true if lOne contains all elements of lTwo
     */
    private static boolean hasAll(List<Integer> lOne, List<Integer> lTwo) {
        for (Integer lTwoEntry : lTwo) if (!lOne.remove(lTwoEntry)) return false;

        return true;
    }
}