package ru.culture.house.controller

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import ru.culture.house.IntegrationTests

class CategoryControllerIntegrationTest : IntegrationTests() {
    @Test
    fun `visitor endpoint returns seeded categories ordered by id`() {
        mockMvc.get("/api/v1/categories/visitor").andExpect {
            status { isOk() }
            content {
                json(
                    """[
                        {"id":1,"name":"Дети","ageGroup":"CHILD"},
                        {"id":2,"name":"Подростки","ageGroup":"TEEN"},
                        {"id":3,"name":"Взрослые","ageGroup":"ADULT"}
                    ]""",
                    org.springframework.test.json.JsonCompareMode.STRICT,
                )
            }
        }
    }

    @Test
    fun `disability endpoint returns seeded categories and discounts ordered by id`() {
        mockMvc.get("/api/v1/categories/disability").andExpect {
            status { isOk() }
            content {
                json(
                    """[
                        {"id":1,"name":"Инвалид 1 группы","discountPercent":100},
                        {"id":2,"name":"Инвалид 2-3 группы","discountPercent":50},
                        {"id":3,"name":"Многодетные","discountPercent":50},
                        {"id":4,"name":"Ребёнок-инвалид","discountPercent":100},
                        {"id":5,"name":"СВО","discountPercent":100}
                    ]""",
                    org.springframework.test.json.JsonCompareMode.STRICT,
                )
            }
        }
    }
}
