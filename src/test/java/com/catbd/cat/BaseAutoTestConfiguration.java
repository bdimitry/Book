package com.catbd.cat;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(CatdbApp.class)
public class BaseAutoTestConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
