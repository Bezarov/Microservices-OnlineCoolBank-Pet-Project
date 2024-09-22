package com.example.apigatewaycomponent.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationDeserializer extends JsonDeserializer<Authentication> {

    @Override
    public Authentication deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String principal = node.get("principal").asText();
        String credentials = node.get("credentials").asText();
        JsonNode authoritiesNode = node.get("authorities");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authoritiesNode.forEach(authorityNode ->
                authorities.add(new SimpleGrantedAuthority(authorityNode.get("authority").asText())));

        return new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
    }
}

