/*
##########################################################################################
### DEV ENABLEMENT - JENKINS PIPELINE OFFERING - Version 3.1.0
### NOTE:   PIPELINE CONFIGURATION FILE CONTAINS GENERIC AND ENVIRONMENT SPECIFIC
###         CONFIGURATION FOR JENKINS PIPELINE
##########################################################################################
*/

CONFIGURATION = [

    integrations: [
        // third-party integrations
        thirdParty: [
            nexus: [
                enabled: false,
                credentialsId: 'nexus-private',   // Jenkins credentials ID
                repoUrl: 'https://www.nexus.ford.com/repository/[TEAM-REPO-NAME]',
            ],

            sonarQube: [
                enabled: false,
                credentialsId: 'sonar-private',   // Jenkins credentials ID
                projectKey: '[SONARQUBE-PROJECT-KEY]',
                hostUrl: '', // Leave this empty to default to https://www.sonarqube.ford.com
                enableBranchAnalysis: true,  // If enabled, uses value from BRANCH_NAME Jenkins environment variable to write analysis results
                enableBreakBuildOnIssue: false, // If enabled, requires SonarQube Scanner for Jenkins plugin. Refer to SonarQube Dev-Guide for details.
            ],

            checkmarx: [
                enabled: false,
                credentialsId: 'checkmarx-private',   // Jenkins credentials ID
                serverUrl: 'https://www.checkmarx.ford.com',
                projectName: '[CHECKMARX-PROJECT-NAME]',
                teamPath: '[CHECKMARX-TEAM-PATH]',    
                enableOSA: false,
            ],

            fossa: [
                enabled: false,
                credentialsId: 'fossa-private',
                projectName: 'vdcc-service-poc', // We have defaulted to same value as rootProject.name from settings.gradle
                teamName: '[TEAM_NAME]',          // The name of your Team as it appears in FOSSA
                policyName: '[POLICY_NAME]',      // Most Spring Boot apps can use 'Website/Hosted Service Use' as policy. Leaving it blank will use FOSSA's most restrictive policy.
                enableBreakBuildOnIssue: false,
            ],
        ],
    ],

    // cli environment variables (optional)
    env: [
        //if your Jenkins supports multiple JDK's (8 & 11) you may need to set JAVA_HOME
        //below is example where Jenkins defaults to 8, but we need 11 for this project;
        //you can set JDK 11 path in JAVA_HOME_ADOPTJDK11 env variable or replace the entire value with the direct path from Jenkins
        //JAVA_HOME: "${JAVA_HOME_ADOPTJDK11}",
    ],

    // deploy environments (e.g. DEV, QA) and their space(s)
    /***** REMOVE COMMENT BLOCK LINES (THIS LINE AND BELOW) AND FILL IN THIS SECTION:
    cfEnvironments: [
        DEV: [
            spaces: [
                EDC1: [
                    apiEndpoint: 'https://api.sys.pp01.edc1.cf.ford.com',
                    org: 'TEAM-ORG',
                    space: 'TEAM-DEV',
                    credentialsId: 'pcf-pre-prod', // Jenkins credentials ID
                    env: [ // cli environment values for this space only
                        cfDomains: 'apps.pp01i.edc1.cf.ford.com',  // comma delimited list of CF route domains
                        cfManifestTarget: 'dev-edc1',     // key tied to the set of values to use in manifest yml file (see manifest-template-settings.json)
                    ]
                ],
                EDC2: [
                    apiEndpoint: 'https://api.sys.pp01.edc2.cf.ford.com',
                    org: 'TEAM-ORG',
                    space: 'TEAM-DEV',
                    credentialsId: 'pcf-pre-prod',
                    env: [
                        cfDomains: 'apps.pp01i.edc2.cf.ford.com',
                        cfManifestTarget: 'dev-edc2',
                    ]
                ]
            ],
            env: [ // cli environment values for all DEV spaces
                // sample oauth2 configuration used by acceptance tests
                ACCEPTANCE_DEFAULT_OAUTH2_CLIENT_ACCESS_TOKEN_URI: 'https://corpqa.sts.ford.com/adfs/oauth2/token',
                ACCEPTANCE_DEFAULT_OAUTH2_APP_CLIENT_ID: 'urn:mywebsite',
                ACCEPTANCE_DEFAULT_OAUTH2_RESOURCE: 'urn:resource:api_ecoboost:qa',
            ],

            // credentials configuration for acceptance tests
            acceptanceTesting: [
                genericId: [
                    credentialsId: ''  // Jenkins credentials ID for Resource Owner Password Credentials
                ],
                clientCredentials: [
                    credentialsId: ''  // Jenkins credentials ID for Client Credentials
                ],
                basicAuthentication: [
                    credentialsId: ''  // Jenkins credentials ID for Basic Authentication
                ]
            ],

            // CF Environment specific integrations
            integrations: [

                // third-party integrations
                thirdParty: [
                    gitHub: [
                        branchName: 'master' //GitHub Branch Name pointed by CF Environment
                    ],
                    nexus: [
                        // Applicable only for higher environments using same github branch.
                        // Set downloadMyPreviouslyBuiltArtifact to true to skip the build and download the previously built latest version of artifact from Nexus (if enabled).
                        // Use BOOST_OVERRIDE_DOWNLOAD_ARTIFACT_VERSION environment variable to override the artifact version to be downloaded from nexus.
                        downloadMyPreviouslyBuiltArtifact: false
                    ],
                ],

                // cloud services integrations
                cloud: [
                    config_server: [
                        enabled: true,
                        configured: false,
                        credentialsId: 'github-sshkey-private', // Jenkins credentials ID
                        repoSSHUrl: '[TEAM-CONFIG-REPO-SSH-URL]',
                    ],
                ],
            ],
        ],

        QA: [
            spaces: [
                EDC1: [
                    apiEndpoint: 'https://api.sys.pp01.edc1.cf.ford.com',
                    org: 'TEAM-ORG',
                    space: 'TEAM-DEV',
                    credentialsId: 'pcf-pre-prod', // Jenkins credentials ID
                    env: [ // cli environment values for this space only
                        cfDomains: 'apps.pp01i.edc1.cf.ford.com',  // comma delimited list of CF route domains
                        cfManifestTarget: 'dev-edc1',     // key tied to the set of values to use in manifest yml file (see manifest-template-settings.json)
                    ]
                ],
                EDC2: [
                    apiEndpoint: 'https://api.sys.pp01.edc2.cf.ford.com',
                    org: 'TEAM-ORG',
                    space: 'TEAM-DEV',
                    credentialsId: 'pcf-pre-prod',
                    env: [
                        cfDomains: 'apps.pp01i.edc2.cf.ford.com', // comma delimited list of CF route domains
                        cfManifestTarget: 'dev-edc2',
                    ]
                ]
            ],

            env: [ // cli environment values for all DEV spaces
                // sample oauth2 configuration used by acceptance tests
                ACCEPTANCE_DEFAULT_OAUTH2_CLIENT_ACCESS_TOKEN_URI: 'https://corpqa.sts.ford.com/adfs/oauth2/token',
                ACCEPTANCE_DEFAULT_OAUTH2_APP_CLIENT_ID: 'urn:mywebsite',
                ACCEPTANCE_DEFAULT_OAUTH2_RESOURCE: 'urn:myservice',
            ],

            // credentials configuration for acceptance tests
            acceptanceTesting: [
                genericId: [
                    credentialsId: ''  // Jenkins credentials ID for Resource Owner Password Credentials
                ],
                clientCredentials: [
                    credentialsId: ''  // Jenkins credentials ID for Client Credentials
                ],
                basicAuthentication: [
                    credentialsId: ''  // Jenkins credentials ID for Basic Authentication
                ]
            ],

            // CF Environment specific integrations
            integrations: [

                // third-party integrations
                thirdParty: [
                    gitHub: [
                        branchName: 'master' //GitHub Branch Name pointed by CF Environment
                    ],
                    nexus: [
                        // Applicable only for higher environments using same github branch.
                        // Set downloadMyPreviouslyBuiltArtifact to true to skip the build and download the previously built latest version of artifact from Nexus (if enabled).
                        // Use BOOST_OVERRIDE_DOWNLOAD_ARTIFACT_VERSION environment variable to override the artifact version to be downloaded from nexus.
                        downloadMyPreviouslyBuiltArtifact: false
                    ],
                ],

                // cloud services integrations
                cloud: [
                    config_server: [
                        enabled: true,
                        configured: false,
                        credentialsId: 'github-sshkey-private', // Jenkins credentials ID
                        repoSSHUrl: '[TEAM-CONFIG-REPO-SSH-URL]',
                    ],
                ],
            ],
        ],
    ]
    ***** COMMENT BLOCK LINE *****/

]
// DEV ENABLEMENT - PIPELINE CONFIGURATION FILE
