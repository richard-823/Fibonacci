package services;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by dmao on 12/23/2017.
 */
@Singleton
public class FibonacciService {

    // Constructor
    @Inject
    public FibonacciService() {
    }

    /**
     * Gets a lit of fibonacci numbers with the specified start and count
     *
     * @param from the start index,
     * @param count amount of numbers
     * @return a list of fibonacci numbers
     */
    public ImmutableList<Long> getFibonacciSequenceNumbers(long from, long count) {
        ImmutableList.Builder<Long> numbers = new ImmutableList.Builder<Long>();

        if (count > 0) {
            Long first = getNthFibonacciSequenceNumberIterative(from);
            numbers.add(first);

            if (count > 1) {
                Long second = getNthFibonacciSequenceNumberIterative(from + 1);
                numbers.add(second);

                for (long i = 2; i < count; i++) {
                    Long current = first + second;
                    numbers.add(current);
                    first = second;
                    second = current;
                }
            }
        }

        return numbers.build();
    }

    @Deprecated
    private long getNthFibonacciSequenceNumberRecursive(long index) {
        if (index == 0 || index == 1) {
            return index;
        }
        // deprecate this method as it's subject to stack overflow and introduced much duplicated calculation
        return (getNthFibonacciSequenceNumberRecursive(index - 1) + getNthFibonacciSequenceNumberRecursive(index - 2));
    }

    private long getNthFibonacciSequenceNumberIterative(long index) {
        if (index == 0 || index == 1) {
            return index;
        }
        long first = 0;
        long second = 1;
        long current = -1;
        for (long i = 2; i <= index; i++) {
            current = first + second;
            first = second;
            second = current;
        }
        return current;
    }

    private long getNthFibonacciSequenceNumberCached(long index) {
        // TODO: introduce cached solution (static or dynamic cache) to achieve even better
        throw new RuntimeException("Not implemented yet.");
    }
}
