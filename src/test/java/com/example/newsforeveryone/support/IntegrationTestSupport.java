package com.example.newsforeveryone.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
public class IntegrationTestSupport extends IntegrationTestContainerSupport {

}

