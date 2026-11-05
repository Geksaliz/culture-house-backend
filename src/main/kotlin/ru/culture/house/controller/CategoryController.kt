package ru.culture.house.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.culture.house.controller.dto.DisabilityCategoryResponse
import ru.culture.house.controller.dto.VisitorCategoryResponse
import ru.culture.house.service.CategoryService

@RestController
@RequestMapping("/api/{version}/categories")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping("/visitor")
    fun getVisitorCategories(): List<VisitorCategoryResponse> =
        categoryService.getVisitorCategories()

    @GetMapping("/disability")
    fun getDisabilityCategories(): List<DisabilityCategoryResponse> =
        categoryService.getDisabilityCategories()
}
