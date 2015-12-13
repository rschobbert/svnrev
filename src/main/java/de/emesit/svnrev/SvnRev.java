package de.emesit.svnrev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * When used as a standalone tool, call svnrev with:
 * <ul style="list-style-type:none">
 * <li><code>java -jar svnrev.jar [wcRoot [outputDir [outputFilename [templateFilename]]]]</code></li>
 * </ul>
 * where svnrev.jar ist the standalone jar which also includes all dependencies.
 * <p/>
 * @author Ruediger Schobbert, emesit GmbH & Co. KG
 */
public class SvnRev {
    public static final String DEFAULT_FILENAME_SUFFIX = ".build.properties";
    
    public static void main(String[] args) throws IOException {
        String workingCopyRoot = ".";
        File outputDir = new File("."); 
        String outputFilename = outputDir.getCanonicalFile().getName()+DEFAULT_FILENAME_SUFFIX;
        String templatePath = null;
        
        if (args.length >= 4) {
            templatePath = args[3];
        }
        if (args.length >= 3) {
            outputFilename = args[2];
        } 
        if (args.length >= 2) {
            outputDir = new File(args[1]); 
        }
        if (args.length >= 1) {
            workingCopyRoot = args[0];
        }
        execute(workingCopyRoot, outputDir, outputFilename, templatePath);
    }
    
    static BuildInfo execute(String workingCopyRoot, File outputDir, String outputFilename, String templatePath) throws IOException {
        if (outputDir == null) {
            outputDir = new File(".");
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        if (!outputDir.isDirectory()) {
            throw new SvnRevException("$outputDir.absolutePath is not a directory");
        }
        BuildInfo buildInfo = createBuildInfo(workingCopyRoot);
        File templateFile = (templatePath != null) ? new File(templatePath) : null;
        buildInfo.writeToFile(new File(outputDir, outputFilename), templateFile);
        return buildInfo;
    }
    
    static BuildInfo createBuildInfo(String workingCopyRoot) {
        BuildInfo buildInfo = new BuildInfo();
        if (SVNWCUtil.isVersionedDirectory(new File(workingCopyRoot))) {
            SVNClientManager clientManager = SVNClientManager.newInstance();
            SVNStatusClient statusClient = clientManager.getStatusClient();
            
            fillBuildInfo(buildInfo, workingCopyRoot, statusClient);
        } else {
            System.err.println(workingCopyRoot+" is not under version control");
        }
        return buildInfo;
    }
    private static void fillBuildInfo(final BuildInfo buildInfo, String workingCopyRoot, SVNStatusClient statusClient) {
        ISVNStatusHandler handler = new ISVNStatusHandler() {
            @Override
            public void handleStatus(SVNStatus status) throws SVNException {
                if (status.getRevision() != SVNRevision.UNDEFINED && status.getRevision().getNumber() > 0L) {
                    buildInfo.setRevisionNumber(Long.valueOf(status.getRevision().getNumber()));
                }
                if (!buildInfo.isLocallyModified()) {
                    buildInfo.setLocallyModified(isModified(status));
                }
                buildInfo.setLastCommitDateTime(status.getCommittedDate());
            }
        };
        try {
            boolean remote = false;
            boolean reportAll = true; 
            boolean includeIgnored = false;
            boolean collectParentExternals = false;
            
			statusClient.doStatus(
			        new File(workingCopyRoot), 
			        SVNRevision.HEAD, 
			        SVNDepth.INFINITY , 
			        remote, 
			        reportAll, 
			        includeIgnored, 
			        collectParentExternals, 
			        handler, 
			        new ArrayList<String>());
		} catch (SVNException exc) {
			throw new SvnRevException(exc);
		} 
    }

    private static boolean isModified(SVNStatus status) {
        if (status.isLocked()) {
            return true;
        }
        if (status.getContentsStatus() == SVNStatusType.STATUS_MODIFIED){
            return true;
        } else if(status.getContentsStatus() == SVNStatusType.STATUS_CONFLICTED){
            return true;
        }
        if (status.getPropertiesStatus() == SVNStatusType.STATUS_MODIFIED){
            return true;
        } else if(status.getPropertiesStatus() == SVNStatusType.STATUS_CONFLICTED){
            return true;
        }
        return false;
    }

}
