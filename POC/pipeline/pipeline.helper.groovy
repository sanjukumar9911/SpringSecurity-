/*##########################################################################################
### DEV ENABLEMENT - JENKINS PIPELINE OFFERING - Version 3.1.0
### NOTE:   PIPELINE HELPER FILE CONTAINS HELPER FUNCTIONS FOR JENKINS PIPELINE
##########################################################################################*/

def _config, _configCf, _version, _pipelineFolder

// initializes pipeline with configurations
def initialize(envKey) {
    _version = "3.1.0"
    _pipelineFolder = 'pipeline/'
    env.BOOST_PIPELINE_OFFERING_VERSION = _version
    printVersionNumber()
    readConfigFile("${envKey}")
    configureThirdPartyIntegrations()
    configureCloudServiceIntegrations()
    configureVanityUrl()
    configureC2CNetworkPolicies()
    configureAcceptanceTestCredentials()
    loadEnvironmentConfigurations()
}

// read configuration from external file
def readConfigFile(envKey = 'DEV') {
    evaluate readFile(_pipelineFolder + 'pipeline.configuration.groovy')
            _config = CONFIGURATION
            _configCf = _config.cfEnvironments?.getAt("${envKey}") ?: [:]
}

// configure environment relating to third-party integrations
def configureThirdPartyIntegrations() {
    // configure nexus w/ credentials
    if (property('integrations.thirdParty.nexus.enabled')) {
        _withCredentials(property('integrations.thirdParty.nexus.credentialsId')) { user, password ->
            env.BOOST_PUBLISH_TEAM_REPO_USER = user;
            env.BOOST_PUBLISH_TEAM_REPO_PASS = password;
            env.BOOST_PUBLISH_TEAM_REPO_URL = property('integrations.thirdParty.nexus.repoUrl');
        }
    }
    if (canDownloadMyPreviouslyBuiltArtifact()) env.BOOST_DOWNLOAD_ARTIFACT_ENABLED = true

    // configure sonarqube w/ credentials
    if (property('integrations.thirdParty.sonarQube.enabled')) {
        getAccessToken(property('integrations.thirdParty.sonarQube.credentialsId')) { token ->
            env.BOOST_SONARQUBE_SONAR_LOGIN = token;
            env.BOOST_SONARQUBE_SONAR_PROJECTKEY = property('integrations.thirdParty.sonarQube.projectKey');
            maybeSetBranchNameForSonarQube();
            maybeOverrideSonarQubeHostUrl();
        }
    }

    // configure fossa w/ credentials
    if (property('integrations.thirdParty.fossa.enabled')) {
        _withCredentials(property('integrations.thirdParty.fossa.credentialsId')) { user, password ->
            env.FOSSA_API_KEY = password;
            env.BOOST_FOSSA_PROJECT_NAME = property('integrations.thirdParty.fossa.projectName');
            env.BOOST_FOSSA_TEAM_NAME = property('integrations.thirdParty.fossa.teamName');
            env.BOOST_FOSSA_POLICY_NAME = property('integrations.thirdParty.fossa.policyName');
            env.BOOST_FOSSA_BREAK_BUILD_ON_ISSUE = property('integrations.thirdParty.fossa.enableBreakBuildOnIssue');
        }
    }
}

def maybeSetBranchNameForSonarQube() {
    if (!property('integrations.thirdParty.sonarQube.enableBranchAnalysis')) return
    env.BOOST_SONARQUBE_SONAR_BRANCH_NAME = env.BRANCH_NAME
}

def maybeOverrideSonarQubeHostUrl() {
    if (!property('integrations.thirdParty.sonarQube.hostUrl')) return
    env.BOOST_SONARQUBE_SONAR_HOST_URL = property('integrations.thirdParty.sonarQube.hostUrl')
}

// configure environment relating to cloud-service integrations
def configureCloudServiceIntegrations() {
    // configure jasypt password in credhub
    if (propertyCf('integrations.cloud.credhub_secretzero.enabled')) {
        env.CREDHUB_ENABLED = true
        _withCredentials(propertyCf('integrations.cloud.credhub_secretzero.credentialsId')) { user, password ->
            env.SECRET_ZERO = password
            env.JASYPT_ENCRYPTOR_PASSWORD = password;
        }
    }

    // configure cloud config server
    if (propertyCf('integrations.cloud.config_server.enabled')) {
        env.CONFIGSERVER_ENABLED = true
        if (propertyCf('integrations.cloud.config_server.configured')) {
            env.CONFIGSERVER_CONFIGURED = true
            _withCredentials(propertyCf('integrations.cloud.config_server.credentialsId')) { user, password ->
               env.GITHUB_SSH_KEY = password
               env.GITHUB_CONFIG_REPO_URL = propertyCf('integrations.cloud.config_server.repoSSHUrl')
            }
        }
    }

    // configure smb volume service
    if (propertyCf('integrations.cloud.smb_volume.enabled')) {
        env.SMB_VOLUME_SERVICE_ENABLED = true
        env.SMB_VOLUME_SERVICE_HOSTNAME = propertyCf('integrations.cloud.smb_volume.hostName')
        env.SMB_VOLUME_SERVICE_SHARENAME = propertyCf('integrations.cloud.smb_volume.shareName')
        env.SMB_VOLUME_SERVICE_MOUNTPATH = propertyCf('integrations.cloud.smb_volume.mountPath')
        _withCredentials(propertyCf('integrations.cloud.smb_volume.credentialsId')) { user, password ->
            env.SMB_VOLUME_SERVICE_USERNAME = user
            env.SMB_VOLUME_SERVICE_PASSWORD = password
        }
    }
}

// configure vanity url
def configureVanityUrl() {
    if (propertyCf('vanityUrl.enabled')) {
        env.BOOST_VANITY_URL_MAPPING_ENABLED = true
        if (propertyCf('vanityUrl.domain')) env.BOOST_VANITY_URL_DOMAIN = propertyCf('vanityUrl.domain')
        if (propertyCf('vanityUrl.hostName')) env.BOOST_VANITY_URL_HOST_NAME = propertyCf('vanityUrl.hostName')
        if (propertyCf('vanityUrl.path')) env.BOOST_VANITY_URL_PATH = propertyCf('vanityUrl.path')
        if (propertyCf('vanityUrl.appSpecificGSLB')) env.BOOST_VANITY_URL_APPSPECIFICGSLB = propertyCf('vanityUrl.appSpecificGSLB')
        if (propertyCf('vanityUrl.appSpecificGSLBAppName')) env.BOOST_VANITY_URL_APPSPECIFICGSLB_APPNAME = propertyCf('vanityUrl.appSpecificGSLBAppName')
    }
}

// configure environment relating to C2C Network Policies
def configureC2CNetworkPolicies() {
    if (propertyCf('c2cNetworkPolicies.enabled')) {
        env.BOOST_C2C_ENABLED = true
        if (propertyCf('c2cNetworkPolicies.outbound')) env.BOOST_C2C_OUTBOUND_APPS = propertyCf('c2cNetworkPolicies.outbound')
        if (propertyCf('c2cNetworkPolicies.inbound'))  env.BOOST_C2C_INBOUND_APPS = propertyCf('c2cNetworkPolicies.inbound')
        if (propertyCf('c2cNetworkPolicies.createAppsInternalRoute')) env.BOOST_C2C_CREATE_APPSINTERNAL_ROUTE = propertyCf('c2cNetworkPolicies.createAppsInternalRoute')
        if (propertyCf('c2cNetworkPolicies.deleteExternalRoutesForThisApp')) env.BOOST_C2C_DELETE_EXTERNAL_ROUTES = propertyCf('c2cNetworkPolicies.deleteExternalRoutesForThisApp')
    }
}

// configure acceptance test credentials from Jenkins Credentials based on credential type
def configureAcceptanceTestCredentials() {
    if (propertyCf('acceptanceTesting.genericId.credentialsId')) {
        _withCredentials(propertyCf('acceptanceTesting.genericId.credentialsId')) { user, password ->
            env.ACCEPTANCE_DEFAULT_OAUTH2_ROPC_USERNAME = user
            env.ACCEPTANCE_DEFAULT_OAUTH2_ROPC_PASSWORD = password
        }
    }
    if (propertyCf('acceptanceTesting.clientCredentials.credentialsId')) {
        _withCredentials(propertyCf('acceptanceTesting.clientCredentials.credentialsId')) { user, password ->
            env.ACCEPTANCE_DEFAULT_OAUTH2_CLIENT_ID = user
            env.ACCEPTANCE_DEFAULT_OAUTH2_CLIENT_SECRET = password
        }
    }
    if (propertyCf('acceptanceTesting.basicAuthentication.credentialsId')) {
        _withCredentials(propertyCf('acceptanceTesting.basicAuthentication.credentialsId')) { user, password ->
            env.ACCEPTANCE_DEFAULT_BASIC_AUTH_USER = user
            env.ACCEPTANCE_DEFAULT_BASIC_AUTH_PASS = password
        }
    }
}

// re/load environment configurations using global, specific cf environment, and specific cf space values
def loadEnvironmentConfigurations(spaceKey = null) {
    def setEnvs = { envMap -> for (def e in envMap) env."${e.key}" = e.value }
    env.cfManifestFile = 'manifest-generated.yml'
    setEnvs _config.env
    setEnvs _configCf.env
    if (spaceKey) {
        setEnvs _configCf.spaces?.getAt(spaceKey)?.env
        cfLogin spaceKey
    }
}

// perform an action for CF space
def stageForEachCfSpace(caption, closure) {
    for (def spaceKey in _configCf.spaces?.keySet()) {
        stage(String.format(caption, spaceKey)) {
            loadEnvironmentConfigurations spaceKey
            closure spaceKey
        }
    }
}

// login and cache cf session
def cfLogin(spaceKey) {
    env.CF_HOME = "${env.WORKSPACE}/cf-sessions/cf-${spaceKey}"

    if (!fileExists(env.CF_HOME)) {
        def spaceInfo = _configCf.spaces[spaceKey]
        _withCredentials(spaceInfo.credentialsId) { user, password ->
            sh """ mkdir -p "${env.CF_HOME}"; cf login -a "${spaceInfo.apiEndpoint}" -o "${spaceInfo.org}" -s "${spaceInfo.space}" -u "${user}" -p "${password}" """
        }
    }
}

// retrieve value for single or nested property from configuration (i.e. pipeline.configuration.groovy)
def property(name, value = _config, defaultValue = null) {
    name.tokenize('.').each { value = value?.get(it) }
    value ?: defaultValue
}

// retrieve a boolean value for single or nested property from configuration (i.e. pipeline.configuration.groovy)
boolean propertyAsBoolean(name, value = _config, defaultValue = false) {
    boolean returnVal = property(name, value, defaultValue)
    returnVal.booleanValue()
}

// retrieve value for single or nested CF property from configuration (i.e. pipeline.configuration.groovy)
def propertyCf(name, defaultValue = null) {
    property(name, _configCf, defaultValue)
}

// credentials wrapper helper to get username/password from Jenkins Credentials Manager
def _withCredentials(credentialsId, closure) {
    if (!credentialsId) return
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: credentialsId, usernameVariable: '_user_', passwordVariable: '_password_']]) {
        closure _user_, _password_
    }
}

// credentials wrapper helper to get access token (secret text) from Jenkins Credentials Manager
def _withTokenCredentials(credentialsId, closure) {
    if (!credentialsId) return
    withCredentials([string(credentialsId: credentialsId, variable: '_token_')]) {
        closure _token_
    }
}

def getAccessToken(credentialsId, closure) {
    try {
        _withTokenCredentials(credentialsId, closure)
    } catch (Exception ex) {
        _withCredentials(credentialsId) { user, password ->
            closure password;
        }
    }
}

def canPublish() {
    if (!property('integrations.thirdParty.nexus.enabled')) { return false }
    if (propertyCf('integrations.thirdParty.nexus.downloadMyPreviouslyBuiltArtifact'))  { return false }
    return true
}

def canDownloadMyPreviouslyBuiltArtifact() {
    if (!property('integrations.thirdParty.nexus.enabled')) { return false }
    if (!propertyCf('integrations.thirdParty.nexus.downloadMyPreviouslyBuiltArtifact')) { return false }
    return true
}

def getReleaseStageLabelText() {
    return propertyCf('c2cNetworkPolicies.enabled') ? 'Green: Release w/ C2C (%s)' : 'Green: Release (%s)'
}

def executePipelineCfServicesScript() {
    def cfServicesScriptFile = _pipelineFolder + 'pipeline.cf-services.sh'
    sh "[ ! -f $cfServicesScriptFile ] || (chmod +x $cfServicesScriptFile && ./$cfServicesScriptFile)"
}

// finalize
def cleanUp() {
    dir("${env.WORKSPACE}/cf-sessions") { deleteDir() }
    cleanEnv()
}

def cleanEnv() {
    def sensitiveEnvVars = ['BOOST_PUBLISH_TEAM_REPO_USER', 'BOOST_PUBLISH_TEAM_REPO_PASS', 'BOOST_SONARQUBE_SONAR_LOGIN',
                            'GITHUB_SSH_KEY', 'SECRET_ZERO', 'FOSSA_API_KEY', 'SMB_VOLUME_SERVICE_USERNAME',
                            'SMB_VOLUME_SERVICE_PASSWORD', 'ACCEPTANCE_DEFAULT_OAUTH2_ROPC_USERNAME',
                            'ACCEPTANCE_DEFAULT_OAUTH2_ROPC_PASSWORD', 'ACCEPTANCE_DEFAULT_OAUTH2_CLIENT_ID',
                            'ACCEPTANCE_DEFAULT_OAUTH2_CLIENT_SECRET', 'ACCEPTANCE_DEFAULT_BASIC_AUTH_USER',
                            'ACCEPTANCE_DEFAULT_BASIC_AUTH_PASS']

    sensitiveEnvVars.each { env."${it}" = '' }
}

// executes Checkmarx analysis
def runCheckmarx() {
    step([$class: 'CxScanBuilder', comment: 'Scan Triggered by Jenkins Pipeline', credentialsId: property('integrations.thirdParty.checkmarx.credentialsId'), excludeFolders: '', excludeOpenSourceFolders: '',
          exclusionsSetting: 'global', failBuildOnNewResults: true, failBuildOnNewSeverity: 'HIGH', filterPattern: '''!**/_cvs/**/*,     !**/.svn/**/*,     !**/.hg/**/*,     !**/.git/**/*,
                  !**/.bzr/**/*,    !**/bin/**/*,    !**/obj/**/*,    !**/backup/**/*,     !**/.idea/**/*,    !**/*.DS_Store,    !**/*.ipr,    !**/*.iws,
                  !**/*.bak,    !**/*.tmp,    !**/*.aac,    !**/*.aif,     !**/*.iff,    !**/*.m3u,    !**/*.mid,    !**/*.mp3,
                  !**/*.mpa,    !**/*.ra,    !**/*.wav,    !**/*.wma,     !**/*.3g2,    !**/*.3gp,    !**/*.asf,    !**/*.asx,
                  !**/*.avi,    !**/*.flv,    !**/*.mov,    !**/*.mp4,     !**/*.mpg,    !**/*.rm,    !**/*.swf,    !**/*.vob,
                  !**/*.wmv,    !**/*.bmp,    !**/*.gif,    !**/*.jpg,     !**/*.png,    !**/*.psd,    !**/*.tif,    !**/*.swf,
                  !**/*.jar,    !**/*.zip,    !**/*.rar,    !**/*.exe,     !**/*.dll,    !**/*.pdb,    !**/*.7z,    !**/*.gz,
                  !**/*.tar.gz,    !**/*.tar,    !**/*.gz,    !**/*.ahtm,     !**/*.ahtml,    !**/*.fhtml,    !**/*.hdm,    !**/*.hdml,
                  !**/*.hsql,    !**/*.ht,    !**/*.hta,    !**/*.htc,     !**/*.htd,    !**/*.war,    !**/*.ear,    !**/*.htmls,
                  !**/*.ihtml,    !**/*.mht,    !**/*.mhtm,    !**/*.mhtml,     !**/*.ssi,    !**/*.stm,    !**/*.stml,    !**/*.ttml,
                  !**/*.txn,    !**/*.xhtm,    !**/*.xhtml,    !**/*.class,     !**/*.iml,    !Checkmarx/Reports/*.*''',
          fullScanCycle: 10, teamPath: property('integrations.thirdParty.checkmarx.teamPath'), includeOpenSourceFolders: '', osaArchiveIncludePatterns: '*.jar', osaEnabled: propertyAsBoolean('integrations.thirdParty.checkmarx.enableOSA'),
          vulnerabilityThresholdEnabled:true, osaHighThreshold:0, osaMediumThreshold:10, osaInstallBeforeScan: false, password: '',
          preset: '36', projectName: property('integrations.thirdParty.checkmarx.projectName'), sastEnabled: true, serverUrl: property('integrations.thirdParty.checkmarx.serverUrl'),
          sourceEncoding: '1', useOwnServerCredentials: true, username: '', vulnerabilityThresholdResult: 'FAILURE', waitForResultsEnabled: true]
    )
}

// Versioning
def printVersionNumber() {
    sh '''
    set +x
    echo "\n*******************************************************************************"
    echo "***                                                                         ***"
    echo "***       DEV ENABLEMENT - JENKINS PIPELINE OFFERING - VERSION: "''' + _version + '''"       ***"
    echo "***                                                                         ***"
    echo "*******************************************************************************\n"
    '''
}

return this;

// DEV ENABLEMENT - PIPELINE HELPER FILE