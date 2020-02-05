/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.client.config;

/**
 * Ballerina Client Configuration.
 */
public class BallerinaClientConfig {
    private final String home;
    private final boolean allowExperimental;
    private final boolean linterSkipped;
    private final boolean debugLog;
    private final CodeLensConfig codeLens;
    private final boolean showLSErrors;

    private BallerinaClientConfig() {
        this.home = "";
        this.allowExperimental = false;
        this.linterSkipped = false;
        this.debugLog = false;
        this.codeLens = new CodeLensConfig();
        this.showLSErrors = false;
    }

    /**
     * Returns default ballerina client configuration.
     *
     * @return {@link BallerinaClientConfig}
     */
    public static BallerinaClientConfig getDefault() {
        return new BallerinaClientConfig();
    }

    /**
     * Returns home.
     *
     * @return home
     */
    public String getHome() {
        return home;
    }

    /**
     * Returns True if allow experimental enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isAllowExperimental() {
        return allowExperimental;
    }

    /**
     * Returns True if allow debug log enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isDebugLog() {
        return debugLog;
    }

    /**
     * Returns Code Lens Configs.
     *
     * @return {@link CodeLensConfig}
     */
    public CodeLensConfig getCodeLens() {
        return codeLens;
    }

    /**
     * Returns True if show LS errors enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isShowLSErrors() {
        return showLSErrors;
    }

    /**
     * Returns True if show linter enabled, False otherwise.
     *
     * @return True if enabled, False otherwise
     */
    public boolean isLinterSkipped() {
        return linterSkipped;
    }
}
