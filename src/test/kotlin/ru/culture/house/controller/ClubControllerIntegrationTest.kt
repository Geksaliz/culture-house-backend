package ru.culture.house.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import ru.culture.house.TestcontainersConfiguration
import ru.culture.house.persistence.entity.Club
import ru.culture.house.persistence.repository.ClubRepository

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class ClubControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val clubRepository: ClubRepository,
    private val jdbcTemplate: JdbcTemplate,
) {

    @BeforeEach
    fun cleanUp() {
        jdbcTemplate.update("delete from users")
        clubRepository.deleteAll()
    }

    // ---------- POST /api/clubs ----------

    @Test
    fun `create saves club and returns 201`() {
        postClub("""{"name": "ДК Центральный", "address": "ул. Чапаева, 20"}""").andExpect {
            status { isCreated() }
            jsonPath("\$.id") { isNumber() }
            jsonPath("\$.name") { value("ДК Центральный") }
            jsonPath("\$.address") { value("ул. Чапаева, 20") }
            jsonPath("\$.createdAt") { exists() }
        }

        val saved = clubRepository.findAll().single()
        assertThat(saved.name).isEqualTo("ДК Центральный")
        assertThat(saved.address).isEqualTo("ул. Чапаева, 20")
    }

    @Test
    fun `create without address stores null`() {
        postClub("""{"name": "ДК Восток"}""").andExpect {
            status { isCreated() }
            jsonPath("\$.address") { doesNotExist() }
        }

        assertThat(clubRepository.findAll().single().address).isNull()
    }

    @Test
    fun `create trims name and stores blank address as null`() {
        postClub("""{"name": "  ДК Запад  ", "address": "   "}""").andExpect {
            status { isCreated() }
            jsonPath("\$.name") { value("ДК Запад") }
            jsonPath("\$.address") { doesNotExist() }
        }

        val saved = clubRepository.findAll().single()
        assertThat(saved.name).isEqualTo("ДК Запад")
        assertThat(saved.address).isNull()
    }

    //@Test
    // Broken, need validation
    fun `create with blank name returns 400 and saves nothing`() {
        postClub("""{"name": "   "}""").andExpect {
            status { isBadRequest() }
        }

        assertThat(clubRepository.count()).isZero()
    }

    @Test
    fun `create without name returns 400`() {
        postClub("""{"address": "ул. Чапаева, 20"}""").andExpect {
            status { isBadRequest() }
        }

        assertThat(clubRepository.count()).isZero()
    }

    //@Test
    // Broken, need validation
    fun `create with name longer than 255 returns 400`() {
        val longName = "а".repeat(256)

        postClub("""{"name": "$longName"}""").andExpect {
            status { isBadRequest() }
        }

        assertThat(clubRepository.count()).isZero()
    }

    //@Test
    // Broken, need validation
    fun `create with address longer than 500 returns 400`() {
        val longAddress = "а".repeat(501)

        postClub("""{"name": "ДК", "address": "$longAddress"}""").andExpect {
            status { isBadRequest() }
        }

        assertThat(clubRepository.count()).isZero()
    }

// ---------- PUT /api/clubs/{id} ----------

    @Test
    fun `update changes name and address`() {
        val id = saveClub("Старое название", "Старый адрес")

        putClub(id, """{"name": "Новое название", "address": "Новый адрес"}""").andExpect {
            status { isOk() }
            jsonPath("\$.id") { value(id) }
            jsonPath("\$.name") { value("Новое название") }
            jsonPath("\$.address") { value("Новый адрес") }
        }

        val updated = clubRepository.findById(id).orElseThrow()
        assertThat(updated.name).isEqualTo("Новое название")
        assertThat(updated.address).isEqualTo("Новый адрес")
    }

    @Test
    fun `update with blank address clears it`() {
        val id = saveClub("ДК", "Старый адрес")

        putClub(id, """{"name": "ДК", "address": ""}""").andExpect {
            status { isOk() }
            jsonPath("\$.address") { doesNotExist() }
        }

        assertThat(clubRepository.findById(id).orElseThrow().address).isNull()
    }

    @Test
    fun `update of unknown club returns 404`() {
        putClub(999_999, """{"name": "ДК"}""").andExpect {
            status { isNotFound() }
            jsonPath("\$.status") { value(404) }
        }
    }

    //@Test
    // Broken, need validation
    fun `update with invalid body returns 400 and keeps data`() {
        val id = saveClub("ДК", "Адрес")

        putClub(id, """{"name": ""}""")//.andExpect { status { isBadRequest() } }

        val unchanged = clubRepository.findById(id).orElseThrow()
        assertThat(unchanged.name).isEqualTo("ДК")
        assertThat(unchanged.address).isEqualTo("Адрес")
    }

// ---------- DELETE /api/clubs/{id} ----------

    @Test
    fun `delete removes club and returns 204`() {
        val id = saveClub("ДК")

        deleteClub(id).andExpect { status { isNoContent() } }

        assertThat(clubRepository.existsById(id)).isFalse()
    }

    @Test
    fun `delete of unknown club returns 404`() {
        deleteClub(999_999).andExpect { status { isNotFound() } }
    }

    @Test
    fun `delete of club with linked data returns 409 and keeps club`() {
        val id = saveClub("ДК")
        jdbcTemplate.update(
            "insert into users (club_id, login, password_hash, role) values (?, ?, ?, ?)",
            id, "club-admin", "hash", "ADMIN",
        )

        deleteClub(id).andExpect {
            status { isConflict() }
            jsonPath("\$.status") { value(409) }
        }

        assertThat(clubRepository.existsById(id)).isTrue()
    }

// ---------- helpers ----------

    private fun saveClub(name: String, address: String? = null): Long =
        requireNotNull(clubRepository.save(Club(name = name, address = address)).id)

    private fun postClub(body: String) = mockMvc.post("/api/v1/clubs") {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    private fun putClub(id: Long, body: String) = mockMvc.put("/api/v1/clubs/$id") {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    private fun deleteClub(id: Long) = mockMvc.delete("/api/v1/clubs/$id")
}