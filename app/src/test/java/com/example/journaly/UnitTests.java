package com.example.journaly;

import com.example.journaly.model.journals.JournalEntry;
import com.example.journaly.model.nlp.CloudNlpClient;
import com.example.journaly.model.users.User;
import com.example.journaly.model.users.UserInNeedUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTests {

    @Test
    public void performSentimentAnalysisWithGoogleNLP() {
        CloudNlpClient.getInstance().performSentimentAnalysis("I love you").blockingSubscribe(sentimentAnalysis -> {
            assertTrue(sentimentAnalysis.getScore() > 0);
        }, throwable -> {
            fail(throwable.toString());
        });
    }

    @Test
    public void UserInNeedIsDetected(){
        String userId = "123";
        User user = User.withDefaultValues(userId, null, null);
        List<JournalEntry> entries = new ArrayList<>();


        //list of entries: 2
        entries.add(generateJournalEntry(userId, "a", 2));
        performAnalysisAndExpectChange(user, entries, false, user.getNegativityThreshold(), user.getIdOfLastJournalEntryAnalyzed());

        //list of entries: 2, 0
        entries.add(generateJournalEntry(userId, "b", 0));
        performAnalysisAndExpectChange(user, entries, false, user.getNegativityThreshold(), user.getIdOfLastJournalEntryAnalyzed());

        //list of entries: 2, 0, -20
        entries.add(generateJournalEntry(userId, "c", -20));
        double newNegativityThreshold = user.getNegativityThreshold() * UserInNeedUtils.THRESHOLD_UPDATE_FACTOR_NEGATIVE;
        performAnalysisAndExpectChange(user, entries, true, newNegativityThreshold, "c");
        user.setIdOfLastJournalEntryAnalyzed("c");
        user.setNegativityThreshold(newNegativityThreshold);

        //list of entries: 2, 0, -20, 1, last analyzed = -20
        entries.add(generateJournalEntry(userId, "d", 1));
        performAnalysisAndExpectChange(user, entries, false, user.getNegativityThreshold(), user.getIdOfLastJournalEntryAnalyzed());

        //list of entries: 2, 0, -20, 1, 3, last analyzed = -20
        entries.add(generateJournalEntry(userId, "e", 3));
        performAnalysisAndExpectChange(user, entries, false, user.getNegativityThreshold(), user.getIdOfLastJournalEntryAnalyzed());

        //list of entries: 2, 0, -20, 1, 3, -30 last analyzed = -20
        entries.add(generateJournalEntry(userId, "f", -30));
        newNegativityThreshold = user.getNegativityThreshold() * UserInNeedUtils.THRESHOLD_UPDATE_FACTOR_NEGATIVE;
        performAnalysisAndExpectChange(user, entries, true, newNegativityThreshold, "f");
        user.setIdOfLastJournalEntryAnalyzed("f");
        user.setNegativityThreshold(newNegativityThreshold);
    }

    //performs in need analysis and assert that the response of the analysis matches what is expected
    private void performAnalysisAndExpectChange(User user,
                                            List<JournalEntry> entries,
                                            boolean expectedInNeed,
                                            double expectedNegativityThreshold,
                                            String expectedLastEntryIdAnalyzed){
        UserInNeedUtils.Response a = UserInNeedUtils.isUserInNeed(entries, user);
        assertEquals(a.isInNeed(), expectedInNeed);
        assertEquals(a.getUpdatedNegativityThreshold(), expectedNegativityThreshold, 0.001);
        assertEquals(a.getLastEntryIdAnalyzed(), expectedLastEntryIdAnalyzed);
    }

    private JournalEntry generateJournalEntry(String userId, String entryId, double sentiment){
        JournalEntry entry = new JournalEntry(null, null, 0, true, sentiment, userId, false, null);
        entry.setId(entryId);
        return entry;
    }
}