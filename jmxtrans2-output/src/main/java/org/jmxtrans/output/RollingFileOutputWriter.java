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
package org.jmxtrans.output;

import org.jmxtrans.config.QueryResult;
import org.jmxtrans.utils.IoUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;

import static org.jmxtrans.utils.ConfigurationUtils.*;

public class RollingFileOutputWriter extends AbstractOutputWriter {

    public final static String SETTING_FILE_NAME = "fileName";
    public final static String SETTING_FILE_NAME_DEFAULT_VALUE = "jmxtrans-agent.data";
    public final static String SETTING_MAX_FILE_SIZE = "maxFileSize";
    public final static long SETTING_MAX_FILE_SIZE_DEFAULT_VALUE=10;
    public final static String SETTING_MAX_BACKUP_INDEX = "maxBackupIndex";
    public final static int SETTING_MAX_BACKUP_INDEX_DEFAULT_VALUE = 5; 
    private static DateFormat dfISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    
    protected Writer temporaryFileWriter;
    protected File temporaryFile;
    protected File file = new File(SETTING_FILE_NAME_DEFAULT_VALUE);
    protected long maxFileSize;
    protected int maxBackupIndex;

    @Override
    public synchronized void postConstruct(Map<String, String> settings) {
        super.postConstruct(settings);
        TimeZone tz = TimeZone.getTimeZone("UTC");
        dfISO8601.setTimeZone(tz);
        file = new File(getString(settings, SETTING_FILE_NAME, SETTING_FILE_NAME_DEFAULT_VALUE));
        maxFileSize = getLong(settings, SETTING_MAX_FILE_SIZE, SETTING_MAX_FILE_SIZE_DEFAULT_VALUE);
        maxBackupIndex = getInt(settings, SETTING_MAX_BACKUP_INDEX, SETTING_MAX_BACKUP_INDEX_DEFAULT_VALUE);
        // Performance related after 10mb the Filechanel output.transferTo() performs badly
        if (maxFileSize > 10 || maxFileSize < 0) maxFileSize = 10;
        maxFileSize = maxFileSize * 1000000; //converts to bytes.
        logger.log(getInfoLevel(), "RollingFileOutputWriter configured with file " + file.getAbsolutePath());
    }

    protected Writer getTemporaryFileWriter() throws IOException {
        if (temporaryFile == null) {
            temporaryFile = File.createTempFile("jmxtrans-agent-", ".data");
            temporaryFile.deleteOnExit();
            if (logger.isLoggable(getDebugLevel()))
                logger.log(getDebugLevel(), "Created temporary file " + temporaryFile.getAbsolutePath());

            temporaryFileWriter = null;
        }
        if (temporaryFileWriter == null) {
            temporaryFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temporaryFile, false), Charset.forName("UTF-8")));
        }

        return temporaryFileWriter;
    }

    public synchronized void writeQueryResult(@Nonnull String name, @Nullable String type, @Nullable Object value) throws IOException {
        try {
            getTemporaryFileWriter().write("["+dfISO8601.format(Calendar.getInstance().getTime()) +"] "+name + " " + value + "\n");
        } catch (IOException e) {
            releaseTemporaryWriter();
            throw e;
        }
    }

    @Override
    public synchronized void write(QueryResult result) throws IOException {
        try {
            getTemporaryFileWriter().write("["+dfISO8601.format(Calendar.getInstance().getTime()) +"] "+ result.getName() + " " + result.getValue() + "\n");
        } catch (IOException e) {
            releaseTemporaryWriter();
            throw e;
        }
    }

    protected void releaseTemporaryWriter() {
        try {
            IoUtils.closeQuietly(getTemporaryFileWriter());
        } catch (IOException e) {
            // silently skip
        }
        if (temporaryFile != null) {
            if (!temporaryFile.delete()) {
                logger.log(Level.WARNING, "Could not delete temporary file [" + temporaryFile.getAbsolutePath() + "].");
            }
        }
        temporaryFile = null;

    }

    @Override
    public synchronized void postCollect() throws IOException {
        try {
            getTemporaryFileWriter().close();
            if (logger.isLoggable(getDebugLevel()))
                logger.log(getDebugLevel(), "Overwrite " + file.getAbsolutePath() + " by " + temporaryFile.getAbsolutePath());
            IoUtils.appendToFile(temporaryFile, file, maxFileSize, maxBackupIndex);
        } finally {
            temporaryFileWriter = null;
        }
    }

}

