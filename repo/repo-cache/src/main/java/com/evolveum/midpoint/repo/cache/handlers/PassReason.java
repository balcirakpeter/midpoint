/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.repo.cache.handlers;

import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.RetrieveOption;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CacheUseCategoryTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CacheUseTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CaseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.evolveum.midpoint.repo.cache.handlers.PassReason.PassReasonType.*;
import static com.evolveum.midpoint.repo.cache.handlers.PassReason.PassReasonType.UNSUPPORTED_OPTION;
import static com.evolveum.midpoint.schema.GetOperationOptions.createRetrieve;

/**
 * Reason why an operation request passes a cache.
 */
final class PassReason {

    /**
     * Categorization of why a request passes the cache.
     */
    private final PassReasonType type;

    /**
     * More detailed information on the pass reason. For the time being it is provided as plain text.
     */
    private final String comment;

    enum PassReasonType {
        NOT_CACHEABLE_TYPE, MULTIPLE_OPTIONS, NON_ROOT_OPTIONS, UNSUPPORTED_OPTION, INCLUDE_OPTION_PRESENT, ZERO_STALENESS_REQUESTED
    }

    private PassReason(PassReasonType type) {
        this.type = type;
        this.comment = null;
    }

    private PassReason(PassReasonType type, String comment) {
        this.type = type;
        this.comment = comment;
    }

    /**
     * Main entry point. By looking at situation we determine if there's a reason to pass the cache.
     */
    @Nullable
    static PassReason determine(Collection<SelectorOptions<GetOperationOptions>> options, Class<?> objectType) {
        if (alwaysNotCacheable(objectType)) {
            return new PassReason(NOT_CACHEABLE_TYPE);
        }
        if (options == null || options.isEmpty()) {
            return null;
        }
        if (options.size() > 1) {
            return new PassReason(MULTIPLE_OPTIONS);
        }
        SelectorOptions<GetOperationOptions> selectorOptions = options.iterator().next();
        if (!selectorOptions.isRoot()) {
            return new PassReason(NON_ROOT_OPTIONS);
        }
        if (selectorOptions.getOptions() == null) {
            return null;
        }
        Long staleness = selectorOptions.getOptions().getStaleness();
        if (staleness != null && staleness == 0) {
            return new PassReason(ZERO_STALENESS_REQUESTED);
        }
        GetOperationOptions cloned = selectorOptions.getOptions().clone();

        // Eliminate harmless options
        cloned.setAllowNotFound(null);
        cloned.setExecutionPhase(null);
        cloned.setReadOnly(null);
        cloned.setNoFetch(null);
        cloned.setPointInTimeType(null);            // This is not used by repository anyway.
        // We know the staleness is not zero, so caching is (in principle) allowed.
        // More detailed treatment of staleness is not yet available.
        cloned.setStaleness(null);
        if (cloned.equals(GetOperationOptions.EMPTY)) {
            return null;
        }
        if (cloned.equals(createRetrieve(RetrieveOption.INCLUDE))) {
            if (SelectorOptions.isRetrievedFullyByDefault(objectType)) {
                return null;
            } else {
                return new PassReason(INCLUDE_OPTION_PRESENT);
            }
        }
        return new PassReason(UNSUPPORTED_OPTION, cloned.toString());
    }

    /**
     * Main reason of cache pass:
     *
     * Tasks are usually rapidly changing.
     *
     * Cases are perhaps not changing that rapidly but these are objects that are used for communication of various parties;
     * so - to avoid having stale data - we skip caching them altogether.
     */
    static boolean alwaysNotCacheable(Class<?> type) {
        return type.equals(TaskType.class) || type.equals(CaseType.class);
    }

    CacheUseTraceType toCacheUse() {
        return new CacheUseTraceType()
                .category(CacheUseCategoryTraceType.PASS)
                .comment(type + (comment != null ? ": " + comment : ""));
    }
}
