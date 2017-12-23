package controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.Configuration;
import play.mvc.Result;
import services.FibonacciService;
import utils.Utilities;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Controller.request;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

/**
 * Created by dmao on 12/23/2017.
 */
@Singleton
public class FibonacciController {
    // Types
    private static class FibonacciInput {
        @JsonProperty @NotNull
        public Long amount;

        @JsonProperty @Nullable
        public Long offset;

        @JsonProperty @Nullable
        public Long limit;
    }

    private static class FibonacciPaginatedOutput {
        @JsonProperty @NotNull
        private final Long amount;
        @JsonProperty @NotNull
        private final ImmutableList<Long> numbers;  //TODO: use Long for prototpye; consider using BigInteger
        @JsonProperty @Nullable
        private final String previous;
        @JsonProperty @Nullable
        private final String next;

        public FibonacciPaginatedOutput(Long amount, ImmutableList<Long> numbers, String previous, String next) {
            this.amount = amount;
            this.numbers = numbers;
            this.previous = previous;
            this.next = next;
        }
    }

    private static class ErrorOutput {
        @JsonProperty @NotNull
        private final String error;

        @JsonProperty @Nullable
        private final String debug;

        public ErrorOutput(String error, String debug) {
            this.error = error;
            this.debug = debug;
        }

        public ErrorOutput(String error) {
            this.error = error;
            this.debug = null;
        }
    }

    // Fields
    private final FibonacciService fibonacciService;
    private final Utilities utilities;
    // max amount of numbers allowed to retrieve (depends on business)
    private final Long maxAmount;

    // Constructor
    @Inject
    FibonacciController(FibonacciService fibonacciService, Utilities utilities, Configuration configuration) {
        this.fibonacciService = fibonacciService;
        this.utilities = utilities;
        this.maxAmount = Preconditions.checkNotNull(configuration.getLong("fibonacci.max_amount"));
    }

    // Action Methods
    public CompletionStage<Result> getFibonacciNumbers() {
        try {
            final FibonacciInput input = utilities.getInputFromQueryParamsOrThrow(FibonacciInput.class);

            if (input.amount < 0) {
                return CompletableFuture.completedFuture(badRequest(utilities.toJson(new ErrorOutput("Query parameter 'amount' shouldn't be a negative number."))));
            } else if (input.amount > this.maxAmount) {
                return CompletableFuture.completedFuture(badRequest(utilities.toJson(new ErrorOutput("Query parameter 'amount' shouldn't be greater than the upper limit " + this.maxAmount + "."))));
            }

            if (input.offset == null && input.limit == null) {
                // get a simple list
                return getFibonacciNumbersWithList(input);
            } else {
                // pagination support
                return getFibonacciNumbersWithPagination(input);
            }
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(badRequest(utilities.toJson(new ErrorOutput("Unrecogonized query parameter(s).", ex.toString()))));
        }
    }

    // Internal Methods
    private CompletionStage<Result> getFibonacciNumbersWithList(FibonacciInput input) {
        return CompletableFuture.completedFuture(ok(utilities.toJson(fibonacciService.getFibonacciSequenceNumbers(0L, input.amount))));
    }

    private CompletionStage<Result> getFibonacciNumbersWithPagination(FibonacciInput input) {
        if (input.offset != null) {
            if (input.offset < 0) {
                return CompletableFuture.completedFuture(badRequest(utilities.toJson(new ErrorOutput("Query parameter 'offset' shouldn't be a negative number."))));
            } else if (input.offset >= input.amount) {
                return CompletableFuture.completedFuture(badRequest(utilities.toJson(new ErrorOutput("Query parameter 'offset' shouldn't be greater than or equal to 'amount'."))));
            }
        }

        if (input.limit != null) {
            if (input.limit < 0) {
                return CompletableFuture.completedFuture(badRequest(utilities.toJson(new ErrorOutput("Query parameter 'limit' shouldn't be a negative number."))));
            } else if (input.limit > input.amount) {
                return CompletableFuture.completedFuture(badRequest(utilities.toJson(new ErrorOutput("Query parameter 'limit' shouldn't be greater than 'amount'."))));
            }
        }

        Long offset = input.offset != null ? input.offset : 0;
        Long limit = input.limit != null ? input.limit : input.amount;

        ImmutableList<Long> numbers = fibonacciService.getFibonacciSequenceNumbers(offset, Math.min(limit, input.amount - offset));

        String prev = null;
        String next = null;
        String endpoint = "http://localhost:9000" + request().path();   // TODO：do not hardcode server host & port
        if (offset > 0 && limit > 0) {  // has previous
            StringBuilder sbPrev = new StringBuilder(endpoint);
            sbPrev.append("?amount=").append(input.amount);
            if (input.offset != null) {
                sbPrev.append("&offset=").append(Math.max(offset - limit, 0));
            }
            if (input.limit != null) {
                sbPrev.append("&limit=").append(input.limit);
            }
            prev = sbPrev.toString();
        }
        if (offset + limit < input.amount  && limit > 0) {  // has next
            StringBuilder sbNext = new StringBuilder(endpoint);
            sbNext.append("?amount=").append(input.amount);
            if (input.offset != null) {
                sbNext.append("&offset=").append(Math.min(offset + limit, Math.max(input.amount - 1, 0)));
            }
            if (input.limit != null) {
                sbNext.append("&limit=").append(input.limit);
            }
            next = sbNext.toString();
        }

        FibonacciPaginatedOutput output = new FibonacciPaginatedOutput(input.amount, numbers, prev, next);
        return CompletableFuture.completedFuture(ok(utilities.toJson(output)));
    }
}
