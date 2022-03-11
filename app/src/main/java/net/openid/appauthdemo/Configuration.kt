/*
 * Original Java code was adapted to Kotlin by R. Stephan, 2022-Mar-08.
 * We also removed usage of ConnectionBuilderForTesting.
 *
 * Copyright 2016 The AppAuth for Android Authors. All Rights Reserved.
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
package net.openid.appauthdemo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.text.TextUtils
import de.lichessbyvoice.R
import de.lichessbyvoice.SingletonHolder
import net.openid.appauth.connectivity.ConnectionBuilder
import net.openid.appauth.connectivity.DefaultConnectionBuilder
import okio.BufferedSource
import okio.buffer
import okio.source
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

/**
 * Reads and validates the demo app configuration from `res/raw/auth_config.json`. Configuration
 * changes are detected by comparing the hash of the last known configuration to the read
 * configuration. When a configuration change is detected, the app state is reset.
 */
class Configuration private constructor(context: Context) {
    private val mPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mResources: Resources = context.resources
    private var mConfigJson: JSONObject? = null
    private var mConfigHash: String? = null

    /**
     * Returns a description of the configuration error, if the configuration is invalid.
     */
    private var configurationError: String? = null
    private var clientId: String? = null
    private var mScope: String? = null
    private var mRedirectUri: Uri? = null
    private var endSessionRedirectUri: Uri? = null
    private var discoveryUri: Uri? = null
    private lateinit var authEndpointUri: Uri
    private lateinit var tokenEndpointUri: Uri
    private lateinit var endSessionEndpoint: Uri
    private lateinit var registrationEndpointUri: Uri
    private var userInfoEndpointUri: Uri? = null
    private var isHttpsRequired = false

    init {
        try {
            readConfiguration(context)
        } catch (ex: Configuration.InvalidConfigurationException) {
            configurationError = ex.message
        }
    }
    /**
     * Indicates whether the configuration has changed from the last known valid state.
     */
    fun hasConfigurationChanged(): Boolean {
        val lastHash = lastKnownConfigHash
        return mConfigHash != lastHash
    }

    /**
     * Indicates whether the current configuration is valid.
     */
    val isValid: Boolean
        get() = configurationError == null

    /**
     * Indicates that the current configuration should be accepted as the "last known valid"
     * configuration.
     */
    fun acceptConfiguration() {
        mPrefs.edit().putString(KEY_LAST_HASH, mConfigHash).apply()
    }

    val scope: String
        get() = (mScope)!!
    val connectionBuilder: ConnectionBuilder
        get() {
            return DefaultConnectionBuilder.INSTANCE
        }
    private val lastKnownConfigHash: String?
        get() = mPrefs.getString(KEY_LAST_HASH, null)

    @Throws(InvalidConfigurationException::class)
    private fun readConfiguration(context: Context) {
        val configSource: BufferedSource =
            mResources.openRawResource(R.raw.auth_config).source().buffer()
        val configData = okio.Buffer()
        try {
            configSource.readAll(configData)
            mConfigJson = JSONObject(configData.readString(Charset.forName("UTF-8")))
        } catch (ex: IOException) {
            throw InvalidConfigurationException(
                "Failed to read configuration: " + ex.message
            )
        } catch (ex: JSONException) {
            throw InvalidConfigurationException(
                "Unable to parse configuration: " + ex.message
            )
        }
        mConfigHash = configData.sha256().base64()
        clientId = getConfigString("client_id")
        mScope = getRequiredConfigString("authorization_scope")
        mRedirectUri = getRequiredConfigUri("redirect_uri")
        endSessionRedirectUri = getRequiredConfigUri("end_session_redirect_uri")
        if (!isRedirectUriRegistered(context)) {
            throw InvalidConfigurationException(
                "redirect_uri is not handled by any activity in this app! "
                        + "Ensure that the appAuthRedirectScheme in your build.gradle file "
                        + "is correctly configured, or that an appropriate intent filter "
                        + "exists in your app manifest."
            )
        }
        if (getConfigString("discovery_uri") == null) {
            authEndpointUri = getRequiredConfigWebUri("authorization_endpoint_uri")
            tokenEndpointUri = getRequiredConfigWebUri("token_endpoint_uri")
            userInfoEndpointUri = getRequiredConfigWebUri("user_info_endpoint_uri")
            endSessionEndpoint = getRequiredConfigUri("end_session_endpoint")
            registrationEndpointUri = getRequiredConfigWebUri("registration_endpoint_uri")
        } else {
            discoveryUri = getRequiredConfigWebUri("discovery_uri")
        }
        isHttpsRequired = mConfigJson!!.optBoolean("https_required", true)
    }

    /**
     * Returns a description of the configuration error, if the configuration is invalid.
     */
    fun getConfigurationError(): String? {
        return configurationError
    }

    fun getClientId(): String? {
        return clientId
    }

    fun getRedirectUri(): Uri {
        return mRedirectUri!!
    }

    fun getDiscoveryUri(): Uri? {
        return discoveryUri
    }

    fun getEndSessionRedirectUri(): Uri? {
        return endSessionRedirectUri
    }

    fun getAuthEndpointUri(): Uri {
        return authEndpointUri
    }

    fun getTokenEndpointUri(): Uri {
        return tokenEndpointUri
    }

    fun getEndSessionEndpoint(): Uri {
        return endSessionEndpoint
    }

    fun getRegistrationEndpointUri(): Uri {
        return registrationEndpointUri
    }

    fun getUserInfoEndpointUri(): Uri? {
        return userInfoEndpointUri
    }

    fun isHttpsRequired(): Boolean {
        return isHttpsRequired
    }

    private fun getConfigString(propName: String?): String? {
        var value: String = mConfigJson!!.optString(propName) ?: return null
        value = value.trim { it <= ' ' }
        return if (TextUtils.isEmpty(value)) {
            null
        } else value
    }

    @Throws(InvalidConfigurationException::class)
    private fun getRequiredConfigString(propName: String): String {
        return getConfigString(propName)
            ?: throw InvalidConfigurationException(
                "$propName is required but not specified in the configuration"
            )
    }

    @Throws(InvalidConfigurationException::class)
    fun getRequiredConfigUri(propName: String): Uri {
        val uriStr = getRequiredConfigString(propName)
        val uri: Uri
        try {
            uri = Uri.parse(uriStr)
        } catch (ex: Throwable) {
            throw InvalidConfigurationException("$propName could not be parsed", ex)
        }
        if (!uri.isHierarchical || !uri.isAbsolute) {
            throw InvalidConfigurationException(
                "$propName must be hierarchical and absolute"
            )
        }
        if (!TextUtils.isEmpty(uri.encodedUserInfo)) {
            throw InvalidConfigurationException("$propName must not have user info")
        }
        if (!TextUtils.isEmpty(uri.encodedQuery)) {
            throw InvalidConfigurationException("$propName must not have query parameters")
        }
        if (!TextUtils.isEmpty(uri.encodedFragment)) {
            throw InvalidConfigurationException("$propName must not have a fragment")
        }
        return uri
    }

    @Throws(InvalidConfigurationException::class)
    fun getRequiredConfigWebUri(propName: String): Uri {
        val uri = getRequiredConfigUri(propName)
        val scheme = uri.scheme
        if (TextUtils.isEmpty(scheme) || !(("http" == scheme) || ("https" == scheme))) {
            throw InvalidConfigurationException(
                "$propName must have an http or https scheme"
            )
        }
        return uri
    }

    // ensure that the redirect URI declared in the configuration is handled by some activity
    // in the app, by querying the package manager speculatively
    private fun isRedirectUriRegistered(context: Context): Boolean {
            // ensure that the redirect URI declared in the configuration is handled by some activity
            // in the app, by querying the package manager speculatively
            val redirectIntent = Intent()
            redirectIntent.setPackage(context.packageName)
            redirectIntent.action = Intent.ACTION_VIEW
            redirectIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            redirectIntent.data = mRedirectUri
            return context.packageManager.queryIntentActivities(redirectIntent, 0).isNotEmpty()
        }

    class InvalidConfigurationException : Exception {
        internal constructor(reason: String?) : super(reason) {}
        internal constructor(reason: String?, cause: Throwable?) : super(reason, cause) {}
    }

    companion object  : SingletonHolder<Configuration, Context>(::Configuration) {
        private const val TAG = "Configuration"
        private const val PREFS_NAME = "config"
        private const val KEY_LAST_HASH = "lastHash"

/*
        fun getInstance(context: Context): Configuration {
            var config = sInstance.get()
            if (config == null) {
                config = Configuration(context)
                sInstance = WeakReference(config)
            }
            return config
        }
*/
    }
}