package model;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class Serializer {

    public Gson getSerializer() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Event.class, new EventSerializer());
        builder.serializeNulls();
        return builder.create();
    }

    public void writeCredentials(JsonWriter writer, String username, String password) throws IOException {
        writer.name("credentials");
        writer.beginObject();
        writer.name("username").value(username);
        writer.name("password").value(password);
        writer.endObject();
    }

    public void writeEvents(JsonWriter writer, Gson g, List<Event> events) throws IOException {
        writer.name("events");
        writer.beginArray();
        for (Event e: events) writer.jsonValue(g.toJson(e));
        writer.endArray();
    }

    private static class EventSerializer implements JsonSerializer<Event>, JsonDeserializer<Event> {

        @Override
        public Event deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject obj = jsonElement.getAsJsonObject();
            String id = obj.get("uuid").getAsString();
            String name = obj.get("name").isJsonNull() ? null : obj.get("name").getAsString();
            String url = obj.get("url").isJsonNull() ? null : obj.get("url").getAsString();
            LocalDate date = obj.get("date").isJsonNull() ? null : LocalDate.parse(obj.get("date").getAsString());
            LocalTime time = obj.get("time").isJsonNull() ? null : LocalTime.parse(obj.get("time").getAsString());
            boolean recurring = obj.get("recurring").getAsBoolean();
            boolean nextSignupExecuted = obj.get("nextSignupExecuted").getAsBoolean();

            return new Event(name, url, date, time, recurring, nextSignupExecuted, id);
        }

        @Override
        public JsonElement serialize(Event event, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject obj = new JsonObject();
            obj.add("uuid", new JsonPrimitive(event.getId()));
            obj.add("name", event.getName() == null ? null : new JsonPrimitive(event.getName()));
            obj.add("url", event.getUrl() == null ? null : new JsonPrimitive(event.getUrl()));
            obj.add("date", event.getNextDate() == null ? null : new JsonPrimitive(event.getNextDate().toString()));
            obj.add("time", event.getTime() == null ? null : new JsonPrimitive(event.getTime().toString()));
            obj.add("recurring", new JsonPrimitive(event.isRecurring()));
            obj.add("nextSignupExecuted", new JsonPrimitive(event.isNextSignupExecuted()));
            return obj;
        }
    }
}
