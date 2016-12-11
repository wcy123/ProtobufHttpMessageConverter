package org.wcy123;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@ConditionalOnProperty("spring.protobuf.http.message.converter.enable")
@Configuration
@EnableWebMvc
@EnableConfigurationProperties(ConfigurationWithProtobufMessageConverter.Params.class)
@Slf4j
public class ConfigurationWithProtobufMessageConverter extends WebMvcConfigurerAdapter {
    @Autowired
    private Params param;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.extendMessageConverters(converters);
        converters.add(0, protobufHttpMessageConverter());
    }

    @Bean
    public ProtobufMessageConverter protobufHttpMessageConverter() {
        JsonFormat.Printer printer = JsonFormat.printer();
        JsonFormat.Parser parser = JsonFormat.parser();
        if (param.getIgnoreUnknownFields()) {
            parser = parser.ignoringUnknownFields();
        }
        if (param.getIncludeDefaultValueFields()) {
            printer = printer.includingDefaultValueFields();
        }
        if (param.getOmittingInsignificantWhitespace()) {
            printer = printer.omittingInsignificantWhitespace();
        }
        if (param.getPreservingProtoFieldNames()) {
            printer = printer.preservingProtoFieldNames();
        }
        if (param.getOuterClasses().isEmpty()) {
            log.warn(
                    "spring.protobuf.http.message.converter.outerClasses is not defined, no type registry loaded");
        }
        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder()
                .add(param.getOuterClasses().stream().flatMap(this::findClasses)
                        .collect(Collectors.toList()))
                .build();
        printer = printer.usingTypeRegistry(typeRegistry);
        parser = parser.usingTypeRegistry(typeRegistry);
        return new ProtobufMessageConverter(printer, parser);
    }

    private Stream<Descriptors.Descriptor> findClasses(String s) {
        log.info("searching protobuf descriptors for {}", s);
        List<Descriptors.Descriptor> ret = new ArrayList<>();
        try {
            final Class<?> aClass = Class.forName(s);
            final Class<?>[] classes = aClass.getClasses();
            for (int i = 0; i < classes.length; ++i) {
                maybeAddTypeRegistry(ret, classes[i]);
            }
        } catch (ClassNotFoundException e) {
            log.warn("error while searching for {}", s, e);
        }
        return ret.stream();
    }

    private void maybeAddTypeRegistry(List<Descriptors.Descriptor> ret, Class<?> aClass) {
        final Class<?> messageClass = aClass;
        if (Message.class.isAssignableFrom(messageClass)) {
            try {
                final Method getDescriptor = messageClass.getMethod("getDescriptor");
                final Object maybeDescriptor = getDescriptor.invoke(null);
                if (maybeDescriptor instanceof Descriptors.Descriptor) {
                    final Descriptors.Descriptor descriptor =
                            (Descriptors.Descriptor) maybeDescriptor;
                    log.info("{} is loaded for type registry", descriptor.getFullName());
                    ret.add(descriptor);
                }
            } catch (NoSuchMethodException | InvocationTargetException
                    | IllegalAccessException e) {
                log.warn("error while searching for {}", messageClass.getCanonicalName(),
                        e);
            }
        }
    }

    @ConfigurationProperties("spring.protobuf.http.message.converter")
    @Data
    public static class Params {
        private Boolean ignoreUnknownFields = false;
        private Boolean includeDefaultValueFields = false;
        private Boolean omittingInsignificantWhitespace = false;
        private Boolean preservingProtoFieldNames = false;
        private List<String> outerClasses = new ArrayList<>();
    }
}
