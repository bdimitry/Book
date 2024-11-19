package com.bookdb.book;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(BookdbApp.class)
public class BaseAutoTestConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
