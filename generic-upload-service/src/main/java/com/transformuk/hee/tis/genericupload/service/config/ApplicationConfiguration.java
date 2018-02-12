package com.transformuk.hee.tis.genericupload.service.config;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.genericupload.service.config.steps.PeopleSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@ConfigurationProperties(ignoreInvalidFields = true, ignoreUnknownFields = true)
@Component
public class ApplicationConfiguration {

    @Autowired
    PeopleSteps peopleSteps;
    private List<StepConfiguration> steps;

    public List<StepConfiguration> getSteps() {
        return steps;
    }

    public void setSteps(List<StepConfiguration> steps) {
        this.steps = steps;
    }

    @PostConstruct
    public void init() {
        steps = Lists.newArrayList();
        peopleSteps.add(steps);
    }

}
