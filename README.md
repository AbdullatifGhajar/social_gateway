# Locus (Social Gateway earlier)

This is just the way Locus was first used. Every feature in the app, such as the time a certain
action takes place, is customizable.

## How it works

Locus is a wrapper app for certain social media apps (see Supported Apps). This means that you can
use it to open apps for social media. The main feature of Locus is the prompts (see Prompts), which
are designed to help users reflect on their media consumption. In addition, Locus notifies users
daily to answer a reflection question about their social media consumption and an EMA about their
feelings. Users' responses are securely transmitted to a server (see Server), where they are
collected for further analysis.

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

## Collected data

### Prompts

When you open apps through Locus, a prompt may appear. Users receive one prompt per day for each
app. The prompt may be a question that you can answer, or simply a tip for the day. These are stored
on the server, so updating these prompts does not require updating the app in the Play Store. The
first prompt of the day is always the same and is as follows:

- What is your goal for social media today?

### Reflection questions

In addition to the prompts, users receive a reflection question each evening (around 9 p.m.) that
focuses on their consumption for the day. This asks, for example, whether the above prompts helped
reduce social media consumption.

### EMAs

EMAs are several questions that provide deeper insight into users' feelings and motivations.
It appears at noon every day for 7 days.

## Server

In the backend, a Python server collects all the users' answers. It also provides the list of
prompts used in Locus. This means that the prompts can be changed at any time without having to
update Locus on the users' phones. In addition, the server authenticates registered
users with an email password combination. For more information, see the server's README:
github.com/AbdullatifGhajar/social_gateway_server

## Download & Instructions

The app is available in the Google App Store for Android devices with an Android version above 7.0 (
Nougat). Here is the link to the locus homepage, including a tutorial:
hpi.com/baudisch/projects/neo4j/api/locus

## Privacy

This app is part of a research study at the College of Washington in Seattle, WA, USA. Only
registered study users can use the app. Data is collected only from registered users and
stored on a secure server. All registered users will receive a consent form that provides full
details about the data collected, how it will be stored, and how it will be used.