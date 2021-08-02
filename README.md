# UofT Sport & Rec Registrant

Apparently registrator isn't a real word. Used to sign up for events [here](https://recreation.utoronto.ca/Program/GetProducts).

Untested on events that cost money.

## Functionality

- Automatic scheduling: set the event date and time, put in the URL and your credentials, and it will sign up for you automatically when signups become available.
- Automatic re-signup: it can sign up for the same event every week, if desired.
- Sends desktop notifications on successful/failed signups
- Integration with Google Calendar to add events to the user's primary calendar on successful signups

## Security:

- User's UTORid and password are stored in plaintext in `data.json`; this data is not used/sent anywhere besides for signup purposes.  
(You can check the source yourself!)

## Requirements to compile source:

- Java 8 (with JavaFX)
- Roboto fonts from [here](https://fonts.google.com/specimen/Roboto): put them in `resources/fonts`; only `Roboto-Medium.ttf` and `Roboto-Regular.ttf` are required.
- A `credentials.json` file in `resources` folder for Google Calendar integration; this file can be obtained by creating a new project [here](https://console.cloud.google.com/). It requires an OAuth 2.0 scope of `Calendar.Events` under the Google Calendar API; more info can be found [here](https://developers.google.com/calendar/api/guides/auth).

## Credits:

- Icon from [here](https://icon-icons.com/icon/alpha-t-circle/138951)
