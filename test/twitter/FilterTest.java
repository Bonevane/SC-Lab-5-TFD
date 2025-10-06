/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy for writtenBy():
     *
     * Partition the inputs as follows:
     * - Number of tweets: 0, 1, >1
     * - Number of matching tweets: 0, 1, >1
     * - Username case: exact match, different case
     * - Order: verify returned tweets maintain original order
     */

    /*
     * Testing strategy for inTimespan():
     *
     * Partition the inputs as follows:
     * - Number of tweets: 0, 1, >1
     * - Tweet timestamp relative to timespan: before, within, after, at boundaries
     * - Number of matching tweets: 0, >1
     * - Order: verify returned tweets maintain original order
     */

    /*
     * Testing strategy for containing():
     *
     * Partition the inputs as follows:
     * - Number of tweets: 0, 1, >1
     * - Number of words to search: 0, 1, >1
     * - Number of matching tweets: 0, 1, >1
     * - Word case: exact match, different case
     * - Word boundaries: whole word, part of larger word
     * - Order: verify returned tweets maintain original order
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T09:00:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // Tests for writtenBy()

    @Test
    public void testWrittenByMultipleTweetsSingleResult() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "alyssa");

        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }

    @Test
    public void testWrittenByNoResults() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "nonexistent");

        assertTrue("expected empty list", writtenBy.isEmpty());
    }

    @Test
    public void testWrittenByEmptyList() {
        List<Tweet> writtenBy = Filter.writtenBy(new ArrayList<>(), "alyssa");

        assertTrue("expected empty list", writtenBy.isEmpty());
    }

    @Test
    public void testWrittenByCaseInsensitive() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "ALYSSA");

        assertEquals("expected singleton list", 1, writtenBy.size());
        assertTrue("expected list to contain tweet", writtenBy.contains(tweet1));
    }

    @Test
    public void testWrittenByMultipleResults() {
        Tweet tweet3 = new Tweet(3, "alyssa", "another tweet from alyssa", d3);
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3), "alyssa");

        assertEquals("expected two tweets", 2, writtenBy.size());
        assertEquals("expected correct order", 0, writtenBy.indexOf(tweet1));
        assertEquals("expected correct order", 1, writtenBy.indexOf(tweet3));
    }

    @Test
    public void testWrittenByMaintainsOrder() {
        Tweet tweet3 = new Tweet(3, "alyssa", "third tweet", d3);
        Tweet tweet4 = new Tweet(4, "alyssa", "fourth tweet", d4);
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet4, tweet1, tweet3), "alyssa");

        assertEquals("expected three tweets", 3, writtenBy.size());
        assertEquals("expected tweet4 first", tweet4, writtenBy.get(0));
        assertEquals("expected tweet1 second", tweet1, writtenBy.get(1));
        assertEquals("expected tweet3 third", tweet3, writtenBy.get(2));
    }

    // Tests for inTimespan()

    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));

        assertFalse("expected non-empty list", inTimespan.isEmpty());
        assertTrue("expected list to contain tweets", inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, inTimespan.indexOf(tweet1));
    }

    @Test
    public void testInTimespanNoResults() {
        Instant testStart = Instant.parse("2016-02-17T13:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T14:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(testStart, testEnd));

        assertTrue("expected empty list", inTimespan.isEmpty());
    }

    @Test
    public void testInTimespanEmptyList() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(new ArrayList<>(), new Timespan(testStart, testEnd));

        assertTrue("expected empty list", inTimespan.isEmpty());
    }

    @Test
    public void testInTimespanBoundaries() {
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2), new Timespan(d1, d2));

        assertEquals("expected both tweets at boundaries", 2, inTimespan.size());
        assertTrue("expected list to contain both tweets", inTimespan.containsAll(Arrays.asList(tweet1, tweet2)));
    }

    @Test
    public void testInTimespanOutsideTimespan() {
        Tweet tweet3 = new Tweet(3, "user", "early tweet", d4);
        Tweet tweet4 = new Tweet(4, "user", "late tweet", d3);
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet3, tweet4), new Timespan(d1, d2));

        assertTrue("expected empty list, tweets outside timespan", inTimespan.isEmpty());
    }

    @Test
    public void testInTimespanMaintainsOrder() {
        Tweet tweet3 = new Tweet(3, "user", "tweet3", d3);
        Tweet tweet4 = new Tweet(4, "user", "tweet4", d4);
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet2, tweet4, tweet1), new Timespan(d4, d2));

        assertEquals("expected tweet2 first", tweet2, inTimespan.get(0));
        assertEquals("expected tweet4 second", tweet4, inTimespan.get(1));
        assertEquals("expected tweet1 third", tweet1, inTimespan.get(2));
    }

    // Tests for containing()

    @Test
    public void testContaining() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("talk"));

        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }

    @Test
    public void testContainingNoResults() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("nonexistent"));

        assertTrue("expected empty list", containing.isEmpty());
    }

    @Test
    public void testContainingEmptyInputs() {
        List<Tweet> emptyTweets = Filter.containing(new ArrayList<>(), Arrays.asList("talk"));
        List<Tweet> emptyWords = Filter.containing(Arrays.asList(tweet1, tweet2), new ArrayList<>());

        assertTrue("expected empty list for empty tweets", emptyTweets.isEmpty());
        assertTrue("expected empty list for empty words", emptyWords.isEmpty());
    }

    @Test
    public void testContainingCaseInsensitive() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2), Arrays.asList("TALK", "RIVEST"));

        assertEquals("expected two tweets", 2, containing.size());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2)));
    }

    @Test
    public void testContainingWordBoundaries() {
        Tweet tweet3 = new Tweet(3, "user", "talking about stuff", d3);
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet3), Arrays.asList("talk"));

        assertTrue("expected empty list, 'talking' is not 'talk'", containing.isEmpty());
    }

    @Test
    public void testContainingMaintainsOrder() {
        Tweet tweet3 = new Tweet(3, "user", "talk about things", d3);
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet2, tweet1, tweet3), Arrays.asList("talk"));

        assertEquals("expected three tweets", 3, containing.size());
        assertEquals("expected tweet2 first", tweet2, containing.get(0));
        assertEquals("expected tweet1 second", tweet1, containing.get(1));
        assertEquals("expected tweet3 third", tweet3, containing.get(2));
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting
     * (temporarily) your version of Filter with the staff's version.
     * DO NOT strengthen the spec of Filter or its methods.
     *
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Filter, because that means you're testing a stronger
     * spec than Filter says. If you need such helper methods, define them in a
     * different class. If you only need them in this test class, then keep them
     * in this test class.
     */

}