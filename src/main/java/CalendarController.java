import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import model.DataModel;
import model.Event;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

public class CalendarController {

    private static final String APPLICATION_NAME = "Registrant for UofT Sport & Rec";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);

    private final Credential credentials;
    private final Calendar service;
    private final String primaryCalendarId;

    CalendarController() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        credentials = getCredentials(HTTP_TRANSPORT);
        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
        primaryCalendarId = service.calendarList().get("primary").getCalendarId();
    }

    public static boolean existsCredentials() {
        return new File(TOKENS_DIRECTORY_PATH).exists();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = CalendarController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void upsertEvent(Event e) throws IOException {
        if (credentials.getExpiresInSeconds() <= 60) {
            credentials.refreshToken();
        }
        Events events = service.events().list("primary")
                .setTimeMin(new DateTime(LocalDateTime.now().minusMonths(6) // look back 6 months
                        .atZone(ZoneOffset.systemDefault())
                        .toEpochSecond() * 1000))
                .setMaxResults(350)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<com.google.api.services.calendar.model.Event> items = events.getItems();
        System.out.println(items.size());
        boolean existsEvent = false;
        for (com.google.api.services.calendar.model.Event item: items) {
            if (item.getId().equals(e.getId())) {
                existsEvent = true;
                break;
            }
        }
        if (existsEvent) updateEvent(e);
        else addEvent(e);
        System.out.println("event added");

        // reset to null to avoid missing updates to an event (if the event signup gets executed twice due to changes)
        DataModel.setLatestSignedUpEvent(null);
    }

    public void addEvent(Event e) throws IOException {
        com.google.api.services.calendar.model.Event googleEvent = configureEvent(e);
        service.events().insert(primaryCalendarId, googleEvent).execute();
    }

    public void updateEvent(Event e) throws IOException {
        com.google.api.services.calendar.model.Event googleEvent = configureEvent(e);
        service.events().update(primaryCalendarId, e.getId(), googleEvent).execute();
    }

    public com.google.api.services.calendar.model.Event configureEvent(Event e) {
        com.google.api.services.calendar.model.Event googleEvent = new com.google.api.services.calendar.model.Event();
        EventDateTime startDateTime = new EventDateTime()
                .setDateTime(new DateTime(LocalDateTime.of(e.getNextDate(), e.getTime())
                        .atZone(ZoneOffset.systemDefault())
                        .toEpochSecond() * 1000))
                .setTimeZone("America/Toronto");
        EventDateTime endDateTime = new EventDateTime()
                .setDateTime(new DateTime(LocalDateTime.of(e.getNextDate(), e.getTime().plusMinutes(45))
                        .atZone(ZoneOffset.systemDefault())
                        .toEpochSecond() * 1000))
                .setTimeZone("America/Toronto");
        return googleEvent
                .setId(e.getId())
                .setStart(startDateTime)
                .setEnd(endDateTime)
                .setSummary(e.getName());
    }
}
