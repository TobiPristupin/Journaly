Original App Design Project - README Template
===

# Journaly

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)
3. [Demo] (#Demo)

## Overview
### Description
Journaly is a journaling and community building app. Journaly allows users to write their own journal entries. Once an entry is written, Journaly will use Text Sentiment Analysis AI in the background to determine the overall mood of the journal entry (positive, neutral, negative), and record that in the background. Using that information, Journaly will identify users that have been repeatedly posting negative content, and prompt other users in their social circle to reach out to them, becoming part of their support network.
### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Journaling / Mood tracker
- **Mobile:** Mobile first, uses camera
- **Story:** Allow users to quickly journal and use AI to track the user's mood. Connect with other users. Build a support network
- **Market:** People who already journal, and people who are wanting to get started and looking for a fast and easy alternative.
- **Habit:** Users can journal multiple times per day, and create notifications to remind them to journal. 
- **Scope:** Expand into tracking of other activities, such as fitness, eating habits, etc.

## Product Spec

### User Stories (Required and Optional)


**Features by screen:**

- **Home Feed:** View user’s personal entries as well as entries from users they follow.
- **Create:** Create/edit/delete a new journal entry, and include an optional image. Once entry is finished, it will be analyzed using a Sentiment Analysis API to determine the of the journal entry (positive, neutral, negative), and record that.
- **Profile:** Shows a user’s personal entries, and allows them to add a short bio, profile picture, and contact information (could be social media handles, phone number, etc.)
- **Search:** Search for other users by username, view their profiles, and follow them\
- **Users In Need Feed:** This feed will identify other users that have been repeatedly posting negative content, and display them here. For example, if user A has been posting a lot of negative journals recently, and user B follows them, then user A will show up in user B’s feed. User B will not be able to see the negative content user A posted (due to privacy), but they will have user A’s contact information, so they can reach out and talk to them. An identified user in need will stay in the “users in need” feed of all of their followers until at least one follower reaches out to them. 
- **Goals:** Create a goal of the form “Journal X times every Y days”. Ability to set notification reminders to meet your goal. Users can also add an “Accountability Contact”. When a user fails to meet their goal, an automatic SMS with an embarrassing message will be sent to this contact.

### Users in need detection

Using the Google NLP API, I can perform sentiment analysis on a journal entry. This analysis will provide me with two values, score and magnitude. score is a value from -1.0 to 1.0, indicating the emotion of the text (negative values indicate negative emotions). magnitude is a value from 0 to infinity indicating how strongly that emotion is shown in the text. I determine the overall sentiment of a journal entry as score * magnitude.
  
To identify a user in need, the app must first group all posts by their respective creators. Then, it must add up the sentiments of that users last 5 posts. If that accumulated sentiment is less than a threshold (take for example -10), then I can consider that user as “in need”. If a user is determined “in need”, then that threshold will be slightly increased to -8. This is because a user that has already been identified at risk is more likely to repeat those patterns of behavior, so we must identify that early next time. The threshold can keep decreasing until it reaches -4. Note that thresholds are individual to each user, they all start at -10 but can change over time.
 
Similarly, if a user’s accumulated sentiment over their last 5 posts is very positive, then I can decrease their threshold slightly. For example, if a user has a very negative streak of posts, causing their threshold to move from -10 to -3, but then they begin to post multiple positive posts, their threshold might slowly move back to -8. 

The goal of this method is to allow Journaly to keep personalized tracking of the mood of all of their users, and help them find community when they are at their most vulnerable.


### Navigation

Bottom navigation bar with five components:
- Home feed
- Users in Need Feed 
- Floating Action Button to add an entry
- Search Screen
- Profile Screen
    - Profile screen has tabs for profile info, journals posted, and goal section, as well as access to settings

## Wireframes
![](https://i.imgur.com/RJGdfZV.jpg)


## Schema 
Using Firebase's nosql realtime database. One "table" for entries, one for users, and one for users in need.
Also using Firebase Auth and Firebase Cloud Storage for file hosting.

### Models
Entry:
*    id: String
*    title: String
*    text: String
*    createdAt: long
*    date: long
*    isPublic: boolean
*    userId: String 
*    private: boolean
*    containsImage: boolean
*    image: String url (optional)
*    sentiment: double

User:
*    id: String
*    email: String
*    bio: String
*    contactInfo: String
*    followers: List<String (userId)>
*    following: List<String (userId)>
*    followers: List<User> (optional feature)
*    negativityThreshold: double
*    idOfLastJournalEntryAnalyzed: String
*    isInNeed: boolean
*    goal: Goal (optional)

Goal:
*    reminderDays: List<String>
*    reminderHour: int
*    reminderMinute: int
*    contact: Contact
*    createdAt: long
*    lastFailTime: long

Contact: 
*    name: String
*    phoneNumber: String
*    messageToSend: String

    
### Networking
Google NLP API endpoint: https://language.googleapis.com/v1/documents:analyzeSentiment/
Avatar Generator endpoint: https://avatars.dicebear.com/api/jdenticon/

## Demo
