# Gateway app

### Short summary
This app is the gateway, and it should separate the frontend and the backend.
The frontend should only be able to communicate with backend services through this app. This app should not contain any more advanced logic other than forwarding requests to the right places using proper rules.

### Creating/updating the .jar
#### In IntelliJ with Maven
<ol>
    <li> Open the Maven side panel </li>
    <li> In "Lifecycle" select "package" and run it </li>
</ol>

