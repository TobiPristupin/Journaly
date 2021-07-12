Original App Design Project - README Template
===

# Journaly

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Journaly is a journaling app that allows users to quickly jot down their daily thoughts and feeling, through text and images/video. In the background, journaly uses Sentiment Analysis AI to analyze your text and determine if it contains positive, negative, or neutral emotions. Journaly will then keep track of your mood over time and give you an overview of your mental health. Journaly contains a social aspect also, it lets users follow other users and see their entries (if not set as private).

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Journaling / Mood tracker
- **Mobile:** Mobile first, uses camera
- **Story:** Allow users to quickly journal and use AI to track the user's mood. Connect with other users.
- **Market:** People who already journal, and people who are wanting to get started and looking for a fast and easy alternative.
- **Habit:** Users can journal multiple times per day, and create notifications to remind them to journal. 
- **Scope:** Expand into tracking of other activities, such as fitness, eating habits, etc.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

- User can sign up, log in a logout
- Can create journal entries with date, title, body, and optional image
- Can edit and delete entries
- Data is stored online and synced
- Perform sentiment analysis on text to determine mood
- Social feed that shows entries of other users
- Analysis page that includes:
    - A calendar showing mood distribution over the past month
**Optional Nice-to-have Stories**

- Suggest resources/old entries if mood is low
- Instead of showing posts from all users, allow users to follow other users and show only those
- Add to Analysis page:
    - Writing streak
    - Inspirational quotes
- Notification reminders
- Some type of shaming if users do not meet goals.
- Voice notes for journal entries

### 2. Screen Archetypes

* Login Screen
   * User can sign up, log in
* Home Feed
   * View journal entries
   * Toggle to switch between personal entries and all entries
* Add Screen
    * Add/edit entry
    * Perform sentiment analysis
* Analysis Screen
    * Calendar showing mood distribution over the past month
* "More" Page
    * Page that contains links to quick settings and other options



### 3. Navigation

Bottom navigation bar with four components:
- Home feed
- Floating Action Button to add an entry
- Analysis Page
- More Page

## Wireframes
![](https://i.imgur.com/RJGdfZV.jpg)


## Schema 
Using Firebase's nosql realtime database. One table for entries and one table for users.

### Models
Entry:
*    id: String
*    title: String
*    text: String
*    owner: User
*    private: boolean
*    image: File (optional)

User:
*    id: String
*    username: String
*    password: String
*    followers: List<User> (optional feature)
    
    
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
