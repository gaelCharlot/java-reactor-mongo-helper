package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@TestConfiguration
@PropertySource("classpath:config/application.properties")
public class TestConfig {

    @Bean
    Clock getClock() {
        return Clock.fixed(LocalDateTime.of(2023, 4, 5, 22, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }

    @Bean
    Jackson2ObjectMapperBuilder builder() {
        return new Jackson2ObjectMapperBuilder();
    }

    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
