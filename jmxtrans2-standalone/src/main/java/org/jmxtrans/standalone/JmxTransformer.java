/**
 * The MIT License
 * Copyright (c) 2014 JMXTrans Team
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
package org.jmxtrans.standalone;

import java.util.concurrent.TimeUnit;

import org.jmxtrans.core.config.JmxTransBuilder;
import org.jmxtrans.core.log.Logger;
import org.jmxtrans.core.log.LoggerFactory;
import org.jmxtrans.core.scheduler.NaiveScheduler;
import org.jmxtrans.standalone.cli.JmxTransParameters;
import org.jmxtrans.utils.appinfo.AppInfo;

import com.beust.jcommander.JCommander;

import static org.jmxtrans.core.scheduler.NaiveScheduler.State.TERMINATED;

public class JmxTransformer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JmxTransformer.class.getName());

    public static void main(String[] args) throws Exception {

        AppInfo<JmxTransformer> appInfo = AppInfo.load(JmxTransformer.class);
        appInfo.print(System.out);

        JmxTransParameters parameters = new JmxTransParameters();
        JCommander jCommander = new JCommander(parameters, args);
        jCommander.setProgramName(appInfo.getName());

        if (parameters.isHelp()) {
            jCommander.usage();
            System.exit(0);
        }

        final NaiveScheduler scheduler = new JmxTransBuilder(
                parameters.isIgnoringParsingErrors(),
                parameters.getConfigResources())
                .build();
        
        scheduler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scheduler.stop();
                } catch (Exception e) {
                    LOGGER.error("Could not stop JmxTrans cleanly", e);
                } catch (Throwable t) {
                    LOGGER.error("Could not stop JmxTrans cleanly", t);
                    throw t;
                }
            }
        }));

        // there should be a better way to do this ...
        while (!scheduler.getState().equals(TERMINATED)) scheduler.awaitTermination(1, TimeUnit.HOURS);
    }

}
