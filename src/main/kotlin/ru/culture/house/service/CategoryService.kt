package ru.culture.house.service

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.culture.house.controller.dto.DisabilityCategoryResponse
import ru.culture.house.controller.dto.VisitorCategoryResponse
import ru.culture.house.persistence.repository.DisabilityCategoryRepository
import ru.culture.house.persistence.repository.VisitorCategoryRepository

@Service
class CategoryService(
    private val visitorCategoryRepository: VisitorCategoryRepository,
    private val disabilityCategoryRepository: DisabilityCategoryRepository,
) {
    @Transactional(readOnly = true)
    fun getVisitorCategories(): List<VisitorCategoryResponse> =
        visitorCategoryRepository.findAll(Sort.by("id")).map {
            VisitorCategoryResponse(it.name, it.ageGroup)
        }

    @Transactional(readOnly = true)
    fun getDisabilityCategories(): List<DisabilityCategoryResponse> =
        disabilityCategoryRepository.findAll(Sort.by("id")).map {
            DisabilityCategoryResponse(it.name, it.discountPercent)
        }
}
