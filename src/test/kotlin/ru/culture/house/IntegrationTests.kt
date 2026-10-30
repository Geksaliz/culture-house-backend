package ru.culture.house

import org.junit.jupiter.api.BeforeEach
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
import ru.culture.house.persistence.entity.Club
import ru.culture.house.persistence.repository.ClubRepository

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
abstract class IntegrationTests {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    protected lateinit var clubRepository: ClubRepository

    /**
     * TRUNCATE clubs CASCADE очищает все таблицы, прямо или косвенно ссылающиеся
     * на клубы (пользователи, мероприятия, билеты, касса), поэтому список не
     * надо расширять при появлении новых таблиц. Справочники категорий
     * (засеянные миграциями) на клубы не ссылаются и остаются нетронутыми.
     */
    @BeforeEach
    fun cleanDatabase() {
        jdbcTemplate.execute("truncate table clubs cascade")
    }

    protected fun saveClub(name: String = "ДК", address: String? = null): Long =
        requireNotNull(clubRepository.save(Club(name = name, address = address)).id)

    protected fun postJson(url: String, body: String) = mockMvc.post(url) {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    protected fun putJson(url: String, body: String) = mockMvc.put(url) {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    protected fun deleteRequest(url: String) = mockMvc.delete(url)
}
