package com.example.journaly;

import com.example.journaly.goals_screen.GoalsChecker;
import com.example.journaly.model.journals.JournalEntry;
import com.example.journaly.model.journals.JournalRepository;
import com.example.journaly.model.users.Goal;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;

public class GoalCheckingTest {

    private String defaultUserId = "randomId";
    private long oneDayMillis = 86400000;


    @Test
    public void goalChecking(){
        List<JournalEntry> entries = new ArrayList<>();

        //goal: journal 2 times every 3 days
        Goal goal = new Goal(2, 3, null, -1, -1, null);
        goal.setCreatedAt(0);

        mockClock(0);

        checkGoalAndExpectResult(goal, entries, true); //entries: []

        entries.add(generateJournalEntry(1));
        checkGoalAndExpectResult(goal, entries, true); //entries: [0] (number represents interval time group it corresponds to)

        mockClock(oneDayMillis * 3 + 1); // Set clock to day 3
        checkGoalAndExpectResult(goal, entries, false); //entries: [0]

        entries.add(generateJournalEntry(2));
        checkGoalAndExpectResult(goal, entries, true); //entries: [0, 0]

        mockClock(oneDayMillis * 6 + 1); // Set clock to day 6
        checkGoalAndExpectResult(goal, entries, false); //entries: [0, 0]

        entries.add(generateJournalEntry(oneDayMillis * 4));
        checkGoalAndExpectResult(goal, entries, false); //entries: [0, 0, 1]

        entries.add(generateJournalEntry(oneDayMillis * 5));
        checkGoalAndExpectResult(goal, entries, true); //entries: [0, 0, 1, 1]
    }

    private void mockClock(long time){ //mock the internal clock of the goal checker class
        GoalsChecker.clock = Clock.fixed(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    private JournalEntry generateJournalEntry(long createdAt){
        JournalEntry entry =  generateJournalEntry(defaultUserId, "randomEntryId", 0);
        entry.setCreatedAt(createdAt);
        return entry;
    }

    private JournalEntry generateJournalEntry(String userId, String entryId, double sentiment){
        JournalEntry entry = new JournalEntry(null, null, 0, 0, true, sentiment, userId, false, null);
        entry.setId(entryId);
        return entry;
    }

    private void checkGoalAndExpectResult(Goal goal, List<JournalEntry> entries, boolean expectedResult){
        boolean result = GoalsChecker.isGoalMet(goal, entries);
        Assert.assertEquals(result, expectedResult);
    }
}
