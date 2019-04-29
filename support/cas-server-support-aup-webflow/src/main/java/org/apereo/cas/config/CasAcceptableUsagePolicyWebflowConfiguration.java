package org.apereo.cas.config;

import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.DefaultAcceptableUsagePolicyRepository;
import org.apereo.cas.aup.GroovyAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.AcceptableUsagePolicySubmitAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyVerifyAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasAcceptableUsagePolicyWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casAcceptableUsagePolicyWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.acceptableUsagePolicy", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CasAcceptableUsagePolicyWebflowConfiguration implements CasWebflowExecutionPlanConfigurer {

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicySubmitAction")
    public Action acceptableUsagePolicySubmitAction() {
        return new AcceptableUsagePolicySubmitAction(acceptableUsagePolicyRepository());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "acceptableUsagePolicyVerifyAction")
    public Action acceptableUsagePolicyVerifyAction() {
        return new AcceptableUsagePolicyVerifyAction(acceptableUsagePolicyRepository());
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer() {
        return new AcceptableUsagePolicyWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry.getIfAvailable(), applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "acceptableUsagePolicyRepository")
    @Bean
    @RefreshScope
    public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
        val groovy = casProperties.getAcceptableUsagePolicy().getGroovy();
        if (groovy.getLocation() != null) {
            return new GroovyAcceptableUsagePolicyRepository(groovy.getLocation(), applicationContext);
        }

        return new DefaultAcceptableUsagePolicyRepository(
            ticketRegistrySupport.getIfAvailable(),
            casProperties.getAcceptableUsagePolicy());
    }

    @ConditionalOnMissingBean(name = "casAcceptableUsagePolicyWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer casAcceptableUsagePolicyWebflowExecutionPlanConfigurer() {
        return new CasWebflowExecutionPlanConfigurer() {
            @Override
            public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
                plan.registerWebflowConfigurer(acceptableUsagePolicyWebflowConfigurer());
            }
        };
    }
}