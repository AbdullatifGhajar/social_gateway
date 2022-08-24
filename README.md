# Locus (Social Gateway earlier)

This is just the way Locus was first used. Every feature in the app, such as the time a certain
action takes place, is customizable.

## How it works

Locus is a wrapper app for certain social media apps (see [Supported Apps](#supported-apps)). It means you can open social media apps directly through Locus. The main feature of Locus is the socalled prompts (see [Prompts](#prompts)), which are designed to help users reflect on their social media consumption. In addition, Locus notifies users daily to answer a reflection question about their social media consumption and an EMA about their feelings. Users' responses are securely transmitted to a server (see [Server](#server)), where they are collected for further analysis.

### Supported apps

- Snapchat
- TikTok
- Instagram
- Twitter
- Discord
- Reddit
- Tumblr
- WhatsApp
- Facebook
- Facebook Messenger
- Youtube
- Telegram
- Signal

This list is stored in the app and changing it required updating Locus.

## Collected data

### Prompts

When a user opens an app through Locus, a prompt may appear. Users receive one prompt per day for each app. The prompt may be **answerable**, which means it is basically a question that you can answer. It can also be **unanswerable**, which is simply a tip for the day like *remember your goals for today*. These are stored on the server, so updating these prompts does not require updating the app in the Play Store. To learn more about prompts, see the server's documentation: github.com/AbdullatifGhajar/social_gateway_server

The first prompt of the day is always the same and is as follows:

- *What is your goal for social media today?*

### Reflection questions

In addition to the prompts, users receive a reflection question each evening (around 9 p.m.) that
extends the purpose of the prompts. This asks, for example, whether the above prompts helped reduce social media consumption. Reflection questions are also customisable and are stored on the server.

### EMAs

EMAs are several questions that provide deeper insight into users' feelings and motivations.
It appears at noon every day for 7 days. They are hard-coded on the app, since they don't change. If they have to change from time to time, moving them to the server might be beneficial.

## Server

In the backend, a Python server provides the list of prompts and reflection questions and collects all the users' answers from the app. This means that the prompts can be changed at any time without having to update Locus on the users' phones. In addition, the server authenticates registered users with an email and password combination. For more information, see the server's documentation:
github.com/AbdullatifGhajar/social_gateway_server

## Download & Tutorial

The app is available in the Google App Store for Android devices with an Android version above 7.0 (Nougat). Here is the link to the locus homepage, including a tutorial:
hpi.com/baudisch/projects/neo4j/api/locus

## Privacy

This app is part of a research study at the College of Washington in Seattle, WA, USA. Only
registered study users can use the app. Data is collected only from registered users and
stored on a secure server. All registered users will receive a consent form that provides full
details about the data collected, how it will be stored, and how it will be used.
