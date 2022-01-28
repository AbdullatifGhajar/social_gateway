package com.socialgateway.socialgateway.data

import com.socialgateway.socialgateway.data.model.LoggedInUser
import java.io.IOException
import java.lang.Exception

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private fun getUser(username: String, password: String): LoggedInUser {
        // TODO: call server
        return LoggedInUser("id", "", "")
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            Result.Success(getUser(username, password))
        } catch (e: Exception) {
            Result.Error(IOException("Error logging in"))
        }
    }
}