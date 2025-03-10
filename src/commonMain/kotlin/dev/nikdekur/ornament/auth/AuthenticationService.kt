package dev.nikdekur.ornament.auth

import dev.nikdekur.ndkore.map.ListsMap

public typealias Headers = ListsMap<String, String>

public interface AuthenticationService {

    /**
     * Logs in the user based on the login and password.
     *
     * @param login The login of the user.
     * @param password The password of the user.
     * @return The result of the login.
     */
    public suspend fun login(login: String, password: String): LoginResult

    public sealed interface LoginResult {
        public object AccountNotFound : LoginResult
        public object WrongCredentials : LoginResult
        public class Success(public val data: Map<String, String>) : LoginResult
    }

    /**
     * Logs out the user based on the headers.
     *
     * @param headers The headers of the request.
     * @return The result of the logout.
     */
    public suspend fun logout(headers: Headers): LogoutResult

    public sealed interface LogoutResult {
        public object Success : LogoutResult
        public object NotAuthenticated : LogoutResult
    }

    /**
     * Returns the authentication state of the user based on the headers.
     *
     * @param headers The headers of the request.
     * @return The authentication state.
     * @see AuthState
     */
    public suspend fun getAuthState(headers: Headers): AuthState


    /**
     * Represents the state of the authentication.
     *
     * State is returned by [getAuthState] method.
     */
    public sealed interface AuthState {

        /**
         * The user is authenticated.
         */
        public data class Authenticated(
            val login: String
        ) : AuthState


        /**
         * The user is not authenticated.
         */
        public object NotAuthenticated : AuthState
    }
}