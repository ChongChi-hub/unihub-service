rootProject.name = "unihub-service"

// Libraries
include("libs:common-starter")

// Microservices
include("services:api-gateway")
include("services:identity-service")
include("services:academic-service")
include("services:learning-service")
include("services:assessment-service")
include("services:notification-service")
include("services:file-service")
