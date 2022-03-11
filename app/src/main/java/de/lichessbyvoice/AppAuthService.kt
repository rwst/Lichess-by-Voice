/*
 * Adapted from net.openid.appauthdemo.LoginActivity by R. Stephan, 2022-Mar-09
 * Copyright 2015 The AppAuth for Android Authors. All Rights Reserved.
 *
     * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lichessbyvoice

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.browser.customtabs.CustomTabsIntent
import net.openid.appauth.*
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.browser.BrowserMatcher
import net.openid.appauthdemo.AuthStateManager
import net.openid.appauthdemo.Configuration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class AppAuthService private constructor(context: Context) {
    private val mClientId = AtomicReference<String>()
    private val mAuthRequest = AtomicReference<AuthorizationRequest>()
    private val mAuthIntent = AtomicReference<CustomTabsIntent>()
    private var mAuthIntentLatch = CountDownLatch(1)
    private val mExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()
    private val mBrowserMatcher: BrowserMatcher = AnyBrowserMatcher.INSTANCE

    private val completionIntent = Intent(context, SelectGameActivity::class.java)
    private val cancelIntent = Intent(context, AuthFailedActivity::class.java)
    private val pflags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
    private val pcompl = PendingIntent.getActivity(context, 0, completionIntent, pflags)
    private val pcancel = PendingIntent.getActivity(context, 0, cancelIntent, pflags)
    private val mAuthStateManager = AuthStateManager.getInstance(context)
    private val mConfiguration = Configuration.getInstance(context)
    private var mAuthService: AuthorizationService = createAuthorizationService(context)

    init {
        if (!mConfiguration.isValid) {
            mConfiguration.getConfigurationError()?.let { Log.e(TAG, it) }
        }
        else
            mExecutor.submit { this.initializeAppAuth(context) }
    }

    private fun recreateAuthorizationService(context: Context) {
        Log.i(TAG, "Discarding existing AuthService instance")
        mAuthService.dispose()
        mAuthService = createAuthorizationService(context)
        mAuthRequest.set(null)
        mAuthIntent.set(null)
    }

    private fun createAuthRequest(loginHint: String?) {
        Log.i(TAG,"Creating auth request for login hint: $loginHint")
        val authRequestBuilder = AuthorizationRequest.Builder(
            mAuthStateManager.current.authorizationServiceConfiguration!!,
            mClientId.get(),
            ResponseTypeValues.CODE,
            mConfiguration.getRedirectUri()
        )
            .setScope(mConfiguration.scope)
        if (!TextUtils.isEmpty(loginHint)) {
            authRequestBuilder.setLoginHint(loginHint)
        }
        mAuthRequest.set(authRequestBuilder.build())
    }

    private fun warmUpBrowser() {
        mAuthIntentLatch = CountDownLatch(1)
        mExecutor.execute {
            Log.i(TAG,"Warming up browser instance for auth request")
            val intentBuilder =
                mAuthService.createCustomTabsIntentBuilder(mAuthRequest.get().toUri())
            //intentBuilder.setToolbarColor(getColorCompat(R.color.colorPrimary))
            mAuthIntent.set(intentBuilder.build())
            mAuthIntentLatch.countDown()
        }
    }

    @MainThread
    private fun initializeAuthRequest() {
        createAuthRequest("")
        warmUpBrowser()
        // displayAuthOptions()
    }

    @MainThread
    private fun handleRegistrationResponse(
        response: RegistrationResponse?,
        ex: AuthorizationException
    ) {
        mAuthStateManager.updateAfterRegistration(response, ex)
        if (response == null) {
            Log.i(TAG,"Failed to dynamically register client",
                ex
            )
            return
        }
        Log.i(TAG,"Dynamically registered client: " + response.clientId)
        mClientId.set(response.clientId)
        initializeAuthRequest()
    }

    /**
     * Initiates a dynamic registration request if a client ID is not provided by the static
     * configuration.
     */
    private fun initializeClient() {
        Log.i(TAG, "initializeClient()")
        if (mConfiguration.getClientId() != null) {
            Log.i(TAG,"Using static client ID: " + mConfiguration.getClientId()
            )
            // use a statically configured client ID
            mClientId.set(mConfiguration.getClientId())
            initializeAuthRequest()
            return
        }
        val lastResponse = mAuthStateManager.current.lastRegistrationResponse
        if (lastResponse != null) {
            Log.i(TAG,"Using dynamic client ID: " + lastResponse.clientId)
            // already dynamically registered a client ID
            mClientId.set(lastResponse.clientId)
            initializeAuthRequest()
            return
        }

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        Log.i(TAG, "Dynamically registering client")
        val registrationRequest = RegistrationRequest.Builder(
            mAuthStateManager.current.authorizationServiceConfiguration!!,
            listOf(mConfiguration.getRedirectUri())
        )
            .setTokenEndpointAuthenticationMethod(ClientSecretBasic.NAME)
            .build()
        mAuthService.performRegistrationRequest(
            registrationRequest
        ) { response: RegistrationResponse?, ex: AuthorizationException? ->
            if (ex != null) {
                this.handleRegistrationResponse(
                    response,
                    ex
                )
            }
        }
    }

    @MainThread
    private fun handleConfigurationRetrievalResult(
        config: AuthorizationServiceConfiguration?,
        ex: AuthorizationException
    ) {
        if (config == null) {
            Log.i(TAG,"Failed to retrieve discovery document",
                ex
            )
            return
        }
        Log.i(TAG, "Discovery document retrieved")
        mAuthStateManager.replace(AuthState(config))
        mExecutor.submit { initializeClient() }
    }

    /**
     * Initializes the authorization service configuration if necessary, either from the local
     * static values or by retrieving an OpenID discovery document.
     */
    private fun initializeAppAuth(context: Context) {
        Log.i(TAG, "Initializing AppAuth")
        recreateAuthorizationService(context)
        if (mAuthStateManager.current.authorizationServiceConfiguration != null) {
            // configuration is already created, skip to client initialization
            Log.i(TAG, "auth config already established")
            initializeClient()
            return
        }

        // if we are not using discovery, build the authorization service configuration directly
        // from the static configuration values.
        if (mConfiguration.getDiscoveryUri() == null) {
            Log.i(TAG,"Creating auth config from res/raw/auth_config.json")
            Log.i(TAG,"getAuthEndpointUri: " + mConfiguration.getAuthEndpointUri())
            Log.i(TAG,"getTokenEndpointUri: " + mConfiguration.getTokenEndpointUri())
            Log.i(TAG,"getRegistrationEndpointUri: " + mConfiguration.getRegistrationEndpointUri())
            Log.i(TAG,"getEndSessionEndpoint: " + mConfiguration.getEndSessionEndpoint())

            val config = AuthorizationServiceConfiguration(
                mConfiguration.getAuthEndpointUri(),
                mConfiguration.getTokenEndpointUri(),
                mConfiguration.getRegistrationEndpointUri(),
                mConfiguration.getEndSessionEndpoint()
            )
            Log.i(TAG, "config: " + config.toJsonString())

            mAuthStateManager.replace(AuthState(config))
            initializeClient()
            return
        }

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        Log.i(TAG, "Retrieving OpenID discovery doc")
        AuthorizationServiceConfiguration.fetchFromUrl(
            mConfiguration.getDiscoveryUri()!!,
            { config: AuthorizationServiceConfiguration?, ex: AuthorizationException? ->
                if (ex != null) {
                    this.handleConfigurationRetrievalResult(
                        config,
                        ex
                    )
                }
            },
            mConfiguration.connectionBuilder
        )
    }

    @MainThread
    fun linkAccount() {

        // WrongThread inference is incorrect for lambdas
        // noinspection WrongThread
        mExecutor.submit { this.doAuth() }
    }

    private fun createAuthorizationService(context: Context): AuthorizationService {
        Log.i(TAG, "Creating authorization service")
        val builder = AppAuthConfiguration.Builder()
        builder.setBrowserMatcher(mBrowserMatcher)
        //if (mConfiguration == null) Log.e(TAG, "null")
        builder.setConnectionBuilder(mConfiguration.connectionBuilder)
        return AuthorizationService(context, builder.build())
    }

    @WorkerThread
    private fun doAuth() {
        Log.i(TAG, "doAuth")
        try {
            mAuthIntentLatch.await()
        } catch (ex: InterruptedException) {
            Log.w(
                TAG,
                "Interrupted while waiting for auth intent"
            )
        }
        if (mUsePendingIntents) {
            cancelIntent.putExtra(AuthFailedActivity.EXTRA_FAILED, true)
            cancelIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            Log.i(TAG, "call performAuthorizationRequest: $pcompl, $pcancel")
            mAuthService.performAuthorizationRequest(
                mAuthRequest.get(),
                pcompl,
                pcancel,
                mAuthIntent.get()
            )
        }
        else
        {
            val intent = mAuthService.getAuthorizationRequestIntent(
                mAuthRequest.get(),
                mAuthIntent.get()
            )
        }
    }

    fun performTokenRequest(
        createTokenExchangeRequest: TokenRequest,
        tokenResponseCallback: AuthorizationService.TokenResponseCallback
    ) {
        mAuthService.performTokenRequest(createTokenExchangeRequest, tokenResponseCallback)
    }

    companion object : SingletonHolder<AppAuthService, Context>(::AppAuthService) {
        private const val TAG = "AppAuthService"
        private const val mUsePendingIntents = true
    }
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