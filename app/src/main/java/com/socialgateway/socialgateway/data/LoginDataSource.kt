package com.socialgateway.socialgateway.data

import com.socialgateway.socialgateway.ServerException
import com.socialgateway.socialgateway.ServerInterface
import com.socialgateway.socialgateway.data.model.LoggedInUser
import java.io.IOException
import java.net.UnknownHostException
import kotlin.Exception

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private fun getUser(email: String, password: String): LoggedInUser {
        try {
            return ServerInterface.getAuthenticatedUser(email, password)
        } catch (e: UnknownHostException){
            throw Exception("Something went wrong. Be sure you are connected to the internet")
        } catch (e: ServerException){
            throw Exception("User could not be authenticated")
        }
    }

    fun login(email: String, password: String): Result<LoggedInUser> {
        return try {
            Result.Success(getUser(email, password))
        } catch (e: Exception) {
            Result.Error(IOException(e.message))
        }
    }
}