package br.edu.utfpr.fb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    //@Override
    //public void configureMessageBroker(MessageBrokerRegistry registry) {
    //    registry.setApplicationDestinationPrefixes("/app");
    //    registry.enableSimpleBroker("/topic");
    //}
    
    //  @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        // Use this for enabling a Full featured broker like RabbitMQ
        registry.enableStompBrokerRelay("/topic")
                .setRelayHost("35.247.222.114")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest");
    }
}