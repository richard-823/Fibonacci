package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

import static play.mvc.Controller.request;

/**
 * Created by dmao on 12/23/2017.
 */
@Singleton
public class Utilities {
    private final ObjectMapper mapper;
    private final Validator validator;

    @Inject
    public Utilities(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public <T> T getInputFromQueryParamsOrThrow(Class<T> clazz) {
        ImmutableMap.Builder<String, String> parameters = ImmutableMap.builder();
        request().queryString().forEach((key, vals) -> {
            parameters.put(key, vals[0]);
        });

        return convert(parameters.build(), clazz);
    }

    public <A> A convert(Object json, Class<A> clazz) {
        Preconditions.checkNotNull(json);
        A rv = this.mapper.convertValue(json, clazz);
        Set<ConstraintViolation<A>> violations = validator.validate(rv);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return rv;
    }

    public JsonNode toJson(Object obj) {
        return this.mapper.valueToTree(obj);
    }

    public String getURL() {
        return request().path();
    }
}
