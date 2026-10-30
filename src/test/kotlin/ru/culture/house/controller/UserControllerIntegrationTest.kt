package ru.culture.house.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import ru.culture.house.IntegrationTests
import ru.culture.house.persistence.entity.UserRole
import ru.culture.house.persistence.repository.UserRepository

class UserControllerIntegrationTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : IntegrationTests() {

    // ---------- успешное создание ----------

    @ParameterizedTest
    @EnumSource(value = UserRole::class, names = ["ADMIN", "EMPLOYEE"])
    fun `create user of a club returns 201 and saves it`(role: UserRole) {
        val clubId = saveClub()

        postUser(userJson(login = "anna", role = role.name, clubId = clubId)).andExpect {
            status { isCreated() }
            jsonPath("\$.id") { isNumber() }
            jsonPath("\$.login") { value("anna") }
            jsonPath("\$.role") { value(role.name) }
            jsonPath("\$.clubId") { value(clubId) }
            jsonPath("\$.createdAt") { exists() }
        }

        val row = userRows().single()
        assertThat(row["login"]).isEqualTo("anna")
        assertThat(row["role"]).isEqualTo(role.name)
        assertThat(row["club_id"]).isEqualTo(clubId)
    }

    @Test
    fun `create super admin without club returns 201 and stores null club`() {
        postUser(userJson(login = "root", role = "SUPER_ADMIN")).andExpect {
            status { isCreated() }
            jsonPath("\$.role") { value("SUPER_ADMIN") }
            jsonPath("\$.clubId") { doesNotExist() }
        }

        val row = userRows().single()
        assertThat(row["role"]).isEqualTo("SUPER_ADMIN")
        assertThat(row["club_id"]).isNull()
    }

    @Test
    fun `create stores bcrypt hash and never returns password or hash`() {
        val clubId = saveClub()

        postUser(userJson(password = "secret123", clubId = clubId)).andExpect {
            status { isCreated() }
            jsonPath("\$.password") { doesNotExist() }
            jsonPath("\$.passwordHash") { doesNotExist() }
        }

        val hash = userRows().single()["password_hash"] as String
        assertThat(hash).isNotEqualTo("secret123")
        assertThat(passwordEncoder.matches("secret123", hash)).isTrue()
    }

    @Test
    fun `create trims login`() {
        val clubId = saveClub()

        postUser(userJson(login = "  anna  ", clubId = clubId)).andExpect {
            status { isCreated() }
            jsonPath("\$.login") { value("anna") }
        }

        assertThat(userRows().single()["login"]).isEqualTo("anna")
    }

    // ---------- бизнес-правило привязки к клубу ----------

    @Test
    fun `create super admin with club returns 400 and saves nothing`() {
        val clubId = saveClub()

        postUser(userJson(role = "SUPER_ADMIN", clubId = clubId)).andExpect {
            status { isBadRequest() }
            jsonPath("\$.status") { value(400) }
        }

        assertThat(userRepository.count()).isZero()
    }

    @ParameterizedTest
    @EnumSource(value = UserRole::class, names = ["ADMIN", "EMPLOYEE"])
    fun `create club user without club returns 400 and saves nothing`(role: UserRole) {
        postUser(userJson(role = role.name)).andExpect { status { isBadRequest() } }

        assertThat(userRepository.count()).isZero()
    }

    @Test
    fun `create with unknown club returns 404 and saves nothing`() {
        postUser(userJson(clubId = 999_999)).andExpect {
            status { isNotFound() }
            jsonPath("\$.status") { value(404) }
        }

        assertThat(userRepository.count()).isZero()
    }

    // ---------- уникальность логина ----------

    @Test
    fun `create with taken login returns 409 and keeps the first user`() {
        val clubId = saveClub()
        postUser(userJson(login = "anna", password = "first-password", clubId = clubId))
            .andExpect { status { isCreated() } }

        postUser(userJson(login = "anna", password = "other-password", clubId = clubId)).andExpect {
            status { isConflict() }
            jsonPath("\$.status") { value(409) }
        }

        val row = userRows().single()
        assertThat(passwordEncoder.matches("first-password", row["password_hash"] as String)).isTrue()
    }

    // ---------- некорректное тело ----------

    @Test
    fun `create with unknown role returns 400`() {
        val clubId = saveClub()

        postUser(userJson(role = "ROOT", clubId = clubId)).andExpect { status { isBadRequest() } }

        assertThat(userRepository.count()).isZero()
    }

    @Test
    fun `create without password returns 400`() {
        val clubId = saveClub()

        postUser("""{"login": "anna", "role": "EMPLOYEE", "clubId": $clubId}""")
            .andExpect { status { isBadRequest() } }

        assertThat(userRepository.count()).isZero()
    }

    // ---------- доступ ----------

    //@Test
    //@WithMockUser(roles = ["ADMIN"])
    fun `admin gets 403 and nothing is created`() {
        val clubId = saveClub()

        postUser(userJson(clubId = clubId)).andExpect { status { isForbidden() } }

        assertThat(userRepository.count()).isZero()
    }

    //@Test
    //@WithMockUser(roles = ["EMPLOYEE"])
    fun `employee gets 403 and nothing is created`() {
        val clubId = saveClub()

        postUser(userJson(clubId = clubId)).andExpect { status { isForbidden() } }

        assertThat(userRepository.count()).isZero()
    }

    // ---------- helpers ----------

    private fun userRows(): List<Map<String, Any?>> =
        jdbcTemplate.queryForList("select login, role, club_id, password_hash from users order by id")

    private fun userJson(
        login: String = "anna",
        password: String = "secret123",
        role: String = "EMPLOYEE",
        clubId: Long? = null,
    ): String {
        val clubPart = clubId?.let { """, "clubId": $it""" } ?: ""
        return """{"login": "$login", "password": "$password", "role": "$role"$clubPart}"""
    }

    private fun postUser(body: String) = postJson("/api/v1/users", body)
}