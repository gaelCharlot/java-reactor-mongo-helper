package org.niogatori.mongohelper.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        Jackson2ObjectMapperBuilder.json().featuresToDisable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        objectMapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false);
        return objectMapper;
    }

    @Bean
    MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(ZonedDateTimeToDate.INSTANCE);
        converters.add(DateToZonedDateTime.INSTANCE);
        return new MongoCustomConversions(converters);
    }

    @ReadingConverter
    private enum DateToZonedDateTime implements Converter<Date, ZonedDateTime> {
        INSTANCE;

        @Override
        public ZonedDateTime convert(Date date) {
            return ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
        }
    }

    @WritingConverter
    private enum ZonedDateTimeToDate implements Converter<ZonedDateTime, Date> {
        INSTANCE;

        @Override
        public Date convert(ZonedDateTime zonedDateTime) {
            return Date.from(zonedDateTime.toInstant());
        }
    }
}
