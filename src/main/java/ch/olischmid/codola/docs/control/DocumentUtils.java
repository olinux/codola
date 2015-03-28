package ch.olischmid.codola.docs.control;

import org.eclipse.jgit.lib.Ref;

/**
 * Created by oli on 27.03.15.
 */
public class DocumentUtils {

    /**
     * @return the actual branch name (excluding prefixes such as "ref", "head", etc)
     */
    public String getSimpleBranchName(Ref branch) {
        return getSimpleBranchName(branch.getName());
    }

    public String getSimpleBranchName(String branchName) {
        return branchName.substring(branchName.lastIndexOf('/') + 1);
    }

}
