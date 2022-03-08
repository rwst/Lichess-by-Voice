package de.lichessbyvoice

object AppAuthService {

    fun linkAccount() {}
/*        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://lichess.org/oauth"), // authorization endpoint
            Uri.parse("https://lichess.org/api/token") // token endpoint
        )
        val clientId = "de.lichessbyvoice"
        val redirectUri = Uri.parse("de.lichessbyvoice:/oauth2callback")
        val builder = AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        )
        builder.setScopes("web:login")

        val authRequest = builder.build()
        val authService = AuthorizationService(this)
        val action = "net.openid.appauth.HANDLE_AUTHORIZATION_RESPONSE"
        val authIntent = Intent(action)
        val pendingIntent = PendingIntent.getActivity(this, authRequest.hashCode(), authIntent, 0)

        authService.performAuthorizationRequest(
            authRequest,
            pendingIntent,
            pendingIntent
        )
    }
*/
}