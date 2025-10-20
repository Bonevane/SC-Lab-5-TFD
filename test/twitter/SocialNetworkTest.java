/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy for guessFollowsGraph():
     *
     * Partition the inputs as follows:
     * - tweets.size(): 0, 1, >1
     * - number of @-mentions per tweet: 0, 1, >1
     * - case of usernames: lowercase, uppercase, mixed case
     * - self-mentions: user mentions themselves
     * - duplicate mentions: same user mentioned multiple times in one tweet
     * - multiple tweets from same author: repeated mentions, different mentions
     *
     * Cover each part testing coverage.
     */

    /*
     * Testing strategy for influencers():
     *
     * Partition the inputs as follows:
     * - followsGraph.size(): 0, 1, >1
     * - number of followers per user: 0, 1, >1
     * - tied influence: multiple users with same follower count
     * - users with no followers vs users with followers
     *
     * Cover each part testing coverage.
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // Test cases for guessFollowsGraph()

    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());

        assertTrue("expected empty graph", followsGraph.isEmpty());
    }

    @Test
    public void testGuessFollowsGraphNoMentions() {
        Tweet tweet1 = new Tweet(1, "alice", "This is a tweet without mentions", d1);
        Tweet tweet2 = new Tweet(2, "bob", "Another tweet with no mentions", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        assertTrue("expected empty graph or graph with empty sets",
                followsGraph.isEmpty() ||
                        (followsGraph.containsKey("alice") && followsGraph.get("alice").isEmpty()) ||
                        (followsGraph.containsKey("bob") && followsGraph.get("bob").isEmpty()));
    }

    @Test
    public void testGuessFollowsGraphSingleMention() {
        Tweet tweet = new Tweet(1, "alice", "Hey @bob how are you?", d1);
        List<Tweet> tweets = Arrays.asList(tweet);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        assertTrue("alice should follow bob",
                followsGraph.containsKey("alice") &&
                        followsGraph.get("alice").contains("bob"));
    }

    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        Tweet tweet = new Tweet(1, "alice", "Hey @bob and @charlie, check this out!", d1);
        List<Tweet> tweets = Arrays.asList(tweet);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        assertTrue("alice should follow bob",
                followsGraph.containsKey("alice") &&
                        followsGraph.get("alice").contains("bob"));
        assertTrue("alice should follow charlie",
                followsGraph.containsKey("alice") &&
                        followsGraph.get("alice").contains("charlie"));
    }

    @Test
    public void testGuessFollowsGraphMultipleTweetsFromOneUser() {
        Tweet tweet1 = new Tweet(1, "alice", "Hey @bob!", d1);
        Tweet tweet2 = new Tweet(2, "alice", "Hello @charlie and @bob again", d2);
        List<Tweet> tweets = Arrays.asList(tweet1, tweet2);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        assertTrue("alice should follow bob",
                followsGraph.containsKey("alice") &&
                        followsGraph.get("alice").contains("bob"));
        assertTrue("alice should follow charlie",
                followsGraph.containsKey("alice") &&
                        followsGraph.get("alice").contains("charlie"));
    }

    @Test
    public void testGuessFollowsGraphCaseInsensitive() {
        Tweet tweet = new Tweet(1, "ALICE", "Hey @BoB!", d1);
        List<Tweet> tweets = Arrays.asList(tweet);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        // Check that usernames are stored in consistent case (lowercase)
        boolean found = false;
        for (String key : followsGraph.keySet()) {
            if (key.equalsIgnoreCase("alice")) {
                for (String followed : followsGraph.get(key)) {
                    if (followed.equalsIgnoreCase("bob")) {
                        found = true;
                    }
                }
            }
        }
        assertTrue("alice should follow bob (case insensitive)", found);
    }

    @Test
    public void testGuessFollowsGraphNoSelfFollow() {
        Tweet tweet = new Tweet(1, "alice", "Hey @alice, remember this!", d1);
        List<Tweet> tweets = Arrays.asList(tweet);

        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(tweets);

        // alice should not follow herself
        if (followsGraph.containsKey("alice")) {
            for (String followed : followsGraph.get("alice")) {
                assertFalse("user should not follow themselves",
                        followed.equalsIgnoreCase("alice"));
            }
        }
    }

    // Test cases for influencers()

    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertTrue("expected empty list", influencers.isEmpty());
    }

    @Test
    public void testInfluencersSingleUserNoFollowers() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>());

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertTrue("expected list with alice who has no followers",
                influencers.size() == 1 && influencers.get(0).equalsIgnoreCase("alice"));
    }

    @Test
    public void testInfluencersSingleInfluencer() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(Arrays.asList("bob")));

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        // Should contain both alice and bob
        // bob has 1 follower, alice has 0
        assertEquals("should have 2 users", 2, influencers.size());
        assertTrue("bob should be first (most followers)", influencers.get(0).equalsIgnoreCase("bob"));
        assertTrue("alice should be second", influencers.get(1).equalsIgnoreCase("alice"));
    }

    @Test
    public void testInfluencersMultipleInfluencers() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(Arrays.asList("charlie")));
        followsGraph.put("bob", new HashSet<>(Arrays.asList("charlie")));
        followsGraph.put("dave", new HashSet<>(Arrays.asList("bob")));

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        // charlie has 2 followers, bob has 1, dave has 0, alice has 0
        assertTrue("charlie should be first", influencers.get(0).equalsIgnoreCase("charlie"));
        assertTrue("bob should be second", influencers.get(1).equalsIgnoreCase("bob"));
    }

    @Test
    public void testInfluencersTiedInfluence() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alice", new HashSet<>(Arrays.asList("charlie", "dave")));
        followsGraph.put("bob", new HashSet<>(Arrays.asList("eve", "frank")));

        List<String> influencers = SocialNetwork.influencers(followsGraph);

        // charlie, dave, eve, frank all have 1 follower each
        // alice and bob have 0 followers
        assertEquals("should have 6 users total", 6, influencers.size());
        // First 4 should be the ones with 1 follower (order may vary among tied users)
        // Last 2 should be alice and bob (order may vary)
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     *
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */
}