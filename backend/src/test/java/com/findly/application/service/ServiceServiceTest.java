package com.findly.application.service;

import com.findly.api.exception.ApiException;
import com.findly.api.exception.ResourceNotFoundException;
import com.findly.application.dto.request.CreateServiceRequest;
import com.findly.application.dto.request.UpdateServiceRequest;
import com.findly.application.dto.response.ServiceResponse;
import com.findly.application.mapper.ServiceMapper;
import com.findly.domain.entity.Business;
import com.findly.domain.entity.Service;
import com.findly.domain.entity.User;
import com.findly.domain.enums.BusinessStatus;
import com.findly.domain.enums.UserRole;
import com.findly.domain.repository.BusinessRepository;
import com.findly.domain.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private ServiceMapper serviceMapper;

    @InjectMocks
    private ServiceService serviceService;

    private UUID ownerId;
    private UUID businessId;
    private UUID serviceId;
    private User owner;
    private Business business;
    private Service service;
    private ServiceResponse serviceResponse;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        serviceId = UUID.randomUUID();

        owner = User.builder()
                .email("owner@test.com")
                .role(UserRole.BUSINESS_OWNER)
                .build();
        owner.setId(ownerId);

        business = Business.builder()
                .owner(owner)
                .name("Test Business")
                .status(BusinessStatus.ACTIVE)
                .build();
        business.setId(businessId);

        service = Service.builder()
                .business(business)
                .name("Haircut")
                .description("A standard haircut")
                .durationMins(30)
                .price(BigDecimal.valueOf(50))
                .currency("ILS")
                .active(true)
                .sortOrder(0)
                .build();
        service.setId(serviceId);

        serviceResponse = ServiceResponse.builder()
                .id(serviceId)
                .name("Haircut")
                .description("A standard haircut")
                .durationMins(30)
                .formattedPrice("₪50")
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Get Active Services Tests")
    class GetActiveServicesTests {

        @Test
        @DisplayName("Should get active services for a business")
        void shouldGetActiveServices() {
            when(businessRepository.existsById(businessId)).thenReturn(true);
            when(serviceRepository.findByBusinessIdAndActiveTrueOrderBySortOrderAsc(businessId))
                    .thenReturn(List.of(service));
            when(serviceMapper.toResponseList(anyList())).thenReturn(List.of(serviceResponse));

            List<ServiceResponse> result = serviceService.getActiveServicesByBusiness(businessId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Haircut");
        }

        @Test
        @DisplayName("Should throw when business not found")
        void shouldThrowWhenBusinessNotFound() {
            when(businessRepository.existsById(businessId)).thenReturn(false);

            assertThatThrownBy(() -> serviceService.getActiveServicesByBusiness(businessId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return empty list when no active services")
        void shouldReturnEmptyListWhenNoActiveServices() {
            when(businessRepository.existsById(businessId)).thenReturn(true);
            when(serviceRepository.findByBusinessIdAndActiveTrueOrderBySortOrderAsc(businessId))
                    .thenReturn(List.of());
            when(serviceMapper.toResponseList(anyList())).thenReturn(List.of());

            List<ServiceResponse> result = serviceService.getActiveServicesByBusiness(businessId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Services for Owner Tests")
    class GetServicesForOwnerTests {

        @Test
        @DisplayName("Should get all services for owner's business")
        void shouldGetAllServicesForOwner() {
            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.findByBusinessIdOrderBySortOrderAsc(businessId))
                    .thenReturn(List.of(service));
            when(serviceMapper.toResponseList(anyList())).thenReturn(List.of(serviceResponse));

            List<ServiceResponse> result = serviceService.getServicesByBusiness(ownerId, businessId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should throw when business not owned by user")
        void shouldThrowWhenBusinessNotOwnedByUser() {
            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceService.getServicesByBusiness(ownerId, businessId))
                    .isInstanceOf(ApiException.class);
        }
    }

    @Nested
    @DisplayName("Create Service Tests")
    class CreateServiceTests {

        @Test
        @DisplayName("Should create service successfully")
        void shouldCreateService() {
            CreateServiceRequest request = CreateServiceRequest.builder()
                    .name("New Service")
                    .description("A new service")
                    .durationMins(45)
                    .price(BigDecimal.valueOf(100))
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.existsByBusinessIdAndName(businessId, "New Service"))
                    .thenReturn(false);
            when(serviceRepository.save(any(Service.class))).thenReturn(service);
            when(serviceMapper.toResponse(any(Service.class))).thenReturn(serviceResponse);

            ServiceResponse result = serviceService.createService(ownerId, businessId, request);

            assertThat(result).isNotNull();
            verify(serviceRepository).save(any(Service.class));
        }

        @Test
        @DisplayName("Should create service with default currency")
        void shouldCreateServiceWithDefaultCurrency() {
            CreateServiceRequest request = CreateServiceRequest.builder()
                    .name("New Service")
                    .durationMins(45)
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.existsByBusinessIdAndName(businessId, "New Service"))
                    .thenReturn(false);
            when(serviceRepository.save(any(Service.class))).thenReturn(service);
            when(serviceMapper.toResponse(any(Service.class))).thenReturn(serviceResponse);

            ServiceResponse result = serviceService.createService(ownerId, businessId, request);

            assertThat(result).isNotNull();
            verify(serviceRepository).save(argThat(s -> s.getCurrency().equals("ILS")));
        }

        @Test
        @DisplayName("Should throw when duplicate service name")
        void shouldThrowWhenDuplicateServiceName() {
            CreateServiceRequest request = CreateServiceRequest.builder()
                    .name("Haircut")
                    .durationMins(30)
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.existsByBusinessIdAndName(businessId, "Haircut"))
                    .thenReturn(true);

            assertThatThrownBy(() -> serviceService.createService(ownerId, businessId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Update Service Tests")
    class UpdateServiceTests {

        @Test
        @DisplayName("Should update service name")
        void shouldUpdateServiceName() {
            UpdateServiceRequest request = UpdateServiceRequest.builder()
                    .name("Updated Haircut")
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId))
                    .thenReturn(Optional.of(service));
            when(serviceRepository.existsByBusinessIdAndName(businessId, "Updated Haircut"))
                    .thenReturn(false);
            when(serviceRepository.save(any(Service.class))).thenReturn(service);
            when(serviceMapper.toResponse(any(Service.class))).thenReturn(serviceResponse);

            ServiceResponse result = serviceService.updateService(ownerId, businessId, serviceId, request);

            assertThat(result).isNotNull();
            verify(serviceRepository).save(any(Service.class));
        }

        @Test
        @DisplayName("Should update service price")
        void shouldUpdateServicePrice() {
            UpdateServiceRequest request = UpdateServiceRequest.builder()
                    .price(BigDecimal.valueOf(75))
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId))
                    .thenReturn(Optional.of(service));
            when(serviceRepository.save(any(Service.class))).thenReturn(service);
            when(serviceMapper.toResponse(any(Service.class))).thenReturn(serviceResponse);

            ServiceResponse result = serviceService.updateService(ownerId, businessId, serviceId, request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should deactivate service")
        void shouldDeactivateService() {
            UpdateServiceRequest request = UpdateServiceRequest.builder()
                    .active(false)
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId))
                    .thenReturn(Optional.of(service));
            when(serviceRepository.save(any(Service.class))).thenReturn(service);
            when(serviceMapper.toResponse(any(Service.class))).thenReturn(serviceResponse);

            ServiceResponse result = serviceService.updateService(ownerId, businessId, serviceId, request);

            assertThat(result).isNotNull();
            verify(serviceRepository).save(argThat(s -> !s.isActive()));
        }

        @Test
        @DisplayName("Should throw when service not found")
        void shouldThrowWhenServiceNotFound() {
            UpdateServiceRequest request = UpdateServiceRequest.builder()
                    .name("Updated")
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceService.updateService(ownerId, businessId, serviceId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw when renaming to duplicate name")
        void shouldThrowWhenRenamingToDuplicateName() {
            service.setName("Original Name");

            UpdateServiceRequest request = UpdateServiceRequest.builder()
                    .name("Existing Name")
                    .build();

            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId))
                    .thenReturn(Optional.of(service));
            when(serviceRepository.existsByBusinessIdAndName(businessId, "Existing Name"))
                    .thenReturn(true);

            assertThatThrownBy(() -> serviceService.updateService(ownerId, businessId, serviceId, request))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Delete Service Tests")
    class DeleteServiceTests {

        @Test
        @DisplayName("Should delete service")
        void shouldDeleteService() {
            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId))
                    .thenReturn(Optional.of(service));

            serviceService.deleteService(ownerId, businessId, serviceId);

            verify(serviceRepository).delete(service);
        }

        @Test
        @DisplayName("Should throw when deleting non-existent service")
        void shouldThrowWhenDeletingNonExistentService() {
            when(businessRepository.findByIdAndOwnerId(businessId, ownerId))
                    .thenReturn(Optional.of(business));
            when(serviceRepository.findByIdAndBusinessId(serviceId, businessId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceService.deleteService(ownerId, businessId, serviceId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
