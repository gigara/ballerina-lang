/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.model.Package;
import org.ballerinalang.toml.model.BuildOptions;
import org.ballerinalang.toml.model.Platform;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * {@code PackageConfig} contains necessary configuration elements required to
 * create an instance of a {@code Package}.
 *
 * @since 2.0.0
 */
public class PackageConfig {
    // TODO this class represents the Ballerina.toml file

    // This class should contain Specific project-agnostic information
    private final PackageId packageId;
    private final Path packagePath;
    // Ballerina toml file config
    private final ModuleConfig defaultModule;
    private final List<ModuleConfig> otherModules;
    private final Package pkg;
    private final Map<String, Object> dependencies;
    private final Platform platform;
    private final BuildOptions buildOptions;

    private PackageConfig(PackageId packageId,
                          Path packagePath,
                          ModuleConfig defaultModule,
                          List<ModuleConfig> otherModules,
                          Package pkg,
                          Map<String, Object> dependencies,
                          Platform platform,
                          BuildOptions buildOptions) {
        this.packageId = packageId;
        this.packagePath = packagePath;
        this.defaultModule = defaultModule;
        this.otherModules = otherModules;
        this.pkg = pkg;
        this.dependencies = dependencies;
        this.platform = platform;
        this.buildOptions = buildOptions;
    }

    public static PackageConfig from(PackageId packageId,
                                     Path packagePath,
                                     ModuleConfig defaultModule,
                                     List<ModuleConfig> otherModules,
                                     Package pkg,
                                     Map<String, Object> dependencies,
                                     Platform platform,
                                     BuildOptions buildOptions) {
        return new PackageConfig(packageId, packagePath, defaultModule, otherModules, pkg, dependencies, platform,
                buildOptions);
    }

    public PackageId packageId() {
        return packageId;
    }

    // TODO Check whether it makes sense to expose Java Path in the API
    // TODO Should I use a String here
    public Path packagePath() {
        return packagePath;
    }

    public ModuleConfig defaultModule() {
        return defaultModule;
    }

    public List<ModuleConfig> otherModules() {
        return otherModules;
    }

    public Package getPackage () {
        return this.pkg;
    }

    public Map<String, Object> dependencies() {
        return this.dependencies;
    }

    public Platform platform() {
        return this.platform;
    }

    public BuildOptions buildOptions() {
        return this.buildOptions;
    }
}
