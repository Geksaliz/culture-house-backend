package ru.culture.house.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import ru.culture.house.IntegrationTests

class EventControllerIntegrationTest : IntegrationTests() {

    @Test
    fun `planned event can be completed later and attendance replaced`() {
        val clubId = saveClub()
        jdbcTemplate.update(
            "insert into users (club_id, login, password_hash, role) values (?, 'employee', 'hash', 'EMPLOYEE')",
            clubId,
        )
        val userId = jdbcTemplate.queryForObject("select id from users where login='employee'", Long::class.java)!!
        val id = createEvent(clubId, """{"name":"Концерт","eventDate":"2026-12-01"}""")
        mockMvc.get("/api/v1/events/$id").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("PLANNED") }
            jsonPath("$.type") { value("FREE") }
            jsonPath("$.totalVisitors") { value(0) }
        }
        val body = """{
            "name":"Концерт","eventDate":"2026-12-01","status":"COMPLETED",
            "responsibleUserIds":[$userId],
            "attendance":[{"categoryId":1,"count":10}],
            "disabilityAttendance":[{"categoryId":1,"count":2}]
        }"""
        repeat(2) {
            putJson("/api/v1/events/$id", body).andExpect {
                status { isOk() }
                jsonPath("$.status") { value("COMPLETED") }
                jsonPath("$.totalVisitors") { value(10) }
                jsonPath("$.responsibleUserIds[0]") { value(userId) }
                jsonPath("$.disabilityAttendance[0].count") { value(2) }
            }
        }
        assertThat(jdbcTemplate.queryForList(
            "select category_id, count from event_attendance where event_id=?", id,
        )).containsExactly(mapOf<String, Any>("category_id" to 1L, "count" to 10))
        assertThat(jdbcTemplate.queryForList(
            "select category_id, count from event_disability_attendance where event_id=?", id,
        )).containsExactly(mapOf<String, Any>("category_id" to 1L, "count" to 2))
        putJson("/api/v1/events/$id", """{
            "name":"Концерт","eventDate":"2026-12-01","status":"COMPLETED",
            "attendance":[{"categoryId":2,"count":5}]
        }""").andExpect {
            status { isOk() }
            jsonPath("$.totalVisitors") { value(5) }
            jsonPath("$.attendance.length()") { value(1) }
            jsonPath("$.responsibleUserIds") { isEmpty() }
            jsonPath("$.disabilityAttendance") { isEmpty() }
        }
        assertThat(jdbcTemplate.queryForObject("select count(*) from event_attendance where event_id=?", Long::class.java, id))
            .isEqualTo(1L)
        assertThat(jdbcTemplate.queryForList(
            "select category_id, count from event_attendance where event_id=?", id,
        )).containsExactly(mapOf<String, Any>("category_id" to 2L, "count" to 5))
        assertThat(jdbcTemplate.queryForList(
            "select category_id, count from event_disability_attendance where event_id=?", id,
        )).isEmpty()
        deleteRequest("/api/v1/events/$id").andExpect { status { isNoContent() } }
        mockMvc.get("/api/v1/events/$id").andExpect { status { isNotFound() } }
        assertThat(jdbcTemplate.queryForObject("select count(*) from event_attendance where event_id=?", Long::class.java, id))
            .isZero()
    }

    @Test
    fun `completed event can be created with attendance and no visitors`() {
        val clubId = saveClub()
        val id = createEvent(clubId, """{
            "name":"Встреча","eventDate":"2026-12-02","status":"COMPLETED",
            "attendance":[{"categoryId":3,"count":7}],
            "disabilityAttendance":[{"categoryId":2,"count":3}]
        }""")
        assertThat(jdbcTemplate.queryForList(
            "select category_id, count from event_attendance where event_id=?", id,
        )).containsExactly(mapOf<String, Any>("category_id" to 3L, "count" to 7))
        assertThat(jdbcTemplate.queryForList(
            "select category_id, count from event_disability_attendance where event_id=?", id,
        )).containsExactly(mapOf<String, Any>("category_id" to 2L, "count" to 3))
        mockMvc.get("/api/v1/events/$id").andExpect {
            status { isOk() }
            jsonPath("$.totalVisitors") { value(7) }
        }
        val emptyId = createEvent(clubId, """{"name":"Без посетителей","eventDate":"2026-12-03","status":"COMPLETED"}""")
        mockMvc.get("/api/v1/events/$emptyId").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("COMPLETED") }
            jsonPath("$.totalVisitors") { value(0) }
        }
    }

    @Test
    fun `list filters club and inclusive period`() {
        val clubId = saveClub()
        val otherClubId = saveClub("Другой")
        createEvent(clubId, """{"name":"Позже","eventDate":"2026-12-03"}""")
        val id = createEvent(clubId, """{"name":"Раньше","eventDate":"2026-12-01"}""")
        createEvent(otherClubId, """{"name":"Другой","eventDate":"2026-12-01"}""")
        mockMvc.get("/api/v1/events?clubId=$clubId").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].id") { value(id) }
        }
        mockMvc.get("/api/v1/events?clubId=$clubId&dateFrom=2026-12-01&dateTo=2026-12-01").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].id") { value(id) }
        }
        mockMvc.get("/api/v1/events?clubId=$clubId&dateFrom=2026-12-03&dateTo=2026-12-01")
            .andExpect { status { isBadRequest() } }
        mockMvc.get("/api/v1/events?clubId=999999").andExpect { status { isNotFound() } }
    }

    @Test
    fun `invalid attendance and times do not modify event`() {
        val clubId = saveClub()
        val id = createEvent(clubId, """{"name":"Исходное","eventDate":"2026-12-01"}""")
        val invalid = listOf(
            """"attendance":[{"categoryId":1,"count":0}]""",
            """"attendance":[{"categoryId":1,"count":-1}]""",
            """"attendance":[{"categoryId":1,"count":1},{"categoryId":1,"count":2}]""",
            """"disabilityAttendance":[{"categoryId":1,"count":0}]""",
            """"eventStartTime":"2026-12-02T12:00:00"""",
            """"eventStartTime":"2026-12-01T12:00:00","eventEndTime":"2026-12-01T11:00:00"""",
        )
        invalid.forEach {
            putJson("/api/v1/events/$id", """{"name":"Изменено","eventDate":"2026-12-01",$it}""")
                .andExpect { status { isBadRequest() } }
        }
        putJson("/api/v1/events/$id", """{
            "name":"Изменено","eventDate":"2026-12-01","attendance":[{"categoryId":999999,"count":1}]
        }""").andExpect { status { isNotFound() } }
        mockMvc.get("/api/v1/events/$id").andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Исходное") }
            jsonPath("$.totalVisitors") { value(0) }
        }
    }

    @Test
    fun `responsible must be active user of same club`() {
        val clubId = saveClub()
        val otherClub = saveClub("Другой")
        jdbcTemplate.update(
            "insert into users (club_id, login, password_hash, role, is_active) values (?, 'other', 'hash', 'EMPLOYEE', true), (?, 'inactive', 'hash', 'ADMIN', false)",
            otherClub, clubId,
        )
        val ids = jdbcTemplate.queryForList("select id from users", Long::class.java)
        ids.forEach {
            postJson("/api/v1/events?clubId=$clubId", """{
                "name":"Встреча","eventDate":"2026-12-01","responsibleUserIds":[$it]
            }""").andExpect { status { isBadRequest() } }
        }
        assertThat(jdbcTemplate.queryForObject("select count(*) from events", Long::class.java)).isZero()
    }

    @Test
    fun `unknown event returns 404 for read update and delete`() {
        mockMvc.get("/api/v1/events/999999").andExpect { status { isNotFound() } }
        putJson("/api/v1/events/999999", """{"name":"Встреча","eventDate":"2026-12-01"}""")
            .andExpect { status { isNotFound() } }
        deleteRequest("/api/v1/events/999999").andExpect { status { isNotFound() } }
    }

    private fun createEvent(clubId: Long, body: String): Long {
        val result = postJson("/api/v1/events?clubId=$clubId", body).andReturn()
        result.resolvedException?.let { throw AssertionError("Не удалось создать мероприятие", it) }
        assertThat(result.response.status).isEqualTo(201)
        return jdbcTemplate.queryForObject("select max(id) from events", Long::class.java)!!
    }
}
