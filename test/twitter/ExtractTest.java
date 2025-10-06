/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy for getTimespan():
     *
     * Partition the inputs as follows:
     * - Number of tweets: 0, 1, 2, >2
     * - Temporal ordering: ascending, descending, unordered, all same time
     * - Time differences: zero (same timestamp), small, large
     *
     * Cover each part testing coverage.
     */

    /*
     * Testing strategy for getMentionedUsers():
     *
     * Partition the inputs as follows:
     * - Number of tweets: 0, 1, >1
     * - Mentions per tweet: 0, 1, >1
     * - Mention format:
     *   - Valid: @username at start, middle, end of text
     *   - Invalid: email addresses (user@domain.com), @username with invalid chars before/after
     * - Username case: lowercase, uppercase, mixed case
     * - Duplicate mentions: same user mentioned multiple times (same tweet, different tweets)
     * - Username validity: valid characters (letters, digits, underscore), invalid characters
     *
     * Cover each part testing coverage.
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

    // Tests for getTimespan()

    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }

    @Test
    public void testGetTimespanSingleTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }

    @Test
    public void testGetTimespanMultipleTweetsUnordered() {
        Tweet tweet3 = new Tweet(3, "user3", "test tweet", d3);
        Tweet tweet4 = new Tweet(4, "user4", "another tweet", d4);

        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, tweet4, tweet1, tweet3));

        assertEquals("expected start", d4, timespan.getStart());
        assertEquals("expected end", d3, timespan.getEnd());
    }

    @Test
    public void testGetTimespanMultipleTweetsSameTime() {
        Tweet tweetA = new Tweet(5, "userA", "same time tweet 1", d1);
        Tweet tweetB = new Tweet(6, "userB", "same time tweet 2", d1);

        Timespan timespan = Extract.getTimespan(Arrays.asList(tweetA, tweetB));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }

    @Test
    public void testGetTimespanDescendingOrder() {
        Tweet tweet3 = new Tweet(3, "user3", "test tweet", d3);

        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet3, tweet2, tweet1));

        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d3, timespan.getEnd());
    }

    // Tests for getMentionedUsers()

    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));

        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersEmptyList() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(new ArrayList<>());

        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersSingleMention() {
        Tweet tweet = new Tweet(10, "user", "Hey @alice how are you?", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("expected one user", 1, mentionedUsers.size());
        assertTrue("expected alice mentioned",
                mentionedUsers.contains("alice") || mentionedUsers.contains("ALICE"));
    }

    @Test
    public void testGetMentionedUsersMultipleMentions() {
        Tweet tweet = new Tweet(11, "user", "@alice and @bob should meet @charlie", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("expected three users", 3, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersCaseInsensitive() {
        Tweet tweet1 = new Tweet(12, "user1", "@Alice rocks", d1);
        Tweet tweet2 = new Tweet(13, "user2", "@ALICE is great", d2);
        Tweet tweet3 = new Tweet(14, "user3", "@alice is awesome", d3);

        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1, tweet2, tweet3));

        assertEquals("expected one unique user", 1, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersEmailNotMention() {
        Tweet tweet = new Tweet(15, "user", "Email me at alice@mit.edu", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertTrue("expected empty set, email should not be mention", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersValidBoundaries() {
        Tweet tweet = new Tweet(16, "user", "Hi @user123 and (@alice) see @bob!", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("expected three users", 3, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersAtEnd() {
        Tweet tweet = new Tweet(17, "user", "Thanks @alice", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("expected one user", 1, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersAtStart() {
        Tweet tweet = new Tweet(18, "user", "@alice you're great", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("expected one user", 1, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersWithUnderscore() {
        Tweet tweet = new Tweet(19, "user", "Hey @user_name check this", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("expected one user", 1, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersDuplicatesInSameTweet() {
        Tweet tweet = new Tweet(20, "user", "@alice and @bob and @alice again", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("expected two unique users", 2, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersMultipleTweets() {
        Tweet tweet1 = new Tweet(21, "user1", "@alice hello", d1);
        Tweet tweet2 = new Tweet(22, "user2", "@bob hi there", d2);

        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1, tweet2));

        assertEquals("expected two users", 2, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersInvalidCharactersBefore() {
        Tweet tweet = new Tweet(23, "user", "test@alice is email not mention", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    @Test
    public void testGetMentionedUsersWithHyphenAfter() {
        Tweet tweet = new Tweet(24, "user", "check @alice-bob for updates", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        // Hyphen is not a valid username character, so @alice should be recognized
        assertEquals("expected one user", 1, mentionedUsers.size());
    }

    @Test
    public void testGetMentionedUsersComplexEmail() {
        Tweet tweet = new Tweet(25, "user", "Contact alice@company.com for info", d1);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet));

        assertTrue("expected empty set, email with letter before @ should not be mention",
                mentionedUsers.isEmpty());
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     *
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}