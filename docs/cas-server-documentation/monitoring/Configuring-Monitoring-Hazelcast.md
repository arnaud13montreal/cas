---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# Hazelcast Monitoring

Monitor the status and state of a cache backed by Hazelcast.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-monitor" %}

{% include_cached casproperties.html properties="cas.monitor.warn." %}

{% include_cached actuators.html endpoints="health" healthIndicators="hazelcastHealthIndicator" %}