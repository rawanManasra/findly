package com.findly.application.service;

import com.findly.api.exception.ApiException;
import com.findly.api.exception.ResourceNotFoundException;
import com.findly.application.dto.request.CreateBusinessRequest;
import com.findly.application.dto.request.UpdateBusinessRequest;
import com.findly.application.dto.request.UpdateLocationRequest;
import com.findly.application.dto.response.BusinessDetailResponse;
import com.findly.application.dto.response.BusinessResponse;
import com.findly.application.mapper.BusinessMapper;
import com.findly.domain.entity.Business;
import com.findly.domain.entity.Category;
import com.findly.domain.entity.User;
import com.findly.domain.enums.BusinessStatus;
import com.findly.domain.enums.UserRole;
import com.findly.domain.repository.BusinessRepository;
import com.findly.domain.repository.CategoryRepository;
import com.findly.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BusinessMapper businessMapper;

    @InjectMocks
    private BusinessService businessService;

    private UUID ownerId;
    private UUID businessId;
    private UUID categoryId;
    private User owner;
    private Business business;
    private Category category;
    private BusinessDetailResponse businessDetailResponse;
    private BusinessResponse businessResponse;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        owner = User.builder()
                .email("owner@test.com")
                .firstName("Business")
                .lastName("Owner")
                .role(UserRole.BUSINESS_OWNER)
                .build();
        owner.setId(ownerId);

        category = Category.builder()
                .name("Test Category")
                .slug("test-category")
                .build();
        category.setId(categoryId);

        business = Business.builder()
                .owner(owner)
                .name("Test Business")
                .description("A test business")
                .category(category)
                .status(BusinessStatus.ACTIVE)
                .city("Tel Aviv")
                .country("Israel")
                .build();
        business.setId(businessId);

        businessDetailResponse = BusinessDetailResponse.builder()
                .id(businessId)
                .name("Test Business")
                .status(BusinessStatus.ACTIVE)
                .build();

        businessResponse = BusinessResponse.builder()
                .id(businessId)
                .name("Test Business")
                .status(BusinessStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Search Nearby Tests")
    class SearchNearbyTests {

        @Test
        @DisplayName("Should search nearby businesses without filters")
        void shouldSearchNearbyWithoutFilters() {
            double lat = 32.0853;
            double lng = 34.7818;
            Pageable pageable = PageRequest.of(0, 10);

            Object[] row = new Object[]{businessId, 1500.0};
            List<Object[]> rows = new ArrayList<>();
            rows.add(row);
            Page<Object[]> results = new PageImpl<>(rows, pageable, 1);

            when(businessRepository.findNearbyBusinesses(lng, lat, 5000.0, BusinessStatus.ACTIVE, pageable))
                    .thenReturn(results);
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessMapper.toResponseWithDistance(any(Business.class), anyDouble())).thenReturn(businessResponse);

            Page<BusinessResponse> result = businessService.searchNearby(lat, lng, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(businessRepository).findNearbyBusinesses(lng, lat, 5000.0, BusinessStatus.ACTIVE, pageable);
        }

        @Test
        @DisplayName("Should search nearby businesses with search term")
        void shouldSearchNearbyWithSearchTerm() {
            double lat = 32.0853;
            double lng = 34.7818;
            String searchTerm = "salon";
            Pageable pageable = PageRequest.of(0, 10);

            Object[] row = new Object[]{businessId, 1500.0};
            List<Object[]> rows = new ArrayList<>();
            rows.add(row);
            Page<Object[]> results = new PageImpl<>(rows, pageable, 1);

            when(businessRepository.searchNearbyBusinesses(lng, lat, 5000.0, searchTerm, BusinessStatus.ACTIVE, pageable))
                    .thenReturn(results);
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessMapper.toResponseWithDistance(any(Business.class), anyDouble())).thenReturn(businessResponse);

            Page<BusinessResponse> result = businessService.searchNearby(lat, lng, null, searchTerm, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(businessRepository).searchNearbyBusinesses(lng, lat, 5000.0, searchTerm, BusinessStatus.ACTIVE, pageable);
        }

        @Test
        @DisplayName("Should search nearby businesses by category")
        void shouldSearchNearbyByCategory() {
            double lat = 32.0853;
            double lng = 34.7818;
            Pageable pageable = PageRequest.of(0, 10);

            Object[] row = new Object[]{businessId, 1500.0};
            List<Object[]> rows = new ArrayList<>();
            rows.add(row);
            Page<Object[]> results = new PageImpl<>(rows, pageable, 1);

            when(businessRepository.findNearbyBusinessesByCategory(lng, lat, 5000.0, categoryId, BusinessStatus.ACTIVE, pageable))
                    .thenReturn(results);
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessMapper.toResponseWithDistance(any(Business.class), anyDouble())).thenReturn(businessResponse);

            Page<BusinessResponse> result = businessService.searchNearby(lat, lng, null, null, categoryId, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(businessRepository).findNearbyBusinessesByCategory(lng, lat, 5000.0, categoryId, BusinessStatus.ACTIVE, pageable);
        }

        @Test
        @DisplayName("Should use custom radius when provided")
        void shouldUseCustomRadius() {
            double lat = 32.0853;
            double lng = 34.7818;
            double customRadius = 10000.0;
            Pageable pageable = PageRequest.of(0, 10);

            Page<Object[]> results = new PageImpl<>(List.of(), pageable, 0);

            when(businessRepository.findNearbyBusinesses(lng, lat, customRadius, BusinessStatus.ACTIVE, pageable))
                    .thenReturn(results);

            Page<BusinessResponse> result = businessService.searchNearby(lat, lng, customRadius, null, null, pageable);

            verify(businessRepository).findNearbyBusinesses(lng, lat, customRadius, BusinessStatus.ACTIVE, pageable);
        }
    }

    @Nested
    @DisplayName("Get Business By ID Tests")
    class GetBusinessByIdTests {

        @Test
        @DisplayName("Should get active business by ID")
        void shouldGetActiveBusinessById() {
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessMapper.toDetailResponse(business)).thenReturn(businessDetailResponse);

            BusinessDetailResponse result = businessService.getBusinessById(businessId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(businessId);
        }

        @Test
        @DisplayName("Should throw when business not found")
        void shouldThrowWhenBusinessNotFound() {
            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> businessService.getBusinessById(businessId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when business not active")
        void shouldThrowWhenBusinessNotActive() {
            business.setStatus(BusinessStatus.INACTIVE);
            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

            assertThatThrownBy(() -> businessService.getBusinessById(businessId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Create Business Tests")
    class CreateBusinessTests {

        @Test
        @DisplayName("Should create business successfully")
        void shouldCreateBusiness() {
            CreateBusinessRequest request = CreateBusinessRequest.builder()
                    .name("New Business")
                    .description("A new business")
                    .categoryId(categoryId)
                    .city("Tel Aviv")
                    .latitude(32.0853)
                    .longitude(34.7818)
                    .build();

            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(businessRepository.save(any(Business.class))).thenReturn(business);
            when(businessMapper.toOwnerDetailResponse(any(Business.class))).thenReturn(businessDetailResponse);

            BusinessDetailResponse result = businessService.createBusiness(ownerId, request);

            assertThat(result).isNotNull();
            verify(businessRepository).save(any(Business.class));
        }

        @Test
        @DisplayName("Should create business without category")
        void shouldCreateBusinessWithoutCategory() {
            CreateBusinessRequest request = CreateBusinessRequest.builder()
                    .name("New Business")
                    .city("Tel Aviv")
                    .latitude(32.0853)
                    .longitude(34.7818)
                    .build();

            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(businessRepository.save(any(Business.class))).thenReturn(business);
            when(businessMapper.toOwnerDetailResponse(any(Business.class))).thenReturn(businessDetailResponse);

            BusinessDetailResponse result = businessService.createBusiness(ownerId, request);

            assertThat(result).isNotNull();
            verify(categoryRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw when owner not found")
        void shouldThrowWhenOwnerNotFound() {
            CreateBusinessRequest request = CreateBusinessRequest.builder()
                    .name("New Business")
                    .build();

            when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> businessService.createBusiness(ownerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when category not found")
        void shouldThrowWhenCategoryNotFound() {
            CreateBusinessRequest request = CreateBusinessRequest.builder()
                    .name("New Business")
                    .categoryId(categoryId)
                    .build();

            when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> businessService.createBusiness(ownerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update Business Tests")
    class UpdateBusinessTests {

        @Test
        @DisplayName("Should update business name")
        void shouldUpdateBusinessName() {
            UpdateBusinessRequest request = UpdateBusinessRequest.builder()
                    .name("Updated Name")
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.of(business));
            when(businessRepository.save(any(Business.class))).thenReturn(business);
            when(businessMapper.toOwnerDetailResponse(any(Business.class))).thenReturn(businessDetailResponse);

            BusinessDetailResponse result = businessService.updateBusiness(ownerId, businessId, request);

            assertThat(result).isNotNull();
            verify(businessRepository).save(any(Business.class));
        }

        @Test
        @DisplayName("Should update business category")
        void shouldUpdateBusinessCategory() {
            UUID newCategoryId = UUID.randomUUID();
            Category newCategory = Category.builder().name("New Category").build();
            newCategory.setId(newCategoryId);

            UpdateBusinessRequest request = UpdateBusinessRequest.builder()
                    .categoryId(newCategoryId)
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.of(business));
            when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(newCategory));
            when(businessRepository.save(any(Business.class))).thenReturn(business);
            when(businessMapper.toOwnerDetailResponse(any(Business.class))).thenReturn(businessDetailResponse);

            BusinessDetailResponse result = businessService.updateBusiness(ownerId, businessId, request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw when business not found for owner")
        void shouldThrowWhenBusinessNotFoundForOwner() {
            UpdateBusinessRequest request = UpdateBusinessRequest.builder()
                    .name("Updated Name")
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> businessService.updateBusiness(ownerId, businessId, request))
                    .isInstanceOf(ApiException.class);
        }
    }

    @Nested
    @DisplayName("Update Location Tests")
    class UpdateLocationTests {

        @Test
        @DisplayName("Should update business location")
        void shouldUpdateLocation() {
            UpdateLocationRequest request = UpdateLocationRequest.builder()
                    .latitude(32.1)
                    .longitude(34.8)
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.of(business));
            when(businessRepository.save(any(Business.class))).thenReturn(business);
            when(businessMapper.toOwnerDetailResponse(any(Business.class))).thenReturn(businessDetailResponse);

            BusinessDetailResponse result = businessService.updateLocation(ownerId, businessId, request);

            assertThat(result).isNotNull();
            verify(businessRepository).save(any(Business.class));
        }
    }

    @Nested
    @DisplayName("Owner Operations Tests")
    class OwnerOperationsTests {

        @Test
        @DisplayName("Should get all owner businesses")
        void shouldGetOwnerBusinesses() {
            when(businessRepository.findByOwnerId(ownerId)).thenReturn(List.of(business));
            when(businessMapper.toOwnerDetailResponse(any(Business.class))).thenReturn(businessDetailResponse);

            List<BusinessDetailResponse> result = businessService.getOwnerBusinesses(ownerId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get specific owner business")
        void shouldGetOwnerBusiness() {
            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.of(business));
            when(businessMapper.toOwnerDetailResponse(business)).thenReturn(businessDetailResponse);

            BusinessDetailResponse result = businessService.getOwnerBusiness(ownerId, businessId);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should soft delete business")
        void shouldSoftDeleteBusiness() {
            when(businessRepository.findByIdAndOwnerId(businessId, ownerId)).thenReturn(Optional.of(business));
            when(businessRepository.save(any(Business.class))).thenReturn(business);

            businessService.deleteBusiness(ownerId, businessId);

            verify(businessRepository).save(any(Business.class));
        }
    }

    @Nested
    @DisplayName("Activate Business Tests")
    class ActivateBusinessTests {

        @Test
        @DisplayName("Should activate business")
        void shouldActivateBusiness() {
            business.setStatus(BusinessStatus.PENDING_APPROVAL);

            when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
            when(businessRepository.save(any(Business.class))).thenReturn(business);
            when(businessMapper.toOwnerDetailResponse(any(Business.class))).thenReturn(businessDetailResponse);

            BusinessDetailResponse result = businessService.activateBusiness(businessId);

            assertThat(result).isNotNull();
            verify(businessRepository).save(argThat(b -> b.getStatus() == BusinessStatus.ACTIVE));
        }

        @Test
        @DisplayName("Should throw when activating non-existent business")
        void shouldThrowWhenActivatingNonExistentBusiness() {
            when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> businessService.activateBusiness(businessId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
