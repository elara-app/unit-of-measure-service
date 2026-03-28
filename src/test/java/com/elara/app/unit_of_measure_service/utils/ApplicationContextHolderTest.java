package com.elara.app.unit_of_measure_service.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("ApplicationContextHolder")
class ApplicationContextHolderTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("getBean_retrievesExistingBean_returnsBean")
    void getBean_retrievesExistingBean_returnsBean() {
        MessageService messageService = ApplicationContextHolder.getBean(MessageService.class);

        assertThat(messageService).isNotNull();
        assertThat(messageService).isInstanceOf(MessageService.class);
    }

    @Test
    @DisplayName("setApplicationContext_setsContext_contextIsAccessible")
    void setApplicationContext_setsContext_contextIsAccessible() {
        ApplicationContextHolder holder = new ApplicationContextHolder();
        
        holder.setApplicationContext(applicationContext);

        MessageService messageService = ApplicationContextHolder.getBean(MessageService.class);
        assertThat(messageService).isNotNull();
    }

}
