package com.example.journaly.model.users;

import android.util.Range;

import com.example.journaly.model.journals.JournalEntry;

import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

/* Class that determines if a user X is in need. An analysis is triggered everytime a users posts something.
Algorithm to do so:
1) filter all journal entries to those posted by X
2) Obtain the sentiment of the 1-4 most recent posts that were posted after the last time that user exceeded their threshold. Sum up those values.
    For example: given the following timeline of user posts, and a negativity threshold of -6
        1 -> analyze 1
        1, 2 -> analyze 1 + 2
        1, 2, -2 -> analyze, 1 + 2 + -2
        1, 2, -2, 0 -> analyze 1 + 2 + -2 + 0
        1, 2, -2, 0, 3 -> analyze 2 + -2 + 0 + 3
        1, 2, -2, 0, 3, -20 -> analyze -2 + 0 + 3 + -20
        Note that summing up all those values comes out to -19, which exceeds our threshold of -6, so we mark this user as in need.
        We also decrease their threshold to a smaller value such as -7.
        We also make sure to remember the ID of the post with sentiment -20, since this was the last post
        at the time that the user exceeded their threshold. Next time we analyze, we will only consider posts after -20.

        Now that the user is in need, we won't perform any new analysis.
        Now say a couple of days after, user is removed from "in need". We resume our periodic analysis.

       1, 2, -2, 0, -20, 2 -> analyze 2, since we only consider entries after -20
       1, 2, -2, 0, -20, 2, -12 -> analyze 2 + -12. Sum is -10, we exceed our threshold of -7.
       Mark user as in need, mark post with sentiment -12 as last post at the time user exceeded their threshold.

       In a summary, we always grab the last 1-4 posts, depending on how many are available. But every time we
       mark a user as in need, we will mark the last post at that moment. Next time we perform an analysis,
       we will only consider those posts that come after that post we marked.

       To increase the threshold when multiple positive posts are posted, the exact same logic is used,
       but instead of checking against a negativity threshold of -6 for example, we check against a positivity threshold
       of +6.
* */

public class UserInNeedUtils {

    public static class Response {

        private final boolean inNeed;
        private final double updatedNegativityThreshold;
        private final String lastEntryIdAnalyzed;

        private Response(boolean inNeed, double updatedNegativityThreshold, String lastEntryIdAnalyzed) {
            this.inNeed = inNeed;
            this.updatedNegativityThreshold = updatedNegativityThreshold;
            this.lastEntryIdAnalyzed = lastEntryIdAnalyzed;
        }

        public boolean isInNeed() {
            return inNeed;
        }

        public double getUpdatedNegativityThreshold() {
            return updatedNegativityThreshold;
        }

        public String getLastEntryIdAnalyzed() {
            return lastEntryIdAnalyzed;
        }
    }

    /*
    If a user exceeds their negativity threshold, and thus is considered in need, their new negativity threshold should be updated using the
    formula: old threshold * THRESHOLD_UPDATE_FACTOR_NEGATIVE
    */
    public static final double THRESHOLD_UPDATE_FACTOR_NEGATIVE = 0.8;

    /*
    If a user exceeds their positivity threshold, their new negativity threshold should be updated using the
    formula: old threshold * THRESHOLD_UPDATE_FACTOR_POSITIVE
    */
    public static double THRESHOLD_UPDATE_FACTOR_POSITIVE = 1.2;

    //Limit the bounds of the negativity threshold. If a user repeatedly posts negatively,
    //their negativity threshold will increase until it reaches HIGHEST_NEGATIVITY_THRESHOLD. If a user
    //repeatedly posts positively, their negativity threshold will increase until LOWEST_NEGATIVITY_THRESHOLD
    public static double HIGHEST_NEGATIVITY_THRESHOLD = -1;
    public static double LOWEST_NEGATIVITY_THRESHOLD = User.DEFAULT_NEGATIVITY_THRESHOLD;


    public static Response isUserInNeed(List<JournalEntry> entries, User user){
        if (user.isInNeed()){
            //user is already in need, no need to make him in need, again poor guy :(((
            //if a user is already in need this method should not be called in the first place, so this is
            //just an extra safeguard
            return new Response(false, user.getNegativityThreshold(), user.getIdOfLastJournalEntryAnalyzed());
        }

        List<JournalEntry> entriesToAnalyze = entries.stream()
                .filter(journalEntry -> journalEntry.getUserId().equals(user.getUid()))
                .filter(journalEntry -> journalEntry.getId().compareTo(user.getIdOfLastJournalEntryAnalyzed()) > 0)
                .sorted((o1, o2) -> o1.getId().compareTo(o2.getId()))
                .collect(Collectors.toList());

        int entriesToTakeFromEnd = Math.min(entriesToAnalyze.size(), 4);
        double accumulatedSentiment = entriesToAnalyze.stream()
                .skip(entriesToAnalyze.size() - entriesToTakeFromEnd) //take last 4
                .mapToDouble(value -> value.getSentiment())
                .peek(operand -> System.out.print("analyzed " + operand))
                .peek(operand -> System.out.println())
                .reduce(0.0, Double::sum);

        if (accumulatedSentiment <= user.getNegativityThreshold()){
            double updatedThreshold = Math.min(user.getNegativityThreshold() * THRESHOLD_UPDATE_FACTOR_NEGATIVE, HIGHEST_NEGATIVITY_THRESHOLD);
            String idOfLastAnalyzed = entriesToAnalyze.get(entriesToAnalyze.size() - 1).getId();
            return new Response(true, updatedThreshold, idOfLastAnalyzed);
        }

        if (accumulatedSentiment >= user.getPositivityThreshold()){
            double updatedThreshold = Math.max(user.getNegativityThreshold() * THRESHOLD_UPDATE_FACTOR_POSITIVE, LOWEST_NEGATIVITY_THRESHOLD);
            String idOfLastAnalyzed = entriesToAnalyze.get(entriesToAnalyze.size() - 1).getId();
            return new Response(false, updatedThreshold, idOfLastAnalyzed);
        }


        return new Response(false, user.getNegativityThreshold(), user.getIdOfLastJournalEntryAnalyzed());
    }

}
