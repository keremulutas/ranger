package io.smartcat.data.loader;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.smartcat.data.loader.rules.DiscreteRule;
import io.smartcat.data.loader.rules.RangeRuleDate;
import io.smartcat.data.loader.rules.RangeRuleDouble;
import io.smartcat.data.loader.rules.RangeRuleLong;
import io.smartcat.data.loader.rules.Rule;
import io.smartcat.data.loader.rules.SubListRule;
import io.smartcat.data.loader.rules.SubSetRule;
import io.smartcat.data.loader.util.Randomizer;
import io.smartcat.data.loader.util.RandomizerImpl;

/**
 * Class used for building random objects of certain type.
 *
 * @param <T>
 */
public class RandomBuilder<T> {

    private Class<T> objectType;

    private Map<String, Rule<?>> fieldRules = new HashMap<>();

    private int numberOfObjects;

    private Map<String, RandomBuilder<?>> nestedObjectBuilderMap = new HashMap<>();

    private Randomizer random = new RandomizerImpl();

    /**
     * Creates RandomBuilder that is used with passed {@code objectType}.
     *
     * @param objectType type of the object to be built with this builder
     */
    public RandomBuilder(Class<T> objectType) {
        this.objectType = objectType;
    }

    /**
     * Creates RandomBuilder that is used with passed {@code objectType} and randomizer.
     *
     * @param objectType type of the object to be built with this builder
     * @param random Randomizer impl.
     */
    public RandomBuilder(Class<T> objectType, Randomizer random) {
        this.objectType = objectType;
        this.random = random;
    }

    /**
     * Sets the allowed ranges of dates for the field with {@code fieldName}.
     *
     * @param fieldName name of the field in the type <T>
     * @param startDate start of the range (inclusive)
     * @param endDate end of the range (exclusive)
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> randomFromRange(String fieldName, LocalDateTime startDate, LocalDateTime endDate) {
        Instant lower = startDate.toInstant(ZoneOffset.UTC);
        Instant upper = endDate.toInstant(ZoneOffset.UTC);

        fieldRules.put(fieldName, RangeRuleDate.withRanges(Date.from(lower), Date.from(upper)).withRandom(random));

        return this;
    }

    /**
     * Sets the exclusive allowed ranges of dates for the field with {@code fieldName}. That means that only this
     * instance of the builder is allowed to set property with passed name {@code fieldName} from these ranges.
     *
     * @param fieldName name of the field in the type <T>
     * @param startDate start of the range (inclusive)
     * @param endDate end of the range (exclusive)
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> exclusiveRandomFromRange(String fieldName, LocalDateTime startDate, LocalDateTime endDate) {
        Instant lower = startDate.toInstant(ZoneOffset.UTC);
        Instant upper = endDate.toInstant(ZoneOffset.UTC);

        fieldRules.put(fieldName, RangeRuleDate.withRangesX(Date.from(lower), Date.from(upper)).withRandom(random));

        return this;
    }

    /**
     * Sets the allowed ranges of Longs for the field with {@code fieldName}.
     *
     * @param fieldName name of the field in the type <T>
     * @param lower start of the range (inclusive)
     * @param upper end of the range (exclusive)
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> randomFromRange(String fieldName, Long lower, Long upper) {
        fieldRules.put(fieldName, RangeRuleLong.withRanges(lower, upper).withRandom(random));
        return this;
    }

    /**
     * Sets the allowed ranges of Longs for the field with {@code fieldName}. That means that only this instance of the
     * builder is allowed to set property with passed name {@code fieldName} from these ranges.
     *
     * @param fieldName name of the field in the type <T>
     * @param lower start of the range (inclusive)
     * @param upper end of the range (exclusive)
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> exclusiveRandomFromRange(String fieldName, Long lower, Long upper) {
        fieldRules.put(fieldName, RangeRuleLong.withRangesX(lower, upper).withRandom(random));
        return this;
    }

    /**
     * Sets the allowed ranges of Doubles for the field with {@code fieldName}.
     *
     * @param fieldName name of the field in the type <T>
     * @param lower start of the range (inclusive)
     * @param upper end of the range (exclusive)
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> randomFromRange(String fieldName, Double lower, Double upper) {
        fieldRules.put(fieldName, RangeRuleDouble.withRanges(lower, upper).withRandom(random));
        return this;
    }

    /**
     * Sets the allowed ranges of Doubles for the field with {@code fieldName}. That means that only this instance of
     * the builder is allowed to set property with passed name {@code fieldName} from these ranges.
     *
     * @param fieldName name of the field in the type <T>
     * @param lower start of the range (inclusive)
     * @param upper end of the range (exclusive)
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> exclusiveRandomFromRange(String fieldName, Double lower, Double upper) {
        fieldRules.put(fieldName, RangeRuleDouble.withRangesX(lower, upper).withRandom(random));
        return this;
    }

    /**
     * Sets the allowed list of String values for the field with {@code fieldName}.
     *
     * @param fieldName name of the field in the type <T>
     * @param values list of allowed values
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> randomFrom(String fieldName, String... values) {
        fieldRules.put(fieldName, DiscreteRule.newSet(values).withRandom(random));
        return this;
    }

    /**
     * Sets the builder for the property for the field with {@code fieldName}.
     *
     * @param fieldName name of the field in the type <T>
     * @param builder instance of RandomBuilder<?> for the type of the field with passed {@code fieldName}
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> randomWithBuilder(String fieldName, RandomBuilder<?> builder) {
        nestedObjectBuilderMap.put(fieldName, builder);
        return this;
    }

    /**
     * Sets the allowed list of String values for the field with {@code fieldName}. That means that only this instance
     * of the builder is allowed to set property with passed name {@code fieldName} with these values.
     *
     * @param fieldName name of the field in the type <T>
     * @param values list of allowed values
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> exclusiveRandomFrom(String fieldName, String... values) {
        fieldRules.put(fieldName, DiscreteRule.newSetExclusive(values).withRandom(random));
        return this;
    }

    /**
     * Sets the allowed list of String values for the field with {@code fieldName} from which a random sub set should be
     * chosen (including empty set).
     *
     * @param fieldName name of the field in the type <T>
     * @param values list of allowed values
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> randomSubsetFrom(String fieldName, String... values) {
        fieldRules.put(fieldName, SubSetRule.withValues(Arrays.asList(values)).withRandom(random));
        return this;
    }

    /**
     * Sets the allowed list of String values for the field with {@code fieldName} from which a random sub list should
     * be chosen (including empty list).
     *
     * @param fieldName name of the field in the type <T>
     * @param values list of allowed values
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> randomSubListFrom(String fieldName, String... values) {
        fieldRules.put(fieldName, SubListRule.withValues(Arrays.asList(values)).withRandom(random));
        return this;
    }

    /**
     * Sets the builder for the property for the field with {@code fieldName}. The passed builder will be used to create
     * a list of size between passed {@code lower} value (inclusive) and passed {@code upper} value (exclusive).
     *
     * @param fieldName name of the field in the type <T>
     * @param builder builder for the type of the field with passed {@code fieldName}
     * @param lower lower bound of random list size
     * @param upper uppoer bound of random list size
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> randomSubListWithBuilder(String fieldName, RandomBuilder<?> builder, long lower,
            long upper) {
        // TODO as part of issue #37
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Sets the number of entities of type {@code <T>} to be built.
     *
     * @param numberOfObjects number of objects to be built
     * @return RandomBuilder<T>
     */
    public RandomBuilder<T> toBeBuilt(int numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
        return this;
    }

    /**
     * Builds passed {@code numberOfObjects} of entities of type {@code <T>}.
     *
     * @param numberOfObjects number of entities to be built
     * @return List<T>
     */
    public List<T> build(long numberOfObjects) {
        List<T> result = new ArrayList<>();
        for (long i = 1; i <= numberOfObjects; i++) {
            final T randomEntity = buildOne();
            result.add(randomEntity);
        }

        return result;
    }

    private T buildOne() {
        final T instance = initObject();

        fieldRules.keySet().forEach(key -> set(instance, key, fieldRules.get(key).getRandomAllowedValue()));

        nestedObjectBuilderMap.keySet().forEach(key -> set(instance, key, nestedObjectBuilderMap.get(key).buildOne()));

        return instance;
    }

    private T initObject() {
        T instance;
        try {
            instance = objectType.newInstance();
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds set {@code numberOfObjects} of entities of type {@code <T>}.
     *
     * @param numberOfEntitiesToBuild
     * @return List<T>
     */
    public List<T> buildAll() {
        return this.build(numberOfObjects);
    }

    public Map<String, Rule<?>> getFieldRules() {
        return fieldRules;
    }

    public Map<String, RandomBuilder<?>> getNestedObjectBuilderMap() {
        return nestedObjectBuilderMap;
    }

    public void setNestedObjectBuilderMap(Map<String, RandomBuilder<?>> nestedObjectBuilderMap) {
        this.nestedObjectBuilderMap = nestedObjectBuilderMap;
    }

    /**
     * Sets the passed value {@code fieldValue} to field {@code fieldName} on the object {@code object}.
     *
     * @param object which field should be set with value
     * @param fieldName name of the field that should be set with value
     * @param fieldValue the value which should be set to field with {@code fieldName}
     * @return true if value is successfully set. Otherwise, false.
     */
    public static boolean set(Object object, String fieldName, Object fieldValue) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);

                field.setAccessible(true);
                field.set(object, fieldValue);
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

}