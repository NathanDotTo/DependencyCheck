/*
 * This file is part of dependency-check-core.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright (c) 2023 Jeremy Long. All Rights Reserved.
 */
package org.owasp.dependencycheck.data.update.nvd.api;

import io.github.jeremylong.openvulnerability.client.nvd.DefCveItem;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.owasp.dependencycheck.data.nvd.ecosystem.CveEcosystemMapper;
import org.owasp.dependencycheck.data.nvdcve.CveDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores a collection of NVD CVE Data from the NVD API into the database.
 *
 * @author Jeremy Long
 */
public class NvdApiProcessor implements Callable<NvdApiProcessor> {

    /**
     * The Logger for use throughout the class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NvdApiProcessor.class);
    /**
     * A reference to the database.
     */
    private final CveDB cveDB;
    /**
     * The collection of NVD API data to add to the database.
     */
    private Collection<DefCveItem> data;
    /**
     * Reference to the CVE Ecosystem Mapper object.
     */
    private final CveEcosystemMapper mapper = new CveEcosystemMapper();
    /**
     * The start time.
     */
    private final long startTime;
    /**
     * The end time.
     */
    private long endTime = 0;

    /**
     * Create a new processor to put the NVD data into the database.
     *
     * @param cveDB a reference to the database
     * @param data the data to add to the database
     * @param startTime the start time of the update process.
     */
    public NvdApiProcessor(final CveDB cveDB, Collection<DefCveItem> data, long startTime) {
        this.cveDB = cveDB;
        this.data = data;
        this.startTime = startTime;
    }

    /**
     * Create a new processor to put the NVD data into the database.
     *
     * @param cveDB a reference to the database
     * @param data the data to add to the database
     */
    public NvdApiProcessor(final CveDB cveDB, Collection<DefCveItem> data) {
        this(cveDB, data, System.currentTimeMillis());
    }

    @Override
    public NvdApiProcessor call() throws Exception {
        for (DefCveItem entry : data) {
            try {
            cveDB.updateVulnerability(entry, mapper.getEcosystem(entry));
            } catch (Exception ex) {
                LOGGER.error("Failed to process " + entry.getCve().getId(), ex);
            }
        }
        endTime = System.currentTimeMillis();
        data = null;
        return this;
    }

    /**
     * Calculates how long the update process took.
     *
     * @return the number of milliseconds that the update process took
     */
    public long getDurationMillis() {
        return endTime - startTime;
    }
}
