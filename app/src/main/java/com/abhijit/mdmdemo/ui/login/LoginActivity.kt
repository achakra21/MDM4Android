package com.abhijit.mdmdemo.ui.login

import android.app.Activity
import android.content.Context
import android.content.RestrictionsManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast

import com.abhijit.mdmdemo.R

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private var KEY_EMAIL :String = "email"
    private var KEY_PASSWORD :String = "password"
    private lateinit var username:EditText
    private lateinit var password:EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        username = findViewById<EditText>(R.id.username)
        password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
                .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                        username.text.toString(),
                        password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                                username.text.toString(),
                                password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }

    override fun onResume(){
        super.onResume()
        resolveRestrictions()


    }

    private fun resolveRestrictions() {

        var myRestrictionsMgr =
                getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager

        val restrictions = myRestrictionsMgr.getApplicationRestrictions()

        if (restrictions != null) {
            if (restrictions.containsKey(KEY_EMAIL)) {
                updateEmail(restrictions)
            }
            if (restrictions.containsKey(KEY_PASSWORD)) {
                updatePassword(restrictions)
            }
             else {

                for (s in restrictions.keySet()) {
                    println("key in b is : $s")
                }

            }
        }

    }

    private fun updatePassword(restrictions: Bundle?) {
        if (restrictions != null) {
            if (restrictions != null || restrictions.containsKey(KEY_PASSWORD)) {
               var  PASSWORD = restrictions!!.getString(KEY_PASSWORD)
                password.setText(PASSWORD.toString())
            }
        }

    }

    private fun updateEmail(restrictions: Bundle?) {

        if (restrictions != null) {
            if (restrictions != null || restrictions.containsKey(KEY_EMAIL)) {
                var  email = restrictions!!.getString(KEY_EMAIL)
                username.setText(email.toString())
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
                applicationContext,
                "$welcome $displayName",
                Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })



}