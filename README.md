# UofT Sport & Rec Registrant

Apparently registrator isn't a real word. Used to sign up for events [here](https://recreation.utoronto.ca/Program/GetProducts).

Untested on events that cost money.

## Functionality

- Automatic scheduling: set the event date and time, put in the URL and your credentials, and it will sign up for you automatically when signups become available.
- Automatic re-signup: it can sign up for the same event every week, if desired.
- Sends desktop notifications on successful/failed signups

## Security:

- User's UTORid and password are stored in plaintext in `data.json`; this data is not used/sent anywhere besides for signup purposes.  
(You can check the source yourself!)

## Requirements to compile source:

- Java 8 (with JavaFX)
- Roboto fonts from [here](https://fonts.google.com/specimen/Roboto): put them in `resources/fonts`; only `Roboto-Medium.ttf` and `Roboto-Regular.ttf` are required.

## Possible future functionality:

- Integration with Google Calendar to add events on successful signups
- Automatic startup on PC startup/user login

## Credits:

- Icon from [here](https://icon-icons.com/icon/alpha-t-circle/138951)
