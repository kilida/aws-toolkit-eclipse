/*
 * Copyright 2015 Amazon Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *    http://aws.amazon.com/apache2.0
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazonaws.eclipse.core.mobileanalytics;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.annotation.Immutable;
import com.amazonaws.eclipse.core.mobileanalytics.internal.Constants;
import com.amazonaws.eclipse.core.mobileanalytics.internal.ToolkitSession;
import com.amazonaws.services.mobileanalytics.model.Event;
import com.amazonaws.util.DateUtils;

@Immutable
public class ToolkitEvent {

    private ToolkitSession session;

    private String eventType;
    private Date timestamp;

    private final Map<String, String> attributes = new HashMap<String, String>();
    private final Map<String, Double> metrics = new HashMap<String, Double>();

    /**
     * @return convert to the low-level {@link Event} object that is accepted
     *         by the Mobile Analytics service API.
     */
    public Event toMobileAnalyticsEvent() {
        Event event = new Event();

        event.setSession(this.session.toMobileAnalyticsSession());

        event.setEventType(this.eventType);
        event.setTimestamp(DateUtils.formatISO8601Date(this.timestamp));
        event.setAttributes(this.attributes);
        event.setMetrics(this.metrics);

        event.setVersion(Constants.MOBILE_ANALYTICS_SERVICE_VERSION);

        return event;
    }

    /**
     * http://docs.aws.amazon.com/mobileanalytics/latest/ug/limits.html
     */
    public boolean isValid() {
        if (session == null) {
            return false;
        }
        if (session.getId() == null) {
            return false;
        }
        if (session.getStartTimestamp() == null) {
            return false;
        }
        if (eventType == null || eventType.isEmpty()) {
            return false;
        }
        if (timestamp == null) {
            return false;
        }
        if (attributes.size() + metrics.size() > Constants.MAX_ATTRIBUTES_AND_METRICS_PER_EVENT) {
            return false;
        }
        for (Entry<String, String> attribute : attributes.entrySet()) {
            if (attribute.getKey().length() > Constants.MAX_ATTRIBUTE_OR_METRIC_NAME_LENGTH) {
                return false;
            }
            if (attribute.getValue() == null) {
                return false;
            }
            if (attribute.getValue().length() > Constants.MAX_ATTRIBUTE_VALUE_LENGTH) {
                return false;
            }
        }
        for (Entry<String, Double> metric : metrics.entrySet()) {
            if (metric.getKey().length() > Constants.MAX_ATTRIBUTE_OR_METRIC_NAME_LENGTH) {
                return false;
            }
            if (metric.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * The constructor is intentionally marked as private; caller should use
     * ToolkitEventBuilder to create event instance
     */
    private ToolkitEvent() {}

    public static class ToolkitEventBuilder {

        private final ToolkitEvent event = new ToolkitEvent();

        public ToolkitEventBuilder(ToolkitSession session) {
            this.event.session = session;
        }

        public ToolkitEventBuilder setEventType(String eventType) {
            this.event.eventType = eventType;
            return this;
        }

        /**
         * If not specified, the timestamp is by default set to the current
         * time.
         */
        public ToolkitEventBuilder setTimestamp(Date timestamp) {
            this.event.timestamp = timestamp;
            return this;
        }

        public ToolkitEventBuilder addAttribute(String name, String value) {
            this.event.attributes.put(name, value);
            return this;
        }

        public ToolkitEventBuilder addMetric(String name, double value) {
            this.event.metrics.put(name, value);
            return this;
        }

        public ToolkitEventBuilder addBooleanMetric(String name, boolean value) {
            this.event.metrics.put(name, value ? 1.0 : 0.0);
            return this;
        }

        public ToolkitEvent build() {
            if (this.event.timestamp == null) {
                this.event.timestamp = new Date();
            }
            return this.event;
        }

    }

}
