/*
 * The MIT License
 * Copyright © 2021-present KuFlow S.L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.kuflow.samples.temporal.worker.uivision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "uivision")
@Validated
public class UIVisionProperties {

    @NotBlank
    private String command;

    @NotBlank
    private String logDirectory;

    @NotBlank
    private String autoRunHtml;

    @NotBlank
    private String macro;

    private boolean closeBrowser = true;

    private boolean closeRpa = true;

    @NotNull
    private Duration executionTimeout;

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getLogDirectory() {
        return this.logDirectory;
    }

    public void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
    }

    public String getAutoRunHtml() {
        return this.autoRunHtml;
    }

    public void setAutoRunHtml(String autoRunHtml) {
        this.autoRunHtml = autoRunHtml;
    }

    public String getMacro() {
        return this.macro;
    }

    public void setMacro(String macro) {
        this.macro = macro;
    }

    public boolean isCloseBrowser() {
        return this.closeBrowser;
    }

    public int getCloseBrowserAsInt() {
        return BooleanUtils.toInteger(this.closeBrowser);
    }

    public void setCloseBrowser(boolean closeBrowser) {
        this.closeBrowser = closeBrowser;
    }

    public boolean isCloseRpa() {
        return this.closeRpa;
    }

    public int getCloseRpaAsInt() {
        return BooleanUtils.toInteger(this.closeRpa);
    }

    public void setCloseRpa(boolean closeRPA) {
        this.closeRpa = closeRPA;
    }

    public Duration getExecutionTimeout() {
        return this.executionTimeout;
    }

    public void setExecutionTimeout(Duration executionTimeout) {
        this.executionTimeout = executionTimeout;
    }
}
