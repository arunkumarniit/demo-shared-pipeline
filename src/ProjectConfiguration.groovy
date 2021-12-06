#!groovy

package sharedlibrary

public class ProjectConfiguration implements Serializable {
    String repoUrl;
    String repoBranch;
    String registry;
    String registryCredentials;

}