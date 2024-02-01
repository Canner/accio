/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.accio.base.client.duckdb;

import io.airlift.configuration.Config;
import io.airlift.units.DataSize;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class DuckDBConfig
{
    private DataSize memoryLimit = DataSize.of(Runtime.getRuntime().maxMemory() / 2, DataSize.Unit.BYTE);
    private String homeDirectory;
    private String tempDirectory = "/tmp/duck";
    private int maxConcurrentTasks = 10;
    private int maxConcurrentMetadataQueries = 10;
    private double maxCacheTableSizeRatio = 0.5;
    private long maxCacheQueryTimeout = 20;
    private long cacheTaskRetryDelay = 60;

    public DataSize getMemoryLimit()
    {
        return memoryLimit;
    }

    @Config("duckdb.memory-limit")
    public void setMemoryLimit(DataSize memoryLimit)
    {
        this.memoryLimit = memoryLimit;
    }

    public String getHomeDirectory()
    {
        return homeDirectory;
    }

    @Config("duckdb.home-directory")
    public void setHomeDirectory(String homeDirectory)
    {
        this.homeDirectory = homeDirectory;
    }

    public String getTempDirectory()
    {
        return tempDirectory;
    }

    @Config("duckdb.temp-directory")
    public void setTempDirectory(String tempDirectory)
    {
        this.tempDirectory = tempDirectory;
    }

    public int getMaxConcurrentTasks()
    {
        return maxConcurrentTasks;
    }

    @Config("duckdb.max-concurrent-tasks")
    public void setMaxConcurrentTasks(int maxConcurrentTasks)
    {
        this.maxConcurrentTasks = maxConcurrentTasks;
    }

    public int getMaxConcurrentMetadataQueries()
    {
        return maxConcurrentMetadataQueries;
    }

    @Config("duckdb.max-concurrent-metadata-queries")
    public void setMaxConcurrentMetadataQueries(int maxConcurrentMetadataQueries)
    {
        this.maxConcurrentMetadataQueries = maxConcurrentMetadataQueries;
    }

    @Min(0)
    @Max(1)
    public double getMaxCacheTableSizeRatio()
    {
        return maxCacheTableSizeRatio;
    }

    @Config("duckdb.max-cache-table-size-ratio")
    public void setMaxCacheTableSizeRatio(double maxCacheTableSizeRatio)
    {
        this.maxCacheTableSizeRatio = maxCacheTableSizeRatio;
    }

    public long getMaxCacheQueryTimeout()
    {
        return maxCacheQueryTimeout;
    }

    @Config("duckdb.max-cache-query-timeout")
    public void setMaxCacheQueryTimeout(long maxCacheQueryTimeout)
    {
        this.maxCacheQueryTimeout = maxCacheQueryTimeout;
    }

    public long getCacheTaskRetryDelay()
    {
        return cacheTaskRetryDelay;
    }

    @Config("duckdb.cache-task-retry-delay")
    public void setCacheTaskRetryDelay(long cacheTaskRetryDelay)
    {
        this.cacheTaskRetryDelay = cacheTaskRetryDelay;
    }
}
