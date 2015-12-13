package de.emesit.svnrev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class to hold the gathered information.
 * 
 * @author Ruediger Schobbert, emesit GmbH & Co. KG
 */
public class BuildInfo {
    private Long    revisionNumber;
    private boolean locallyModified;
    private Date    buildDateTime = new Date();
    private Date    commitDateTime;

    public void setLastCommitDateTime(Date date) {
        if (commitDateTime == null || (date != null && commitDateTime.before(date))) {
            commitDateTime = date;
        }
    }

    public void writeToFile(String outputFilePath) throws IOException {
        writeToFile(new File(outputFilePath), null);
    }

    public void writeToFile(String outputFilePath, String templateFilePath) throws IOException {
        writeToFile(new File(outputFilePath), new File(templateFilePath));
    }

    public void writeToFile(File outputFile, File templateFile) throws IOException {
        if (revisionNumber != null) {
            StringBuilder templateBuilder = null;
            if (templateFile != null) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(templateFile));
                    String nl = System.getProperty("line.separator");
                    templateBuilder = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        templateBuilder.append(line).append(nl);
                    }
                } catch (Exception exc) {
                    throw new IOException("Error reading template file " + templateFile.getAbsolutePath(), exc);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception exc) {
                            throw new IOException("Error closing template file reader " + templateFile.getAbsolutePath(), exc);
                        }
                    }
                }
            }
            
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(new FileWriter(outputFile));
                String template = (templateBuilder!=null) ? templateBuilder.toString() : getDefaultTemplate();
                pw.print(toString(template));
            } catch (Exception exc) {
                throw new IOException("Error writing to file " + outputFile.getAbsolutePath(), exc);
            } finally {
                if (pw != null) {
                    try {
                        pw.close();
                    } catch (Exception exc) {
                        throw new IOException("Error closing file " + outputFile.getAbsolutePath(), exc);
                    }
                }
            }
        }
    }

    public String formatDateTime(Date date) {
        return (date != null) ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(date) : "";
    }

    public static String getDefaultTemplate() {
        String nl = System.getProperty("line.separator");
        return 
            "svnrev_build_number=${REV}"+nl+
            "svnrev_build_commit_time=${COMMIT_DATETIME}"+nl+
            "svnrev_build_time=${BUILD_DATETIME}"+nl+
            "svnrev_build_local_modification=${LOCALLY_MODIFIED}"+nl;
    }

    @Override
    public String toString() {
        return toString(getDefaultTemplate());
    }

    public String toString(String template) {
        return toString(template, asParameterMap());
    }

    public String toString(String template, Map<String, String> params) {
        if (revisionNumber != null) {
            String substitute = template;
            for (Map.Entry<String, String> nextEntry : params.entrySet()) {
                substitute = substitute.replace(nextEntry.getKey(), nextEntry.getValue());
            }
            return substitute;
        }
        return "NotUnderVersionControl";
    }
    
    
    public Map<String, String> asParameterMap() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("${REV}", "svn"+revisionNumber+(locallyModified ? "m" : ""));
        params.put("${COMMIT_DATETIME}", formatDateTime(commitDateTime));
        params.put("${BUILD_DATETIME}", formatDateTime(buildDateTime));
        params.put("${LOCALLY_MODIFIED}", Boolean.toString(locallyModified));
        return params;
    }
    public Long getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Long revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public boolean isLocallyModified() {
        return locallyModified;
    }

    public void setLocallyModified(boolean locallyModified) {
        this.locallyModified = locallyModified;
    }

    public Date getBuildDateTime() {
        return buildDateTime;
    }

    public void setBuildDateTime(Date buildDateTime) {
        this.buildDateTime = buildDateTime;
    }

    public Date getCommitDateTime() {
        return commitDateTime;
    }

    public void setCommitDateTime(Date commitDateTime) {
        this.commitDateTime = commitDateTime;
    }
}
