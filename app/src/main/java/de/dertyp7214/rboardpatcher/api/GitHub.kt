package de.dertyp7214.rboardpatcher.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.dertyp7214.rboardpatcher.utils.TypeTokens
import java.net.URL

object GitHub {
    val gson = Gson()
    val url: (User, Repo, branch: String, file: String) -> String =
        { user, repo, branch, file -> "https://raw.githubusercontent.com/${user.userName}/${repo.repoName}/$branch/$file" }

    object GboardThemes : User {
        override val userName = "GboardThemes"

        object Patches : Repo {
            override val repoName = "Patches"
            override var branch = "master"

            operator fun invoke(branch: String): Patches {
                this.branch = branch
                return this
            }

            inline operator fun <reified T> get(file: String): T {
                return gson.fromJson(
                    URL(url(GboardThemes, this, branch, file)).readText(),
                    TypeTokens<T>()
                )
            }
        }
    }
}

interface Repo {
    val repoName: String
    var branch: String
}

interface User {
    val userName: String
}