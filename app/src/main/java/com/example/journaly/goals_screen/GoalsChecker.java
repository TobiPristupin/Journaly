package com.example.journaly.goals_screen;

import com.example.journaly.model.journals.JournalEntry;
import com.example.journaly.model.journals.JournalRepository;
import com.example.journaly.model.users.Goal;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Single;


import static java.time.temporal.ChronoUnit.DAYS;

//Description of the algorithm for determining if a goal is met can be found in the {performGoalAnalysis} method.
public class GoalsChecker {

    //make clock a public variable so we can mock it in tests. Ideally this should be private and mocked
    //with a library such as mockito. Or even better, use some type of dependency injection to pass in the clock.
    public static Clock clock = Clock.fixed(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());

    //Determines if a goal is met, given all the posts by a user.
    //A goal is of the form: post X journal entries every Y days. A goal becomes active the moment it is created.
    public static boolean isGoalMet(Goal goal, List<JournalEntry> entriesByUser) {
        /*
        For all the following comments in this method, take as an example a goal that requires the user to post 3 journal entries every 4 days.

        First we must group all journal entries into their different groups. We must create a group for all entries posted between [0, 4) days
        after the creation of the goal (*or the last time this goal was marked as failed), another entry for posts created [4, 8) days, another for posts created [8, 12) days, and so on.
        We then must make sure of two things:
            1) We have the number of required groups: If this goal was created 12 days ago, we need the three groups described above.
              For example, we cannot be missing group [4, 8), since that means the user has no posts in that timeframe.
            2) Every group meets the minimum number of posts. For this example, every group must have at least 3 posts.

        HOWEVER, we must be careful not to analyze the final group. Say the goal above was created 9 days ago, thus we would end up
        creating groups for days [0,4), [4,8), and (8,9]. Since our last interval (8,9] is still ongoing (we have three days left)
        then we cannot yet consider a goal "not met" if the user hasn't posted 3 times in (8,9]. We must wait until the interval completes.

        How will we implement this? Make an array of length: required number of groups. Fill it with zeros. Iterate through all posts,
        and calculate their corresponding group. Add one to the array at the position of the corresponding group. Once finished, the array should
        have no zeros, and all numbers > than the required number of posts per group.

        * We begin counting days since the last time this goal was failed, or the creation date of the goal, if the goal was never failed.
        This is to avoid double failing a goal. For example, if the goal is journal 1 time every 1 day, and we mark it as failed on a certain day,
        we don't want to mark it as failed again on that day, since that will trigger an SMS being sent again.
        */


        List<JournalEntry> entries = entriesByUser.stream()
                .filter(journalEntry -> journalEntry.getCreatedAt() > goal.getCreatedAt()) //only take posts created after the creation of the goal
                .filter(journalEntry -> journalEntry.getCreatedAt() > goal.getLastFailTime()) //only take posts created after the last time this goal was failed
                .collect(Collectors.toList());

        int requiredNumberOfGroups = calculateRequiredNumberOfGroups(goal);

        if (requiredNumberOfGroups == 0){ //we haven't elapse a 3 day timeframe yet, so can't determine if goal is met.
            return true;
        }

        int[] entriesByGroup = new int[requiredNumberOfGroups];

        for (JournalEntry entry : entries){
            entriesByGroup[calculateGroupForEntry(entry, goal)] += 1;
        }

        for (int groupCount : entriesByGroup) {
            if (groupCount < goal.getTimesFrequency()) {
                return false;
            }
        }

        return true;
    }

    private static int calculateRequiredNumberOfGroups(Goal goal) {
        LocalDate startTime;
        if (goal.getLastFailTime() != -1){
            startTime = Instant.ofEpochMilli(goal.getLastFailTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            startTime = Instant.ofEpochMilli(goal.getCreatedAt()).atZone(ZoneId.systemDefault()).toLocalDate();
        }

        LocalDate currentTime = LocalDate.now(clock);
        long daysSinceGoalCreated = ChronoUnit.DAYS.between(startTime, currentTime);
        /*
        By dividing daysSinceGoalCreated with the goal's day frequency, and the casting to int, causing the value to truncate,
        we achieve the desired value. For example, if it has been 11 days since goal creation, and this goal requires user to post
        every 4 days, then (int) (11/4) = 2, which corresponds to groups [0,4), [4,8). The reason we don't include [8, 12) is explained
        in {performGoalAnalysis}
        */
        return (int) (daysSinceGoalCreated / goal.getDaysFrequency());
    }

    private static int calculateGroupForEntry(JournalEntry entry, Goal goal) {
        LocalDate startTime;
        if (goal.getLastFailTime() != -1){
            startTime = Instant.ofEpochMilli(goal.getLastFailTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            startTime = Instant.ofEpochMilli(goal.getCreatedAt()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        LocalDate entryPosted = Instant.ofEpochMilli(entry.getCreatedAt()).atZone(ZoneId.systemDefault()).toLocalDate();

        long daysBetween = DAYS.between(startTime, entryPosted);

        //Explanation of this division can be found in {calculateRequiredNumberOfGroups}
        return (int) (daysBetween / goal.getDaysFrequency());
    }

}
