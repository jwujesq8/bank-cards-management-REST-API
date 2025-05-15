package com.api.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupChecker implements ApplicationListener<ApplicationReadyEvent> {

    private final Scheduler scheduler;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        scheduler.checkExpiredCards();
    }
}
