package com.cinesync.soap;

import com.cinesync.config.WebServiceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.XsdSchema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WebServiceConfigTest {

    private final WebServiceConfig config = new WebServiceConfig();

    @Test
    void testXsdSchemaLoads() throws Exception {
        org.springframework.xml.xsd.SimpleXsdSchema schema = (org.springframework.xml.xsd.SimpleXsdSchema) config.cinesyncSchema();
        schema.afterPropertiesSet();
        assertThat(schema).isNotNull();
        assertThat(schema.getTargetNamespace()).isEqualTo("http://cinesync.com/soap");
        assertThat(schema.getSource()).isNotNull();
    }

    @Test
    void testDefaultWsdl11DefinitionConfiguration() {
        XsdSchema schema = config.cinesyncSchema();
        DefaultWsdl11Definition definition = config.defaultWsdl11Definition(schema);
        assertThat(definition).isNotNull();
    }

    @Test
    void testMessageDispatcherServletRegistration() {
        ApplicationContext context = mock(ApplicationContext.class);
        ServletRegistrationBean<MessageDispatcherServlet> registration = config.messageDispatcherServlet(context);

        assertThat(registration).isNotNull();
        assertThat(registration.getUrlMappings()).contains("/ws/*");
    }
}
