package uk.gov.companieshouse.efs.api.events.service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DelayedSubmissionHandlerContext {
    private Map<ServiceLevel, DelayedSubmissionHandlerStrategy> strategyImplementations;

    public enum ServiceLevel {
        STANDARD, SAMEDAY
    }

    @Autowired
    public DelayedSubmissionHandlerContext(
        final Set<DelayedSubmissionHandlerStrategy> implementationSet) {
        implementationSet.forEach(s -> System.out.println(s.getServiceLevel() + "," + s));
        this.strategyImplementations = implementationSet.stream()
            .collect(Collectors.toMap(DelayedSubmissionHandlerStrategy::getServiceLevel,
                Function.identity()));
    }

    public DelayedSubmissionHandlerStrategy getStrategy(final ServiceLevel serviceLevel) {
        return strategyImplementations.get(serviceLevel);
    }
}
