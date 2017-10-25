package com.jebussystems.leaguescheduler.entities;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Serializer {

	public static final Gson GSON;

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Date.class, new DateAdapter());
		GSON = builder.create();
	}

	private static class DateAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {
		static private final DateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			String date = ISO8601FORMAT.format(src);
			JsonElement element = new JsonPrimitive(date);
			return element;
		}

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(json.getAsString());
			return calendar.getTime();
		}
	}
}
