package com.gepardec.hogarama.domain.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;

public class Metrics {

    public static final Summary requestLatency = Summary.build()
            .name("hogarama_requests_latency_seconds")
            .help("Request latency in seconds.").labelNames("hogarama_module", "request_type").register();

    public static final Counter requestFailures = Counter.build()
            .name("hogarama_requests_failures_total")
            .help("Request failures.").labelNames("hogarama_module", "request_type").register();

    public static final Counter requestsTotal = Counter.build()
            .name("hogarama_requests_total")
            .help("Requests").labelNames("hogarama_module", "request_type").register();

    public static final Counter exceptionsThrown = Counter.build()
            .name("hogarama_exceptions_total")
            .help("Exceptions thrown in hogajama").labelNames("hogarama_modul", "exception_name", "method").register();

    public static final Counter wateringEventsFired = Counter.build()
            .name("hogarama_watering_events_fired")
            .help("Number of times a watering event has been fired")
            .labelNames("sensor_name", "actor_name").register();

    public static final Gauge sensorValues = Gauge.build()
            .name("hogarama_sensor_value")
            .help("Water Sensor Values.")
            .labelNames("sensor_name", "device_id", "unit_name").register();

    public static final Gauge actorValues = Gauge.build()
            .name("hogarama_actor_value")
            .help("Actor Values.")
            .labelNames("actor_name", "senor_name", "unit_name").register();


}
